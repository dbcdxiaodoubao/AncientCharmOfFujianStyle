const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const os = require('node:os');
const root = path.resolve(__dirname, '..');

function application() {
    const storage = new Map();
    const calls = [];
    let app;
    const wx = {
        getStorageSync: key => storage.get(key),
        setStorageSync: (key, value) => storage.set(key, value),
        removeStorageSync: key => storage.delete(key),
        showToast: () => {},
        request: options => { calls.push(options); return { onChunkReceived() {}, abort() {} }; },
        uploadFile: options => { calls.push(options); return {}; },
        switchTab() {}, navigateTo() {}, setNavigationBarTitle() {},
        showModal: options => options.success({ confirm: true })
    };
    vm.runInNewContext(fs.readFileSync(path.join(root, 'app.js'), 'utf8'), {
        App: value => { app = value; }, wx, require: () => ({ baseUrl: 'http://127.0.0.1:8080' }), console
    });
    const identity = { userId: 1, userName: 'tester', token: 'session-one', expiresAt: Date.now() + 60000 };
    return { app, storage, calls, wx, identity };
}

function page(file, env) {
    let definition;
    const source = fs.readFileSync(path.join(root, 'pages', file), 'utf8').replace(/^import .*;\r?\n/m, '');
    vm.runInNewContext(source, {
        Page: value => { definition = value; }, getApp: () => env.app, wx: env.wx,
        require: () => ({}), console: { log() {}, error() {}, warn() {} },
        setTimeout: callback => { callback(); return 0; }, clearTimeout() {},
        Uint8Array, Map, Set
    });
    definition.setData = function (patch, callback) { Object.assign(this.data, patch); if (callback) callback(); };
    return definition;
}

test('legacy cached identity is not a session and expired credentials are cleared', () => {
    const env = application();
    env.storage.set('userInfo', { userId: 5, userName: 'legacy' });
    env.app.onLaunch();
    assert.equal(env.app.globalData.userInfo, null);
    env.app.setSession(env.identity);
    assert.equal(env.app.getAuthHeader().Authorization, 'Bearer session-one');
    env.storage.get('session').expiresAt = Date.now() - 1;
    assert.equal(env.app.getAuthHeader().Authorization, undefined);
    assert.equal(env.app.globalData.userInfo, null);
});

test('REST, upload and chunked AI requests attach the verified session', () => {
    const env = application();
    env.app.setSession(env.identity);
    env.app.rawRequest({ url: '/ai/chat', enableChunked: true });
    env.app.uploadFile({ url: '/check-in/upload' });
    assert.equal(env.calls[0].header.Authorization, 'Bearer session-one');
    assert.equal(env.calls[0].enableChunked, true);
    assert.equal(env.calls[1].header.Authorization, 'Bearer session-one');
    env.app.rawRequest({ url: '/sysuser/login' });
    assert.equal(env.calls[2].header.Authorization, undefined);
});

test('an old request returning 401 cannot clear a newly established login', () => {
    const env = application();
    env.app.setSession(env.identity);
    env.app.rawRequest({ url: '/favorite/1' });
    env.app.setSession({ ...env.identity, token: 'session-two' });
    env.calls[0].success({ statusCode: 401 });
    assert.equal(env.app.globalData.authToken, 'session-two');
    env.app.rawRequest({ url: '/favorite/1' });
    env.calls[1].success({ statusCode: 401 });
    assert.equal(env.app.globalData.authToken, '');
});

test('recommendations retain server order and refresh for the same user after a change', async () => {
    const env = application();
    env.app.setSession(env.identity);
    let calls = 0;
    env.app.request = async () => {
        calls++;
        return { data: [{ id: 2, score: 10 }, { id: 1, score: 100 }] };
    };
    const index = page('index/index.js', env);
    await index.loadRecommendations();
    assert.deepEqual(Array.from(index.data.recommendationItems, item => item.id), [2, 1]);
    index.onShow();
    assert.equal(calls, 1);
    env.app.markRecommendationsChanged();
    index.onShow();
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(calls, 2);
});

test('preference and favorite updates invalidate homepage recommendations', async () => {
    const env = application();
    env.app.setSession(env.identity);
    env.app.request = async () => ({ data: {} });
    const personal = page('userDetail/userDetail.js', env);
    personal.data.preferenceCity = 3;
    personal.data.preferenceCategory = '传统技艺';
    const version = env.app.globalData.recommendationVersion;
    personal.savePreference();
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(env.app.globalData.recommendationVersion, version + 1);
    const detail = page('detail/detail.js', env);
    detail.data.item = { id: 3 };
    await detail.onFavoriteTap();
    assert.equal(env.app.globalData.recommendationVersion, version + 2);
});

test('a preference change during an in-flight recommendation discards the stale result', async () => {
    const env = application();
    env.app.setSession(env.identity);
    let finishFirst;
    let calls = 0;
    env.app.request = () => {
        calls++;
        if (calls === 1) return new Promise(resolve => { finishFirst = resolve; });
        return Promise.resolve({ data: [{ id: 9, score: 5 }] });
    };
    const index = page('index/index.js', env);
    const first = index.loadRecommendations();
    env.app.markRecommendationsChanged();
    index.onShow();
    finishFirst({ data: [{ id: 1, score: 100 }] });
    await first;
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(calls, 2);
    assert.deepEqual(Array.from(index.data.recommendationItems, item => item.id), [9]);
});

test('login stores the server session and logout revokes it before clearing local state', async () => {
    const env = application();
    env.app.request = async () => ({ data: env.identity });
    const login = page('login/login.js', env);
    login.data.userName = 'tester'; login.data.password = 'testpass';
    await login.handleLogin();
    assert.equal(env.app.globalData.authToken, env.identity.token);
    let endpoint;
    env.app.request = async options => { endpoint = options.url; return {}; };
    const personal = page('userDetail/userDetail.js', env);
    personal.handleLogout();
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(endpoint, '/sysuser/logout');
    assert.equal(env.app.globalData.authToken, '');
    assert.equal(env.storage.has('session'), false);
});

test('experience configuration rejects development credentials and keeps the component dependency', () => {
    const { configure } = require('../scripts/configure.cjs');
    const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'fujian-config-'));
    fs.writeFileSync(path.join(tmp, 'project.config.json'), JSON.stringify({ compileType: 'miniprogram' }));
    try {
        assert.throws(() => configure(['--base-url', 'http://127.0.0.1:8080', '--appid', 'touristappid'], tmp));
        assert.throws(() => configure(['--base-url', 'https://demo.example.com', '--appid', 'touristappid'], tmp));
        configure(['--base-url', 'https://demo.example.com', '--appid', 'wx0123456789abcdef'], tmp);
        assert.equal(JSON.parse(fs.readFileSync(path.join(tmp, 'project.config.json'))).appid, 'wx0123456789abcdef');
        const ignores = JSON.parse(fs.readFileSync(path.join(root, 'project.config.json'))).packOptions.ignore;
        assert.equal(ignores.some(rule => rule.value === 'components/ec-canvas'), false);
    } finally {
        // Remove only the two files authored by this test, without recursive deletion.
        fs.unlinkSync(path.join(tmp, 'project.config.json'));
        if (fs.existsSync(path.join(tmp, 'config.js'))) fs.unlinkSync(path.join(tmp, 'config.js'));
        fs.rmdirSync(tmp);
    }
});

const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const assert = require('node:assert/strict');

async function check() {
    let app, register;
    let response;
    let toast;
    const wx = {
        request: options => options.success(response),
        getStorageSync() {}, removeStorageSync() {},
        showToast: options => { toast = options.title; }
    };
    vm.runInNewContext(fs.readFileSync(path.join(__dirname, 'app.js'), 'utf8'), {
        App: value => { app = value; }, wx, require: () => ({ baseUrl: 'http://localhost:8080' })
    });
    vm.runInNewContext(fs.readFileSync(path.join(__dirname, 'pages/register/register.js'), 'utf8'), {
        Page: value => { register = value; }, wx, getApp: () => app,
        console: { error() {} }, setTimeout() {}
    });
    register.setData = values => Object.assign(register.data, values);
    Object.assign(register.data, { userName: 'tester', password: 'secret12', rePassword: 'secret12' });
    for (const [statusCode, code, msg, expected] of [
        [400, 400, '用户名格式无效', '用户名格式无效'],
        [200, 500, '用户名重复', '用户名已被注册'],
        [200, 500, '该账号已被注册', '用户名已被注册'],
        [200, 500, '密码长度不能少于6位', '密码长度不能少于6位']
    ]) {
        response = { statusCode, data: { code, msg } };
        await register.handleRegister();
        assert.equal(toast, expected);
    }
    console.log('注册错误提示行为检查通过：4项');
}
check().catch(error => { console.error(error); process.exitCode = 1; });

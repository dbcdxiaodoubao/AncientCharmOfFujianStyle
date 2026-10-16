const fs = require('node:fs');
const path = require('node:path');

function configure(args, root = path.resolve(__dirname, '..')) {
    const values = {};
    for (let i = 0; i < args.length; i += 2) {
        if (!['--base-url', '--appid', '--mode'].includes(args[i]) || !args[i + 1]) {
            throw new Error('用法：node scripts/configure.cjs --base-url 地址 --appid AppID --mode development|experience');
        }
        values[args[i]] = args[i + 1];
    }
    const mode = values['--mode'] || 'experience';
    if (!['development', 'experience'].includes(mode)) throw new Error('环境必须为 development 或 experience');
    const origin = new URL(values['--base-url']);
    if (!['http:', 'https:'].includes(origin.protocol) || origin.username || origin.password
            || origin.pathname !== '/' || origin.search || origin.hash) {
        throw new Error('后端地址必须是完整域名或IP的 origin，不含账号、路径、查询或片段');
    }
    const appid = values['--appid'];
    if (mode === 'experience') {
        if (origin.protocol !== 'https:' || /^(localhost|127\.|0\.|\[::1\])/.test(origin.hostname)) {
            throw new Error('体验环境必须使用手机可访问的 HTTPS 域名');
        }
        if (!/^wx[a-f0-9]{16}$/i.test(appid || '')) throw new Error('体验环境需要真实微信 AppID');
    } else if (appid !== 'touristappid' && !/^wx[a-f0-9]{16}$/i.test(appid || '')) {
        throw new Error('开发环境请填写真实 AppID 或 touristappid');
    }
    const projectFile = path.join(root, 'project.config.json');
    const project = JSON.parse(fs.readFileSync(projectFile, 'utf8'));
    project.appid = appid;
    fs.writeFileSync(projectFile, JSON.stringify(project, null, 2) + '\n');
    fs.writeFileSync(path.join(root, 'config.js'), 'module.exports = ' + JSON.stringify({ baseUrl: origin.origin, mode }, null, 2) + ';\n');
    return { baseUrl: origin.origin, appid, mode };
}

if (require.main === module) {
    try { console.log('配置已写入：', configure(process.argv.slice(2))); }
    catch (error) { console.error(error.message); process.exitCode = 1; }
}
module.exports = { configure };

const fs = require('fs');
const path = require('path');

const appSource = fs.readFileSync(path.join(__dirname, 'app.js'), 'utf8');
const registerSource = fs.readFileSync(path.join(__dirname, 'pages', 'register', 'register.js'), 'utf8');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(
  /if \(res\.statusCode !== 200\) \{\s*return reject\(\{\s*httpStatus: res\.statusCode,\s*msg: res\.data\?\.msg \|\| '网络请求失败'\s*\}\);\s*\}/.test(appSource),
  'HTTP 非 200 响应必须优先保留 res.data.msg'
);

assert(
  /error\.msg\?\.includes\('用户名重复'\) \|\| error\.msg\?\.includes\('已被注册'\)/.test(registerSource),
  '注册页必须识别“用户名重复”和“已被注册”'
);

assert(
  /let title = error\.msg \|\| '注册失败';/.test(registerSource),
  '注册页必须直接展示其他后端错误消息'
);

console.log('注册错误提示静态契约通过');

# 古韵闽风

福建非遗检索、规则推荐、AI问答、路线规划与打卡小程序。前端为微信原生小程序，后端为 Spring Boot / MyBatis-Plus。

## 环境与入口

- JDK 17 或 21、Maven 3.8+；本轮在 JDK 21 和 Maven 3.9.9 验证。
- Node.js 18+ 用于配置脚本和前端行为测试。
- 微信开发者工具导入 `frontend`。已有 `components/ec-canvas`，当前不需要运行 npm install。
- 默认后端 origin 为 `http://127.0.0.1:8080`，API 路径前缀为 `/AncientCharmOfFujianStyle`。

## 1. 无外部数据库的演示

```sh
cd backend
mvn clean package
java -jar target/AncientCharmOfFujianStyle-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

访问 `http://127.0.0.1:8080/AncientCharmOfFujianStyle/map` 可看到10条带“演示”前缀的模拟项目；API文档位于 `/AncientCharmOfFujianStyle/doc.html`。先在小程序注册自己的测试账户，再登录、收藏、设偏好、生成路线和发布打卡，没有默认共享账号。

演示模式使用内存 H2 数据库，自动创建全部基础表与辅助表。项目名称与说明均为团队编写的测试夹具，不代表真实非遗名录、真实场所或开放信息；不使用来源不明的照片。演示数据库在应用退出后清空，上传图片存放在启动工作目录的 `uploads`。正式部署使用下面的 MySQL 模式。

未设置 `AI_API_KEY` 时，地图、推荐、路线及打卡照常运行，AI页面会提示助手暂未开放。不会伪造模型回复。

## 2. MySQL 空数据库启动

需要 MySQL 8.x。先用数据库管理账户创建一个专用数据库：

```sql
CREATE DATABASE ancient_charm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

为应用配置专用账号，授予该数据库建表、查询及写入权限；启动会执行 `schema.sql` 中的 `CREATE TABLE IF NOT EXISTS`。本项目不会删除已有表或覆盖既有业务数据。已有数据库必须检查基础表字段是否匹配 `schema.sql`，该脚本不负责改造旧表。

在 Windows PowerShell 中，从 `backend` 目录启动：

```powershell
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/ancient_charm?serverTimezone=Asia/Shanghai&characterEncoding=utf8'
$env:DB_USER = '你的应用数据库用户名'
$env:DB_PASSWORD = '你的应用数据库密码'
$env:UPLOAD_PATH = 'D:/app-data/ancient-charm/uploads'
java -jar target/AncientCharmOfFujianStyle-0.0.1-SNAPSHOT.jar
```

Linux/macOS 设置同名环境变量后运行相同 jar 即可。数据库密码、模型Key和实际运营账户不放入源码。也可以把 `application.example.yml` 复制到 jar 工作目录下的 `config/application.yml`，按本机情况配置；配置文件中保留环境变量引用。

表结构包括 `sys_user`、`fyinfo`、`check_in`、`auth_session`、`user_favorite`、`user_preference`、`user_browse_history`、`check_in_like`、`check_in_comment`。空 MySQL 数据库默认没有非遗业务记录，导入已核查并有权使用的数据后才能展示真实项目。原有图片目录不能替代项目数据。

如只想在一个**新建、专用的 MySQL 演示库**里加载同样的模拟数据，可以第一次启动时添加：

```sh
java -jar target/AncientCharmOfFujianStyle-0.0.1-SNAPSHOT.jar --spring.sql.init.data-locations=classpath:demo-data.sql
```

该样例导入是一次性选项，不应在已有业务库或每次重启时重复使用。默认正式启动不加载演示数据。数据初始化方式遵循 [Spring Boot 数据初始化说明](https://docs.spring.io/spring-boot/how-to/data-initialization.html)。

## 3. 小程序模拟器、真机与体验版

默认是开发环境和游客 AppID，仅用于开发者工具模拟器。在 `frontend` 目录运行配置脚本：

```sh
# 本机模拟器
node scripts/configure.cjs --base-url http://127.0.0.1:8080 --appid touristappid --mode development

# 同一局域网真机调试：替换电脑IP和真实AppID
node scripts/configure.cjs --base-url http://192.168.1.20:8080 --appid wx0123456789abcdef --mode development

# 体验版：替换为手机能访问的真实HTTPS域名和真实AppID
node scripts/configure.cjs --base-url https://your-real-api-domain.example --appid wx0123456789abcdef --mode experience
```

上面的域名、局域网IP和 `wx0123456789abcdef` 都是格式示例，不能直接当作已开通的部署。脚本修改 `config.js` 和 `project.config.json`；地址只填写 origin，不附加 `/AncientCharmOfFujianStyle`。

局域网开发时，手机与电脑必须在同一网络，电脑防火墙允许指定端口，并在开发者工具中按微信的开发调试设置使用。手机上的 `127.0.0.1` 指向手机本身，不能用它访问电脑。游客 AppID 不作为可分发的真机体验版。

体验版需要团队持有的真实小程序 AppID、有效证书的 HTTPS 后端、微信小程序后台配置的 request/uploadFile/downloadFile 合法域名，以及为评委配置的体验权限。反向代理必须转发 `/AncientCharmOfFujianStyle`，AI SSE 请求关闭代理响应缓冲，并把读取超时设置为至少180秒。正式体验版不依赖关闭合法域名校验。

当前脚本只配置项目，不注册AppID、不购买域名、不发布后端，也不创建微信体验权限。

## 4. AI、图片与会话配置

| 环境变量 | 用途 | 默认 |
| --- | --- | --- |
| `PORT` | HTTP端口 | 8080 |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | MySQL连接 | 本地专用库，密码为空，需自行配置 |
| `UPLOAD_PATH` | 上传存储目录 | `./uploads` |
| `AI_API_URL` | 模型聊天接口 | 已配置的智谱接口 |
| `AI_API_KEY` | 模型访问Key | 空，助手暂未开放 |
| `AI_MODEL` | 模型名称 | glm-4-flash，可根据账户支持情况调整 |
| `SESSION_TTL_SECONDS` | 会话有效期 | 86400秒 |

模型调用不依赖 `OPENAI_API_KEY`。实际模型可用性、费用和额度由所使用的平台账户决定。

登录返回 `userId`、`userName`、`loginStatus`、`token`、`expiresAt`。受保护请求携带 `Authorization: Bearer <token>`，前端已统一处理普通请求、图片上传和AI流式请求。数据库只保存令牌哈希；会话过期、退出撤销或用户被封禁后拒绝请求。`POST /sysuser/logout` 撤销当前令牌。升级前仅含用户信息的缓存会被清除，需要重新登录。

旧接口中的 `userId` 参数保留兼容性，但必须与令牌身份一致。收藏、偏好、浏览、个人打卡、点赞和评论不能通过更换用户ID冒用他人。AI画像使用认证身份，匿名请求只能使用游客上下文。用户详情和账户列表不返回密码；因为本应用没有实现管理员角色，账户列表仅返回当前用户。

上传支持内容有效的 PNG/JPEG，最大5MB，服务端创建目录并生成随机文件名。上传失败不写入打卡记录，删除只能操作自己的打卡。

## 5. 验证

```sh
cd backend
mvn test
mvn package -DskipTests
cd ../frontend
node --test tests/main-flow.test.cjs
node check-register-error-contract.js
node pages/index/recommendation-contract-check.cjs
```

后端集成测试使用真实 Spring Security、MyBatis 和空演示数据库，覆盖登录、收藏、偏好、浏览推荐、路线参数/同城/无项目、打卡图片、其他用户越权、会话过期与撤销。图片存储故障另有失败路径测试。前端行为测试执行 App/Page 逻辑，验证会话存储、请求认证、推荐刷新与后端排名保留；这不等同于微信真机测试。

真机验收顺序：注册登录 → 收藏项目 → 保存偏好 → 返回首页确认推荐变化 → 生成同城/跨城路线 → AI回复或未配置提示 → 发布图片打卡 → 查看自己的打卡 → 删除自己的打卡 → 退出后重新登录。建议再用第二个账号核实无法访问或删除第一个账号的数据。

路线仍基于城市邻接关系与非遗级别，不保证真实交通时间最短；本轮没有加入真实地点、开放时段、预算约束或行程保存功能。数据检索与真实行程优化属于后续改造范围。

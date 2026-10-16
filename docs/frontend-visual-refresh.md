# 前端视觉更新

本轮只调整原生微信小程序的视图、样式、地图外观和导航图标。页面路由、原生 tabBar、AppID、后端地址、接口、会话认证、表单事件和页面业务脚本均保留。`pages/index/index.js` 仅修改 ECharts 地图展示配置，地图点击仍使用原始城市名称和 ID 筛选。

## 视觉与素材

- 纸白 `#F0EBE3`，正文 `#383C30`，辅助文字 `#716B60`，苔绿 `#596638`，鼠尾草绿 `#A6B3A7`，陶土 `#A0522D`。
- 首页、路线、个人中心、详情、登录注册、打卡和 AI 聊天统一卡片、输入框与按钮；毛玻璃不被支持时保留浅色实底与边框。
- 底部仍为微信原生导航，只修改颜色与本地图标，不引入自定义 tabBar。
- `frontend/images/ui/fujian-landscape.jpg` 为本次用**内置 imagegen**生成的装饰插画，压缩后约 100 KB，保存在项目中，无需外部图片服务。它不对应具体非遗项目或真实地点。
- 没有项目照片时显示带“示意”字样的“艺”字占位图。真实项目名称、推荐顺序、评分与路线均继续使用后端返回值，不用生成图片冒充项目实拍。
- 导航图标由 `frontend/scripts/build-ui-assets.py` 以代码绘制，输出独立 PNG，不覆盖旧图标。Pillow 只在重新生成素材时需要，小程序运行不依赖 Python。

重新优化插画并生成图标：

```powershell
cd frontend
python scripts/build-ui-assets.py --landscape '生成的原始PNG路径'
```

## 检查结果

- 现有前端行为测试 8 项、注册错误提示 4 项、推荐等静态契约 27 项通过。
- 9 个页面原有的 61 处交互元素（事件、输入绑定、操作数据及禁用状态）与原版本核对一致；路由和 tab 目标不变。
- 微信开发者工具附带的原生 WXML、WXSS 编译器通过。
- 使用当前模板、样式、实际地图 GeoJSON 及运行中的演示接口做浏览器布局检查：9 个页面，另含 320px 首页/路线/个人中心/注册和发布弹窗，共 14 个视图，无横向溢出或缺失图片。
- 调用现有首页页面方法验证地图点击、城市/级别筛选、搜索与清空、地图项目计数和推荐展开，均通过。

浏览器布局检查不是微信真机验收。当前开发者工具服务端口关闭，未自动操作模拟器。请在微信开发者工具点击“编译”，确认滚动、城市选择、收藏、偏好保存、打卡上传及 AI 流式消息；这几项的原有业务代码和绑定均未改动。本地 HTTP 调试继续遵循 README 的域名校验设置。

## 插画生成提示词

生成工具：内置 `imagegen`；非透明背景。只对输出做等比缩小与 JPEG 压缩，不改变画面内容。

```text
Use case: illustration-story. Asset type: decorative header illustration for the existing Chinese mini-program 古韵闽风, to be saved and shipped locally.
Generate ONE wide landscape watercolor and delicate Chinese ink illustration, approx 3:2 aspect ratio, high resolution. NO text, NO calligraphy, NO lettering, NO UI, NO border, NO watermarks, NO photographs.
Subject: tranquil Fujian cultural landscape with traditional southern Fujian swallowtail roof silhouettes at the lower-right quarter, finely drawn aged pale plaster walls and warm clay roof tiles, a few thin olive branches on far right, distant misty layered mountains and a quiet river behind, small distant pagoda silhouette far right. Buildings only along lower right, not central foreground. Soft warm daylight, meditative Japanese wabi-sabi handmade watercolor aesthetics with very subtle rice paper grain, airy and minimal.
COMPOSITION CRITICAL: left 55 percent of entire image mostly empty warm ivory #F0EBE3 for application title text overlaid later. Keep meaningful architecture on right 40 percent and lower half. Fade top, left and bottom edges gradually and seamlessly to EXACT warm ivory #F0EBE3. Top 20 percent is nearly blank ivory haze. Background color warm ivory #F0EBE3. Quiet restrained Morandi sage #A6B3A7, muted sand #D4C5A9, warm gray #8B7D6B, tiny ochre/tan #C4A882 and terracotta #A0522D accents. Avoid saturated green, dark dominating roofs, heavily aged/brown paper, red seals, decorative frames, dense foliage or objects in left half. Delicately detailed on right, quiet open space on left. This is clearly an artistic illustration, no claim to depict a specific real heritage site.
```

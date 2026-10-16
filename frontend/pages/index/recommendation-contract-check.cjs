const fs = require('fs');
const path = require('path');

const pagesRoot = path.resolve(__dirname, '..');
const indexJs = fs.readFileSync(path.join(pagesRoot, 'index', 'index.js'), 'utf8');
const indexWxml = fs.readFileSync(path.join(pagesRoot, 'index', 'index.wxml'), 'utf8');
const detailJs = fs.readFileSync(path.join(pagesRoot, 'detail', 'detail.js'), 'utf8');
const detailWxml = fs.readFileSync(path.join(pagesRoot, 'detail', 'detail.wxml'), 'utf8');
const userDetailJs = fs.readFileSync(path.join(pagesRoot, 'userDetail', 'userDetail.js'), 'utf8');
const userDetailWxml = fs.readFileSync(path.join(pagesRoot, 'userDetail', 'userDetail.wxml'), 'utf8');
const myCheckInJs = fs.readFileSync(path.join(pagesRoot, 'myCheckIn', 'myCheckIn.js'), 'utf8');
const myCheckInWxml = fs.readFileSync(path.join(pagesRoot, 'myCheckIn', 'myCheckIn.wxml'), 'utf8');
const checkInJs = fs.readFileSync(path.join(pagesRoot, 'checkIn', 'checkIn.js'), 'utf8');
const checkInWxml = fs.readFileSync(path.join(pagesRoot, 'checkIn', 'checkIn.wxml'), 'utf8');

const checks = [
    ['首页请求推荐接口', indexJs.includes('/recommendation')],
    ['首页在 onShow 刷新推荐', /onShow\s*\([^)]*\)\s*\{[\s\S]*loadRecommendations/.test(indexJs)],
    ['首页渲染推荐卡片', indexWxml.includes('recommendationItems')],
    ['详情写入浏览足迹', detailJs.includes('/recommendation/browse')],
    ['详情读取收藏列表', detailJs.includes('/favorite/${userId}')],
    ['详情切换收藏接口', detailJs.includes("method: isFavorite ? 'DELETE' : 'POST'")],
    ['详情提供收藏事件', detailWxml.includes('bindtap="onFavoriteTap"')],
    ['首页推荐按请求身份去重', indexJs.includes('recommendationInFlightByUser')],
    ['首页推荐仅接受最新请求结果', indexJs.includes('recommendationRequestId')],
    ['首页仅接受当前身份的推荐响应', indexJs.includes('getCurrentUserId() !== userId')],
    ['首页图片统一解析绝对地址', indexJs.includes('resolveImageUrl(pictureUrl)')],
    ['详情在 onShow 按身份同步', /onShow\s*\([^)]*\)\s*\{[\s\S]*syncUserActions/.test(detailJs)],
    ['详情按身份避免重复足迹', detailJs.includes('browsedUserId')],
    ['详情 HTTP 失败走错误展示', detailJs.includes("throw new Error('HTTP请求失败')")],
    ['详情图片统一解析绝对地址', detailJs.includes('resolveImageUrl(pictureUrl)')],
    ['收藏详情请求有限并发', userDetailJs.includes('mapWithConcurrency')],
    ['收藏详情并发上限为三', userDetailJs.includes('mapWithConcurrency(favorites, 3')],
    ['收藏详情补全使用全局并发队列', userDetailJs.includes('detailRequestQueue')],
    ['收藏加载保留用户身份快照', userDetailJs.includes('const userId = String(app.globalData.userInfo.userId)')],
    ['收藏加载使用请求序号防止过期写入', userDetailJs.includes('favoriteRequestId')],
    ['同一用户收藏批次请求去重', userDetailJs.includes('favoriteInFlightByUser')],
    ['首页推荐按用户键维护 in-flight', indexJs.includes('recommendationInFlightByUser')],
    ['首页游客使用独立 in-flight 键', indexJs.includes("userId || 'guest'")],
    ['打卡列表生成图片展示地址', checkInJs.includes('displayPictureUrl')],
    ['打卡列表使用图片展示地址', checkInWxml.includes('{{item.displayPictureUrl}}')],
    ['用户打卡记录生成图片展示地址', myCheckInJs.includes('displayPictureUrl')],
    ['用户打卡记录使用图片展示地址', myCheckInWxml.includes('{{item.displayPictureUrl}}')]
];

const failed = checks.filter(([, passed]) => !passed).map(([name]) => name);
if (failed.length) {
    console.error(`静态契约检查失败: ${failed.join('、')}`);
    process.exit(1);
}

console.log(`静态契约检查通过: ${checks.length} 项`);

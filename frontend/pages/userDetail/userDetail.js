const app = getApp();
const detailRequestQueue = [];
let activeDetailRequests = 0;

function requestFavoriteDetail(options) {
    return new Promise((resolve, reject) => {
        const run = () => {
            activeDetailRequests++;
            app.request(options).then(resolve, reject).finally(() => {
                activeDetailRequests--;
                const next = detailRequestQueue.shift();
                if (next) next();
            });
        };

        if (activeDetailRequests < 3) run();
        else detailRequestQueue.push(run);
    });
}

function mapWithConcurrency(items, concurrency, mapper) {
    const results = new Array(items.length);
    let nextIndex = 0;

    const worker = async () => {
        while (nextIndex < items.length) {
            const index = nextIndex++;
            results[index] = await mapper(items[index]);
        }
    };

    return Promise.all(Array.from({ length: Math.min(concurrency, items.length) }, worker)).then(() => results);
}

Page({
    data: {
        baseUrl: app.globalData.baseUrl + '/AncientCharmOfFujianStyle',
        userInfo: {
            userId: '无',
            userName: '无',
            status: '异常'
        },
        userList: [],
        loading: false,
        showUserList: false,
        favoriteList: [],
        favoriteLoading: false,
        favoriteLoaded: false,
        preferenceCity: '',
        preferenceCategory: '',
        preferenceLoading: false,
        preferenceLoaded: false,
        preferenceSaving: false,
        cityOptions: [
            { id: 1, name: '漳州市' }, { id: 2, name: '厦门市' }, { id: 3, name: '泉州市' },
            { id: 4, name: '莆田市' }, { id: 5, name: '福州市' }, { id: 6, name: '宁德市' },
            { id: 7, name: '南平市' }, { id: 8, name: '三明市' }, { id: 9, name: '龙岩市' }
        ],
        categoryOptions: ['传统技艺', '传统美术', '传统音乐', '传统舞蹈', '传统戏剧', '民俗']
    },

    onLoad(options) {
        this.checkLoginStatus();
    },

    onShow() {
        if (this.checkLoginStatus()) {
            this.loadUserInfo();
            this.loadFavorites();
            this.loadPreference();
        }
    },

    loadFavorites() {
        const userId = String(app.globalData.userInfo.userId);
        this.favoriteInFlightByUser = this.favoriteInFlightByUser || new Map();
        if (this.favoriteInFlightByUser.has(userId)) return;

        const favoriteRequestId = (this.favoriteRequestId || 0) + 1;
        this.favoriteRequestId = favoriteRequestId;
        this.favoriteInFlightByUser.set(userId, favoriteRequestId);
        this.setData({ favoriteLoading: true, favoriteLoaded: false });
        app.request({
            url: `/favorite/${userId}`,
            method: 'GET'
        }).then(res => {
            const favorites = Array.isArray(res.data) ? res.data : [];
            return mapWithConcurrency(favorites, 3, item => {
                const fyId = item.fyId || item.fy_id;
                if (!fyId) return { ...item, fyId: '', name: '未知非遗项目' };
                return requestFavoriteDetail({
                    url: `/map/dtl/${fyId}`,
                    method: 'GET'
                }).then(detail => ({
                    ...item,
                    fyId,
                    name: detail.data && detail.data.name ? detail.data.name : `非遗项目 ${fyId}`
                })).catch(() => ({ ...item, fyId, name: `非遗项目 ${fyId}` }));
            });
        }).then(favoriteList => {
            if (this.favoriteRequestId !== favoriteRequestId || String((app.globalData.userInfo || {}).userId || '') !== userId) return;
            this.setData({ favoriteList });
        }).catch(err => {
            console.error('获取收藏失败:', err);
            wx.showToast({ title: err.msg || '收藏加载失败', icon: 'none' });
            if (this.favoriteRequestId === favoriteRequestId && String((app.globalData.userInfo || {}).userId || '') === userId) {
                this.setData({ favoriteList: [] });
            }
        }).finally(() => {
            if (this.favoriteInFlightByUser.get(userId) === favoriteRequestId) {
                this.favoriteInFlightByUser.delete(userId);
            }
            if (this.favoriteRequestId === favoriteRequestId && String((app.globalData.userInfo || {}).userId || '') === userId) {
                this.setData({ favoriteLoading: false, favoriteLoaded: true });
            }
        });
    },

    loadPreference() {
        const userId = app.globalData.userInfo.userId;
        this.setData({ preferenceLoading: true, preferenceLoaded: false });
        app.request({
            url: `/preference/${userId}`,
            method: 'GET'
        }).then(res => {
            const preference = res.data || {};
            this.setData({
                preferenceCity: preference.city ? Number(preference.city) : '',
                preferenceCategory: preference.category || ''
            });
        }).catch(err => {
            console.error('获取偏好失败:', err);
            wx.showToast({ title: err.msg || '偏好加载失败', icon: 'none' });
        }).finally(() => {
            this.setData({ preferenceLoading: false, preferenceLoaded: true });
        });
    },

    selectPreferenceCity(e) {
        this.setData({ preferenceCity: Number(e.currentTarget.dataset.city) });
    },

    selectPreferenceCategory(e) {
        this.setData({ preferenceCategory: e.currentTarget.dataset.value });
    },

    savePreference() {
        const { preferenceCity: city, preferenceCategory: category } = this.data;
        if (!city || !category) {
            wx.showToast({ title: '请选择城市和类别', icon: 'none' });
            return;
        }
        this.setData({ preferenceSaving: true });
        app.request({
            url: '/preference',
            method: 'POST',
            data: { userId: app.globalData.userInfo.userId, city, category }
        }).then(() => {
            app.markRecommendationsChanged();
            wx.showToast({ title: '偏好已保存', icon: 'success' });
        }).catch(err => {
            console.error('保存偏好失败:', err);
            wx.showToast({ title: err.msg || '保存失败', icon: 'none' });
        }).finally(() => {
            this.setData({ preferenceSaving: false });
        });
    },

    goFavoriteDetail(e) {
        const fyId = e.currentTarget.dataset.fyId;
        if (fyId) wx.navigateTo({ url: `/pages/detail/detail?id=${fyId}` });
    },

    loadUserInfo() {
        const userInfo = app.globalData.userInfo;
        if (!userInfo || !userInfo.userId) {
            console.warn('未找到用户ID，无法请求详情');
            return;
        }

        console.log('正在请求用户详情接口，ID:', userInfo.userId);
        app.request({
            url: `/sysuser/${userInfo.userId}`,
            method: 'GET'
        })
            .then(res => {
                console.log('【详情接口】返回数据:', res);
                const serverData = res.data || res;
                console.log('【详情接口】status字段值:', serverData.status);

                const statusVal = serverData.status;
                let statusText = '异常';

                if (statusVal === 0 || statusVal === '0') {
                    statusText = '正常';
                } else if (statusVal === 1 || statusVal === '1') {
                    statusText = '封禁';
                } else if (typeof statusVal === 'string') {
                    if (statusVal.includes('正常')) {
                        statusText = '正常';
                    } else if (statusVal.includes('封禁')) {
                        statusText = '封禁';
                    } else {
                        statusText = statusVal;
                    }
                } else if (typeof statusVal === 'boolean') {
                    statusText = statusVal ? '正常' : '异常';
                } else if (statusVal === undefined) {
                    statusText = '正常';
                }

                const updatedUserInfo = {
                    ...userInfo,
                    status: statusText
                };
                app.globalData.userInfo = updatedUserInfo;
                wx.setStorageSync('userInfo', updatedUserInfo);

                this.setData({
                    userInfo: {
                        userId: serverData.userId || serverData.user_id || userInfo.userId,
                        userName: serverData.userName || serverData.user_name || userInfo.userName,
                        status: statusText
                    }
                });
                console.log('【更新后用户信息】:', this.data.userInfo);
            })
            .catch(err => {
                console.error('获取用户详情失败:', err);
                this.setData({
                    userInfo: {
                        userId: userInfo.userId,
                        userName: userInfo.userName,
                        status: userInfo.status || '正常'
                    }
                });
            });
    },

    checkLoginStatus() {
        const globalUser = app.globalData.userInfo;
        const storageUser = wx.getStorageSync('userInfo');

        if (app.globalData.authToken && globalUser && globalUser.userName && globalUser.userId) {
            return true;
        }

        if (app.globalData.authToken && storageUser && storageUser.userName && storageUser.userId) {
            app.globalData.userInfo = storageUser;
            return true;
        }

        wx.showToast({ title: '请先登录', icon: 'none', duration: 1500 });
        wx.redirectTo({ url: '/pages/login/login' });
        return false;
    },

    formatDate(time) {
        if (!time) return '无';
        if (typeof time === 'string') {
            return time.split('T')[0] || time;
        }
        if (typeof time === 'number') {
            const date = new Date(time);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        }
        if (typeof time === 'object' && time.year) {
            const year = time.year + 1900;
            const month = (time.month + 1).toString().padStart(2, '0');
            const date = time.date.toString().padStart(2, '0');
            return `${year}-${month}-${date}`;
        }
        return '无';
    },

    queryUserList(showList = false) {
        if (!this.checkLoginStatus()) return;
        this.setData({ loading: true });
        app.request({
            url: '/sysuser/list',
            method: 'GET'
        }).then(res => {
            const formatUserList = (res.data || []).map(item => ({
                ...item,
                userId: item.user_id || item.userId || '无',
                formatCreateTime: this.formatDate(item.createTime || item.create_time)
            }));
            this.setData({
                userList: formatUserList,
                showUserList: showList
            });
        }).catch(err => {
            wx.showToast({ title: err.msg || '查询失败', icon: 'none' });
        }).finally(() => {
            this.setData({ loading: false });
        });
    },

    showUserListManual() {
        if (this.data.userList.length > 0) {
            this.setData({ showUserList: true });
        } else {
            this.queryUserList(true);
        }
    },

    closeUserList() {
        this.setData({ showUserList: false });
    },

    handleLogout() {
        wx.showModal({
            title: '确认退出',
            content: '确定要退出当前账号吗？',
            confirmText: '确定退出',
            cancelText: '取消',
            confirmColor: '#dc3545',
            success: async (res) => {
                if (res.confirm) {
                    try { await app.request({ url: '/sysuser/logout', method: 'POST' }); }
                    catch (error) {
                        if (error.httpStatus !== 401) {
                            wx.showToast({ title: '退出失败，请重试', icon: 'none' });
                            return;
                        }
                    }
                    app.clearSession();
                    this.setData({
                        userInfo: { userId: '无', userName: '无', status: '异常' },
                        userList: [],
                        showUserList: false
                    });
                    wx.showToast({ title: '已退出登录', icon: 'success' });
                    setTimeout(() => {
                        wx.switchTab({ url: '/pages/index/index' });
                    }, 1000);
                }
            }
        })
    },

    goBackHome() {
        wx.switchTab({ url: '/pages/index/index' });
    },

    goMyCheckIn() {
        if (!this.checkLoginStatus()) return;
        wx.navigateTo({ url: '/pages/myCheckIn/myCheckIn' });
    }
});

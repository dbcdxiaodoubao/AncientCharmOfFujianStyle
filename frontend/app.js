const config = require('./config');

App({
    globalData: { userInfo: null, authToken: '', baseUrl: config.baseUrl, recommendationVersion: 0 },

    onLaunch() {
        const session = wx.getStorageSync('session');
        if (session && session.token && session.expiresAt > Date.now() && session.userInfo) {
            this.globalData.authToken = session.token;
            this.globalData.userInfo = session.userInfo;
            wx.setStorageSync('userInfo', session.userInfo);
        } else {
            this.clearSession();
        }
    },

    setSession(identity) {
        if (!identity || !identity.userId || !identity.token || !(identity.expiresAt > Date.now())) {
            throw new Error('登录返回的会话无效，请重试');
        }
        const userInfo = { userId: Number(identity.userId), userName: identity.userName, status: '正常' };
        this.globalData.userInfo = userInfo;
        this.globalData.authToken = identity.token;
        wx.setStorageSync('session', { token: identity.token, expiresAt: identity.expiresAt, userInfo });
        wx.setStorageSync('userInfo', userInfo);
        this.markRecommendationsChanged();
    },

    clearSession() {
        this.globalData.authToken = '';
        this.globalData.userInfo = null;
        wx.removeStorageSync('session');
        wx.removeStorageSync('userInfo');
        this.markRecommendationsChanged();
    },

    markRecommendationsChanged() {
        this.globalData.recommendationVersion++;
    },

    getAuthHeader() {
        const session = wx.getStorageSync('session');
        if (session && session.expiresAt <= Date.now()) this.clearSession();
        return this.globalData.authToken ? { Authorization: 'Bearer ' + this.globalData.authToken } : {};
    },

    handleUnauthorized(response, requestToken) {
        if (response.statusCode === 401 && requestToken && requestToken === this.globalData.authToken) {
            this.clearSession();
            wx.showToast({ title: '登录已失效，请重新登录', icon: 'none' });
        }
    },

    // Preserve the native RequestTask, including chunked AI responses.
    rawRequest(options) {
        const isLogin = /\/sysuser\/(login|register)$/.test(options.url);
        const header = { ...options.header, ...(isLogin ? {} : this.getAuthHeader()) };
        const requestToken = this.globalData.authToken;
        return wx.request({ ...options, header, success: response => {
            this.handleUnauthorized(response, requestToken);
            if (options.success) options.success(response);
        } });
    },

    uploadFile(options) {
        const header = { ...options.header, ...this.getAuthHeader() };
        const requestToken = this.globalData.authToken;
        return wx.uploadFile({ ...options, header, success: response => {
            this.handleUnauthorized(response, requestToken);
            if (options.success) options.success(response);
        } });
    },

    request({ url, method = 'GET', data = {}, header = {} }) {
        return new Promise((resolve, reject) => {
            this.rawRequest({
                url: this.globalData.baseUrl + '/AncientCharmOfFujianStyle' + url,
                method, data, header: { 'content-type': 'application/json', ...header },
                success: response => {
                    if (response.statusCode !== 200) {
                        reject({ httpStatus: response.statusCode, msg: response.data?.msg || '请求失败，请重试' });
                    } else if (response.data && response.data.code === 200) {
                        resolve(response.data);
                    } else {
                        reject({ httpStatus: 200, msg: response.data?.msg || '操作失败' });
                    }
                },
                fail: () => reject({ msg: '网络异常，请检查网络连接' })
            });
        });
    }
});

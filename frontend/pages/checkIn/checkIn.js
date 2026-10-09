const app = getApp();

const AVATAR_COLORS = ['#9F6B65', '#C08A6A', '#7C9A92', '#8E7CC3', '#B98B85', '#6A8CAF', '#C99A5B', '#A26D8A'];

function avatarFor(name) {
    const text = (name || '匿名用户').trim() || '匿名用户';
    let sum = 0;
    for (let i = 0; i < text.length; i++) {
        sum += text.charCodeAt(i);
    }
    return {
        avatarText: text.charAt(0),
        avatarColor: AVATAR_COLORS[sum % AVATAR_COLORS.length]
    };
}

function formatMomentTime(value) {
    if (!value) return '';
    const text = String(value).replace('T', ' ');
    const match = text.match(/^(\d{4})-(\d{2})-(\d{2})[ ]?(\d{2}):(\d{2})/);
    if (match) {
        return `${Number(match[2])}月${Number(match[3])}日 ${match[4]}:${match[5]}`;
    }
    return text.split(' ')[0];
}

Page({
    data: {
        activeTab: 'fyId',
        searchValue: '',
        pageNum: 1,
        pageSize: 10,
        total: 0,
        totalPages: 0,
        checkInList: [],
        loading: false,
        baseUrl: '',
        commentDraft: {},
        // 上传打卡
        checkInVisible: false,
        checkInLoading: false,
        fyId: '',
        checkInTxt: '',
        tempImagePath: '',
        fyName: ''
    },

    onLoad() {
        this.setData({ baseUrl: getApp().globalData.baseUrl + '/AncientCharmOfFujianStyle' });
    },

    onShow() {
        this.refreshList();
    },

    refreshList() {
        this.setData({
            pageNum: 1
        }, () => {
            this.fetchCheckInList();
        });
    },

    switchTab(e) {
        const tab = e.currentTarget.dataset.tab;
        this.setData({
            activeTab: tab,
            searchValue: '',
            pageNum: 1
        }, () => {
            this.fetchCheckInList();
        });
    },

    onSearchInput(e) {
        const value = e.detail.value.trim();
        this.setData({
            searchValue: value,
            pageNum: 1
        }, () => {
            this.fetchCheckInList();
        });
    },

    onImageTap(e) {
        const imageUrl = e.currentTarget.dataset.image;
        wx.previewImage({
            urls: [imageUrl],
            current: imageUrl
        });
    },

    resolveImageUrl(pictureUrl) {
        const normalizedUrl = (pictureUrl || '').replace(/\\/g, '/');
        if (!normalizedUrl || /^https?:\/\//i.test(normalizedUrl)) return normalizedUrl;
        return `${this.data.baseUrl}/${normalizedUrl.replace(/^\/+/, '')}`;
    },

    changePage(e) {
        const type = e.currentTarget.dataset.type;
        let { pageNum, totalPages } = this.data;
        if (type === 'prev') pageNum = Math.max(1, pageNum - 1);
        if (type === 'next') pageNum = Math.min(totalPages, pageNum + 1);
        this.setData({ pageNum }, () => this.fetchCheckInList());
    },

    fetchCheckInList() {
        const { activeTab, searchValue, pageNum, pageSize, baseUrl } = this.data;
        this.setData({ loading: true });

        let apiUrl = '';
        const requestParams = { pageNum, pageSize };

        if (activeTab === 'fyId') {
            apiUrl = `${baseUrl}/check-in/byfy`;
            if (searchValue && !isNaN(searchValue)) {
                requestParams.fyId = Number(searchValue);
            }
        } else {
            apiUrl = `${baseUrl}/check-in/byuser`;
            if (searchValue && !isNaN(searchValue)) {
                requestParams.userId = Number(searchValue);
            }
        }

        wx.request({
            url: apiUrl,
            method: 'GET',
            data: requestParams,
            header: { 'content-type': 'application/x-www-form-urlencoded' },
            success: (res) => {
                if (res.statusCode === 200) {
                    const { code, rows = [], total = 0 } = res.data;
                    if (code === 0 || code === 200) {
                        const handleRows = rows.map(item => Object.assign({}, item, avatarFor(item.userName), {
                            showTime: formatMomentTime(item.createTime),
                            displayPictureUrl: this.resolveImageUrl(item.pictureUrl),
                            liked: false,
                            likeCount: Number(item.likeCount) || 0,
                            commentCount: Number(item.commentCount) || 0,
                            showComments: false,
                            comments: [],
                            commentsLoaded: false
                        }));
                        const totalPages = Math.ceil(total / pageSize);
                        this.setData({
                            checkInList: handleRows,
                            total,
                            totalPages
                        }, () => this.loadLikedStates(handleRows));
                    } else {
                        this.setData({ checkInList: [], total: 0, totalPages: 0 });
                    }
                }
            },
            fail: () => {
                wx.showToast({ title: '网络异常', icon: 'none' });
            },
            complete: () => {
                this.setData({ loading: false });
            }
        });
    },

    // ============ 上传打卡 ============
    openCheckInModal() {
        const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
        if (!userInfo || !userInfo.userId) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            return;
        }
        this.setData({
            checkInVisible: true,
            fyId: '',
            checkInTxt: '',
            tempImagePath: '',
            fyName: ''
        });
    },

    closeCheckInModal() {
        this.setData({ checkInVisible: false });
    },

    inputFyId(e) {
        const fyId = e.detail.value.trim();
        this.setData({ fyId });

        if (fyId) {
            app.request({
                url: `/map/dtl/${fyId}`,
                method: 'GET'
            }).then(res => {
                if (res.data && res.data.name) {
                    this.setData({ fyName: res.data.name || '' });
                } else {
                    this.setData({ fyName: '' });
                    wx.showToast({ title: '非遗ID无效', icon: 'none' });
                }
            }).catch(() => {
                this.setData({ fyName: '' });
                wx.showToast({ title: '获取非遗信息失败', icon: 'none' });
            });
        } else {
            this.setData({ fyName: '' });
        }
    },

    inputCheckInTxt(e) {
        this.setData({ checkInTxt: e.detail.value });
    },

    chooseCheckInImage() {
        wx.chooseImage({
            count: 1,
            sizeType: ['original', 'compressed'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                this.setData({ tempImagePath: res.tempFilePaths[0] });
            },
            fail: () => {
                wx.showToast({ title: '图片选择失败', icon: 'none' });
            }
        });
    },

    uploadCheckIn() {
        const { fyId, checkInTxt, tempImagePath } = this.data;
        const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');

        if (!userInfo || !userInfo.userId) {
            wx.showToast({ title: '用户未登录', icon: 'none' });
            return;
        }

        const userId = userInfo.userId;
        const fyIdNum = parseInt(fyId);

        if (isNaN(fyIdNum) || fyIdNum <= 0) {
            wx.showToast({ title: '非遗ID不正确', icon: 'none' });
            return;
        }
        if (!tempImagePath) {
            wx.showToast({ title: '请选择图片', icon: 'none' });
            return;
        }

        this.setData({ checkInLoading: true });

        wx.uploadFile({
            url: `${app.globalData.baseUrl}/AncientCharmOfFujianStyle/check-in/upload`,
            filePath: tempImagePath,
            name: 'image',
            formData: {
                userId: userId,
                fyId: fyIdNum,
                txt: checkInTxt.trim()
            },
            success: (res) => {
                let data;
                try {
                    data = JSON.parse(res.data);
                } catch (e) {
                    wx.showToast({ title: '服务器异常', icon: 'none' });
                    return;
                }

                if (data.code === 200 || data.code === 0) {
                    wx.showToast({ title: '打卡成功！', icon: 'success' });
                    this.closeCheckInModal();
                    this.refreshList();
                } else {
                    wx.showToast({ title: data.msg || '打卡失败', icon: 'none' });
                }
            },
            fail: () => {
                wx.showToast({ title: '上传失败', icon: 'none' });
            },
            complete: () => {
                this.setData({ checkInLoading: false });
            }
        });
    },

    // ============ 点赞 / 评论 ============
    getCurrentUserId() {
        const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
        return userInfo && userInfo.userId ? userInfo.userId : null;
    },

    updateItem(id, patch) {
        const list = this.data.checkInList.map(item =>
            String(item.id) === String(id) ? Object.assign({}, item, patch) : item);
        this.setData({ checkInList: list });
    },

    loadLikedStates(items) {
        const userId = this.getCurrentUserId();
        if (!userId || !items || !items.length) return;
        const ids = items.map(item => item.id).filter(id => id).join(',');
        if (!ids) return;
        wx.request({
            url: `${this.data.baseUrl}/check-in/liked`,
            method: 'GET',
            data: { userId, checkinIds: ids },
            header: { 'content-type': 'application/x-www-form-urlencoded' },
            success: (res) => {
                if (res.statusCode === 200 && res.data && (res.data.code === 0 || res.data.code === 200)) {
                    const likedSet = new Set((res.data.data || []).map(String));
                    const list = this.data.checkInList.map(item =>
                        Object.assign({}, item, { liked: likedSet.has(String(item.id)) }));
                    this.setData({ checkInList: list });
                }
            }
        });
    },

    onToggleLike(e) {
        const id = e.currentTarget.dataset.id;
        const userId = this.getCurrentUserId();
        if (!userId) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            return;
        }
        wx.request({
            url: `${this.data.baseUrl}/check-in/like`,
            method: 'POST',
            header: { 'content-type': 'application/x-www-form-urlencoded' },
            data: { checkinId: id, userId },
            success: (res) => {
                if (res.statusCode === 200 && res.data && (res.data.code === 0 || res.data.code === 200) && res.data.data) {
                    this.updateItem(id, {
                        liked: !!res.data.data.liked,
                        likeCount: Number(res.data.data.likeCount) || 0
                    });
                } else {
                    wx.showToast({ title: (res.data && res.data.msg) || '操作失败', icon: 'none' });
                }
            },
            fail: () => wx.showToast({ title: '网络异常', icon: 'none' })
        });
    },

    onToggleComments(e) {
        const id = e.currentTarget.dataset.id;
        const item = this.data.checkInList.find(it => String(it.id) === String(id));
        if (!item) return;
        const show = !item.showComments;
        this.updateItem(id, { showComments: show });
        if (show && !item.commentsLoaded) {
            this.loadComments(id);
        }
    },

    loadComments(id) {
        wx.request({
            url: `${this.data.baseUrl}/check-in/comments`,
            method: 'GET',
            data: { checkinId: id },
            header: { 'content-type': 'application/x-www-form-urlencoded' },
            success: (res) => {
                if (res.statusCode === 200 && res.data && (res.data.code === 0 || res.data.code === 200)) {
                    this.updateItem(id, { comments: res.data.data || [], commentsLoaded: true });
                } else {
                    this.updateItem(id, { commentsLoaded: true });
                }
            },
            fail: () => this.updateItem(id, { commentsLoaded: true })
        });
    },

    onCommentInput(e) {
        const id = e.currentTarget.dataset.id;
        this.setData({ ['commentDraft.' + id]: e.detail.value });
    },

    onSendComment(e) {
        const id = e.currentTarget.dataset.id;
        const userId = this.getCurrentUserId();
        if (!userId) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            return;
        }
        const content = (this.data.commentDraft[id] || '').trim();
        if (!content) {
            wx.showToast({ title: '请输入评论内容', icon: 'none' });
            return;
        }
        wx.request({
            url: `${this.data.baseUrl}/check-in/comment`,
            method: 'POST',
            header: { 'content-type': 'application/json' },
            data: { checkinId: id, userId, content },
            success: (res) => {
                if (res.statusCode === 200 && res.data && (res.data.code === 0 || res.data.code === 200) && res.data.data) {
                    const item = this.data.checkInList.find(it => String(it.id) === String(id));
                    const comments = (item ? item.comments : []).concat([res.data.data]);
                    const commentCount = (item ? Number(item.commentCount) || 0 : 0) + 1;
                    this.updateItem(id, { comments, commentCount, commentsLoaded: true, showComments: true });
                    this.setData({ ['commentDraft.' + id]: '' });
                } else {
                    wx.showToast({ title: (res.data && res.data.msg) || '评论失败', icon: 'none' });
                }
            },
            fail: () => wx.showToast({ title: '网络异常', icon: 'none' })
        });
    }
});

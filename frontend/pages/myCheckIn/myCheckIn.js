const app = getApp();

const AVATAR_COLORS = ['#9F6B65', '#C08A6A', '#7C9A92', '#8E7CC3', '#B98B85', '#6A8CAF', '#C99A5B', '#A26D8A'];

function avatarFor(name) {
    const text = (name || '我').trim() || '我';
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
        list: [],
        loading: false,
        baseUrl: ''
    },

    onLoad() {
        this.setData({ baseUrl: app.globalData.baseUrl + '/AncientCharmOfFujianStyle' });
    },

    onShow() {
        this.fetchList();
    },

    resolveImageUrl(pictureUrl) {
        const normalizedUrl = (pictureUrl || '').replace(/\\/g, '/');
        if (!normalizedUrl || /^https?:\/\//i.test(normalizedUrl)) return normalizedUrl;
        return `${this.data.baseUrl}/${normalizedUrl.replace(/^\/+/, '')}`;
    },

    fetchList() {
        const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
        if (!userInfo || !userInfo.userId) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            return;
        }

        this.setData({ loading: true });
        wx.request({
            url: `${this.data.baseUrl}/check-in/byuser`,
            method: 'GET',
            data: { userId: userInfo.userId, pageNum: 1, pageSize: 50 },
            header: { 'content-type': 'application/x-www-form-urlencoded' },
            success: (res) => {
                if (res.statusCode === 200 && res.data && (res.data.code === 0 || res.data.code === 200)) {
                    const list = (res.data.rows || []).map(item => Object.assign({}, item, avatarFor(item.userName), {
                        showTime: formatMomentTime(item.createTime),
                        displayPictureUrl: this.resolveImageUrl(item.pictureUrl)
                    }));
                    this.setData({ list });
                } else {
                    this.setData({ list: [] });
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

    onImageTap(e) {
        const imageUrl = e.currentTarget.dataset.image;
        if (imageUrl) {
            wx.previewImage({ urls: [imageUrl], current: imageUrl });
        }
    },

    onDelete(e) {
        const id = e.currentTarget.dataset.id;
        if (!id) return;
        wx.showModal({
            title: '确认删除',
            content: '确定删除这条打卡记录？',
            confirmColor: '#dc3545',
            success: (res) => {
                if (res.confirm) this.doDelete(id);
            }
        });
    },

    doDelete(id) {
        app.request({
            url: '/check-in',
            method: 'DELETE',
            data: { id },
            header: { 'Content-Type': 'application/x-www-form-urlencoded' }
        }).then((res) => {
            if (res.code === 0 || res.code === 200) {
                wx.showToast({ title: '删除成功', icon: 'success' });
                this.fetchList();
            } else {
                wx.showToast({ title: res.msg || '删除失败', icon: 'none' });
            }
        }).catch(() => {
            wx.showToast({ title: '网络异常', icon: 'none' });
        });
    }
});

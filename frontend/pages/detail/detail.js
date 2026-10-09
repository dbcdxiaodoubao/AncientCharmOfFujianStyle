Page({
    data: {
        baseUrl: '',
        item: null,
        isLoading: false,
        isFavorite: false,
        isFavoriteLoading: false,
        formattedDtl: [],
        cityMap: {
            1: '漳州市',
            2: '厦门市',
            3: '泉州市',
            4: '莆田市',
            5: '福州市',
            6: '宁德市',
            7: '南平市',
            8: '三明市',
            9: '龙岩市'
        },
        levelMap: {
            1: '世界级',
            2: '国家级',
            3: '省级',
            4: '市级',
            5: '县级'
        }
    },

    onLoad(options) {
        this.setData({ baseUrl: getApp().globalData.baseUrl + '/AncientCharmOfFujianStyle' });
        if (options.id) {
            console.log('通过ID加载详情，ID:', options.id);
            this.loadHeritageDetail(options.id);
        } else if (options.item) {
            try {
                const itemData = JSON.parse(decodeURIComponent(options.item));
                console.log('从列表页接收的item数据:', itemData);
                // 检查是否有ID
                if (itemData.id) {
                    console.log('检测到ID，通过ID加载完整详情:', itemData.id);
                    this.loadHeritageDetail(itemData.id);
                } else {
                    console.log('没有ID，使用列表页传递的数据');
                    this.processAndDisplayData(itemData);
                }
            } catch (error) {
                console.error('解析item数据失败:', error);
                this.showError('数据加载失败');
            }
        } else {
            this.showError('缺少项目数据');
        }
    },

    onShow() {
        if (this.data.item) this.syncUserActions(this.data.item.id);
    },

    async loadHeritageDetail(id) {
        this.setData({ isLoading: true });
        console.log('开始加载详情数据，ID:', id);

        try {
            const res = await new Promise((resolve, reject) => {
                wx.request({
                    url: `${getApp().globalData.baseUrl}/AncientCharmOfFujianStyle/map/dtl/${id}`,
                    method: 'GET',
                    header: { 'content-type': 'application/x-www-form-urlencoded' },
                    success: resolve,
                    fail: reject
                });
            });

            console.log('详情接口响应数据:', res.data);

            if (res.statusCode === 200) {
                if (res.data.code === 200 && res.data.data) {
                    this.processAndDisplayData(res.data.data);
                } else {
                    console.error('接口返回错误状态:', res.data.code);
                    throw new Error(res.data.msg || '接口返回数据为空');
                }
            } else {
                throw new Error('HTTP请求失败');
            }
        } catch (error) {
            console.error(error);
            this.showError(error.msg || error.message || '数据加载失败');
        }
    },

    processAndDisplayData(itemData) {
        const processedItem = this.processItemData(itemData);
        const formattedDtl = this.formatHtmlToNodes(processedItem.dtl || '');

        this.setData({
            item: processedItem,
            formattedDtl: formattedDtl,
            isLoading: false
        }, () => {

        });
        wx.setNavigationBarTitle({ title: processedItem.name || '非遗详情' });
        this.syncUserActions(processedItem.id);
    },

    getCurrentUserId() {
        const app = getApp();
        const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
        return userInfo && userInfo.userId ? userInfo.userId : '';
    },

    async syncUserActions(fyId) {
        const userId = this.getCurrentUserId();
        const syncKey = `${userId || ''}:${fyId || ''}`;
        if (this.syncedUserKey === syncKey) return;
        this.syncedUserKey = syncKey;
        if (!userId || !fyId) {
            this.setData({ isFavorite: false });
            return;
        }

        const query = `?userId=${encodeURIComponent(userId)}&fyId=${encodeURIComponent(fyId)}`;
        try {
            const favorites = await getApp().request({ url: `/favorite/${userId}` });
            if (this.getCurrentUserId() !== userId) return;
            this.setData({
                isFavorite: (favorites.data || []).some(favorite => String(favorite.fyId || favorite.id) === String(fyId))
            });
        } catch (error) {
            console.error('读取收藏状态失败:', error);
            if (this.getCurrentUserId() === userId) this.setData({ isFavorite: false });
        }

        if (this.browsedUserId === String(userId)) return;
        this.browsedUserId = String(userId);
        try {
            await getApp().request({ url: `/recommendation/browse${query}`, method: 'POST' });
        } catch (error) {
            console.error('写入浏览足迹失败:', error);
        }
    },

    async onFavoriteTap() {
        const userId = this.getCurrentUserId();
        const fyId = this.data.item && this.data.item.id;
        if (!userId) {
            wx.showToast({ title: '请先登录后再收藏', icon: 'none' });
            return;
        }
        if (!fyId || this.data.isFavoriteLoading) return;

        const isFavorite = this.data.isFavorite;
        this.setData({ isFavoriteLoading: true });
        try {
            await getApp().request({
                url: `/favorite?userId=${encodeURIComponent(userId)}&fyId=${encodeURIComponent(fyId)}`,
                method: isFavorite ? 'DELETE' : 'POST'
            });
            this.setData({ isFavorite: !isFavorite });
            wx.showToast({ title: isFavorite ? '已取消收藏' : '收藏成功', icon: 'success' });
        } catch (error) {
            console.error('收藏操作失败:', error);
            wx.showToast({ title: error.msg || '操作失败，请重试', icon: 'none' });
        } finally {
            this.setData({ isFavoriteLoading: false });
        }
    },

    processItemData(itemData) {
        if (!itemData) return {};

        const pictureUrl = this.resolveImageUrl(itemData.pictureUrl);

        return {
            ...itemData,
            pictureUrl: pictureUrl,
            cityName: this.getCityName(itemData.city),
            level: this.getLevelName(itemData.level)
        };
    },

    resolveImageUrl(pictureUrl) {
        const normalizedUrl = (pictureUrl || '').replace(/\\/g, '/');
        if (!normalizedUrl || /^https?:\/\//i.test(normalizedUrl)) return normalizedUrl;
        return `${this.data.baseUrl}/${normalizedUrl.replace(/^\/+/, '')}`;
    },

    formatHtmlToNodes(html) {
        // 处理换行标签
        const parts = html.split(/<br\s*\/?>/i);
        const nodes = [];
        
        parts.forEach((part, index) => {
            if (part.trim()) {
                nodes.push({
                    name: 'p',
                    attrs: {
                        style: 'margin-bottom: 5rpx; line-height: 1.8;'
                    },
                    children: [{
                        type: 'text',
                        text: part
                    }]
                });
            }
            if (index !== parts.length - 1) {
                nodes.push({
                    name: 'br'
                });
            }
        });
        return nodes;
    },

    getCityName(cityCode) {
        return this.data.cityMap[cityCode] || '未知城市';
    },

    getLevelName(levelNumber) {
        return this.data.levelMap[levelNumber] || '县级';
    },

    onImageError(e) {
        console.log('Image load error:', e);
    },

    onImageTap(e) {
        const imageUrl = e.currentTarget.dataset.image;
        wx.previewImage({
            urls: [imageUrl],
            current: imageUrl
        });
    },

    showError(message) {
        console.error('错误信息:', message);
        this.setData({ isLoading: false, item: null });
        wx.showToast({ title: message, icon: 'none', duration: 3000 });
        setTimeout(() => {
            if (getCurrentPages().length > 1) wx.navigateBack();
        }, 3000);
    }
});

Page({
    data: {
        item: null,
        isLoading: false,
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

    async loadHeritageDetail(id) {
        this.setData({ isLoading: true });
        console.log('开始加载详情数据，ID:', id);

        try {
            const res = await new Promise((resolve, reject) => {
                wx.request({
                    url: `https://122.246.0.213:45333/AncientCharmOfFujianStyle/map/dtl/${id}`,
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
                console.error('HTTP请求失败');
            }
        } catch (error) {
            console.error(error);
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
    },

    processItemData(itemData) {
        if (!itemData) return {};

        let pictureUrl = itemData.pictureUrl || '';
        if (pictureUrl) {
            pictureUrl = pictureUrl.replace(/\\/g, '/').replace(/^\//, '');
        }

        return {
            ...itemData,
            pictureUrl: pictureUrl,
            cityName: this.getCityName(itemData.city),
            level: this.getLevelName(itemData.level)
        };
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

    onImageError() {

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
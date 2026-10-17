Page({
    data: {
        baseUrl: '',
        cityList: [
            { id: 1, name: '漳州市' },
            { id: 2, name: '厦门市' },
            { id: 3, name: '泉州市' },
            { id: 4, name: '莆田市' },
            { id: 5, name: '福州市' },
            { id: 6, name: '宁德市' },
            { id: 7, name: '南平市' },
            { id: 8, name: '三明市' },
            { id: 9, name: '龙岩市' }
        ],
        startCityIndex: -1, 
        endCityIndex: -1,   
        selectedStartCity: {}, 
        selectedEndCity: {},   
        days: 2,
        transportMinutes: 60,
        isLoading: false, 
        hasPlanned: false,
        routeList: [] // 存储接口返回的非遗路线数据
    },

    // 选择出发城市
    onLoad() {
        this.setData({ baseUrl: getApp().globalData.baseUrl + '/AncientCharmOfFujianStyle' });
    },

    onStartCityChange(e) {
        const index = e.detail.value;
        this.setData({
            startCityIndex: index,
            selectedStartCity: this.data.cityList[index]
        });
    },

    // 选择结束城市
    onEndCityChange(e) {
        const index = e.detail.value;
        this.setData({
            endCityIndex: index,
            selectedEndCity: this.data.cityList[index]
        });
    },

    onDaysInput(e) {
        const days = Math.max(1, Math.min(14, Number(e.detail.value) || 1));
        this.setData({ days });
    },

    onTransportInput(e) {
        const transportMinutes = Math.max(0, Math.min(720, Number(e.detail.value) || 0));
        this.setData({ transportMinutes });
    },

    onAiPlanTap() {
        const { selectedStartCity, selectedEndCity, days, transportMinutes } = this.data;
        if (!selectedStartCity.id || !selectedEndCity.id) {
            wx.showToast({ title: '请先选择出发和结束城市', icon: 'none' });
            return;
        }
        const prompt = [
            '请根据以下路线规划条件，为我设计一份可执行的福建非遗旅行方案。',
            `出发城市：${selectedStartCity.name}`,
            `结束城市：${selectedEndCity.name}`,
            `游玩天数：${days}天`,
            `每天可接受交通时间：${transportMinutes}分钟`,
            '请结合我的用户画像（偏好城市、偏好类别、收藏、浏览和打卡记录）进行个性化推荐。',
            '请按天输出：当天城市顺序、推荐非遗项目、项目间交通安排和当天建议；不要超出游玩天数与每天交通时间。'
        ].join('\n');
        wx.navigateTo({
            url: `/pages/aiChat/aiChat?routePrompt=${encodeURIComponent(prompt)}`
        });
    },

    // 生成路线
    async onPlanTap() {
        const { selectedStartCity, selectedEndCity, days, transportMinutes } = this.data;
        
        // 校验城市选择
        if (!selectedStartCity.id || !selectedEndCity.id) {
            wx.showToast({
                title: '请选择出发和结束城市',
                icon: 'none'
            });
            return;
        }

        this.setData({
            isLoading: true,
            hasPlanned: true
        });

        try {
                const res = await new Promise((resolve, reject) => {
                const userInfo = getApp().globalData.userInfo || wx.getStorageSync('userInfo') || {};
                getApp().rawRequest({
                    url: `${getApp().globalData.baseUrl}/AncientCharmOfFujianStyle/plan`,
                    method: 'POST', 
                    header: { 
                        'content-type': 'application/json' 
                    },
                    data: {
                        start: selectedStartCity.id,  
                        end: selectedEndCity.id,
                        days,
                        transportMinutes,
                        userId: userInfo.userId || null
                    },
                    success: resolve,
                    fail: reject
                });
            });

            console.log('路线接口返回结果:', res.data);

            const levelMap = { 1: '世界级', 2: '国家级', 3: '省级', 4: '市级', 5: '县级' };
            const cityMap = { 1: '漳州市', 2: '厦门市', 3: '泉州市', 4: '莆田市', 5: '福州市', 6: '宁德市', 7: '南平市', 8: '三明市', 9: '龙岩市' };
            const processedData = (Array.isArray(res.data?.data) ? res.data.data : []).map(item => ({
                ...item,
                cityName: cityMap[item.city] || '未知城市', // 补充城市名称
                levelName: levelMap[item.level] || '未知级别',
                pictureUrl: /^https?:\/\//i.test(item.pictureUrl || '') ? item.pictureUrl
                    : (item.pictureUrl ? `${this.data.baseUrl}/${item.pictureUrl.replace(/^\/+/, '')}` : '')
            }));
            
            this.setData({
                isLoading: false,
                routeList: (res.data.code === 0 || res.data.code === 200) ? processedData : []
            });

            if (res.statusCode === 200) {
                if ((res.data.code === 0 || res.data.code === 200) && res.data.data.length === 0) {
                    wx.showToast({
                        title: '暂无路线数据',
                        icon: 'none'
                    });
                } else if (res.data.code !== 0 && res.data.code !== 200) {
                    wx.showToast({
                        title: res.data.msg || '路线规划失败',
                        icon: 'none'
                    });
                }
            }

        } catch (error) {
            console.warn('路线规划接口请求失败:', error);
            wx.showToast({
                title: '路线暂时无法生成，请检查网络后重试',
                icon: 'none',
                duration: 5000
            });
            this.setData({
                isLoading: false,
                routeList: []
            });
        }
    },

    onRouteCardTap(e) {
        // 获取点击卡片的非遗id
        const heritageId = e.currentTarget.dataset.id;
        if (!heritageId) {
            wx.showToast({
                title: '数据异常，无项目ID',
                icon: 'none'
            });
            return;
        }
        // 跳转到详情页
        wx.navigateTo({
            url: `/pages/detail/detail?id=${heritageId}`
        });
    },

    // 进入 AI 对话
    onAiChatTap() {
        wx.navigateTo({
            url: '/pages/aiChat/aiChat'
        });
    }
});

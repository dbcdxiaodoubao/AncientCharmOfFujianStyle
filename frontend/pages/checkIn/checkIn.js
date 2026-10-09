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
        baseUrl: ''
    },
    

    onLoad(options) {
        this.setData({ baseUrl: getApp().globalData.baseUrl + '/AncientCharmOfFujianStyle' });
        this.refreshList();
    },

    onShow() {
        this.refreshList();
    },

    refreshList() {
        this.setData({
            pageNum: 1,
            checkInList: []
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
            if (value) {
                this.fetchCheckInList();
            } else {
                // 清空时重新获取所有数据
                this.fetchCheckInList();
            }
        });
    },

    onPageSizeChange(e) {
        const pageSize = Number(e.detail.value) || 10;
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

    onPageSizeChange(e) {
        const pageSize = Number(e.detail.value) || 10;
        const finalPageSize = Math.max(1, Math.min(50, pageSize));
        this.setData({
            pageSize: finalPageSize,
            pageNum: 1
        }, () => {
            this.fetchCheckInList();
        });
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
        let requestParams = { pageNum, pageSize };

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
                console.log("接口返回:", res.data);
                if (res.statusCode === 200) {
                    const { code, rows = [], total = 0 } = res.data;
                    if (code === 0 || code === 200) {
                        const handleRows = rows.map(item => ({
                            ...item,
                            showDate: item.createTime ? item.createTime.split('T')[0] : '无',
                            displayPictureUrl: this.resolveImageUrl(item.pictureUrl)
                        }));
                        const totalPages = Math.ceil(total / pageSize);
                        this.setData({
                            checkInList: handleRows,
                            total, totalPages
                        });
                    } else {
                        wx.showToast({ title: '暂无数据', icon: 'none' });
                    }
                }
            },
            fail: (err) => {
                console.error("请求失败", err);
                wx.showToast({ title: '网络异常', icon: 'none' });
            },
            complete: () => {
                this.setData({ loading: false });
            }
        });
    }
});

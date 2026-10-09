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
        baseUrl: 'https://122.246.0.213:45333/AncientCharmOfFujianStyle'
    },

    onLoad(options) {
        setTimeout(() => {
            this.fetchCheckInList();
        }, 100);
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
        this.setData({
            searchValue: e.detail.value.trim()
        });
    },

    onPageSizeChange(e) {
        const pageSize = Number(e.detail.value) || 10;
        const finalPageSize = pageSize < 1 ? 10 : (pageSize > 50 ? 50 : pageSize);
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
        if (type === 'prev' && pageNum > 1) {
            pageNum -= 1;
        } else if (type === 'next' && pageNum < totalPages) {
            pageNum += 1;
        }
        this.setData({ pageNum }, () => {
            this.fetchCheckInList();
        });
    },

    fetchCheckInList() {
        const { activeTab, searchValue, pageNum, pageSize, baseUrl } = this.data;
        this.setData({ loading: true });

        let apiUrl = '';
        const requestParams = {
            pageNum: pageNum,
            pageSize: pageSize
        };

        if (activeTab === 'fyId') {
            apiUrl = `${baseUrl}/check-in/byfy`;
            if (searchValue) {
                requestParams.fyId = Number(searchValue);
            }
        } else if (activeTab === 'userId') {
            apiUrl = `${baseUrl}/check-in/byuser`;
            if (searchValue) {
                requestParams.userId = Number(searchValue);
            }
        }

        wx.request({
            url: apiUrl,
            method: 'GET',
            data: requestParams,
            header: {
                'content-type': 'application/x-www-form-urlencoded'
            },
            success: (res) => {
                if (res.statusCode === 200) {
                    const { code, msg, rows = [], total = 0 } = res.data;
                    if (code === 0 || code === 200) {
                        const handleRows = rows.map(item => {
                            let showDate = '无';
                            if (item.createTime) {
                                // 提取YYYY-MM-DD
                                showDate = item.createTime.split('T')[0];
                            }
                            return { ...item, showDate };
                        });

                        const totalPages = Math.ceil(total / pageSize);
                        setTimeout(() => {
                            this.setData({
                                checkInList: handleRows,
                                total: total,
                                totalPages: totalPages
                            });
                        }, 50);
                    } else {
                        wx.showToast({ title: msg || '获取数据失败', icon: 'none' });
                    }
                } else {
                    wx.showToast({ title: `请求失败：${res.statusCode}`, icon: 'none' });
                }
            },
            fail: (err) => {
                console.error('请求失败', err);
                wx.showToast({ title: `接口访问失败：${err.errMsg}`, icon: 'none' });
            },
            complete: () => {
                setTimeout(() => {
                    this.setData({ loading: false });
                }, 100);
            }
        });
    }
});
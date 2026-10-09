import * as echarts from '../../components/ec-canvas/echarts';
const fujianGeo = require('../../static/map/fujian.js');

let chart = null;

const CITY_MAP = {
    1: '漳州市',
    2: '厦门市',
    3: '泉州市',
    4: '莆田市',
    5: '福州市',
    6: '宁德市',
    7: '南平市',
    8: '三明市',
    9: '龙岩市'
};

const CITY_NAME_TO_ID = {
    '漳州市': 1,
    '厦门市': 2,
    '泉州市': 3,
    '莆田市': 4,
    '福州市': 5,
    '宁德市': 6,
    '南平市': 7,
    '三明市': 8,
    '龙岩市': 9
};

const LEVEL_MAP = {
    1: '世界级',
    2: '国家级', 
    3: '省级',
    4: '市级',
    5: '县级'
};

const LEVEL_NAME_TO_ID = {
    'world': 1,       // 世界级对应编号1
    'national': 2,    // 国家级对应编号2
    'provincial': 3,  // 省级对应编号3
    'city': 4,        // 市级对应编号4
    'county': 5       // 县级对应编号5
};

Page({
    data: {
        ec: { lazyLoad: true },
        items: [],
        filterLevel: 'all',
        searchValue: '',
        selectedCity: '',
        filteredItems: [],
        isLoading: false
    },

    onLoad() {
        this.initChart();
        this.loadHeritageList();
    },

    async loadHeritageList() {
        this.setData({ isLoading: true });

        try {
            const res = await new Promise((resolve, reject) => {
                wx.request({
                    url: 'https://122.246.0.213:45333/AncientCharmOfFujianStyle/map',
                    method: 'GET',
                    header: { 'content-type': 'application/x-www-form-urlencoded' },
                    success: resolve,
                    fail: reject
                });
            });

            console.log('接口返回数据:', res.data);

            if (res.statusCode === 200 && res.data.code === 200) {
                const items = this.processApiData(res.data.data || []);
                this.setData({
                    items: items,
                    filteredItems: items,
                    isLoading: false
                });
                this.updateMapData();
            } else {
                throw new Error(res.data.msg || '接口返回错误');
            }
        } catch (error) {
            console.error(error);
            this.setData({ isLoading: false });
        }
    },

    processApiData(apiData) {
        if (!apiData || !Array.isArray(apiData)) return [];

        return apiData.map((item, index) => {
            let pictureUrl = item.pictureUrl || '';
            if (pictureUrl && !pictureUrl.startsWith('http')) {
                pictureUrl = pictureUrl.startsWith('/') ? pictureUrl.substring(1) : pictureUrl;
            }

            const levelName = LEVEL_MAP[item.level] || '县级';
            const cityName = CITY_MAP[item.city || 8] || '未知城市';

            return {
                id: item.id || index + 1,
                name: item.name || '未知非遗项目',
                city: item.city || 8,
                pictureUrl: pictureUrl,
                level: levelName,
                levelNumber: item.level,
                cityName: cityName,
                category: item.category || '传统技艺'
            };
        });
    },

    getLevelName(levelNumber) {
        return LEVEL_MAP[levelNumber] || '县级';
    },

    getLevelNumber(levelName) {
        return LEVEL_NAME_TO_ID[levelName] || 5;
    },

    getCityName(cityId) {
        return CITY_MAP[cityId] || '未知城市';
    },

    initChart() {
        this.ecComponent = this.selectComponent('#mychart-dom-map');
        if (!this.ecComponent) return;

        this.ecComponent.init((canvas, width, height, dpr) => {
            chart = echarts.init(canvas, null, { width, height, devicePixelRatio: dpr });
            canvas.setChart(chart);

            echarts.registerMap('fujian', fujianGeo);

            const option = {
                tooltip: { trigger: 'item', formatter: '{b}: {c}个非遗项目' },
                series: [{
                    type: 'map',
                    map: 'fujian',
                    label: { show: true, color: '#333', fontSize: 12 },
                    itemStyle: { areaColor: '#e7f8ff', borderColor: '#2973ad', borderWidth: 1 },
                    emphasis: {
                        label: { show: true, color: '#fff', fontSize: 14, fontWeight: 'bold' },
                        itemStyle: { areaColor: '#FFA500' }
                    },
                    data: Object.keys(CITY_NAME_TO_ID).map(name => ({ name, value: 0 }))
                }]
            };

            chart.setOption(option);

            chart.off('click');
            chart.on('click', (params) => {
                if (params.seriesType === 'map') {
                    this.setData({ selectedCity: params.name }, () => this.filterData());
                }
            });

            return chart;
        });
    },

    updateMapData() {
        if (!chart || !this.data.items.length) return;

        const cityCounts = {};
        this.data.items.forEach(item => {
            const cityName = this.getCityName(item.city);
            cityCounts[cityName] = (cityCounts[cityName] || 0) + 1;
        });

        const option = chart.getOption();
        option.series[0].data = option.series[0].data.map(city => ({
            ...city,
            value: cityCounts[city.name] || 0
        }));
        chart.setOption(option);
    },

    onItemTap(e) {
        const item = e.currentTarget.dataset.item;
        wx.navigateTo({
            url: `/pages/detail/detail?item=${encodeURIComponent(JSON.stringify(item))}`
        });
    },

    onFilterTap(e) {
        this.setData({ filterLevel: e.currentTarget.dataset.level }, () => this.filterData());
    },

    onSearchInput(e) {
        const value = e.detail.value;
        this.setData({ searchValue: value });
        clearTimeout(this.searchTimer);
        this.searchTimer = setTimeout(() => this.filterData(), 300);
    },

    onSearchClear() {
        this.setData({ searchValue: '' }, () => this.filterData());
    },

    clearCityFilter() {
        this.setData({ selectedCity: '' }, () => this.filterData());
    },

    filterData() {
        const { filterLevel, searchValue, items, selectedCity } = this.data;
        let filtered = [...items];

        if (selectedCity) {
            const cityId = CITY_NAME_TO_ID[selectedCity];
            filtered = filtered.filter(item => item.city === cityId);
        }

        if (filterLevel !== 'all') {
            const targetLevelNumber = this.getLevelNumber(filterLevel);
            filtered = filtered.filter(item => item.levelNumber === targetLevelNumber);
        }

        if (searchValue?.trim()) {
            const keyword = searchValue.trim().toLowerCase();
            filtered = filtered.filter(item =>
                (item.name?.toLowerCase().includes(keyword)) ||
                (item.cityName?.toLowerCase().includes(keyword)) ||
                (item.level?.toLowerCase().includes(keyword))
            );
        }

        this.setData({ filteredItems: filtered });
    },

    onPullDownRefresh() {
        this.loadHeritageList().finally(() => wx.stopPullDownRefresh());
    }
});
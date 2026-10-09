App({
    onLaunch() {
      // 启动时从缓存读取用户信息
      const cacheUserInfo = wx.getStorageSync('userInfo');
      if (cacheUserInfo && cacheUserInfo.userId) {
        this.globalData.userInfo = cacheUserInfo;
      } else {
        this.globalData.userInfo = null;
      }
    },
  
    globalData: {
      userInfo: null,
      baseUrl: 'http://127.0.0.1:8080'
    },
  
    /**
     * 封装的 wx.request 方法
     * @param {Object} options - 请求配置项
     * @returns {Promise} - 返回一个 Promise 对象
     */
    request({ url, method = 'GET', data = {}, header = {} }) {
      return new Promise((resolve, reject) => {
        const fullUrl = `${this.globalData.baseUrl}/AncientCharmOfFujianStyle${url}` ;
  
        let finalHeader = {
          'content-type': 'application/json',
          ...header
        };
  
        wx.request({
          url: fullUrl,
          method: method,
          data: data,
          header: finalHeader,
          success: (res) => {
            // 1. 首先处理 HTTP 状态码
            if (res.statusCode !== 200) {
              return reject({ httpStatus: res.statusCode, msg: res.data?.msg || '网络请求失败' });
            }
  
            // 2. 处理业务状态码
            const responseData = res.data;
  
            if (responseData.code === 200) { // 业务成功
              resolve(responseData);
            } else { 
              reject({
                httpStatus: 200, 
                msg: responseData.msg || '操作失败',
                originalData: responseData 
              });
            }
          },
          fail: (err) => {
            // 3. 处理网络层面的错误，如超时、无网络
            reject({ msg: '网络异常，请检查网络连接', originalError: err });
          }
        });
      });
    }
  });

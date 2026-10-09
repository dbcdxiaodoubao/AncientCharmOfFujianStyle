Page({
    /**
     * 页面的初始数据
     */
    data: {
      userInfo: {} // 初始化用户信息对象
    },
  
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 模拟调用用户详情接口（实际开发中替换为wx.request请求）
      this.getUserDetail(options.id); // 接收跳转时传入的用户ID
    },
  
    /**
     * 获取用户详情数据
     * @param {Number} userId 用户ID
     */
    getUserDetail(userId) {
      // 模拟接口返回数据（实际需替换为真实接口请求）
      const res = {
        "code": 200,
        "msg": "操作成功",
        "data": {
          "userId": 1,
          "userName": "admin",
          "createTime": "2026-02-01T08:40:42.000+00:00",
          "status": 0
        }
      };
  
      // 校验接口返回状态
      if (res.code === 200) {
        this.setData({
          userInfo: res.data
        });
      } else {
        wx.showToast({
          title: res.msg || '获取用户信息失败',
          icon: 'none'
        });
      }
  
      // 真实接口请求示例（替换上面的模拟数据）
      // wx.request({
      //   url: 'http://localhost:8080/AncientCharmOfFujianStyle/sysuser/' + userId,
      //   method: 'GET',
      //   success: (res) => {
      //     if (res.data.code === 200) {
      //       this.setData({
      //         userInfo: res.data.data
      //       });
      //     } else {
      //       wx.showToast({
      //         title: res.data.msg || '获取用户信息失败',
      //         icon: 'none'
      //       });
      //     }
      //   },
      //   fail: () => {
      //     wx.showToast({
      //       title: '网络请求失败',
      //       icon: 'none'
      //     });
      //   }
      // });
    }
  });
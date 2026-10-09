const app = getApp();

Page({
  data: {
    userName: '',
    password: '',
    showLoading: false
  },

  onUserNameInput(e) {
    this.setData({ userName: e.detail.value.trim() });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value.trim() });
  },

  async handleLogin() {
    const { userName, password } = this.data;
  
    if (!userName) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }
  
    this.setData({ showLoading: true });
  
    try {
      const res = await app.request({
        url: '/sysuser/login',
        method: 'POST',
        data: { userName, password },
        header: { 'content-type': 'application/json' }
      });
  
      console.log('【Login Debug】原始响应 res:', res);
      
      // 1. 【核心修复】多重容错提取数据
      let backendData = null;
  
      if (res.data && res.data.data) {
        backendData = res.data.data;
      }
      else if (res.data && res.data.userId) {
        backendData = res.data;
      }
      else if (res.data && res.data.id) {
        backendData = { 
          userId: res.data.id, 
          userName: res.data.userName || userName,
          status: res.data.status 
        };
      }
  
      if (!backendData) {
        throw new Error('无法解析服务器返回数据，请检查控制台日志');
      }
  
      console.log('【Login Debug】提取到的用户数据:', backendData);
  
      // 2. 构建用户信息
      const userInfo = {
        userId: Number(backendData.userId) || Number(backendData.id) || null,
        userName: backendData.userName || userName,
        status: backendData.status === 0 ? '正常' : '异常'
      };
  
      // 3. 再次检查 ID 是否有效
      if (!userInfo.userId || isNaN(userInfo.userId)) {
        throw new Error('登录失败：用户ID无效 (' + JSON.stringify(backendData) + ')');
      }
  
      // 4. 存入全局和缓存
      app.globalData.userInfo = userInfo;
      wx.setStorageSync('userInfo', userInfo);
  
      wx.showToast({ 
        title: '登录成功！', 
        icon: 'success' 
      });
      
      // 5. 登录成功后，直接跳转到详情页
      setTimeout(() => {
        wx.switchTab({ 
          url: '/pages/userDetail/userDetail' 
        });
      }, 1000);
  
    } catch (error) {
      console.error('【Login Error】登录过程发生异常:', error);
      
      let errorTitle = '登录失败';
      
      // 401 错误通常是用户名密码不对
      if (error.httpStatus === 401 || error.statusCode === 401) {
        errorTitle = '用户名或密码错误';
      } 
      // 自定义错误（上面 throw 的）
      else if (error.message) {
        errorTitle = error.message;
      }
  
      wx.showToast({ 
        title: errorTitle, 
        icon: 'none',
        duration: 2000
      });
    } finally {
      this.setData({ showLoading: false });
    }
  },

  gotoRegister() {
    wx.navigateTo({ url: '/pages/register/register' });
  }
});
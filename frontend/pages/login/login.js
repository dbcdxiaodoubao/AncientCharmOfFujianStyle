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
  
      app.setSession(res.data);

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
      else if (error.msg) {
        errorTitle = error.msg;
      } else if (error.message) {
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
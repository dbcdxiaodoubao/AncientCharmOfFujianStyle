const app = getApp();

Page({
  data: {
    userName: '',
    password: '',
    rePassword: '',
    showLoading: false
  },

  onUserNameInput(e) {
    this.setData({ userName: e.detail.value.trim() });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value.trim() });
  },

  onRePasswordInput(e) {
    this.setData({ rePassword: e.detail.value.trim() });
  },

  async handleRegister() {
    const { userName, password, rePassword } = this.data;
  
    if (!userName || password.length < 6 || password !== rePassword) {
      wx.showToast({ title: '输入不合法', icon: 'none' });
      return;
    }
  
    this.setData({ showLoading: true });
    try {
      // 调用注册接口
      const res = await app.request({
        url: '/sysuser/register',
        method: 'POST',
        data: { userName, password }
      });

      // 注册成功
      wx.showToast({
        title: '注册成功！',
        icon: 'success'
      });

      // 延迟跳转登录页
      setTimeout(() => {
        wx.navigateTo({
          url: '/pages/login/login'
        });
      }, 500);

    } catch (error) {
      console.error('注册失败：', error);
      let title = error.msg || '注册失败';
      if (error.msg?.includes('用户名重复') || error.msg?.includes('已被注册')) {
        title = '用户名已被注册';
      }
      wx.showToast({ title, icon: 'none' });
    } finally {
      this.setData({ showLoading: false });
    }
  },

  gotoLogin() {
    wx.navigateBack({ delta: 1 });
  }
});

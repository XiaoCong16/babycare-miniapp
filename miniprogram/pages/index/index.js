Page({
  data: {
    userId: null,
    type: 'feed',
    feedType: 'breast',
    diaperStatus: 'wet',
    note: '',
    recordTime: '',
    records: []
  },

  onLoad() {
    this.login();
    this.loadRecords();
  },

  async login() {
    wx.login({
      success: async (res) => {
        if (!res.code) return;
        const userInfo = await wx.getUserProfile({ desc: '用于完善宝宝记录信息' });
        const { nickName, avatarUrl } = userInfo.userInfo;

        wx.request({
          url: 'http://localhost:8080/api/login',
          method: 'POST',
          data: { code: res.code, nickName, avatarUrl },
          success: (resp) => {
            const data = resp.data.data;
            this.setData({ userId: data.id });
            this.requestSubscribePermission();
            this.loadRecords();
          }
        });
      }
    });
  },

  requestSubscribePermission() {
    wx.requestSubscribeMessage({
      tmplIds: ['你的模板消息ID'],
      success: (res) => {
        console.log('订阅成功', res);
      },
      fail: (err) => {
        console.log('订阅失败', err);
      }
    });
  },

  bindTypeChange(e) {
    this.setData({ type: e.detail.value });
  },

  bindFeedTypeChange(e) {
    this.setData({ feedType: e.detail.value });
  },

  bindDiaperStatusChange(e) {
    this.setData({ diaperStatus: e.detail.value });
  },

  bindTimeChange(e) {
    this.setData({ recordTime: e.detail.value });
  },

  bindNoteInput(e) {
    this.setData({ note: e.detail.value });
  },

  submitRecord() {
    const { userId, type, feedType, diaperStatus, note, recordTime } = this.data;
    wx.request({
      url: 'http://localhost:8080/api/record',
      method: 'POST',
      data: {
        userId,
        type,
        feedType,
        diaperStatus,
        note,
        recordTime: recordTime || new Date().toISOString()
      },
      success: () => {
        wx.showToast({ title: '记录成功' });
        this.loadRecords();
      }
    });
  },

  loadRecords() {
    const { userId } = this.data;
    if (!userId) return;

    wx.request({
      url: `http://localhost:8080/api/records/${userId}`,
      method: 'GET',
      success: (res) => {
        this.setData({ records: res.data.data || [] });
      }
    });
  }
});

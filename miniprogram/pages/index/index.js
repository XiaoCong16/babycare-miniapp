const { getCurrentRuntimeConfig } = require('../../config');
const request = require('../../utils/request');
const { getDateInputValue, getTimeInputValue } = require('../../utils/date');
const {
  DIAPER_OPTIONS,
  FEED_OPTIONS,
  TYPE_OPTIONS,
  buildRecordPayload,
  decorateRecord,
  validateRecordForm
} = require('../../utils/record');

function errorMessage(error, fallback) {
  return error && error.message ? error.message : fallback;
}

Page({
  data: {
    typeOptions: TYPE_OPTIONS,
    feedOptions: FEED_OPTIONS,
    diaperOptions: DIAPER_OPTIONS,
    type: 'feed',
    feedType: 'breast',
    feedTypeLabel: '母乳',
    diaperStatus: 'wet',
    diaperStatusLabel: '尿湿',
    milkAmount: '',
    diaperNote: '',
    sleepDuration: '',
    temperature: '',
    note: '',
    recordDate: '',
    recordTime: '',
    userId: null,
    loginStatus: 'loading',
    loginMessage: '正在连接宝宝档案…',
    records: [],
    loadingRecords: false,
    submitting: false,
    subscribing: false
  },

  onLoad() {
    const now = new Date();
    this.setData({
      recordDate: getDateInputValue(now),
      recordTime: getTimeInputValue(now)
    });
    this.login();
  },

  onPullDownRefresh() {
    const action = this.data.userId ? this.loadRecords() : this.login();
    Promise.resolve(action).finally(() => wx.stopPullDownRefresh());
  },

  login() {
    this.setData({ loginStatus: 'loading', loginMessage: '正在连接宝宝档案…' });
    return new Promise((resolve) => {
      wx.login({
        success: (result) => {
          if (!result.code) {
            this.handleLoginError(new Error('微信登录未返回有效凭证'));
            resolve();
            return;
          }
          request('/api/login', {
            method: 'POST',
            data: { code: result.code }
          }).then((user) => {
            getApp().globalData.userId = user.id;
            wx.setStorageSync('babycareUserId', user.id);
            this.setData({
              userId: user.id,
              loginStatus: 'ready',
              loginMessage: '已连接宝宝档案'
            });
            return this.loadRecords();
          }).catch((error) => {
            this.handleLoginError(error);
          }).finally(resolve);
        },
        fail: (error) => {
          this.handleLoginError(error);
          resolve();
        }
      });
    });
  },

  handleLoginError(error) {
    this.setData({
      loginStatus: 'error',
      loginMessage: errorMessage(error, '登录失败，请稍后重试')
    });
  },

  retryLogin() {
    this.login();
  },

  selectType(event) {
    this.setData({ type: event.currentTarget.dataset.type });
  },

  bindFeedTypeChange(event) {
    const option = FEED_OPTIONS[Number(event.detail.value)];
    this.setData({ feedType: option.value, feedTypeLabel: option.label });
  },

  bindDiaperStatusChange(event) {
    const option = DIAPER_OPTIONS[Number(event.detail.value)];
    this.setData({ diaperStatus: option.value, diaperStatusLabel: option.label });
  },

  bindDateChange(event) {
    this.setData({ recordDate: event.detail.value });
  },

  bindTimeChange(event) {
    this.setData({ recordTime: event.detail.value });
  },

  bindFieldInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [field]: event.detail.value });
  },

  submitRecord() {
    if (!this.data.userId || this.data.loginStatus !== 'ready') {
      wx.showToast({ title: '请先完成登录', icon: 'none' });
      return;
    }
    const validationMessage = validateRecordForm(this.data);
    if (validationMessage) {
      wx.showToast({ title: validationMessage, icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    const payload = buildRecordPayload(this.data);
    request('/api/record', { method: 'POST', data: payload })
      .then(() => {
        wx.showToast({ title: '记录成功', icon: 'success' });
        const now = new Date();
        this.setData({
          milkAmount: '',
          diaperNote: '',
          sleepDuration: '',
          temperature: '',
          note: '',
          recordDate: getDateInputValue(now),
          recordTime: getTimeInputValue(now)
        });
        return this.loadRecords();
      })
      .catch((error) => {
        wx.showToast({ title: errorMessage(error, '提交失败'), icon: 'none', duration: 3000 });
      })
      .finally(() => this.setData({ submitting: false }));
  },

  loadRecords() {
    if (!this.data.userId) return Promise.resolve();
    this.setData({ loadingRecords: true });
    return request(`/api/records/${this.data.userId}`)
      .then((records) => {
        this.setData({ records: (records || []).map(decorateRecord) });
      })
      .catch((error) => {
        wx.showToast({ title: errorMessage(error, '记录加载失败'), icon: 'none', duration: 3000 });
      })
      .finally(() => this.setData({ loadingRecords: false }));
  },

  requestSubscribePermission() {
    const { templateId } = getCurrentRuntimeConfig();
    if (!templateId) {
      wx.showModal({
        title: '提醒模板未配置',
        content: '请先在微信公众平台创建订阅消息模板，再把模板 ID 写入 miniprogram/config.js。',
        showCancel: false
      });
      return;
    }
    this.setData({ subscribing: true });
    wx.requestSubscribeMessage({
      tmplIds: [templateId],
      success: (result) => {
        const accepted = result[templateId] === 'accept';
        wx.showToast({ title: accepted ? '提醒已授权' : '未授权提醒', icon: 'none' });
      },
      fail: (error) => {
        wx.showToast({ title: errorMessage(error, '授权失败'), icon: 'none' });
      },
      complete: () => this.setData({ subscribing: false })
    });
  }
});

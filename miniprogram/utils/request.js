const { getCurrentEnvVersion, getCurrentRuntimeConfig, isConfiguredUrl } = require('../config');

function request(path, options) {
  const runtimeConfig = getCurrentRuntimeConfig();
  const envVersion = getCurrentEnvVersion();
  if (envVersion !== 'develop' && !isConfiguredUrl(runtimeConfig.apiBaseUrl)) {
    return Promise.reject(new Error('体验版/正式版接口域名尚未配置'));
  }
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${runtimeConfig.apiBaseUrl}${path}`,
      method: options && options.method ? options.method : 'GET',
      data: options && options.data ? options.data : undefined,
      timeout: 15000,
      header: { 'content-type': 'application/json' },
      success(response) {
        const body = response.data || {};
        if (response.statusCode < 200 || response.statusCode >= 300) {
          reject(new Error(body.message || `请求失败（${response.statusCode}）`));
          return;
        }
        if (body.success === false) {
          reject(new Error(body.message || '操作失败'));
          return;
        }
        resolve(body.data);
      },
      fail(error) {
        const message = error && error.errMsg && error.errMsg.includes('timeout')
          ? '请求超时，请检查网络后重试'
          : '无法连接服务器，请检查网络和接口地址';
        reject(new Error(message));
      }
    });
  });
}

module.exports = request;


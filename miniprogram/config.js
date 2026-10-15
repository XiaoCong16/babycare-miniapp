// 上线前将 trial/release 的 apiBaseUrl 和 templateId 替换为微信后台已配置的值。
const ENV_CONFIG = {
  develop: { apiBaseUrl: 'http://localhost:8080', templateId: '' },
  trial: { apiBaseUrl: 'https://api.example.com', templateId: '' },
  release: { apiBaseUrl: 'https://api.example.com', templateId: '' }
};

function getRuntimeConfig(envVersion) {
  return ENV_CONFIG[envVersion] || ENV_CONFIG.develop;
}

function getCurrentEnvVersion() {
  if (typeof wx === 'undefined' || typeof wx.getAccountInfoSync !== 'function') return 'develop';
  try {
    const accountInfo = wx.getAccountInfoSync();
    return accountInfo.miniProgram.envVersion || 'develop';
  } catch (error) {
    return 'develop';
  }
}

function getCurrentRuntimeConfig() {
  return getRuntimeConfig(getCurrentEnvVersion());
}

function isConfiguredUrl(url) {
  return Boolean(url) &&
    /^https?:\/\//.test(url) &&
    !/(^|\.)example\.com(?::|\/|$)/.test(url) &&
    !/(^|\.)babycare\.example(?::|\/|$)/.test(url);
}

module.exports = {
  ENV_CONFIG,
  getCurrentEnvVersion,
  getCurrentRuntimeConfig,
  getRuntimeConfig,
  isConfiguredUrl
};


const test = require('node:test');
const assert = require('node:assert/strict');

test('request sends data to the active backend and returns response data', async () => {
  let capturedOptions;
  global.wx = {
    getAccountInfoSync: () => ({ miniProgram: { envVersion: 'develop' } }),
    request: (options) => {
      capturedOptions = options;
      options.success({ statusCode: 200, data: { success: true, data: { id: 9 } } });
    }
  };
  delete require.cache[require.resolve('../utils/request')];
  const request = require('../utils/request');

  const result = await request('/api/login', { method: 'POST', data: { code: 'code' } });

  assert.equal(capturedOptions.url, 'http://localhost:8080/api/login');
  assert.equal(capturedOptions.method, 'POST');
  assert.deepEqual(result, { id: 9 });
  delete global.wx;
});

test('request rejects backend business errors', async () => {
  global.wx = {
    getAccountInfoSync: () => ({ miniProgram: { envVersion: 'develop' } }),
    request: (options) => {
      options.success({ statusCode: 200, data: { success: false, message: '保存失败' } });
    }
  };
  delete require.cache[require.resolve('../utils/request')];
  const request = require('../utils/request');

  await assert.rejects(request('/api/record'), /保存失败/);
  delete global.wx;
});

test('request blocks upload builds that still use the placeholder domain', async () => {
  global.wx = {
    getAccountInfoSync: () => ({ miniProgram: { envVersion: 'release' } }),
    request: () => assert.fail('wx.request should not run with a placeholder production domain')
  };
  delete require.cache[require.resolve('../utils/request')];
  const request = require('../utils/request');

  await assert.rejects(request('/api/login'), /接口域名尚未配置/);
  delete global.wx;
});


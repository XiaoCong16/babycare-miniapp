const test = require('node:test');
const assert = require('node:assert/strict');

const { getRuntimeConfig, isConfiguredUrl } = require('../config');

test('development environment uses the local backend', () => {
  const config = getRuntimeConfig('develop');

  assert.equal(config.apiBaseUrl, 'http://localhost:8080');
});

test('trial and release environments use https endpoints', () => {
  assert.match(getRuntimeConfig('trial').apiBaseUrl, /^https:\/\//);
  assert.match(getRuntimeConfig('release').apiBaseUrl, /^https:\/\//);
});

test('placeholder production endpoint is reported as not configured', () => {
  assert.equal(isConfiguredUrl('https://api.example.com'), false);
  assert.equal(isConfiguredUrl('https://api.babycare.example'), false);
  assert.equal(isConfiguredUrl('https://api.my-domain.cn'), true);
});


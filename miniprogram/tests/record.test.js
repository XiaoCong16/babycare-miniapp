const test = require('node:test');
const assert = require('node:assert/strict');

const {
  buildRecordPayload,
  describeRecord,
  validateRecordForm
} = require('../utils/record');

test('builds a feed payload with only feed fields', () => {
  const payload = buildRecordPayload({
    userId: 7,
    type: 'feed',
    recordDate: '2026-10-15',
    recordTime: '08:05',
    feedType: 'formula',
    milkAmount: '120.5',
    diaperStatus: 'wet',
    sleepDuration: '60',
    temperature: '37.2',
    note: '喝得很好'
  });

  assert.deepEqual(payload, {
    userId: 7,
    type: 'feed',
    recordTime: '2026-10-15T08:05:00',
    feedType: 'formula',
    milkAmount: 120.5,
    note: '喝得很好'
  });
});

test('validates fields required by each record type', () => {
  assert.equal(validateRecordForm({ type: 'sleep', sleepDuration: '' }), '请输入睡眠时长');
  assert.equal(validateRecordForm({ type: 'sleep', sleepDuration: '0' }), '睡眠时长必须大于 0 分钟');
  assert.equal(validateRecordForm({ type: 'temp', temperature: '43' }), '请输入 34℃ 到 42℃ 之间的体温');
  assert.equal(validateRecordForm({ type: 'note', note: '   ' }), '请输入备注内容');
  assert.equal(validateRecordForm({ type: 'feed', milkAmount: '120' }), '');
});

test('describes records in user-facing Chinese', () => {
  assert.equal(describeRecord({ recordType: 'feed', feedType: 'formula', milkAmount: 120 }), '配方奶 · 120ml');
  assert.equal(describeRecord({ recordType: 'sleep', sleepDuration: 90 }), '睡眠 1小时30分钟');
  assert.equal(describeRecord({ recordType: 'temp', temperature: 37.2 }), '体温 37.2℃');
});


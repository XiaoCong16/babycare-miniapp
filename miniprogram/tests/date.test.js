const test = require('node:test');
const assert = require('node:assert/strict');

const {
  formatLocalDateTime,
  getDateInputValue,
  getTimeInputValue,
  toBackendDateTime
} = require('../utils/date');

test('formats local date time without converting to UTC', () => {
  const date = new Date(2026, 9, 15, 8, 5, 6);

  assert.equal(formatLocalDateTime(date), '2026-10-15T08:05:06');
  assert.equal(getDateInputValue(date), '2026-10-15');
  assert.equal(getTimeInputValue(date), '08:05');
});

test('combines picker date and time for the backend', () => {
  assert.equal(toBackendDateTime('2026-10-15', '08:05'), '2026-10-15T08:05:00');
});


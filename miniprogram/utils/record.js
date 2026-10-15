const { formatDisplayTime, toBackendDateTime } = require('./date');

const TYPE_OPTIONS = [
  { value: 'feed', label: '喂奶', icon: '🍼' },
  { value: 'diaper', label: '尿便', icon: '🧷' },
  { value: 'sleep', label: '睡眠', icon: '🌙' },
  { value: 'temp', label: '体温', icon: '🌡️' },
  { value: 'note', label: '备注', icon: '📝' }
];

const FEED_OPTIONS = [
  { value: 'breast', label: '母乳' },
  { value: 'formula', label: '配方奶' },
  { value: 'mix', label: '混合喂养' }
];

const DIAPER_OPTIONS = [
  { value: 'wet', label: '尿湿' },
  { value: 'dry', label: '干爽' },
  { value: 'poop', label: '大便' },
  { value: 'diarrhea', label: '腹泻' }
];

function numberOrNull(value) {
  if (value === '' || value === null || value === undefined) return null;
  return Number(value);
}

function validateRecordForm(form) {
  if (!form.type) return '请选择记录类型';
  if (form.type === 'feed') {
    const milkAmount = numberOrNull(form.milkAmount);
    if (milkAmount !== null && (!Number.isFinite(milkAmount) || milkAmount <= 0 || milkAmount > 9999.9)) {
      return '奶量必须大于 0 且不超过 9999.9ml';
    }
  }
  if (form.type === 'sleep') {
    if (form.sleepDuration === '' || form.sleepDuration === null || form.sleepDuration === undefined) {
      return '请输入睡眠时长';
    }
    const duration = Number(form.sleepDuration);
    if (!Number.isInteger(duration) || duration <= 0 || duration > 1440) {
      return '睡眠时长必须大于 0 分钟';
    }
  }
  if (form.type === 'temp') {
    const temperature = Number(form.temperature);
    if (!Number.isFinite(temperature) || temperature < 34 || temperature > 42) {
      return '请输入 34℃ 到 42℃ 之间的体温';
    }
  }
  if (form.type === 'note' && !String(form.note || '').trim()) return '请输入备注内容';
  return '';
}

function buildRecordPayload(form) {
  const payload = {
    userId: form.userId,
    type: form.type,
    recordTime: toBackendDateTime(form.recordDate, form.recordTime)
  };
  if (form.type === 'feed') {
    payload.feedType = form.feedType;
    const milkAmount = numberOrNull(form.milkAmount);
    if (milkAmount !== null) payload.milkAmount = milkAmount;
  }
  if (form.type === 'diaper') {
    payload.diaperStatus = form.diaperStatus;
    const diaperNote = String(form.diaperNote || '').trim();
    if (diaperNote) payload.diaperNote = diaperNote;
  }
  if (form.type === 'sleep') payload.sleepDuration = Number(form.sleepDuration);
  if (form.type === 'temp') payload.temperature = Number(form.temperature);
  const note = String(form.note || '').trim();
  if (note) payload.note = note;
  return payload;
}

function findLabel(options, value) {
  const option = options.find((item) => item.value === value);
  return option ? option.label : value || '';
}

function formatDuration(minutes) {
  const value = Number(minutes) || 0;
  const hours = Math.floor(value / 60);
  const rest = value % 60;
  if (!hours) return `${rest}分钟`;
  if (!rest) return `${hours}小时`;
  return `${hours}小时${rest}分钟`;
}

function describeRecord(record) {
  switch (record.recordType) {
    case 'feed': {
      const detail = findLabel(FEED_OPTIONS, record.feedType);
      return record.milkAmount ? `${detail} · ${Number(record.milkAmount)}ml` : detail;
    }
    case 'diaper':
      return [findLabel(DIAPER_OPTIONS, record.diaperStatus), record.diaperNote].filter(Boolean).join(' · ');
    case 'sleep':
      return `睡眠 ${formatDuration(record.sleepDuration)}`;
    case 'temp':
      return `体温 ${Number(record.temperature)}℃`;
    case 'note':
      return record.note || '普通备注';
    default:
      return record.note || '';
  }
}

function decorateRecord(record) {
  const type = TYPE_OPTIONS.find((item) => item.value === record.recordType) || {};
  return Object.assign({}, record, {
    typeLabel: type.label || record.recordType,
    typeIcon: type.icon || '📌',
    displayTime: formatDisplayTime(record.recordTime),
    description: describeRecord(record)
  });
}

module.exports = {
  DIAPER_OPTIONS,
  FEED_OPTIONS,
  TYPE_OPTIONS,
  buildRecordPayload,
  decorateRecord,
  describeRecord,
  validateRecordForm
};


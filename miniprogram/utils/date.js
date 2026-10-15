function pad(value) {
  return value < 10 ? `0${value}` : String(value);
}

function formatLocalDateTime(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function getDateInputValue(date) {
  return formatLocalDateTime(date).slice(0, 10);
}

function getTimeInputValue(date) {
  return formatLocalDateTime(date).slice(11, 16);
}

function toBackendDateTime(date, time) {
  return `${date}T${time}:00`;
}

function formatDisplayTime(value) {
  if (!value) return '';
  const match = String(value).match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/);
  if (!match) return String(value);
  return `${match[2]}月${match[3]}日 ${match[4]}:${match[5]}`;
}

module.exports = {
  formatDisplayTime,
  formatLocalDateTime,
  getDateInputValue,
  getTimeInputValue,
  toBackendDateTime
};


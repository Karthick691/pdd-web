export const getCurrentTimestamp = () => {
  const date = new Date();
  const tzOffset = -date.getTimezoneOffset();
  const diff = tzOffset >= 0 ? '+' : '-';
  const pad = (num) => String(num).padStart(2, '0');
  
  const formattedDate = date.getFullYear() +
    '-' + pad(date.getMonth() + 1) +
    '-' + pad(date.getDate()) +
    'T' + pad(date.getHours()) +
    ':' + pad(date.getMinutes()) +
    ':' + pad(date.getSeconds()) +
    diff + pad(Math.floor(Math.abs(tzOffset) / 60)) +
    ':' + pad(Math.abs(tzOffset) % 60);
    
  return formattedDate;
};

export const getTodayDateString = () => {
  const date = new Date();
  const pad = (num) => String(num).padStart(2, '0');
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate());
};

export const getYesterdayDateString = () => {
  const date = new Date();
  date.setDate(date.getDate() - 1);
  const pad = (num) => String(num).padStart(2, '0');
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate());
};

export const formatLogTimestamp = (isoString) => {
  if (!isoString) return "";
  try {
    const datePart = isoString.split('T')[0];
    const timePart = isoString.split('T')[1]?.substring(0, 5) || "";
    
    if (!datePart || !timePart) return isoString;
    
    const today = getTodayDateString();
    const yesterday = getYesterdayDateString();
    
    // Convert 24h to 12h
    const [hour24, minute] = timePart.split(':');
    const hr = parseInt(hour24);
    const suffix = hr >= 12 ? 'PM' : 'AM';
    const hour12 = hr === 0 ? 12 : hr > 12 ? hr - 12 : hr;
    const time12h = `${hour12}:${minute} ${suffix}`;
    
    if (datePart === today) {
      return `Today ${time12h}`;
    } else if (datePart === yesterday) {
      return `Yesterday ${time12h}`;
    } else {
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      const [year, month, day] = datePart.split('-');
      const monthIndex = parseInt(month) - 1;
      return `${months[monthIndex]} ${parseInt(day)}, ${time12h}`;
    }
  } catch (e) {
    return isoString;
  }
};

// Turns an ISO date string from the server into a readable local date and time.
export function formatDateTime(isoString) {
  if (!isoString) {
    return '-';
  }
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) {
    return isoString;
  }
  return date.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

// "1 response", "3 responses"
export function pluralize(count, word) {
  return `${count} ${count === 1 ? word : word + 's'}`;
}

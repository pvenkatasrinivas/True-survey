// Green box for success messages, with a close button.
function SuccessMessage({ message, onClose }) {
  if (!message) {
    return null;
  }
  return (
    <div className="alert alert-success" role="status">
      <span>{message}</span>
      {onClose && (
        <button type="button" className="alert-close" onClick={onClose} aria-label="Close message">
          ×
        </button>
      )}
    </div>
  );
}

export default SuccessMessage;

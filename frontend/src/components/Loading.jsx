// Simple loading text shown while data is being fetched.
function Loading({ message = 'Loading...' }) {
  return (
    <div className="loading" role="status">
      <span className="loading-dot" aria-hidden="true" />
      {message}
    </div>
  );
}

export default Loading;

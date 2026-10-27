// Shown when a list has nothing in it. "children" can hold a button or link.
function EmptyState({ title, message, children }) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      {message && <p>{message}</p>}
      {children && <div className="empty-state-actions">{children}</div>}
    </div>
  );
}

export default EmptyState;

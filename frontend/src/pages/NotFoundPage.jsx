import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';

// Shown for any URL that does not match a page.
function NotFoundPage() {
  return (
    <EmptyState title="Page not found" message="The page you are looking for does not exist.">
      <Link to="/" className="btn btn-primary">
        Go to surveys
      </Link>
    </EmptyState>
  );
}

export default NotFoundPage;

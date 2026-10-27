import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

/**
 * Reads a one-time success message passed with navigate(path, { state: { message } }).
 * After reading it, the message is removed from the browser history state,
 * so it does not show again when the page is refreshed.
 */
function useFlashMessage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [message, setMessage] = useState(location.state?.message || '');

  useEffect(() => {
    if (location.state?.message) {
      setMessage(location.state.message);
      navigate(location.pathname + location.search, { replace: true, state: null });
    }
  }, [location, navigate]);

  return [message, setMessage];
}

export default useFlashMessage;

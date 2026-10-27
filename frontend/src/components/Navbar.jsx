import { Link, NavLink } from 'react-router-dom';

// Top bar shown on every page.
function Navbar() {
  return (
    <header className="navbar">
      <div className="navbar-inner container">
        <Link to="/" className="navbar-brand">
          <img src="/favicon.svg" alt="" width="28" height="28" />
          <span>True Survey</span>
        </Link>
        <nav className="navbar-links">
          {/* "end" makes Home active only on "/" and not on every page */}
          <NavLink to="/" end>
            Home
          </NavLink>
          <NavLink to="/surveys/new">Create Survey</NavLink>
        </nav>
      </div>
    </header>
  );
}

export default Navbar;

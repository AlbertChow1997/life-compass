import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/** Top navigation bar shared across all pages. */
export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleSignOut() {
    logout()
    navigate('/')
  }

  return (
    <header className="navbar">
      <NavLink to="/" className="brand">
        <span className="brand-mark">◆</span> LifeCompass
      </NavLink>
      <nav className="nav-links">
        <NavLink to="/" end>
          Shops
        </NavLink>
        <NavLink to="/posts">Posts</NavLink>
        {user?.role === 'MERCHANT' && <NavLink to="/merchant/vouchers">My Vouchers</NavLink>}
        {user?.role === 'ADMIN' && <NavLink to="/admin/posts">Moderation</NavLink>}
      </nav>
      <div className="nav-actions">
        {user ? (
          <>
            <span className="muted nav-user">
              {user.nickName} · {user.role}
            </span>
            <button className="btn-ghost" type="button" onClick={handleSignOut}>
              Sign out
            </button>
          </>
        ) : (
          <NavLink to="/login" className="btn-ghost">
            Sign in
          </NavLink>
        )}
      </div>
    </header>
  )
}

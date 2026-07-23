import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import UserMenu from './UserMenu'

/**
 * Top navigation bar shared across all pages. Shows role-specific links
 * (merchant/admin only see their own tools) and swaps the sign-in link for
 * the UserMenu once auth state has loaded and a user is present.
 */
export default function Navbar() {
  const { user, loading } = useAuth()

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
        {user?.role === 'ADMIN' && <NavLink to="/admin/support">Support</NavLink>}
      </nav>
      <div className="nav-actions">
        {!loading && user && <UserMenu />}
        {!loading && !user && (
          <NavLink to="/login" className="btn-ghost">
            Sign in
          </NavLink>
        )}
      </div>
    </header>
  )
}

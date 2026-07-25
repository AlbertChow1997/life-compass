import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Second row of the page header, shown below the Banner (see BannerSlot.tsx)
 * on pages that have one, or directly below TopBar on pages that don't.
 * Shows role-specific links (merchant/admin only see their own tools).
 */
export default function SecondaryNav() {
  const { user } = useAuth()

  return (
    <nav className="secondary-nav">
      <NavLink to="/" end>
        Shops
      </NavLink>
      <NavLink to="/posts">Posts</NavLink>
      <NavLink to="/people">People</NavLink>
      <NavLink to="/coming-soon">Takeaway</NavLink>
      <NavLink to="/coming-soon">Movies</NavLink>
      {user?.role === 'MERCHANT' && <NavLink to="/merchant/vouchers">My Vouchers</NavLink>}
      {user?.role === 'ADMIN' && <NavLink to="/admin/posts">Moderation</NavLink>}
      {user?.role === 'ADMIN' && <NavLink to="/admin/support">Support</NavLink>}
    </nav>
  )
}

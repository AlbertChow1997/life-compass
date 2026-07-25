import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Second row of the page header, shown below the Banner (see BannerSlot.tsx)
 * on pages that have one, or directly below TopBar on pages that don't.
 * Shows role-specific links (merchant/admin only see their own tools).
 */
export default function SecondaryNav() {
  const { user } = useAuth()
  const location = useLocation()

  // Takeaway and Movies both point at "/coming-soon" and only differ by
  // ?feature=, but NavLink's built-in active-state matching only looks at
  // the pathname — so both would highlight together otherwise. Passing a
  // *function* to className is required here, not just a plain string:
  // NavLink still auto-appends its own path-based "active" class even when
  // className is a string, and only the function form replaces it outright
  // instead of merging with it.
  const activeFeature = location.pathname === '/coming-soon' ? new URLSearchParams(location.search).get('feature') : null

  return (
    <nav className="secondary-nav">
      <NavLink to="/" end>
        Shops
      </NavLink>
      <NavLink to="/posts">Posts</NavLink>
      <NavLink to="/people">People</NavLink>
      <NavLink to="/coming-soon?feature=takeaway" className={() => (activeFeature === 'takeaway' ? 'active' : '')}>
        Takeaway
      </NavLink>
      <NavLink to="/coming-soon?feature=movies" className={() => (activeFeature === 'movies' ? 'active' : '')}>
        Movies
      </NavLink>
      {user?.role === 'MERCHANT' && <NavLink to="/merchant/vouchers">My Vouchers</NavLink>}
      {user?.role === 'ADMIN' && <NavLink to="/admin/posts">Moderation</NavLink>}
      {user?.role === 'ADMIN' && <NavLink to="/admin/support">Support</NavLink>}
    </nav>
  )
}

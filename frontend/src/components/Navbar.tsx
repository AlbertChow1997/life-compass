import { useEffect, useRef, useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/** Top navigation bar shared across all pages. */
export default function Navbar() {
  const { user, loading, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  // Close the dropdown on an outside click.
  useEffect(() => {
    if (!menuOpen) return
    function handleClick(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [menuOpen])

  function handleSignOut() {
    setMenuOpen(false)
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
        {!loading && user && (
          <div className="user-menu" ref={menuRef}>
            <button className="user-trigger" type="button" onClick={() => setMenuOpen((v) => !v)}>
              {user.icon ? (
                <img className="avatar" src={user.icon} alt="" />
              ) : (
                <span className="avatar avatar-fallback">{user.nickName.charAt(0).toUpperCase()}</span>
              )}
              <span className="nav-user">{user.nickName}</span>
            </button>
            {menuOpen && (
              <div className="user-dropdown">
                <button className="user-dropdown-signout" type="button" onClick={handleSignOut}>
                  Sign out
                </button>
              </div>
            )}
          </div>
        )}
        {!loading && !user && (
          <NavLink to="/login" className="btn-ghost">
            Sign in
          </NavLink>
        )}
      </div>
    </header>
  )
}

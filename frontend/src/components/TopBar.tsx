import { useEffect, useState, type KeyboardEvent } from 'react'
import { NavLink, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import UserMenu from './UserMenu'

/**
 * Slim top bar shown on every page: the favicon as a home mark plus the
 * "LifeCompass" wordmark (hidden on narrow/mobile widths to save space,
 * where the icon alone still links home), a shop/place search box, and the
 * signed-in user's avatar (or a sign-in link) on the far right.
 *
 * The search box is only meaningful on the shop list page ("/"), but it's
 * rendered globally here so it's always in the same spot. It talks to
 * ShopListPage purely through the URL's `q`/`near` query params (both read
 * this via useSearchParams, which works anywhere under the router, not just
 * inside the matched route) rather than through props or a new context:
 *  - typing while already on "/" live-updates `q`, which ShopListPage's
 *    existing client-side name filter reacts to immediately.
 *  - typing on any other page just edits local state until Enter is pressed.
 *  - pressing Enter always navigates to "/?q=<value>&near=1" — `near=1` is a
 *    one-shot signal ShopListPage watches for to run a place-name geocode
 *    search, then strips from the URL.
 */
export default function TopBar() {
  const { user, loading } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const onHome = location.pathname === '/'

  const [value, setValue] = useState(() => (onHome ? searchParams.get('q') ?? '' : ''))

  // Keep the box in sync if the URL's q param changes from elsewhere (e.g.
  // browser back/forward) while on the shop list page.
  useEffect(() => {
    if (onHome) setValue(searchParams.get('q') ?? '')
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [onHome, searchParams.get('q')])

  function onChange(next: string) {
    setValue(next)
    if (onHome) {
      setSearchParams(
        (prev) => {
          const params = new URLSearchParams(prev)
          if (next) params.set('q', next)
          else params.delete('q')
          return params
        },
        { replace: true },
      )
    }
  }

  function onKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key !== 'Enter') return
    e.preventDefault()
    const trimmed = value.trim()
    if (!trimmed) return
    navigate(`/?q=${encodeURIComponent(trimmed)}&near=1`)
  }

  return (
    <header className="topbar">
      <NavLink to="/" className="brand" aria-label="LifeCompass home">
        <img className="brand-mark" src="/images/favicon.ico" alt="" />
        <span className="brand-text">LifeCompass</span>
      </NavLink>
      <input
        className="topbar-search"
        placeholder="Search shops, or type a place and press Enter…"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={onKeyDown}
      />
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

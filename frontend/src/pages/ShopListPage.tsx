import { useEffect, useMemo, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { Link, useSearchParams } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { Shop, ShopType } from '../types'
import { euro, firstImage, stars } from '../format'

const RADIUS_OPTIONS_KM = [1, 3, 5, 10]

/**
 * Home page: browse all shops, filterable by category chip and free-text name
 * search, or switch to a distance-sorted "nearby" view — either from the
 * browser's own geolocation ("Near me") or by typing a place name into the
 * TopBar search box and pressing Enter (geocoded server-side). The search
 * text itself lives in the URL's `q` param (read/written by TopBar too, see
 * TopBar.tsx) rather than local state, since the search box is now rendered
 * globally, outside this page. Fetches the category list and shop list from
 * the backend once on mount; category/name filtering happens client-side on
 * top of whichever list (all shops or nearby) is currently active. Shows a
 * friendly message if the backend can't be reached instead of a blank page.
 */
export default function ShopListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const query = searchParams.get('q') ?? ''
  const [types, setTypes] = useState<ShopType[]>([])
  const [shops, setShops] = useState<Shop[]>([])
  const [nearbyShops, setNearbyShops] = useState<Shop[] | null>(null)
  const [nearbyLoading, setNearbyLoading] = useState(false)
  const [nearbyError, setNearbyError] = useState<string | null>(null)
  const [radiusKm, setRadiusKm] = useState(5)
  const [radiusMenuOpen, setRadiusMenuOpen] = useState(false)
  const [radiusMenuPos, setRadiusMenuPos] = useState<{ top: number; left: number } | null>(null)
  const radiusMenuRef = useRef<HTMLDivElement>(null)
  const radiusDropdownRef = useRef<HTMLDivElement>(null)
  const [activeType, setActiveType] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  // Closes the radius dropdown when the user clicks anywhere outside it —
  // same pattern UserMenu.tsx uses for its own dropdown. Checks both the
  // control and the dropdown itself, since the dropdown is portaled out to
  // document.body (see toggleRadiusMenu below) and so is no longer a DOM
  // descendant of radiusMenuRef.
  useEffect(() => {
    if (!radiusMenuOpen) return
    function handleClick(e: MouseEvent) {
      const target = e.target as Node
      if (radiusMenuRef.current?.contains(target) || radiusDropdownRef.current?.contains(target)) return
      setRadiusMenuOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [radiusMenuOpen])

  // Opens the dropdown, first computing where to place it: it's rendered via
  // a portal straight into document.body (not inside .chips) because .chips
  // scrolls horizontally (overflow-x: auto), and CSS forces overflow-y to
  // compute as auto too whenever overflow-x isn't visible — so anything
  // absolutely positioned inside it, including this dropdown, would get
  // silently clipped/covered by later content (e.g. the shop cards below)
  // no matter how high its z-index was set.
  function toggleRadiusMenu() {
    if (!radiusMenuOpen && radiusMenuRef.current) {
      const rect = radiusMenuRef.current.getBoundingClientRect()
      setRadiusMenuPos({ top: rect.bottom + 8, left: rect.left })
    }
    setRadiusMenuOpen((v) => !v)
  }

  // Fetches categories and shops together on mount. `cancelled` guards against
  // setting state after the component has unmounted (e.g. user navigates away
  // before the request finishes).
  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [typeRes, shopRes] = await Promise.all([
          api.get<ApiResult<ShopType[]>>('/shop-type'),
          api.get<ApiResult<Shop[]>>('/shop'),
        ])
        if (cancelled) return
        setTypes(typeRes.data.data ?? [])
        setShops(shopRes.data.data ?? [])
      } catch {
        if (!cancelled) {
          setError(
            'Could not reach the backend API yet. Start the Spring Boot app and implement the /api/shop endpoints, then refresh.',
          )
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [])

  // Applies the category filter and search query client-side against whichever
  // base list is active (all shops, or the nearby/distance-sorted list) —
  // .filter() preserves the base list's order, so distance sorting survives.
  // Near Me and the category chips are independent selections that combine
  // (AND), not alternatives — picking a category while nearbyShops is set
  // narrows the nearby results to that category rather than replacing them.
  const visible = useMemo(() => {
    const base = nearbyShops ?? shops
    return base.filter((s) => {
      const byType = activeType == null || s.typeId === activeType
      const byName = s.name.toLowerCase().includes(query.trim().toLowerCase())
      return byType && byName
    })
  }, [shops, nearbyShops, activeType, query])

  // Asks the browser for the user's location, then loads shops sorted by
  // distance from it. Geolocation failures (denied permission, unsupported
  // browser, timeout) show an inline message instead of breaking the page.
  function findNearMe() {
    setNearbyError(null)
    if (!('geolocation' in navigator)) {
      setNearbyError('Your browser does not support location search.')
      return
    }
    setNearbyLoading(true)
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        try {
          const res = await api.get<ApiResult<Shop[]>>('/shop/nearby', {
            params: { lat: pos.coords.latitude, lng: pos.coords.longitude, radiusKm },
          })
          setNearbyShops(res.data.data ?? [])
        } catch (err) {
          setNearbyError(apiErrorMessage(err, 'Could not load nearby shops'))
        } finally {
          setNearbyLoading(false)
        }
      },
      () => {
        setNearbyError('Could not get your location — check your browser’s location permission.')
        setNearbyLoading(false)
      },
    )
  }

  // Geocodes whatever place name is in the `q` param server-side, then loads
  // shops sorted by distance from that place — lets someone search "Dundrum"
  // or "NCI" without granting location permission.
  async function searchByPlace(place: string) {
    if (!place) return
    setNearbyError(null)
    setNearbyLoading(true)
    try {
      const res = await api.get<ApiResult<Shop[]>>('/shop/nearby-by-place', { params: { place, radiusKm } })
      setNearbyShops(res.data.data ?? [])
    } catch (err) {
      setNearbyError(apiErrorMessage(err, `Could not find "${place}"`))
    } finally {
      setNearbyLoading(false)
    }
  }

  // TopBar sets `near=1` as a one-shot signal after navigating here with a
  // typed place name in `q` (pressing Enter in the search box) — run the
  // geocode search once, then strip the trigger so it doesn't re-fire on
  // every re-render or when the user edits `q` afterwards.
  useEffect(() => {
    if (searchParams.get('near') !== '1') return
    searchByPlace(query)
    setSearchParams(
      (prev) => {
        const params = new URLSearchParams(prev)
        params.delete('near')
        return params
      },
      { replace: true },
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams.get('near')])

  return (
    <section className="page">
      <div className="chips">
        <button
          className={activeType == null ? 'chip chip-active' : 'chip'}
          onClick={() => setActiveType(null)}
          type="button"
        >
          All
        </button>
        <div className="near-me-control" ref={radiusMenuRef}>
          {/* .near-me-pill owns the capsule shape (rounded + clipped) around just the two
              buttons; the dropdown below is a sibling, not a child, of that clipped pill —
              otherwise its own overflow:hidden would clip the open dropdown panel too. */}
          <div className="near-me-pill">
            <button
              className={nearbyShops ? 'chip near-me-btn chip-active' : 'chip near-me-btn'}
              type="button"
              onClick={findNearMe}
              disabled={nearbyLoading}
            >
              {nearbyLoading ? 'Locating…' : '📍 Near me'}
            </button>
            {/* Small custom dropdown instead of a native <select> — a native select's open
                panel can't be styled consistently across browsers, so this uses the same
                popover look as the other chips/dropdowns in the app instead. */}
            <button
              className="near-me-caret-btn"
              type="button"
              aria-label="Choose search radius"
              aria-expanded={radiusMenuOpen}
              onClick={toggleRadiusMenu}
            >
              ▾
            </button>
          </div>
          {radiusMenuOpen &&
            radiusMenuPos &&
            createPortal(
              <div
                className="radius-dropdown"
                ref={radiusDropdownRef}
                style={{ position: 'fixed', top: radiusMenuPos.top, left: radiusMenuPos.left }}
              >
                {RADIUS_OPTIONS_KM.map((km) => (
                  <button
                    key={km}
                    type="button"
                    className={km === radiusKm ? 'radius-option radius-option-active' : 'radius-option'}
                    onClick={() => {
                      setRadiusKm(km)
                      setRadiusMenuOpen(false)
                    }}
                  >
                    {km} km
                  </button>
                ))}
              </div>,
              document.body,
            )}
        </div>
        {nearbyShops && (
          <button className="chip chip-clear" type="button" onClick={() => setNearbyShops(null)}>
            ✕ Clear
          </button>
        )}
        {types.map((t) => (
          <button
            key={t.id}
            className={activeType === t.id ? 'chip chip-active' : 'chip'}
            onClick={() => setActiveType(t.id)}
            type="button"
          >
            {t.name}
          </button>
        ))}
      </div>

      {nearbyError && <div className="notice notice-error">{nearbyError}</div>}
      {nearbyShops && <p className="muted nearby-status">Showing shops near {query.trim() || 'you'}, closest first.</p>}

      {loading && <p className="muted">Loading…</p>}
      {error && <div className="notice">{error}</div>}

      {!loading && !error && (
        <div className="grid">
          {visible.map((s) => (
            <Link key={s.id} to={`/shop/${s.id}`} className="card card-link">
              <div className="card-row">
                {firstImage(s.images) ? (
                  <img className="card-thumb" src={firstImage(s.images)!} alt="" />
                ) : (
                  <div className="card-thumb card-thumb-placeholder" aria-hidden="true" />
                )}
                <div className="card-body">
                  <h3>{s.name}</h3>
                  <p className="muted card-address" title={`${s.area} · ${s.address}`}>
                    {s.area} · {s.address}
                  </p>
                  <div className="card-meta">
                    <span className="rating">★ {stars(s.score)}</span>
                    <span className="muted card-meta-comments">{s.comments} ratings</span>
                    <span className="price">{euro(s.avgPrice)}/pp</span>
                    {s.distanceKm != null && <span className="muted">{s.distanceKm} km away</span>}
                  </div>
                </div>
              </div>
            </Link>
          ))}
          {visible.length === 0 && <p className="muted">No shops match your search.</p>}
        </div>
      )}
    </section>
  )
}

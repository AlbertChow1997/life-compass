import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { Shop, ShopType } from '../types'
import { euro, firstImage, stars } from '../format'
import Banner from '../components/Banner'

/**
 * Home page: browse all shops, filterable by category chip and free-text name
 * search, or switch to a "Near me" view sorted by distance from the browser's
 * geolocation. Fetches the category list and shop list from the backend once
 * on mount; category/name filtering happens client-side on top of whichever
 * list (all shops or nearby) is currently active. Shows a friendly message if
 * the backend can't be reached instead of a blank/broken page.
 */
export default function ShopListPage() {
  const [types, setTypes] = useState<ShopType[]>([])
  const [shops, setShops] = useState<Shop[]>([])
  const [nearbyShops, setNearbyShops] = useState<Shop[] | null>(null)
  const [nearbyLoading, setNearbyLoading] = useState(false)
  const [nearbyError, setNearbyError] = useState<string | null>(null)
  const [activeType, setActiveType] = useState<number | null>(null)
  const [query, setQuery] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

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
            params: { lat: pos.coords.latitude, lng: pos.coords.longitude },
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

  return (
    <section className="page">
      <Banner />
      <div className="hero">
        <h1>Discover local spots in Ireland</h1>
        <p>Restaurants, pubs, cafes and live music — rated by the community.</p>
        <div className="hero-search-row">
          <input
            className="search"
            placeholder="Search by name…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <button className="btn-ghost" type="button" onClick={findNearMe} disabled={nearbyLoading}>
            {nearbyLoading ? 'Locating…' : '📍 Near me'}
          </button>
          {nearbyShops && (
            <button className="link-button" type="button" onClick={() => setNearbyShops(null)}>
              Clear
            </button>
          )}
          <Link to="/coming-soon" className="btn-ghost" title="Takeaway ordering & movie reviews">
            🍱 More — coming soon
          </Link>
        </div>
        {nearbyError && <div className="notice notice-error">{nearbyError}</div>}
        {nearbyShops && <p className="muted">Showing shops near you, closest first.</p>}
      </div>

      <div className="chips">
        <button
          className={activeType == null ? 'chip chip-active' : 'chip'}
          onClick={() => setActiveType(null)}
          type="button"
        >
          All
        </button>
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
                  <p className="muted">
                    {s.area} · {s.address}
                  </p>
                  <div className="card-meta">
                    <span className="rating">★ {stars(s.score)}</span>
                    <span className="muted">{s.comments} ratings</span>
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

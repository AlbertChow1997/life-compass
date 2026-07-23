import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, type ApiResult } from '../api/client'
import type { Shop, ShopType } from '../types'
import { euro, firstImage, stars } from '../format'
import Banner from '../components/Banner'

/**
 * Home page: browse all shops, filterable by category chip and free-text name
 * search. Fetches the category list and shop list from the backend once on
 * mount; filtering itself happens client-side. Shows a friendly message if the
 * backend can't be reached instead of a blank/broken page.
 */
export default function ShopListPage() {
  const [types, setTypes] = useState<ShopType[]>([])
  const [shops, setShops] = useState<Shop[]>([])
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

  // Applies the category filter and search query client-side against the full
  // shop list already loaded; recomputed only when the inputs actually change.
  const visible = useMemo(() => {
    return shops.filter((s) => {
      const byType = activeType == null || s.typeId === activeType
      const byName = s.name.toLowerCase().includes(query.trim().toLowerCase())
      return byType && byName
    })
  }, [shops, activeType, query])

  return (
    <section className="page">
      <Banner />
      <div className="hero">
        <h1>Discover local spots in Ireland</h1>
        <p>Restaurants, pubs, cafes and live music — rated by the community.</p>
        <input
          className="search"
          placeholder="Search by name…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
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

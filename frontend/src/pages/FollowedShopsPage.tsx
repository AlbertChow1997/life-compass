import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { Shop } from '../types'
import { euro, firstImage, stars } from '../format'

export default function FollowedShopsPage() {
  const [shops, setShops] = useState<Shop[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<ApiResult<Shop[]>>('/user/shops')
      .then((res) => setShops(res.data.data ?? []))
      .catch((err) => setError(apiErrorMessage(err, 'Could not load followed shops.')))
      .finally(() => setLoading(false))
  }, [])

  return (
    <section className="page">
      <div className="hero">
        <h1>Followed shops</h1>
      </div>

      {loading && <p className="muted">Loading…</p>}
      {error && <div className="notice">{error}</div>}

      {!loading && !error && (
        <div className="grid">
          {shops.map((s) => (
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
                    <span className="price">{euro(s.avgPrice)}/pp</span>
                  </div>
                </div>
              </div>
            </Link>
          ))}
          {shops.length === 0 && <p className="muted">You haven't followed any shops yet.</p>}
        </div>
      )}
    </section>
  )
}

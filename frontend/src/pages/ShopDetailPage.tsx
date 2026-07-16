import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { Shop, ShopRating, Voucher } from '../types'
import { useAuth } from '../context/AuthContext'
import { euro, firstImage, stars } from '../format'

/** Shop detail: view info, buy vouchers (req 6), rate the shop (req 3). */
export default function ShopDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()

  const [shop, setShop] = useState<Shop | null>(null)
  const [ratings, setRatings] = useState<ShopRating[]>([])
  const [vouchers, setVouchers] = useState<Voucher[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const [ratingScore, setRatingScore] = useState(5)
  const [ratingContent, setRatingContent] = useState('')

  async function load() {
    setLoading(true)
    setLoadError(null)
    try {
      const [shopRes, ratingsRes, vouchersRes] = await Promise.all([
        api.get<ApiResult<Shop>>(`/shop/${id}`),
        api.get<ApiResult<ShopRating[]>>(`/shop/${id}/ratings`),
        api.get<ApiResult<Voucher[]>>(`/voucher?shopId=${id}`),
      ])
      setShop(shopRes.data.data ?? null)
      setRatings(ratingsRes.data.data ?? [])
      setVouchers(vouchersRes.data.data ?? [])
    } catch (err) {
      setLoadError(apiErrorMessage(err, 'Could not load this shop.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  async function submitRating(e: FormEvent) {
    e.preventDefault()
    setMessage(null)
    try {
      await api.post(`/shop/${id}/ratings`, { score: ratingScore, content: ratingContent })
      setMessage('Thanks for rating!')
      setRatingContent('')
      load()
    } catch (err) {
      setMessage(apiErrorMessage(err, 'Could not submit rating'))
    }
  }

  async function buyVoucher(voucherId: number) {
    setMessage(null)
    try {
      await api.post(`/voucher/${voucherId}/purchase`)
      setMessage('Voucher purchased! Check with the shop to redeem it.')
      load()
    } catch (err) {
      setMessage(apiErrorMessage(err, 'Could not purchase voucher'))
    }
  }

  if (loading) {
    return (
      <section className="page">
        <p className="muted">Loading…</p>
      </section>
    )
  }

  if (loadError || !shop) {
    return (
      <section className="page">
        <div className="notice">{loadError ?? 'Shop not found.'}</div>
      </section>
    )
  }

  return (
    <section className="page">
      <Link to="/" className="back-link">
        ← Back to shops
      </Link>

      {firstImage(shop.images) && <img className="hero-image" src={firstImage(shop.images)!} alt={shop.name} />}

      <div className="hero">
        <h1>{shop.name}</h1>
        <p className="muted">
          {shop.area} · {shop.address}
        </p>
        <div className="card-meta">
          <span className="rating">★ {stars(shop.score)}</span>
          <span className="muted">{shop.comments} ratings</span>
          <span className="price">{euro(shop.avgPrice)}/pp</span>
        </div>
      </div>

      {message && <div className="notice">{message}</div>}

      <h2>Vouchers</h2>
      <div className="grid">
        {vouchers.map((v) => (
          <article key={v.id} className="card">
            <div className="card-body">
              <h3>{v.title}</h3>
              <p className="muted">{v.subTitle}</p>
              <div className="card-meta">
                <span className="price">{euro(v.payValue)}</span>
                <span className="muted">worth {euro(v.actualValue)}</span>
              </div>
              {user ? (
                <button className="btn-primary" type="button" onClick={() => buyVoucher(v.id)}>
                  Buy
                </button>
              ) : (
                <Link to="/login" className="btn-ghost">
                  Sign in to buy
                </Link>
              )}
            </div>
          </article>
        ))}
        {vouchers.length === 0 && <p className="muted">No vouchers available right now.</p>}
      </div>

      <h2>Ratings</h2>
      {user ? (
        <form className="rating-form" onSubmit={submitRating}>
          <label>
            Score
            <select value={ratingScore} onChange={(e) => setRatingScore(Number(e.target.value))}>
              {[5, 4, 3, 2, 1].map((n) => (
                <option key={n} value={n}>
                  {n} star{n > 1 ? 's' : ''}
                </option>
              ))}
            </select>
          </label>
          <input
            placeholder="Optional comment"
            value={ratingContent}
            onChange={(e) => setRatingContent(e.target.value)}
          />
          <button className="btn-primary" type="submit">
            Submit rating
          </button>
        </form>
      ) : (
        <p className="muted">
          <Link to="/login">Sign in</Link> to rate this shop.
        </p>
      )}

      <div className="ratings-list">
        {ratings.map((r) => (
          <div key={r.id} className="rating-item">
            <span className="rating">
              {'★'.repeat(r.score)}
              {'☆'.repeat(5 - r.score)}
            </span>
            {r.content && <p>{r.content}</p>}
          </div>
        ))}
        {ratings.length === 0 && <p className="muted">No ratings yet.</p>}
      </div>
    </section>
  )
}

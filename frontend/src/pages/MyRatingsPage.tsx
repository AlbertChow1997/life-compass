import { useEffect, useState } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { ShopRating } from '../types'

/** Lists every shop review the signed-in user has written, and lets them delete their own reviews. */
export default function MyRatingsPage() {
  const [ratings, setRatings] = useState<ShopRating[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    try {
      const res = await api.get<ApiResult<ShopRating[]>>('/user/ratings')
      setRatings(res.data.data ?? [])
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not load your reviews.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  // Deletes a review after confirmation, then removes it from local state directly
  // (rather than re-fetching the whole list) so the UI updates instantly.
  async function deleteRating(id: number) {
    if (!window.confirm('Delete this review?')) return
    setError(null)
    try {
      await api.delete(`/user/ratings/${id}`)
      setRatings((prev) => prev.filter((r) => r.id !== id))
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not delete this review'))
    }
  }

  return (
    <section className="page">
      <div className="hero">
        <h1>My reviews</h1>
      </div>

      {loading && <p className="muted">Loading…</p>}
      {error && <div className="notice">{error}</div>}

      {!loading && !error && (
        <div className="ratings-list">
          {ratings.map((r) => (
            <div key={r.id} className="rating-item">
              <div className="card-meta">
                <span className="rating">
                  {'★'.repeat(r.score)}
                  {'☆'.repeat(5 - r.score)}
                </span>
                <span className="muted">{r.shopName}</span>
              </div>
              {r.content && <p>{r.content}</p>}
              <button className="link-button" type="button" onClick={() => deleteRating(r.id)}>
                Delete
              </button>
            </div>
          ))}
          {ratings.length === 0 && <p className="muted">You haven't reviewed any shops yet.</p>}
        </div>
      )}
    </section>
  )
}

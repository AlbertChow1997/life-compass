import { useEffect, useState } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { BlogComment } from '../types'

/** Read-only list of every comment the signed-in user has left on posts. */
export default function MyCommentsPage() {
  const [comments, setComments] = useState<BlogComment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<ApiResult<BlogComment[]>>('/user/comments')
      .then((res) => setComments(res.data.data ?? []))
      .catch((err) => setError(apiErrorMessage(err, 'Could not load your comments.')))
      .finally(() => setLoading(false))
  }, [])

  return (
    <section className="page">
      <div className="hero">
        <h1>My comments</h1>
      </div>

      {loading && <p className="muted">Loading…</p>}
      {error && <div className="notice">{error}</div>}

      {!loading && !error && (
        <div className="ratings-list">
          {comments.map((c) => (
            <div key={c.id} className="rating-item">
              <p>{c.content}</p>
            </div>
          ))}
          {comments.length === 0 && <p className="muted">You haven't commented on anything yet.</p>}
        </div>
      )}
    </section>
  )
}

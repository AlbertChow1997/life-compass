import { useEffect, useState, type FormEvent } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'
import type { BlogComment } from '../types'
import { Link } from 'react-router-dom'

/**
 * Inline comment thread for a single post (blog). Fetches comments for `blogId`
 * on mount/when the post changes, and lets signed-in users add a new comment;
 * signed-out users see a sign-in prompt instead of the comment form.
 */
export default function PostComments({ blogId }: { blogId: number }) {
  const { user } = useAuth()
  const [comments, setComments] = useState<BlogComment[]>([])
  const [loading, setLoading] = useState(true)
  const [content, setContent] = useState('')
  const [error, setError] = useState<string | null>(null)

  // Fetches the comment list for this post; also re-used after posting a new comment.
  async function load() {
    setLoading(true)
    try {
      const res = await api.get<ApiResult<BlogComment[]>>(`/blog/${blogId}/comments`)
      setComments(res.data.data ?? [])
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not load comments'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [blogId])

  // Posts the new comment, then reloads the thread so the new entry (with
  // server-assigned id/author) shows up immediately.
  async function submit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      await api.post(`/blog/${blogId}/comments`, { content })
      setContent('')
      load()
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not post comment'))
    }
  }

  return (
    <div className="comments">
      {loading && <p className="muted">Loading comments…</p>}
      {error && <div className="notice notice-error">{error}</div>}
      {!loading &&
        comments.map((c) => (
          <div key={c.id} className="comment-item">
            <span className="comment-author">{c.authorName ?? 'Anonymous'}</span>
            <p>{c.content}</p>
          </div>
        ))}
      {!loading && comments.length === 0 && <p className="muted">No comments yet.</p>}

      {user ? (
        <form className="comment-form" onSubmit={submit}>
          <input
            placeholder="Add a comment…"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />
          <button className="btn-ghost" type="submit">
            Post
          </button>
        </form>
      ) : (
        <p className="muted">
          <Link to="/login">Sign in</Link> to comment.
        </p>
      )}
    </div>
  )
}

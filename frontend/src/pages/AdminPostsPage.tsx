import { useEffect, useState } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { Blog } from '../types'

/** Requirement 8: admin moderation — feature or remove posts. */
export default function AdminPostsPage() {
  const [posts, setPosts] = useState<Blog[]>([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    try {
      const res = await api.get<ApiResult<Blog[]>>('/blog')
      setPosts(res.data.data ?? [])
    } catch (err) {
      setMessage(apiErrorMessage(err, 'Could not load posts.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function toggleFeatured(post: Blog) {
    setMessage(null)
    try {
      await api.put(`/admin/blog/${post.id}/feature?featured=${post.featured !== 1}`)
      load()
    } catch (err) {
      setMessage(apiErrorMessage(err, 'Could not update post'))
    }
  }

  async function deletePost(id: number) {
    if (!window.confirm('Delete this post?')) return
    setMessage(null)
    try {
      await api.delete(`/admin/blog/${id}`)
      load()
    } catch (err) {
      setMessage(apiErrorMessage(err, 'Could not delete post'))
    }
  }

  if (loading) {
    return (
      <section className="page">
        <p className="muted">Loading…</p>
      </section>
    )
  }

  return (
    <section className="page">
      <div className="hero">
        <h1>Moderation</h1>
        <p className="muted">Feature or remove community posts.</p>
      </div>

      {message && <div className="notice">{message}</div>}

      <div className="posts">
        {posts.map((post) => (
          <article key={post.id} className="post-card">
            <div className="post-head">
              <span className="post-author">{post.authorName ?? 'Anonymous'}</span>
              {post.featured === 1 && <span className="badge">Featured</span>}
            </div>
            <h3>{post.title}</h3>
            <p className="post-content">{post.content}</p>
            <div className="card-meta">
              <button className="btn-ghost" type="button" onClick={() => toggleFeatured(post)}>
                {post.featured === 1 ? 'Unfeature' : 'Feature'}
              </button>
              <button className="btn-ghost" type="button" onClick={() => deletePost(post.id)}>
                Delete
              </button>
            </div>
          </article>
        ))}
        {posts.length === 0 && <p className="muted">No posts to moderate.</p>}
      </div>
    </section>
  )
}

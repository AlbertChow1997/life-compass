import { useEffect, useState } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { Blog } from '../types'
import { firstImage } from '../format'

/** Read-only list of every post the signed-in user has authored. */
export default function MyPostsPage() {
  const [posts, setPosts] = useState<Blog[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<ApiResult<Blog[]>>('/user/posts')
      .then((res) => setPosts(res.data.data ?? []))
      .catch((err) => setError(apiErrorMessage(err, 'Could not load your posts.')))
      .finally(() => setLoading(false))
  }, [])

  return (
    <section className="page">
      <div className="hero">
        <h1>My posts</h1>
      </div>

      {loading && <p className="muted">Loading…</p>}
      {error && <div className="notice">{error}</div>}

      {!loading && !error && (
        <div className="posts">
          {posts.map((post) => (
            <article key={post.id} className="post-card">
              <div className="post-head">
                {post.featured === 1 && <span className="badge">Featured</span>}
              </div>
              <h3>{post.title}</h3>
              {firstImage(post.images) && <img className="post-image" src={firstImage(post.images)!} alt="" />}
              <p className="post-content">{post.content}</p>
              <div className="card-meta">
                <span className="muted">♥ {post.liked}</span>
                <span className="muted">{post.comments} comments</span>
              </div>
            </article>
          ))}
          {posts.length === 0 && <p className="muted">You haven't posted anything yet.</p>}
        </div>
      )}
    </section>
  )
}

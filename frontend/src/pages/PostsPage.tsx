import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { Blog, Shop } from '../types'
import { useAuth } from '../context/AuthContext'
import { firstImage } from '../format'
import PostComments from '../components/PostComments'
import Banner from '../components/Banner'

type FeedFilter = 'all' | 'following'

/** Community posts feed (requirement 4): user recommendations, optionally linking a shop and an image. */
export default function PostsPage() {
  const { user } = useAuth()
  const [posts, setPosts] = useState<Blog[]>([])
  const [shops, setShops] = useState<Shop[]>([])
  const [filter, setFilter] = useState<FeedFilter>('all')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [expanded, setExpanded] = useState<number | null>(null)

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [shopId, setShopId] = useState<string>('')
  const [imageUrl, setImageUrl] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const postsUrl = filter === 'following' ? '/blog?followedOnly=true' : '/blog'
      const [postsRes, shopsRes] = await Promise.all([
        api.get<ApiResult<Blog[]>>(postsUrl),
        api.get<ApiResult<Shop[]>>('/shop'),
      ])
      setPosts(postsRes.data.data ?? [])
      setShops(shopsRes.data.data ?? [])
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not load posts.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter])

  async function toggleLike(postId: number) {
    if (!user) return
    try {
      const res = await api.post<ApiResult<{ liked: number; likedByMe: boolean }>>(`/blog/${postId}/like`)
      const data = res.data.data
      if (data) {
        setPosts((prev) =>
          prev.map((p) => (p.id === postId ? { ...p, liked: data.liked, likedByCurrentUser: data.likedByMe } : p)),
        )
      }
    } catch {
      // A failed like toggle isn't worth an error banner; the count just won't change.
    }
  }

  async function toggleFollowAuthor(authorId: number) {
    if (!user) return
    const currentlyFollowed = posts.find((p) => p.userId === authorId)?.authorFollowedByCurrentUser ?? false
    try {
      if (currentlyFollowed) {
        await api.delete(`/user/${authorId}/follow`)
      } else {
        await api.post(`/user/${authorId}/follow`)
      }
      setPosts((prev) =>
        prev.map((p) => (p.userId === authorId ? { ...p, authorFollowedByCurrentUser: !currentlyFollowed } : p)),
      )
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not update follow status'))
    }
  }

  async function handleImageChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setFormError(null)
    setUploading(true)
    try {
      const body = new FormData()
      body.append('file', file)
      // No explicit Content-Type: the browser must set it itself so it can
      // append the multipart boundary — setting it manually breaks parsing.
      const res = await api.post<ApiResult<string>>('/upload', body)
      setImageUrl(res.data.data ?? null)
    } catch (err) {
      setFormError(apiErrorMessage(err, 'Could not upload image'))
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  async function submitPost(e: FormEvent) {
    e.preventDefault()
    setFormError(null)
    try {
      await api.post('/blog', {
        title,
        content,
        images: imageUrl ?? undefined,
        shopId: shopId ? Number(shopId) : undefined,
      })
      setTitle('')
      setContent('')
      setShopId('')
      setImageUrl(null)
      load()
    } catch (err) {
      setFormError(apiErrorMessage(err, 'Could not create post'))
    }
  }

  return (
    <section className="page">
      <Banner />
      <div className="hero">
        <h1>Community posts</h1>
        <p>Recommendations from locals across Ireland.</p>
      </div>

      {user && (
        <div className="account-toggle">
          <button
            className={filter === 'all' ? 'segment segment-active' : 'segment'}
            type="button"
            onClick={() => setFilter('all')}
          >
            All posts
          </button>
          <button
            className={filter === 'following' ? 'segment segment-active' : 'segment'}
            type="button"
            onClick={() => setFilter('following')}
          >
            Following
          </button>
        </div>
      )}

      {user ? (
        <form className="auth-form new-post-form" onSubmit={submitPost}>
          <label>
            Title
            <input value={title} onChange={(e) => setTitle(e.target.value)} required />
          </label>
          <label>
            What did you think?
            <textarea value={content} onChange={(e) => setContent(e.target.value)} required rows={3} />
          </label>
          <label>
            Link a shop (optional)
            <select value={shopId} onChange={(e) => setShopId(e.target.value)}>
              <option value="">— None —</option>
              {shops.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Photo (optional)
            <input type="file" accept="image/jpeg,image/png,image/webp,image/gif" onChange={handleImageChange} />
          </label>
          {uploading && <p className="muted">Uploading…</p>}
          {imageUrl && (
            <div className="image-preview">
              <img src={imageUrl} alt="Upload preview" />
              <button className="link-button" type="button" onClick={() => setImageUrl(null)}>
                Remove photo
              </button>
            </div>
          )}
          {formError && <div className="notice notice-error">{formError}</div>}
          <button className="btn-primary" type="submit" disabled={uploading}>
            Share
          </button>
        </form>
      ) : (
        <p className="muted">
          <Link to="/login">Sign in</Link> to share your own recommendation.
        </p>
      )}

      {loading && <p className="muted">Loading…</p>}
      {error && <div className="notice">{error}</div>}

      {!loading && !error && (
        <div className="posts">
          {posts.map((post) => (
            <article key={post.id} className="post-card">
              <div className="post-head">
                <span className="post-author">{post.authorName ?? 'Anonymous'}</span>
                {user && user.userId !== post.userId && (
                  <button
                    className={post.authorFollowedByCurrentUser ? 'link-button follow-text-active' : 'link-button'}
                    type="button"
                    onClick={() => toggleFollowAuthor(post.userId)}
                  >
                    {post.authorFollowedByCurrentUser ? '✓ Following' : '+ Follow'}
                  </button>
                )}
                {post.featured === 1 && <span className="badge">Featured</span>}
              </div>
              <h3>{post.title}</h3>
              {firstImage(post.images) && (
                <img className="post-image" src={firstImage(post.images)!} alt="" />
              )}
              <p className="post-content">{post.content}</p>
              <div className="card-meta">
                <button
                  className={post.likedByCurrentUser ? 'like-button like-button-active' : 'like-button'}
                  type="button"
                  onClick={() => toggleLike(post.id)}
                  disabled={!user}
                >
                  {post.likedByCurrentUser ? '♥' : '♡'} {post.liked}
                </button>
                <button
                  className="link-button"
                  type="button"
                  onClick={() => setExpanded(expanded === post.id ? null : post.id)}
                >
                  {post.comments} comments
                </button>
              </div>
              {expanded === post.id && <PostComments blogId={post.id} />}
            </article>
          ))}
          {posts.length === 0 && filter === 'following' && (
            <p className="muted">No posts from people you follow yet — follow someone from the "All posts" tab.</p>
          )}
          {posts.length === 0 && filter === 'all' && (
            <p className="muted">No posts yet — be the first to share a recommendation.</p>
          )}
        </div>
      )}
    </section>
  )
}

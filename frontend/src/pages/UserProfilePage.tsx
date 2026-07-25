import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { levelBadge } from '../format'
import type { Blog, UserProfile } from '../types'

/**
 * Public profile page for one user (route: /people/:id) — the "click into
 * someone from the People list" destination. Shows their basic info, bio,
 * level/PRO badges (same levelBadge() logic UserMenu uses for the current
 * user), a follow button, and every post they've published.
 */
export default function UserProfilePage() {
  const { id } = useParams<{ id: string }>()
  const { user } = useAuth()
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [posts, setPosts] = useState<Blog[]>([])
  const [followed, setFollowed] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const followRequest = user
          ? api.get<ApiResult<{ followed: boolean }>>(`/user/${id}/follow`)
          : Promise.resolve(null)
        const [profileRes, postsRes, followRes] = await Promise.all([
          api.get<ApiResult<UserProfile>>(`/users/${id}`),
          api.get<ApiResult<Blog[]>>('/blog', { params: { authorId: id } }),
          followRequest,
        ])
        if (cancelled) return
        setProfile(profileRes.data.data ?? null)
        setPosts(postsRes.data.data ?? [])
        setFollowed(followRes?.data.data?.followed ?? false)
      } catch (err) {
        if (!cancelled) setError(apiErrorMessage(err, 'Could not load this profile'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [id, user])

  async function toggleFollow() {
    if (!user || !id) return
    try {
      if (followed) {
        await api.delete(`/user/${id}/follow`)
      } else {
        await api.post(`/user/${id}/follow`)
      }
      setFollowed((f) => !f)
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not update follow status'))
    }
  }

  if (loading) {
    return (
      <section className="page">
        <p className="muted">Loading…</p>
      </section>
    )
  }

  if (error || !profile) {
    return (
      <section className="page">
        <div className="notice notice-error">{error ?? 'This user could not be found.'}</div>
      </section>
    )
  }

  const level = levelBadge(profile.experience, profile.proThreshold)
  const isPro = profile.experience >= profile.proThreshold
  const progressPct = Math.min(100, (profile.experience / profile.proThreshold) * 100)
  const isSelf = user && user.userId === profile.id

  return (
    <section className="page">
      <div className="profile-header">
        {profile.icon ? (
          <img className="avatar avatar-large" src={profile.icon} alt="" />
        ) : (
          <span className="avatar avatar-fallback avatar-large">{profile.nickName.charAt(0).toUpperCase()}</span>
        )}
        <div className="profile-header-body">
          <h1>{profile.nickName}</h1>
          {profile.city && <p className="muted">{profile.city}</p>}
          {profile.bio && <p className="profile-bio">{profile.bio}</p>}
          <div className="profile-stats-row">
            <div className="profile-stat">
              <strong>{profile.following}</strong>
              <span>Following</span>
            </div>
            <div className="profile-stat">
              <strong>{profile.followers}</strong>
              <span>Followers</span>
            </div>
          </div>
          <div className="xp-row">
            <div className="xp-bar">
              <div className="xp-bar-fill" style={{ width: `${progressPct}%` }} />
            </div>
            <span className={level.isMax ? 'level-badge level-badge-max' : 'level-badge'}>{level.label}</span>
            <span className={isPro ? 'pro-badge pro-badge-active' : 'pro-badge'}>PRO</span>
          </div>
          {!isSelf &&
            (user ? (
              <button
                className={followed ? 'btn-ghost follow-text-active' : 'btn-ghost'}
                type="button"
                onClick={toggleFollow}
              >
                {followed ? '✓ Following' : '+ Follow'}
              </button>
            ) : (
              <a className="btn-ghost" href="/login">
                Sign in to follow
              </a>
            ))}
        </div>
      </div>

      <h2 className="profile-posts-heading">Posts</h2>
      {posts.length === 0 && <p className="muted">No posts yet.</p>}
      <div className="posts">
        {posts.map((post) => (
          <article key={post.id} className="post-card">
            <h3>{post.title}</h3>
            <p className="post-content">{post.content}</p>
            <div className="card-meta">
              <span className="muted">{post.liked} likes</span>
              <span className="muted">{post.comments} comments</span>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

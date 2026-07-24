import { useEffect, useState } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'
import type { UserSummary } from '../types'

/**
 * Public "browse people" directory: search by nickname and follow/unfollow
 * directly from the list, using the same /api/user/{id}/follow endpoints and
 * follow-button styling as the post feed's inline "Follow author" button.
 */
export default function PeoplePage() {
  const { user } = useAuth()
  const [people, setPeople] = useState<UserSummary[]>([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    const timer = setTimeout(async () => {
      setLoading(true)
      setError(null)
      try {
        const res = await api.get<ApiResult<UserSummary[]>>('/users', { params: { name: query || undefined } })
        if (!cancelled) setPeople(res.data.data ?? [])
      } catch (err) {
        if (!cancelled) setError(apiErrorMessage(err, 'Could not load people'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }, 250) // debounce so every keystroke doesn't fire a request
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [query])

  async function toggleFollow(person: UserSummary) {
    if (!user) return
    try {
      if (person.followedByCurrentUser) {
        await api.delete(`/user/${person.id}/follow`)
      } else {
        await api.post(`/user/${person.id}/follow`)
      }
      setPeople((prev) =>
        prev.map((p) => (p.id === person.id ? { ...p, followedByCurrentUser: !p.followedByCurrentUser } : p)),
      )
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not update follow status'))
    }
  }

  return (
    <section className="page">
      <div className="hero">
        <h1>Find people to follow</h1>
        <p>Follow other LifeCompass users to see their posts in your "Following" feed.</p>
        <input
          className="search"
          placeholder="Search by nickname…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      {loading && <p className="muted">Loading…</p>}
      {error && <div className="notice notice-error">{error}</div>}

      {!loading && !error && (
        <div className="grid">
          {people.map((p) => (
            <div key={p.id} className="card">
              <div className="card-row">
                {p.icon ? (
                  <img className="avatar" src={p.icon} alt="" />
                ) : (
                  <span className="avatar avatar-fallback">{p.nickName.charAt(0).toUpperCase()}</span>
                )}
                <div className="card-body">
                  <h3>{p.nickName}</h3>
                  {p.city && <p className="muted">{p.city}</p>}
                </div>
                {user ? (
                  <button
                    className={p.followedByCurrentUser ? 'link-button follow-text-active' : 'link-button'}
                    type="button"
                    onClick={() => toggleFollow(p)}
                  >
                    {p.followedByCurrentUser ? '✓ Following' : '+ Follow'}
                  </button>
                ) : (
                  <a className="link-button" href="/login">
                    Sign in to follow
                  </a>
                )}
              </div>
            </div>
          ))}
          {people.length === 0 && <p className="muted">No one matches your search.</p>}
        </div>
      )}
    </section>
  )
}

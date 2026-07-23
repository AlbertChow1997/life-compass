import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'
import type { UserStats } from '../types'

/**
 * Avatar + dropdown in the navbar. Regular users get the full personal
 * center (follow/follower counts, XP bar, PRO badge, my-content links);
 * merchants/admins get a plain sign-out menu — they already have their own
 * role-specific nav links (My Vouchers / Moderation / Support).
 */
export default function UserMenu() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [stats, setStats] = useState<UserStats | null>(null)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open || user?.role !== 'USER') return
    api
      .get<ApiResult<UserStats>>('/user/stats')
      .then((res) => setStats(res.data.data ?? null))
      .catch(() => setStats(null))
  }, [open, user?.role])

  useEffect(() => {
    if (!open) return
    function handleClick(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [open])

  if (!user) return null

  function handleSignOut() {
    setOpen(false)
    logout()
    navigate('/')
  }

  function go(path: string) {
    setOpen(false)
    navigate(path)
  }

  const isPro = stats != null && stats.experience >= stats.proThreshold
  const progressPct = stats ? Math.min(100, (stats.experience / stats.proThreshold) * 100) : 0

  return (
    <div className="user-menu" ref={menuRef}>
      <button className="user-trigger" type="button" onClick={() => setOpen((v) => !v)}>
        {user.icon ? (
          <img className="avatar" src={user.icon} alt="" />
        ) : (
          <span className="avatar avatar-fallback">{user.nickName.charAt(0).toUpperCase()}</span>
        )}
        <span className="nav-user">{user.nickName}</span>
      </button>

      {open && (
        <div className="user-dropdown">
          {user.role === 'USER' && (
            <>
              <div className="profile-stats">
                <div className="profile-stats-row">
                  <div className="profile-stat">
                    <strong>{stats?.following ?? 0}</strong>
                    <span>Following</span>
                  </div>
                  <div className="profile-stat">
                    <strong>{stats?.followers ?? 0}</strong>
                    <span>Followers</span>
                  </div>
                </div>
                <div className="xp-row">
                  <div className="xp-bar">
                    <div className="xp-bar-fill" style={{ width: `${progressPct}%` }} />
                  </div>
                  <span className={isPro ? 'pro-badge pro-badge-active' : 'pro-badge'}>PRO</span>
                </div>
                <p className="muted xp-label">{stats?.experience ?? 0} XP</p>
              </div>

              <div className="user-dropdown-links">
                <button type="button" onClick={() => go('/profile')}>
                  Profile
                </button>
                <button type="button" onClick={() => go('/profile/shops')}>
                  Followed shops
                </button>
                <button type="button" onClick={() => go('/profile/comments')}>
                  My comments
                </button>
                <button type="button" onClick={() => go('/profile/posts')}>
                  My posts
                </button>
                <button type="button" onClick={() => go('/profile/likes')}>
                  Liked posts
                </button>
                <button type="button" onClick={() => go('/profile/orders')}>
                  My orders
                </button>
              </div>
            </>
          )}

          <button className="user-dropdown-signout" type="button" onClick={handleSignOut}>
            Sign out
          </button>
        </div>
      )}
    </div>
  )
}

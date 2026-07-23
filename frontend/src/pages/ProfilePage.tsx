import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'

interface MeResponse {
  id: number
  nickName: string
  icon: string
  city?: string
  role: string
}

/** Edit basic profile info and avatar. */
export default function ProfilePage() {
  const { refresh } = useAuth()
  const [nickName, setNickName] = useState('')
  const [city, setCity] = useState('')
  const [icon, setIcon] = useState('')
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<ApiResult<MeResponse>>('/auth/me')
      .then((res) => {
        const data = res.data.data
        if (data) {
          setNickName(data.nickName)
          setCity(data.city ?? '')
          setIcon(data.icon)
        }
      })
      .catch(() => setError('Could not load your profile.'))
      .finally(() => setLoading(false))
  }, [])

  async function handleAvatarChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setError(null)
    setUploading(true)
    try {
      const body = new FormData()
      body.append('file', file)
      const res = await api.post<ApiResult<string>>('/upload', body)
      if (res.data.data) setIcon(res.data.data)
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not upload avatar'))
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setMessage(null)
    setSaving(true)
    try {
      await api.put('/user/profile', { nickName, city, icon })
      await refresh()
      setMessage('Profile updated.')
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not save your profile'))
    } finally {
      setSaving(false)
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
    <section className="page auth-page">
      <div className="hero">
        <h1>Your profile</h1>
      </div>

      {error && <div className="notice notice-error">{error}</div>}
      {message && <div className="notice">{message}</div>}

      <form className="auth-form" onSubmit={submit}>
        <div className="image-preview">
          {icon ? (
            <img className="avatar avatar-large" src={icon} alt="Avatar" />
          ) : (
            <span className="avatar avatar-fallback avatar-large">{nickName.charAt(0).toUpperCase()}</span>
          )}
          <label className="btn-ghost">
            {uploading ? 'Uploading…' : 'Change photo'}
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              onChange={handleAvatarChange}
              hidden
            />
          </label>
        </div>

        <label>
          Nickname
          <input value={nickName} onChange={(e) => setNickName(e.target.value)} required />
        </label>
        <label>
          City
          <input value={city} onChange={(e) => setCity(e.target.value)} placeholder="e.g. Dublin" />
        </label>

        <button className="btn-primary" type="submit" disabled={saving || uploading}>
          {saving ? 'Saving…' : 'Save changes'}
        </button>
      </form>
    </section>
  )
}

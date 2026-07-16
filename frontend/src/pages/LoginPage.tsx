import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'
import type { LoginResponse } from '../types'

type Tab = 'credentials' | 'phone'

export default function LoginPage() {
  const [tab, setTab] = useState<Tab>('credentials')
  const { login } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [sending, setSending] = useState(false)

  function handleSuccess(res: LoginResponse) {
    login(res.token, { userId: res.userId, nickName: res.nickName, role: res.role })
    navigate('/')
  }

  async function submitCredentials(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      const res = await api.post<ApiResult<LoginResponse>>('/auth/login', { email, password })
      if (res.data.data) handleSuccess(res.data.data)
    } catch (err) {
      setError(apiErrorMessage(err, 'Login failed'))
    }
  }

  async function sendCode(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSending(true)
    try {
      await api.post('/auth/sms/code', { phone })
      setCodeSent(true)
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not send code'))
    } finally {
      setSending(false)
    }
  }

  async function submitPhoneLogin(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      const res = await api.post<ApiResult<LoginResponse>>('/auth/sms/login', { phone, code })
      if (res.data.data) handleSuccess(res.data.data)
    } catch (err) {
      setError(apiErrorMessage(err, 'Login failed'))
    }
  }

  function switchTab(next: Tab) {
    setTab(next)
    setError(null)
  }

  return (
    <section className="page auth-page">
      <div className="hero">
        <h1>Sign in</h1>
      </div>

      <div className="tabs">
        <button
          className={tab === 'credentials' ? 'tab tab-active' : 'tab'}
          type="button"
          onClick={() => switchTab('credentials')}
        >
          Merchant / Admin
        </button>
        <button
          className={tab === 'phone' ? 'tab tab-active' : 'tab'}
          type="button"
          onClick={() => switchTab('phone')}
        >
          Phone (SMS)
        </button>
      </div>

      {error && <div className="notice notice-error">{error}</div>}

      {tab === 'credentials' && (
        <form className="auth-form" onSubmit={submitCredentials}>
          <label>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>
            Password
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>
          <button className="btn-primary" type="submit">
            Sign in
          </button>
          <p className="muted">
            Try <code>admin@lifecompass.ie</code> / <code>password</code> (admin) or{' '}
            <code>olivia@templebar.ie</code> / <code>password</code> (merchant).
          </p>
        </form>
      )}

      {tab === 'phone' && !codeSent && (
        <form className="auth-form" onSubmit={sendCode}>
          <label>
            Phone number
            <input
              type="tel"
              placeholder="+353851234567"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              required
            />
          </label>
          <button className="btn-primary" type="submit" disabled={sending}>
            {sending ? 'Sending…' : 'Send code'}
          </button>
          <p className="muted">
            SMS isn't configured in this dev environment — the code is printed to the backend
            console log instead of being texted.
          </p>
        </form>
      )}

      {tab === 'phone' && codeSent && (
        <form className="auth-form" onSubmit={submitPhoneLogin}>
          <p className="muted">Code sent to {phone}. Check the backend terminal log for it (dev mode).</p>
          <label>
            Verification code
            <input inputMode="numeric" maxLength={6} value={code} onChange={(e) => setCode(e.target.value)} required />
          </label>
          <button className="btn-primary" type="submit">
            Verify &amp; sign in
          </button>
        </form>
      )}
    </section>
  )
}

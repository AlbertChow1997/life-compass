import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useGoogleIdentityScript } from '../hooks/useGoogleIdentityScript'
import type { LoginResponse } from '../types'

type Tab = 'google' | 'credentials' | 'phone'

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID

export default function LoginPage() {
  const [tab, setTab] = useState<Tab>('google')
  const { login } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [sending, setSending] = useState(false)

  const googleScriptReady = useGoogleIdentityScript()
  const googleButtonRef = useRef<HTMLDivElement>(null)

  function handleSuccess(res: LoginResponse) {
    login(res.token, { userId: res.userId, nickName: res.nickName, role: res.role })
    navigate('/')
  }

  async function handleGoogleCredential(response: { credential: string }) {
    setError(null)
    try {
      const res = await api.post<ApiResult<LoginResponse>>('/auth/google', { idToken: response.credential })
      if (res.data.data) handleSuccess(res.data.data)
    } catch (err) {
      setError(apiErrorMessage(err, 'Google sign-in failed'))
    }
  }

  // Renders Google's own button into googleButtonRef once its script has loaded
  // and we're on the Google tab. Re-runs if the user switches tabs away and back.
  useEffect(() => {
    if (tab !== 'google' || !googleScriptReady || !googleButtonRef.current || !GOOGLE_CLIENT_ID) return
    window.google!.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: handleGoogleCredential,
    })
    window.google!.accounts.id.renderButton(googleButtonRef.current, {
      theme: 'outline',
      size: 'large',
      width: 320,
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab, googleScriptReady])

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
          className={tab === 'google' ? 'tab tab-active' : 'tab'}
          type="button"
          onClick={() => switchTab('google')}
        >
          Google
        </button>
        <button
          className={tab === 'phone' ? 'tab tab-active' : 'tab'}
          type="button"
          onClick={() => switchTab('phone')}
        >
          Phone (SMS)
        </button>
        <button
          className={tab === 'credentials' ? 'tab tab-active' : 'tab'}
          type="button"
          onClick={() => switchTab('credentials')}
        >
          Merchant / Admin
        </button>
      </div>

      {error && <div className="notice notice-error">{error}</div>}

      {tab === 'google' && (
        <div className="google-tab">
          {!GOOGLE_CLIENT_ID && (
            <p className="muted">Google sign-in isn't configured in this environment yet.</p>
          )}
          {GOOGLE_CLIENT_ID && !googleScriptReady && <p className="muted">Loading Google Sign-In…</p>}
          <div ref={googleButtonRef} />
        </div>
      )}

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

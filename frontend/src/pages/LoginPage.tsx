import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useGoogleIdentityScript } from '../hooks/useGoogleIdentityScript'
import type { LoginResponse } from '../types'

type AccountType = 'user' | 'business'
type PhoneStep = 'closed' | 'phone' | 'code'

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID

/**
 * Sign-in page offering three independent auth methods: email/password (for
 * both regular users and merchant/admin accounts), Google OAuth, and phone/SMS
 * one-time-code. Whichever method succeeds calls `login()` from AuthContext
 * and redirects home.
 */
export default function LoginPage() {
  const [accountType, setAccountType] = useState<AccountType>('user')
  const { login } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const [phoneStep, setPhoneStep] = useState<PhoneStep>('closed')
  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [sending, setSending] = useState(false)

  const googleScriptReady = useGoogleIdentityScript()
  const googleButtonRef = useRef<HTMLDivElement>(null)

  const [smsConfigured, setSmsConfigured] = useState<boolean | null>(null)
  useEffect(() => {
    api
      .get<ApiResult<{ smsConfigured: boolean }>>('/auth/config')
      .then((res) => setSmsConfigured(res.data.data?.smsConfigured ?? false))
      .catch(() => setSmsConfigured(false))
  }, [])

  // Common finish for every login method: store the token, load the profile, go home.
  async function handleSuccess(res: LoginResponse) {
    await login(res.token)
    navigate('/')
  }

  // Called by Google's script with the ID token once the user approves the Google sign-in prompt.
  async function handleGoogleCredential(response: { credential: string }) {
    setError(null)
    try {
      const res = await api.post<ApiResult<LoginResponse>>('/auth/google', { idToken: response.credential })
      if (res.data.data) handleSuccess(res.data.data)
    } catch (err) {
      setError(apiErrorMessage(err, 'Google sign-in failed'))
    }
  }

  // Renders Google's own button into googleButtonRef once its script has loaded.
  // Re-runs whenever the button becomes visible again (account type switch).
  useEffect(() => {
    if (!googleScriptReady || !googleButtonRef.current || !GOOGLE_CLIENT_ID) return
    window.google!.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: handleGoogleCredential,
    })
    window.google!.accounts.id.renderButton(googleButtonRef.current, {
      theme: 'outline',
      size: 'large',
      // Match whatever width the surrounding column actually renders at
      // (Google's API wants a concrete pixel number, not a percentage),
      // capped at Google's documented max of 400.
      width: Math.min(googleButtonRef.current.offsetWidth, 400),
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accountType, googleScriptReady])

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

  // Requests an SMS one-time code for the entered phone number, then advances to the code-entry step.
  async function sendCode(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSending(true)
    try {
      await api.post('/auth/sms/code', { phone })
      setPhoneStep('code')
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

  function switchAccountType(next: AccountType) {
    setAccountType(next)
    setError(null)
    setPhoneStep('closed')
  }

  return (
    <section className="page auth-page">
      <div className="hero">
        <h1>Sign in</h1>
      </div>

      <div className="account-toggle">
        <button
          className={accountType === 'user' ? 'segment segment-active' : 'segment'}
          type="button"
          onClick={() => switchAccountType('user')}
        >
          User
        </button>
        <button
          className={accountType === 'business' ? 'segment segment-active' : 'segment'}
          type="button"
          onClick={() => switchAccountType('business')}
        >
          Merchant / Admin
        </button>
      </div>

      {error && <div className="notice notice-error">{error}</div>}

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
        {accountType === 'business' ? (
          <p className="muted">
            Try <code>admin@lifecompass.ie</code> / <code>password</code> (admin) or{' '}
            <code>olivia@templebar.ie</code> / <code>password</code> (merchant).
          </p>
        ) : (
          <p className="muted">
            No account yet? Use Google or phone below, or <Link to="/register">register</Link> with a
            password.
          </p>
        )}
      </form>

      <div className="divider-or">
        <span>or</span>
      </div>

      <div className="alt-login">
        <div className="google-tab">
          {!GOOGLE_CLIENT_ID && (
            <p className="muted">Google sign-in isn't configured in this environment yet.</p>
          )}
          {GOOGLE_CLIENT_ID && !googleScriptReady && <p className="muted">Loading Google Sign-In…</p>}
          <div ref={googleButtonRef} />
        </div>

        {phoneStep === 'closed' && (
          <button className="btn-ghost" type="button" onClick={() => setPhoneStep('phone')}>
            Sign in with Phone (SMS)
          </button>
        )}

        {phoneStep === 'phone' && (
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
              {smsConfigured
                ? "We'll text a 6-digit code to this number."
                : "SMS isn't configured in this dev environment — the code is printed to the backend " +
                  'console log instead of being texted.'}
            </p>
            <button className="link-button" type="button" onClick={() => setPhoneStep('closed')}>
              ← Back
            </button>
          </form>
        )}

        {phoneStep === 'code' && (
          <form className="auth-form" onSubmit={submitPhoneLogin}>
            <p className="muted">
              {smsConfigured
                ? `Code sent to ${phone}.`
                : `Code sent to ${phone}. Check the backend terminal log for it (dev mode).`}
            </p>
            <label>
              Verification code
              <input
                inputMode="numeric"
                maxLength={6}
                value={code}
                onChange={(e) => setCode(e.target.value)}
                required
              />
            </label>
            <button className="btn-primary" type="submit">
              Verify &amp; sign in
            </button>
            <button className="link-button" type="button" onClick={() => setPhoneStep('phone')}>
              ← Back
            </button>
          </form>
        )}
      </div>

      <p className="muted register-footer">
        Don't have an account? <Link to="/register">Register</Link>
      </p>
    </section>
  )
}

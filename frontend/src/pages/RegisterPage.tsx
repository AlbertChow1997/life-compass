import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import { useAuth } from '../context/AuthContext'
import type { LoginResponse } from '../types'

type RoleChoice = 'USER' | 'MERCHANT'

/**
 * Sign-up page for creating a new account with email/password. Lets the
 * visitor pick whether they're registering as a regular user or a merchant;
 * admin accounts can't self-register and are provisioned separately.
 */
export default function RegisterPage() {
  const { login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [nickName, setNickName] = useState('')
  const [city, setCity] = useState('')
  const [role, setRole] = useState<RoleChoice>('USER')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  // Validates the password confirmation locally before hitting the backend,
  // then registers and immediately signs the new account in.
  async function submit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }
    setSubmitting(true)
    try {
      const res = await api.post<ApiResult<LoginResponse>>('/auth/register', {
        email,
        password,
        nickName,
        city,
        role,
      })
      if (res.data.data) {
        await login(res.data.data.token)
        navigate('/')
      }
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not create your account'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="page auth-page">
      <div className="hero">
        <h1>Create an account</h1>
      </div>

      {error && <div className="notice notice-error">{error}</div>}

      <form className="auth-form" onSubmit={submit}>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>
        <label>
          Confirm password
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>
        <label>
          Nickname
          <input value={nickName} onChange={(e) => setNickName(e.target.value)} required />
        </label>
        <label>
          City
          <input value={city} onChange={(e) => setCity(e.target.value)} placeholder="e.g. Dublin" />
        </label>
        <label>
          Account type
          <select value={role} onChange={(e) => setRole(e.target.value as RoleChoice)}>
            <option value="USER">Regular user</option>
            <option value="MERCHANT">Merchant</option>
          </select>
        </label>
        <button className="btn-primary" type="submit" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className="muted">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </section>
  )
}

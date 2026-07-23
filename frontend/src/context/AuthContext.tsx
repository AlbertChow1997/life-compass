import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { api, type ApiResult } from '../api/client'
import type { Role } from '../types'

/**
 * App-wide authentication context: tracks the current logged-in user, exposes
 * login/logout/refresh actions, and re-validates any stored JWT against the
 * backend on first load. Wrap the app in `AuthProvider` and read state via `useAuth()`.
 */
export interface AuthUser {
  userId: number
  nickName: string
  role: Role
  /** Avatar URL; empty when the account has none (Navbar falls back to an initial). */
  icon: string
}

interface AuthContextValue {
  user: AuthUser | null
  /** True until the initial session check (if a token is stored) resolves. */
  loading: boolean
  login: (token: string) => Promise<void>
  logout: () => void
  /** Re-fetches the profile without touching the token — call after editing it. */
  refresh: () => Promise<void>
}

interface MeResponse {
  id: number
  nickName: string
  icon: string
  role: Role
}

const AuthContext = createContext<AuthContextValue | null>(null)

// Fetches the current user's profile using the stored JWT; returns null on any
// failure (expired/invalid token, network error) so callers can treat it as "not logged in".
async function fetchProfile(): Promise<AuthUser | null> {
  try {
    const res = await api.get<ApiResult<MeResponse>>('/auth/me')
    const data = res.data.data
    if (!data) return null
    return { userId: data.id, nickName: data.nickName, role: data.role, icon: data.icon }
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)

  // On app load, a stored token only proves who the user *was* — re-validate
  // against the server (also picks up profile changes like a new avatar).
  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      setLoading(false)
      return
    }
    fetchProfile().then((profile) => {
      if (profile) {
        setUser(profile)
      } else {
        localStorage.removeItem('token')
      }
      setLoading(false)
    })
  }, [])

  async function login(token: string) {
    localStorage.setItem('token', token)
    const profile = await fetchProfile()
    setUser(profile)
  }

  function logout() {
    localStorage.removeItem('token')
    setUser(null)
  }

  async function refresh() {
    const profile = await fetchProfile()
    setUser(profile)
  }

  return <AuthContext.Provider value={{ user, loading, login, logout, refresh }}>{children}</AuthContext.Provider>
}

// Convenience hook for consuming AuthContext; throws if used outside AuthProvider
// so misuse fails loudly during development rather than silently returning undefined.
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

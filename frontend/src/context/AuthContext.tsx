import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { api, type ApiResult } from '../api/client'
import type { Role } from '../types'

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
}

interface MeResponse {
  id: number
  nickName: string
  icon: string
  role: Role
}

const AuthContext = createContext<AuthContextValue | null>(null)

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

  return <AuthContext.Provider value={{ user, loading, login, logout }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

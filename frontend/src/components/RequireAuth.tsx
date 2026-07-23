import { Navigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '../context/AuthContext'

/** Client-side gate: redirects to /login if signed out. Any role is fine — the backend enforces this too. */
export default function RequireAuth({ children }: { children: ReactNode }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}

import { Navigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '../context/AuthContext'

/**
 * Route guard for pages that just need "some" logged-in user (any role).
 * Redirects to /login when signed out. This is a UX convenience only —
 * the backend independently re-checks auth on every request.
 */
export default function RequireAuth({ children }: { children: ReactNode }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}

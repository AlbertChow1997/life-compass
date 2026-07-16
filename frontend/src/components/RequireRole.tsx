import { Navigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '../context/AuthContext'
import type { Role } from '../types'

/** Client-side gate: redirects to /login if signed out, shows a notice if wrong role. The backend enforces this too. */
export default function RequireRole({ role, children }: { role: Role; children: ReactNode }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }
  if (user.role !== role) {
    return (
      <section className="page">
        <div className="notice">You don't have permission to view this page.</div>
      </section>
    )
  }
  return <>{children}</>
}

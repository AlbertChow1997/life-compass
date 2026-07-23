import { Navigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '../context/AuthContext'
import type { Role } from '../types'

/**
 * Route guard for pages restricted to a specific role (e.g. MERCHANT or ADMIN
 * dashboards). Redirects to /login when signed out, or shows a permission
 * notice when signed in as the wrong role. This is a UX convenience only —
 * the backend independently re-checks the role on every request.
 */
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

import React from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function dashboardPath(role) {
  return role ? `/${role.toLowerCase()}` : '/login'
}

export default function ProtectedRoute({ role }) {
  const { user, ready } = useAuth()
  if (!ready) return <div className="page-loading" aria-live="polite">Checking your session…</div>
  if (!user) return <Navigate to="/login" replace />
  if (role && user.role !== role) return <Navigate to="/unauthorized" replace />
  return <Outlet />
}

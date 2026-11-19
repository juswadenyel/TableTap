import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { dashboardPath } from '../components/ProtectedRoute'

export default function UnauthorizedPage() {
  const { user } = useAuth()
  return (
    <main className="message-page">
      <span className="eyebrow">403 · Access restricted</span>
      <h1>This page isn’t on your menu.</h1>
      <p>Your account doesn’t have access to this area.</p>
      <Link className="button button-primary" to={dashboardPath(user?.role)}>Back to your dashboard</Link>
    </main>
  )
}

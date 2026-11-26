import React from 'react'
import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { dashboardPath } from '../components/ProtectedRoute'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const { login, user, ready } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  if (!ready) return <div className="page-loading">Checking your session…</div>
  if (user) return <Navigate to={dashboardPath(user.role)} replace />

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setBusy(true)
    try {
      const account = await login(form)
      navigate(dashboardPath(account.role), { replace: true })
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Invalid email or password')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to continue to your TableTap account."
      footer={<>New to TableTap? <Link to="/register">Create an account</Link></>}>
      <form className="form" onSubmit={handleSubmit}>
        {location.state?.message && <p className="success-alert" role="status">{location.state.message}</p>}
        {error && <p className="form-alert" role="alert">{error}</p>}
        <label>Email address<input type="email" autoComplete="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} required /></label>
        <label>Password<input type="password" autoComplete="current-password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required /></label>
        <div className="forgot-link"><Link to="/forgot-password">Forgot password?</Link></div>
        <button className="button button-primary button-wide" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
      </form>
    </AuthLayout>
  )
}

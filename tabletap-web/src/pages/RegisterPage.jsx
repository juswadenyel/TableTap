import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { dashboardPath } from '../components/ProtectedRoute'
import { useAuth } from '../context/AuthContext'

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phonePattern = /^[0-9+ ]{7,15}$/

export default function RegisterPage() {
  const { register, user, ready } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '', confirmPassword: '' })
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  if (!ready) return <div className="page-loading">Checking your session…</div>
  if (user) return <Navigate to={dashboardPath(user.role)} replace />

  function validate() {
    const next = {}
    if (!form.fullName.trim()) next.fullName = 'Full name is required'
    if (!form.email.trim()) next.email = 'Email is required'
    else if (!emailPattern.test(form.email.trim())) next.email = 'Email must be valid'
    if (form.phone && !phonePattern.test(form.phone)) next.phone = 'Phone must contain 7 to 15 digits, plus signs, or spaces'
    if (!form.password) next.password = 'Password is required'
    else if (form.password.length < 8) next.password = 'Password must be at least 8 characters'
    if (form.confirmPassword !== form.password) next.confirmPassword = 'Passwords do not match'
    return next
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    const validationErrors = validate()
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length) return
    setBusy(true)
    try {
      const account = await register({
        fullName: form.fullName.trim(), email: form.email.trim(),
        phone: form.phone, password: form.password,
      })
      navigate(dashboardPath(account.role), { replace: true })
    } catch (requestError) {
      const data = requestError.response?.data
      setErrors(data?.errors || {})
      setMessage(data?.message || 'We could not create your account. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  function change(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: undefined }))
    setMessage('')
  }

  return (
    <AuthLayout title="Join the table" subtitle="Create your account and get started."
      footer={<>Already have an account? <Link to="/login">Sign in</Link></>}>
      <form className="form" onSubmit={handleSubmit} noValidate>
        {message && <p className="form-alert" role="alert">{message}</p>}
        <label>Full name<input autoComplete="name" value={form.fullName} onChange={(event) => change('fullName', event.target.value)} aria-invalid={!!errors.fullName} />{errors.fullName && <small className="field-error">{errors.fullName}</small>}</label>
        <label>Email address<input type="email" autoComplete="email" value={form.email} onChange={(event) => change('email', event.target.value)} aria-invalid={!!errors.email} />{errors.email && <small className="field-error">{errors.email}</small>}</label>
        <label>Phone <span className="optional">Optional</span><input type="tel" autoComplete="tel" value={form.phone} onChange={(event) => change('phone', event.target.value)} aria-invalid={!!errors.phone} />{errors.phone && <small className="field-error">{errors.phone}</small>}</label>
        <label>Password<input type="password" autoComplete="new-password" value={form.password} onChange={(event) => change('password', event.target.value)} aria-invalid={!!errors.password} />{errors.password && <small className="field-error">{errors.password}</small>}</label>
        <label>Confirm password<input type="password" autoComplete="new-password" value={form.confirmPassword} onChange={(event) => change('confirmPassword', event.target.value)} aria-invalid={!!errors.confirmPassword} />{errors.confirmPassword && <small className="field-error">{errors.confirmPassword}</small>}</label>
        <button className="button button-primary button-wide" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
      </form>
    </AuthLayout>
  )
}

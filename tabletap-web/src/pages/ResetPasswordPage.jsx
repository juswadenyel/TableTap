import React, { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import client from '../api/client'

// This page validates a reset token and submits a replacement password.
export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const [validating, setValidating] = useState(true)
  const [valid, setValid] = useState(false)
  const [error, setError] = useState('')
  const [errors, setErrors] = useState({})
  const [busy, setBusy] = useState(false)
  const [show, setShow] = useState(false)
  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' })

  useEffect(() => {
    let active = true
    if (!token) { setError('This reset link is missing its token.'); setValidating(false); return () => { active = false } }
    client.get('/api/auth/reset-password/validate', { params: { token } }).then(() => { if (active) setValid(true) })
      .catch((requestError) => { if (active) setError(requestError.response?.data?.message || 'This reset link is invalid or has expired.') })
      .finally(() => { if (active) setValidating(false) })
    return () => { active = false }
  }, [token])

  async function submit(event) {
    event.preventDefault(); setError('')
    const next = {}
    if (form.newPassword.length < 8) next.newPassword = 'Password must be at least 8 characters'
    if (form.confirmPassword !== form.newPassword) next.confirmPassword = 'Passwords do not match'
    setErrors(next)
    if (Object.keys(next).length) return
    setBusy(true)
    try {
      await client.post('/api/auth/reset-password', { token, ...form })
      navigate('/login', { replace: true, state: { message: 'Password reset successfully. Please sign in.' } })
    } catch (requestError) {
      const data = requestError.response?.data
      setErrors(data?.fieldErrors || data?.errors || {})
      setError(data?.message || 'This reset link is invalid or has expired.')
    } finally { setBusy(false) }
  }

  if (validating) return <div className="page-loading">Checking reset link…</div>
  return <AuthLayout title="Set a new password" subtitle="Choose a password with at least 8 characters."
    footer={<>Return to <Link to="/login">sign in</Link></>}>
    {!valid ? <p className="form-alert" role="alert">{error}</p> : <form className="form" onSubmit={submit} noValidate>
      {error && <p className="form-alert" role="alert">{error}</p>}
      <label>New password<div className="password-input"><input type={show ? 'text' : 'password'} autoComplete="new-password" value={form.newPassword} onChange={(event) => { setForm({ ...form, newPassword: event.target.value }); setErrors({ ...errors, newPassword: undefined }) }} aria-invalid={!!errors.newPassword} /><button type="button" className="show-password" onClick={() => setShow(!show)}>{show ? 'Hide' : 'Show'}</button></div>{errors.newPassword && <small className="field-error">{errors.newPassword}</small>}</label>
      <label>Confirm password<input type={show ? 'text' : 'password'} autoComplete="new-password" value={form.confirmPassword} onChange={(event) => { setForm({ ...form, confirmPassword: event.target.value }); setErrors({ ...errors, confirmPassword: undefined }) }} aria-invalid={!!errors.confirmPassword} />{errors.confirmPassword && <small className="field-error">{errors.confirmPassword}</small>}</label>
      <button className="button button-primary button-wide" disabled={busy}>{busy ? 'Saving…' : 'Reset password'}</button>
    </form>}
  </AuthLayout>
}

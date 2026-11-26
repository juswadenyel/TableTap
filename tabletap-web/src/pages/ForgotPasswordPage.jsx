import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import client from '../api/client'

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// This page asks the API to send a password reset link.
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(event) {
    event.preventDefault(); setError(''); setMessage('')
    if (!emailPattern.test(email.trim())) { setError('Enter a valid email address.'); return }
    setBusy(true)
    try {
      const { data } = await client.post('/api/auth/forgot-password', { email: email.trim() })
      setMessage(data.message)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'We could not process your request. Please try again.')
    } finally { setBusy(false) }
  }

  return <AuthLayout title="Forgot your password?" subtitle="Enter your account email and we’ll send a reset link."
    footer={<>Remembered it? <Link to="/login">Sign in</Link></>}>
    <form className="form" onSubmit={submit} noValidate>
      {error && <p className="form-alert" role="alert">{error}</p>}
      {message && <p className="success-alert" role="status">{message}</p>}
      <label>Email address<input type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} aria-invalid={!!error} /></label>
      <button className="button button-primary button-wide" disabled={busy}>{busy ? 'Sending…' : 'Send reset link'}</button>
    </form>
  </AuthLayout>
}

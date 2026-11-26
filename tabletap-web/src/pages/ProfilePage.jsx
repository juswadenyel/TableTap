import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'
import { useAuth } from '../context/AuthContext'

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phonePattern = /^[0-9+ ]{7,15}$/

export default function ProfilePage() {
  const { updateUser, logout } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', phone: '' })
  const [savedForm, setSavedForm] = useState({ fullName: '', email: '', phone: '' })
  const [password, setPassword] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [showPassword, setShowPassword] = useState({})
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [editing, setEditing] = useState(false)

  useEffect(() => {
    let active = true
    client.get('/api/users/me').then(({ data }) => {
      if (!active) return
      const profile = { fullName: data.fullName || '', email: data.email || '', phone: data.phone || '' }
      setForm(profile)
      setSavedForm(profile)
      updateUser(data)
    }).catch((requestError) => {
      if (active) setError(requestError.response?.data?.message || 'Could not load your profile.')
    }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [updateUser])

  function validateProfile() {
    const next = {}
    if (!form.fullName.trim()) next.fullName = 'Full name is required'
    else if (form.fullName.trim().length > 100) next.fullName = 'Full name must be at most 100 characters'
    if (!emailPattern.test(form.email.trim())) next.email = 'Email must be valid'
    if (form.phone && !phonePattern.test(form.phone)) next.phone = 'Phone must contain 7 to 15 digits, plus signs, or spaces'
    return next
  }

  async function saveProfile(event) {
    event.preventDefault()
    setMessage(''); setError('')
    const validation = validateProfile()
    setErrors(validation)
    if (Object.keys(validation).length) return
    setBusy(true)
    try {
      const { data } = await client.put('/api/users/me', { ...form, fullName: form.fullName.trim(), email: form.email.trim().toLowerCase(), phone: form.phone.trim() })
      setForm({ fullName: data.fullName, email: data.email, phone: data.phone || '' })
      setSavedForm({ fullName: data.fullName, email: data.email, phone: data.phone || '' })
      updateUser(data)
      setEditing(false)
      setMessage('Profile updated successfully.')
    } catch (requestError) {
      const response = requestError.response?.data
      setErrors(response?.fieldErrors || response?.errors || (requestError.response?.status === 409 ? { email: response?.message || 'Email is already registered' } : {}))
      setError(response?.message || 'Could not save your profile.')
    } finally { setBusy(false) }
  }

  async function changePassword(event) {
    event.preventDefault()
    setMessage(''); setError('')
    const validation = {}
    if (password.newPassword.length < 8) validation.newPassword = 'Password must be at least 8 characters'
    if (password.confirmPassword !== password.newPassword) validation.confirmPassword = 'Passwords do not match'
    if (password.currentPassword && password.newPassword === password.currentPassword) validation.newPassword = 'New password must differ from current password'
    setErrors(validation)
    if (Object.keys(validation).length) return
    setBusy(true)
    try {
      await client.post('/api/users/me/change-password', password)
      await logout(false)
      navigate('/login', { replace: true, state: { message: 'Password changed, please sign in again' } })
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Could not change your password.')
      setBusy(false)
    }
  }

  function fieldError(name) { return errors[name] && <small className="field-error">{errors[name]}</small> }
  function passwordField(label, name, autocomplete) {
    return <label>{label}<div className="password-input"><input type={showPassword[name] ? 'text' : 'password'} autoComplete={autocomplete} value={password[name]} onChange={(event) => { setPassword({ ...password, [name]: event.target.value }); setErrors({ ...errors, [name]: undefined }) }} aria-invalid={!!errors[name]} /><button className="show-password" type="button" onClick={() => setShowPassword({ ...showPassword, [name]: !showPassword[name] })}>{showPassword[name] ? 'Hide' : 'Show'}</button></div>{fieldError(name)}</label>
  }

  if (loading) return <div className="page-loading">Loading your profile…</div>
  return <main className="profile-page">
    <header className="section-heading"><div><span className="eyebrow">Your account</span><h1>Profile</h1></div></header>
    {error && <p className="form-alert" role="alert">{error}</p>}
    {message && <p className="success-alert" role="status">{message}</p>}
    <section className="profile-card">
      <div className="profile-card-heading"><div><h2>Personal details</h2><p className="muted">Your information is used to manage your TableTap account.</p></div>{!editing && <button className="button button-quiet" onClick={() => { setEditing(true); setError(''); setMessage('') }}>Edit</button>}</div>
      <form className="form profile-form" onSubmit={saveProfile} noValidate>
        <label>Full name<input value={form.fullName} disabled={!editing} onChange={(event) => setForm({ ...form, fullName: event.target.value })} aria-invalid={!!errors.fullName} />{fieldError('fullName')}</label>
        <label>Email address<input type="email" value={form.email} disabled={!editing} onChange={(event) => setForm({ ...form, email: event.target.value })} aria-invalid={!!errors.email} />{fieldError('email')}</label>
        <label>Phone <span className="optional">Optional</span><input type="tel" value={form.phone} disabled={!editing} onChange={(event) => setForm({ ...form, phone: event.target.value })} aria-invalid={!!errors.phone} />{fieldError('phone')}</label>
        {editing && <div className="form-actions"><button type="button" className="button button-quiet" disabled={busy} onClick={() => { setEditing(false); setErrors({}); setError(''); setForm(savedForm) }}>Cancel</button><button className="button button-primary" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</button></div>}
      </form>
    </section>
    <section className="profile-card">
      <div className="profile-card-heading"><div><h2>Change password</h2><p className="muted">You will need to sign in again after changing your password.</p></div></div>
      <form className="form profile-form" onSubmit={changePassword} noValidate>
        {passwordField('Current password', 'currentPassword', 'current-password')}
        {passwordField('New password', 'newPassword', 'new-password')}
        {passwordField('Confirm new password', 'confirmPassword', 'new-password')}
        <button className="button button-primary" disabled={busy}>{busy ? 'Changing…' : 'Change password'}</button>
      </form>
    </section>
  </main>
}

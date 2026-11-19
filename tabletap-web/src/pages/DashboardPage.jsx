import { useEffect, useState } from 'react'
import client from '../api/client'

const dashboardCopy = {
  DINER: { eyebrow: 'Your TableTap', title: 'A seat is waiting.' },
  STAFF: { eyebrow: 'Service overview', title: 'Your shift starts here.' },
  OWNER: { eyebrow: 'Restaurant overview', title: 'Your floor, at a glance.' },
}

export default function DashboardPage({ role }) {
  const [dashboard, setDashboard] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    client.get(`/api/dashboard/${role.toLowerCase()}`)
      .then(({ data }) => { if (active) setDashboard(data) })
      .catch((requestError) => {
        if (active && requestError.response?.status !== 401) {
          setError(requestError.response?.data?.message || 'Dashboard could not be loaded. Please try again.')
        }
      })
    return () => { active = false }
  }, [role])

  const copy = dashboardCopy[role]
  return (
    <main className="dashboard-page">
      <section className="dashboard-hero">
        <span className="eyebrow">{copy.eyebrow}</span>
        <h1>{dashboard?.welcomeMessage || copy.title}</h1>
        <p>Your TableTap tools are ready when you are.</p>
        <div className="hero-stamp" aria-hidden="true">{role === 'DINER' ? 'DINING\nMADE EASY' : role === 'STAFF' ? 'SERVICE\nIN SYNC' : 'YOUR PLACE\nYOUR PACE'}</div>
      </section>
      <section className="feature-section">
        <div className="section-heading"><div><span className="eyebrow">At your fingertips</span><h2>What you can do</h2></div><span className="role-pill">{role}</span></div>
        {error && <p className="form-alert" role="alert">{error}</p>}
        {!dashboard && !error && <p className="muted">Loading your dashboard…</p>}
        {dashboard && <div className="feature-grid">{dashboard.features.map((feature, index) => <article className="feature-card" key={feature}><span className="feature-number">0{index + 1}</span><h3>{feature}</h3><span className="feature-arrow" aria-hidden="true">↗</span></article>)}</div>}
      </section>
    </main>
  )
}

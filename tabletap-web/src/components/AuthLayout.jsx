import React from 'react'
import { Link } from 'react-router-dom'

export default function AuthLayout({ title, subtitle, children, footer }) {
  return (
    <main className="auth-page">
      <section className="auth-card">
        <Link to="/" className="brand auth-brand"><span className="brand-mark">T</span><span>tabletap</span></Link>
        <p className="eyebrow">A better seat at the table</p>
        <h1>{title}</h1>
        <p className="auth-subtitle">{subtitle}</p>
        {children}
        <div className="auth-footer">{footer}</div>
      </section>
      <aside className="auth-aside" aria-label="Restaurant welcome">
        <div className="aside-content">
          <span className="eyebrow">Good food. Good company.</span>
          <p>Make room for what matters.</p>
          <span className="aside-note">Your restaurant, made a little simpler.</span>
        </div>
      </aside>
    </main>
  )
}

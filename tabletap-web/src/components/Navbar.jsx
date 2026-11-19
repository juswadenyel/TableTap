import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const linksByRole = {
  DINER: [{ label: 'Reserve a Table', path: '/reserve' }, { label: 'My Reservations', path: '/reservations' }],
  STAFF: [{ label: "Today's Reservations", path: '/today' }, { label: 'Table Status', path: '/table-status' }],
  OWNER: [{ label: 'Floor Plan', path: '/floor-plan' }, { label: 'Tables', path: '/tables' }, { label: 'Staff', path: '/staff-management' }],
}

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    try { await logout() } catch { /* Local session is cleared by AuthContext even if the request fails. */ }
    navigate('/login', { replace: true })
  }

  return (
    <header className="navbar">
      <NavLink className="brand" to={`/${user.role.toLowerCase()}`} aria-label="TableTap home">
        <span className="brand-mark">T</span><span>tabletap</span>
      </NavLink>
      <nav className="nav-links" aria-label="Main navigation">
        {(linksByRole[user.role] || []).map((link) => <NavLink key={link.path} to={link.path}>{link.label}</NavLink>)}
      </nav>
      <div className="nav-user">
        <span className="nav-name">{user.fullName}</span>
        <button className="button button-quiet button-small" onClick={handleLogout}>Log out</button>
      </div>
    </header>
  )
}

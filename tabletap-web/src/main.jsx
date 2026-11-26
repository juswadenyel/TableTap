import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Navbar from './components/Navbar'
import ProtectedRoute, { dashboardPath } from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ProfilePage from './pages/ProfilePage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import DashboardPage from './pages/DashboardPage'
import UnauthorizedPage from './pages/UnauthorizedPage'
import PlaceholderPage from './pages/PlaceholderPage'
import './styles.css'

function HomeRedirect() {
  const { user, ready } = useAuth()
  if (!ready) return <div className="page-loading">Loading TableTap…</div>
  return <Navigate to={user ? dashboardPath(user.role) : '/login'} replace />
}

function AppLayout() {
  return <><Navbar /><Outlet /></>
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/unauthorized" element={<UnauthorizedPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/reserve" element={<PlaceholderPage />} />
          <Route path="/reservations" element={<PlaceholderPage />} />
          <Route path="/today" element={<PlaceholderPage />} />
          <Route path="/table-status" element={<PlaceholderPage />} />
          <Route path="/floor-plan" element={<PlaceholderPage />} />
          <Route path="/tables" element={<PlaceholderPage />} />
          <Route path="/staff-management" element={<PlaceholderPage />} />
          <Route element={<ProtectedRoute role="DINER" />}><Route path="/diner" element={<DashboardPage role="DINER" />} /></Route>
          <Route element={<ProtectedRoute role="STAFF" />}><Route path="/staff" element={<DashboardPage role="STAFF" />} /></Route>
          <Route element={<ProtectedRoute role="OWNER" />}><Route path="/owner" element={<DashboardPage role="OWNER" />} /></Route>
        </Route>
      </Route>
      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  )
}

function App() {
  return <BrowserRouter><AuthProvider><AppRoutes /></AuthProvider></BrowserRouter>
}

ReactDOM.createRoot(document.getElementById('root')).render(<React.StrictMode><App /></React.StrictMode>)

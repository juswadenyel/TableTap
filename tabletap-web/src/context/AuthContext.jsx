import React from 'react'
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import client, { TOKEN_KEY } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState(null)
  const [ready, setReady] = useState(false)

  const saveSession = useCallback((session) => {
    localStorage.setItem(TOKEN_KEY, session.token)
    setToken(session.token)
    setUser(session.user)
  }, [])

  const clearSession = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    let active = true
    if (!token) {
      setReady(true)
      return () => { active = false }
    }
    client.get('/api/auth/me')
      .then(({ data }) => { if (active) setUser(data) })
      .catch(() => { if (active) clearSession() })
      .finally(() => { if (active) setReady(true) })
    return () => { active = false }
  }, [token, clearSession])

  const login = useCallback(async (credentials) => {
    const { data } = await client.post('/api/auth/login', credentials)
    saveSession(data)
    return data.user
  }, [saveSession])

  const register = useCallback(async (details) => {
    const { data } = await client.post('/api/auth/register', details)
    saveSession(data)
    return data.user
  }, [saveSession])

  const logout = useCallback(async (revoke = true) => {
    try {
      if (revoke && localStorage.getItem(TOKEN_KEY)) await client.post('/api/auth/logout')
    } finally {
      clearSession()
    }
  }, [clearSession])

  const updateUser = useCallback((nextUser) => setUser(nextUser), [])
  const value = useMemo(() => ({ user, token, ready, login, register, logout, updateUser }), [user, token, ready, login, register, logout, updateUser])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}

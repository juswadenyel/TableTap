import axios from 'axios'

export const TOKEN_KEY = 'tabletap_token'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  const publicAuthRequest = /^\/api\/auth\/(login|register|forgot-password|reset-password)(\/|$)/.test(config.url || '')
  if (token && !publicAuthRequest) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const publicAuthPath = ['/login', '/forgot-password', '/reset-password'].includes(window.location.pathname)
    if (error.response?.status === 401 && !publicAuthPath) {
      localStorage.removeItem(TOKEN_KEY)
      if (window.location.pathname !== '/login') window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)

export default client

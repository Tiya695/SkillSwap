import axios from 'axios'
import { tokenStorage } from '../auth/tokenStorage'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.request.use((config) => {
  const token = tokenStorage.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

const PUBLIC_PATHS = ['/', '/login', '/register', '/forgot-password']

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url || ''
    const isAuthCall = url.startsWith('/auth/')
    if (error.response?.status === 401 && !isAuthCall) {
      tokenStorage.clear()
      if (!PUBLIC_PATHS.includes(window.location.pathname)) window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)

export default client

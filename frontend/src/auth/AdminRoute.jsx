import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './useAuth'

export default function AdminRoute() {
  const { isAdmin } = useAuth()
  if (!isAdmin) return <Navigate to="/dashboard" replace />
  return <Outlet />
}

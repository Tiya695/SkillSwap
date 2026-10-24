import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './useAuth'
import PageLoader from '../components/ui/PageLoader'

export default function ProtectedRoute() {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <PageLoader />
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return <Outlet />
}

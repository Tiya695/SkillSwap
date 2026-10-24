import { Route, Routes } from 'react-router-dom'
import AdminRoute from '../auth/AdminRoute'
import ProtectedRoute from '../auth/ProtectedRoute'
import AppLayout from '../components/layout/AppLayout'
import PublicLayout from '../components/layout/PublicLayout'
import AdminSkills from '../pages/AdminSkills'
import Dashboard from '../pages/Dashboard'
import ForgotPassword from '../pages/ForgotPassword'
import Landing from '../pages/Landing'
import Login from '../pages/Login'
import Matches from '../pages/Matches'
import MySkills from '../pages/MySkills'
import NotFound from '../pages/NotFound'
import Profile from '../pages/Profile'
import PublicProfile from '../pages/PublicProfile'
import Register from '../pages/Register'
import Skills from '../pages/Skills'
import SwapDetail from '../pages/SwapDetail'
import Swaps from '../pages/Swaps'

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<Landing />} />
      </Route>

      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/skills" element={<Skills />} />
          <Route path="/my-skills" element={<MySkills />} />
          <Route path="/matches" element={<Matches />} />
          <Route path="/swaps" element={<Swaps />} />
          <Route path="/swaps/:id" element={<SwapDetail />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/users/:id" element={<PublicProfile />} />
          <Route element={<AdminRoute />}>
            <Route path="/admin/skills" element={<AdminSkills />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

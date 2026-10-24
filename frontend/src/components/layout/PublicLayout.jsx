import { Outlet } from 'react-router-dom'
import Navbar from './Navbar'
import Footer from './Footer'

export default function PublicLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-white">
      <Navbar />
      <main id="top" className="flex-1">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}

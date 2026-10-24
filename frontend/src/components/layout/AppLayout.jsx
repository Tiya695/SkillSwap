import { Outlet } from 'react-router-dom'
import AppNavbar from './AppNavbar'

export default function AppLayout() {
  return (
    <div className="relative min-h-screen overflow-x-clip bg-canvas">
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-[420px] bg-[radial-gradient(60%_100%_at_50%_0%,#e6deff_0%,rgba(230,222,255,0)_75%)]"
        aria-hidden="true"
      />
      <AppNavbar />
      <main className="relative mx-auto max-w-6xl px-4 py-8 sm:px-6 sm:py-10">
        <Outlet />
      </main>
    </div>
  )
}

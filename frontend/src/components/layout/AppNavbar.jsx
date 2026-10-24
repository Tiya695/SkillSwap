import { useEffect, useRef, useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { LogOut, Menu, ShieldCheck, UserRound, X } from 'lucide-react'
import { useAuth } from '../../auth/useAuth'
import { firstName } from '../../utils/format'
import Avatar from '../ui/Avatar'
import Logo from './Logo'

const links = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/skills', label: 'Browse Skills' },
  { to: '/my-skills', label: 'My Skills' },
  { to: '/matches', label: 'Matches' },
  { to: '/swaps', label: 'Swaps' },
  { to: '/profile', label: 'Profile' },
]

const pill = ({ isActive }) =>
  `rounded-full px-3.5 py-1.5 text-sm font-semibold transition-all ${
    isActive ? 'bg-brand text-white shadow-glow' : 'text-ink/70 hover:bg-brand-100 hover:text-brand-700'
  }`

export default function AppNavbar() {
  const { user, isAdmin, logout } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const menuRef = useRef(null)

  useEffect(() => {
    const onClick = (e) => menuRef.current && !menuRef.current.contains(e.target) && setMenuOpen(false)
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [])

  const handleLogout = () => {
    logout()
    window.location.assign('/')
  }

  return (
    <header className="sticky top-0 z-40 border-b border-line bg-white/85 backdrop-blur-md">
      <div className="mx-auto flex h-[68px] max-w-6xl items-center justify-between gap-4 px-4 sm:px-6">
        <Logo to="/dashboard" />

        <nav className="hidden items-center gap-1 lg:flex" aria-label="Main">
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} className={pill}>
              {l.label}
            </NavLink>
          ))}
        </nav>

        <div className="flex items-center gap-2">
          <div className="relative" ref={menuRef}>
            <button
              type="button"
              onClick={() => setMenuOpen((v) => !v)}
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              aria-label={`Account menu for ${user?.name}`}
              className="group flex flex-col items-center rounded-2xl px-2 py-1"
            >
              <Avatar name={user?.name} src={user?.avatarUrl} size="md" className="transition-transform group-hover:scale-105" />
              <span className="mt-0.5 max-w-[88px] truncate text-[11px] font-semibold leading-none text-ink">
                {firstName(user?.name)}
              </span>
            </button>
            {menuOpen && (
              <div
                role="menu"
                onClick={() => setMenuOpen(false)}
                className="absolute right-0 mt-2 w-60 rounded-2xl border border-line bg-white p-1.5 shadow-float"
              >
                <div className="px-3 py-2">
                  <p className="truncate text-sm font-bold">{user?.name}</p>
                  <p className="truncate text-xs text-muted">{user?.email}</p>
                </div>
                <div className="my-1 border-t border-line" />
                <MenuLink to="/profile" icon={UserRound}>
                  My profile
                </MenuLink>
                {isAdmin && (
                  <MenuLink to="/admin/skills" icon={ShieldCheck}>
                    Manage skills
                  </MenuLink>
                )}
                <button
                  type="button"
                  role="menuitem"
                  onClick={handleLogout}
                  className="flex w-full items-center gap-2.5 rounded-xl px-3 py-2 text-sm font-medium hover:bg-brand-50"
                >
                  <LogOut className="h-4 w-4" aria-hidden="true" /> Log out
                </button>
              </div>
            )}
          </div>

          <button
            type="button"
            className="rounded-full p-2 hover:bg-brand-50 lg:hidden"
            onClick={() => setMobileOpen((v) => !v)}
            aria-label={mobileOpen ? 'Close menu' : 'Open menu'}
            aria-expanded={mobileOpen}
          >
            {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {mobileOpen && (
        <nav
          className="border-t border-line bg-white px-4 py-3 lg:hidden"
          aria-label="Mobile"
          onClick={() => setMobileOpen(false)}
        >
          <div className="grid gap-1">
            {links.map((l) => (
              <NavLink key={l.to} to={l.to} className={pill}>
                {l.label}
              </NavLink>
            ))}
            {isAdmin && (
              <NavLink to="/admin/skills" className={pill}>
                Manage skills
              </NavLink>
            )}
          </div>
        </nav>
      )}
    </header>
  )
}

function MenuLink({ to, icon: Icon, children }) {
  return (
    <Link
      to={to}
      role="menuitem"
      className="flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm font-medium hover:bg-brand-50"
    >
      <Icon className="h-4 w-4" aria-hidden="true" /> {children}
    </Link>
  )
}

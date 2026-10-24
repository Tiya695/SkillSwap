import { useState } from 'react'
import { Menu, X } from 'lucide-react'
import { useAuth } from '../../auth/useAuth'
import Button from '../ui/Button'
import Logo from './Logo'

const links = [
  { href: '/#top', label: 'Home' },
  { href: '/#features', label: 'Features' },
  { href: '/#how-it-works', label: 'How It Works' },
  { href: '/#why', label: 'Why SkillSwap' },
]

export default function Navbar() {
  const { user } = useAuth()
  const [open, setOpen] = useState(false)

  return (
    <header className="sticky top-0 z-40 border-b border-line/70 bg-white/85 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
        <Logo />

        <nav className="hidden items-center gap-1 md:flex" aria-label="Main">
          {links.map((l) => (
            <a
              key={l.href}
              href={l.href}
              className="rounded-full px-3.5 py-1.5 text-xs font-semibold text-ink/80 hover:bg-surface hover:text-ink"
            >
              {l.label}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-2">
          {user ? (
            <Button to="/dashboard" size="sm">
              Dashboard
            </Button>
          ) : (
            <>
              <Button to="/register" variant="ghost" size="sm" className="hidden sm:inline-flex">
                Sign up
              </Button>
              <Button to="/login" variant="dark" size="sm">
                Log in
              </Button>
            </>
          )}
          <button
            type="button"
            className="rounded-full p-2 hover:bg-surface md:hidden"
            onClick={() => setOpen((v) => !v)}
            aria-label={open ? 'Close menu' : 'Open menu'}
            aria-expanded={open}
          >
            {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {open && (
        <nav className="border-t border-line bg-white px-4 py-3 md:hidden" aria-label="Mobile">
          {links.map((l) => (
            <a
              key={l.href}
              href={l.href}
              onClick={() => setOpen(false)}
              className="block rounded-xl px-3 py-2.5 text-sm font-semibold hover:bg-surface"
            >
              {l.label}
            </a>
          ))}
        </nav>
      )}
    </header>
  )
}

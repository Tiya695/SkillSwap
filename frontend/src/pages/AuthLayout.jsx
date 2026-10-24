import { ArrowLeftRight, Music, Code, Languages } from 'lucide-react'
import Logo from '../components/layout/Logo'

export default function AuthLayout({ title, subtitle, children, footer }) {
  return (
    <div className="grid min-h-screen bg-white lg:grid-cols-[minmax(0,1fr)_minmax(0,1.05fr)]">
      <div className="flex flex-col px-5 py-8 sm:px-10">
        <Logo />
        <div className="mx-auto flex w-full max-w-md flex-1 flex-col justify-center py-10">
          <h1 className="text-3xl font-extrabold tracking-tightest sm:text-4xl">{title}</h1>
          <p className="mt-2 text-sm text-muted">{subtitle}</p>
          <div className="mt-8">{children}</div>
          <p className="mt-8 text-sm text-muted">{footer}</p>
        </div>
      </div>

      <aside className="hero-glow relative hidden overflow-hidden border-l border-line lg:block" aria-hidden="true">
        <div className="orb absolute -right-16 top-16 h-56 w-56 rounded-full opacity-90 animate-drift" />
        <div className="orb absolute -left-10 bottom-24 h-40 w-40 rounded-full opacity-80 animate-drift [animation-delay:-4s]" />
        <div className="scene absolute inset-0 flex items-center justify-center">
          <div className="preserve-3d relative h-[420px] w-[360px] [transform:rotateX(14deg)_rotateY(-18deg)]">
            <FloatCard className="left-0 top-4 [--z:30px]" icon={Code} title="Python" text="You teach" delay="0s" />
            <FloatCard className="right-0 top-36 [--z:70px]" icon={Music} title="Guitar" text="You learn" delay="-2s" />
            <FloatCard className="left-6 top-72 [--z:20px]" icon={Languages} title="Spanish" text="Matched" delay="-4s" />
            <span className="icon-3d absolute left-1/2 top-1/2 flex h-16 w-16 -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-3xl text-white animate-float [--z:110px]">
              <ArrowLeftRight className="h-8 w-8" />
            </span>
          </div>
        </div>
        <p className="absolute inset-x-0 bottom-12 px-12 text-center text-2xl font-extrabold tracking-tightest text-ink">
          Teach what you know.
          <br />
          Learn what you love.
        </p>
      </aside>
    </div>
  )
}

function FloatCard({ className, icon: Icon, title, text, delay }) {
  return (
    <div
      style={{ animationDelay: delay }}
      className={`absolute flex w-48 items-center gap-3 rounded-2xl border border-white/70 bg-white/80 p-3.5 shadow-float backdrop-blur animate-float ${className}`}
    >
      <span className="icon-3d flex h-10 w-10 items-center justify-center rounded-xl text-white">
        <Icon className="h-5 w-5" />
      </span>
      <span>
        <span className="block text-sm font-bold">{title}</span>
        <span className="block text-xs text-muted">{text}</span>
      </span>
    </div>
  )
}

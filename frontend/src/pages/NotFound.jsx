import Button from '../components/ui/Button'
import Logo from '../components/layout/Logo'

export default function NotFound() {
  return (
    <div className="hero-glow flex min-h-screen flex-col items-center justify-center bg-white px-4 text-center">
      <Logo />
      <p className="mt-12 text-7xl font-extrabold tracking-tightest">404</p>
      <h1 className="mt-3 text-2xl font-extrabold tracking-tightest">This page does not exist</h1>
      <p className="mt-2 max-w-sm text-sm text-muted">The link may be broken, or the page may have moved.</p>
      <div className="mt-8 flex gap-3">
        <Button to="/">Go home</Button>
        <Button to="/dashboard" variant="secondary">
          Open dashboard
        </Button>
      </div>
    </div>
  )
}

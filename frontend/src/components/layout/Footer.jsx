import { Link } from 'react-router-dom'
import Logo from './Logo'

export default function Footer() {
  return (
    <footer className="border-t border-line bg-white">
      <div className="mx-auto grid max-w-6xl gap-10 px-4 py-12 sm:px-6 md:grid-cols-[1.4fr_1fr_1fr_1fr]">
        <div>
          <Logo />
          <p className="mt-3 max-w-xs text-sm text-muted">
            Teach one skill, learn another. SkillSwap matches people who can teach each other.
          </p>
        </div>
        <FooterColumn title="Product">
          <a href="/#features">Features</a>
          <a href="/#how-it-works">How it works</a>
          <a href="/#why">Why SkillSwap</a>
        </FooterColumn>
        <FooterColumn title="Explore">
          <Link to="/skills">Browse skills</Link>
          <Link to="/matches">Matches</Link>
          <Link to="/swaps">Swaps</Link>
        </FooterColumn>
        <FooterColumn title="Account">
          <Link to="/login">Log in</Link>
          <Link to="/register">Sign up</Link>
          <Link to="/profile">Profile</Link>
        </FooterColumn>
      </div>
      <div className="border-t border-line py-5 text-center text-xs text-muted">
        &copy; {new Date().getFullYear()} SkillSwap
      </div>
    </footer>
  )
}

function FooterColumn({ title, children }) {
  return (
    <div>
      <h3 className="text-sm font-bold">{title}</h3>
      <div className="mt-3 flex flex-col gap-2 text-sm text-muted [&_a:hover]:text-ink">{children}</div>
    </div>
  )
}

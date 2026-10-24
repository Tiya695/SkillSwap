import { Link } from 'react-router-dom'
import { Loader2 } from 'lucide-react'

const variants = {
  primary: 'bg-brand text-white border border-brand hover:bg-brand-600 shadow-glow hover:-translate-y-0.5',
  dark: 'bg-ink text-white border border-ink hover:bg-ink-soft',
  secondary: 'bg-white text-ink border border-line hover:border-brand/50 hover:bg-brand-50',
  ghost: 'bg-transparent text-ink border border-transparent hover:bg-brand-50',
  danger: 'bg-white text-status-rejected border border-status-rejected/30 hover:bg-red-50',
}

const sizes = {
  sm: 'h-8 px-3.5 text-xs',
  md: 'h-10 px-5 text-sm',
  lg: 'h-12 px-7 text-sm',
}

export default function Button({
  variant = 'primary',
  size = 'md',
  to,
  href,
  loading = false,
  disabled = false,
  className = '',
  children,
  type = 'button',
  ...rest
}) {
  const classes = `inline-flex items-center justify-center gap-2 rounded-full font-semibold transition-all duration-200 disabled:cursor-not-allowed disabled:opacity-50 disabled:shadow-none disabled:hover:translate-y-0 ${variants[variant]} ${sizes[size]} ${className}`

  if (href) {
    return (
      <a href={href} className={classes} {...rest}>
        {children}
      </a>
    )
  }
  if (to) {
    return (
      <Link to={to} className={classes} {...rest}>
        {children}
      </Link>
    )
  }
  return (
    <button type={type} className={classes} disabled={disabled || loading} {...rest}>
      {loading && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
      {children}
    </button>
  )
}

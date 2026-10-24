
export default function Chip({ children, icon: Icon, active = false, onClick, className = '' }) {
  const base =
    'inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-semibold transition-colors'
  const tone = active
    ? 'border-brand bg-brand text-white shadow-glow'
    : 'border-line bg-white text-ink' + (onClick ? ' hover:border-brand/50 hover:bg-brand-50' : '')

  const content = (
    <>
      {Icon && <Icon className="h-3.5 w-3.5" aria-hidden="true" />}
      {children}
    </>
  )

  if (onClick) {
    return (
      <button type="button" onClick={onClick} aria-pressed={active} className={`${base} ${tone} ${className}`}>
        {content}
      </button>
    )
  }
  return <span className={`${base} ${tone} ${className}`}>{content}</span>
}

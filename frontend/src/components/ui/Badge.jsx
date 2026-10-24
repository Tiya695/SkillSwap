const tones = {
  neutral: 'bg-brand-50 text-brand-700 border-brand-200',
  dark: 'bg-brand text-white border-brand',
  pending: 'bg-amber-50 text-status-pending border-amber-200',
  accepted: 'bg-sky-50 text-status-accepted border-sky-200',
  completed: 'bg-green-50 text-status-completed border-green-200',
  rejected: 'bg-red-50 text-status-rejected border-red-200',
  cancelled: 'bg-gray-100 text-status-cancelled border-gray-200',
}

export default function Badge({ tone = 'neutral', children, className = '' }) {
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-[11px] font-semibold ${tones[tone]} ${className}`}
    >
      {children}
    </span>
  )
}

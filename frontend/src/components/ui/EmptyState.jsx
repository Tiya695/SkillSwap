
export default function EmptyState({ icon: Icon, title, text, action, tone = 'neutral' }) {
  return (
    <div className="flex flex-col items-center rounded-card border border-dashed border-line bg-white px-6 py-12 text-center">
      {Icon && (
        <span
          className={`mb-4 flex h-12 w-12 items-center justify-center rounded-full ${tone === 'error' ? 'bg-red-50 text-status-rejected' : 'bg-surface text-ink'}`}
        >
          <Icon className="h-6 w-6" aria-hidden="true" />
        </span>
      )}
      <h3 className="text-base font-bold">{title}</h3>
      {text && <p className="mt-1 max-w-sm text-sm text-muted">{text}</p>}
      {action && <div className="mt-5">{action}</div>}
    </div>
  )
}

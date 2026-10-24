export function Skeleton({ className = '' }) {
  return <div aria-hidden="true" className={`animate-pulse rounded-xl bg-surface ${className}`} />
}

export function CardSkeletonList({ count = 3, height = 'h-32' }) {
  return (
    <div className="grid gap-4" role="status" aria-label="Loading">
      {Array.from({ length: count }, (_, i) => (
        <Skeleton key={i} className={`${height} rounded-card`} />
      ))}
    </div>
  )
}

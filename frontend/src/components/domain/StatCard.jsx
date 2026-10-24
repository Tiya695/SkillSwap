import Card from '../ui/Card'
import { Skeleton } from '../ui/Skeleton'
import { useCountUp } from '../../utils/useCountUp'

export default function StatCard({ label, value, hint, icon: Icon, loading, delay = 0 }) {
  return (
    <Card
      tilt
      gradient
      className="animate-fade-up p-5"
      style={{ animationDelay: `${delay}ms` }}
    >
      <div className="flex items-center justify-between" style={{ transform: 'translateZ(30px)' }}>
        <p className="text-sm font-semibold text-muted">{label}</p>
        {Icon && (
          <span className="icon-3d flex h-11 w-11 items-center justify-center rounded-2xl text-white">
            <Icon className="h-5 w-5" aria-hidden="true" />
          </span>
        )}
      </div>
      {loading ? (
        <Skeleton className="mt-3 h-10 w-16" />
      ) : (
        <div className="mt-3 flex items-baseline gap-2.5 flex-wrap" style={{ transform: 'translateZ(40px)' }}>
          <p className="text-5xl font-extrabold tracking-tightest">
            {typeof value === 'number' ? <Counter target={value} /> : value}
          </p>
          {hint && (
            <span className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-bold text-brand">
              {hint}
            </span>
          )}
        </div>
      )}
    </Card>
  )
}

function Counter({ target }) {
  return useCountUp(target)
}

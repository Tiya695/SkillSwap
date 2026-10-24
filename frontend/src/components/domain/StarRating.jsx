import { Star } from 'lucide-react'

export default function StarRating({ value = 0, onChange, size = 'md' }) {
  const dim = size === 'lg' ? 'h-7 w-7' : size === 'sm' ? 'h-3.5 w-3.5' : 'h-5 w-5'

  if (!onChange) {
    return (
      <span className="inline-flex items-center gap-0.5" role="img" aria-label={`${value} out of 5 stars`}>
        {[1, 2, 3, 4, 5].map((n) => (
          <Star
            key={n}
            className={`${dim} ${n <= Math.round(value) ? 'fill-amber-400 text-amber-400' : 'text-brand-200'}`}
            aria-hidden="true"
          />
        ))}
      </span>
    )
  }

  return (
    <div className="inline-flex items-center gap-1" role="radiogroup" aria-label="Rating">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          role="radio"
          aria-checked={n === value}
          aria-label={`${n} star${n > 1 ? 's' : ''}`}
          onClick={() => onChange(n)}
          className="rounded-full p-0.5 transition-transform hover:scale-125"
        >
          <Star className={`${dim} ${n <= value ? 'fill-amber-400 text-amber-400' : 'text-brand-200 hover:text-amber-300'}`} />
        </button>
      ))}
    </div>
  )
}

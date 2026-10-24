import { Check, Circle } from 'lucide-react'
import { passwordChecks } from '../../utils/validation'

export default function PasswordChecklist({ password }) {
  return (
    <ul className="mt-2 grid gap-1 sm:grid-cols-2" aria-label="Password rules">
      {passwordChecks(password).map((c) => (
        <li
          key={c.label}
          className={`flex items-center gap-1.5 text-xs transition-colors ${c.ok ? 'font-semibold text-status-completed' : 'text-muted'}`}
        >
          {c.ok ? <Check className="h-3.5 w-3.5" aria-hidden="true" /> : <Circle className="h-3 w-3" aria-hidden="true" />}
          {c.label}
          <span className="sr-only">{c.ok ? ' done' : ' missing'}</span>
        </li>
      ))}
    </ul>
  )
}

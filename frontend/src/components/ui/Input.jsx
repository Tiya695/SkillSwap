import { useId, useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'

const fieldClass =
  'w-full rounded-xl border bg-white px-4 py-2.5 text-sm text-ink placeholder:text-muted/70 transition-colors focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/15 disabled:bg-surface disabled:text-muted'

export function Field({ label, error, hint, children, htmlFor }) {
  return (
    <div>
      {label && (
        <label htmlFor={htmlFor} className="mb-1.5 block text-sm font-semibold text-ink">
          {label}
        </label>
      )}
      {children}
      {hint && !error && <p className="mt-1 text-xs text-muted">{hint}</p>}
      {error && (
        <p role="alert" className="mt-1 text-xs font-medium text-status-rejected">
          {error}
        </p>
      )}
    </div>
  )
}

export function Input({ label, error, hint, className = '', ...rest }) {
  const id = useId()
  return (
    <Field label={label} error={error} hint={hint} htmlFor={id}>
      <input
        id={id}
        aria-invalid={Boolean(error)}
        className={`${fieldClass} ${error ? 'border-status-rejected' : 'border-line'} ${className}`}
        {...rest}
      />
    </Field>
  )
}

export function PasswordInput({ label, error, hint, className = '', ...rest }) {
  const id = useId()
  const [visible, setVisible] = useState(false)
  return (
    <Field label={label} error={error} hint={hint} htmlFor={id}>
      <div className="relative">
        <input
          id={id}
          type={visible ? 'text' : 'password'}
          aria-invalid={Boolean(error)}
          className={`${fieldClass} pr-11 ${error ? 'border-status-rejected' : 'border-line'} ${className}`}
          {...rest}
        />
        <button
          type="button"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? 'Hide password' : 'Show password'}
          className="absolute right-2 top-1/2 -translate-y-1/2 rounded-full p-1.5 text-muted hover:text-ink"
        >
          {visible ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
        </button>
      </div>
    </Field>
  )
}

export function Textarea({ label, error, hint, rows = 3, className = '', ...rest }) {
  const id = useId()
  return (
    <Field label={label} error={error} hint={hint} htmlFor={id}>
      <textarea
        id={id}
        rows={rows}
        aria-invalid={Boolean(error)}
        className={`${fieldClass} resize-none ${error ? 'border-status-rejected' : 'border-line'} ${className}`}
        {...rest}
      />
    </Field>
  )
}

export function Select({ label, error, hint, children, className = '', ...rest }) {
  const id = useId()
  return (
    <Field label={label} error={error} hint={hint} htmlFor={id}>
      <select
        id={id}
        aria-invalid={Boolean(error)}
        className={`${fieldClass} ${error ? 'border-status-rejected' : 'border-line'} ${className}`}
        {...rest}
      >
        {children}
      </select>
    </Field>
  )
}

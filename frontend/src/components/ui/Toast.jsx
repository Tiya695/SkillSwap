import { useCallback, useMemo, useState } from 'react'
import { CheckCircle2, XCircle } from 'lucide-react'
import { ToastContext } from './toastContext'
export function ToastProvider({ children }) {
  const [items, setItems] = useState([])

  const push = useCallback((type, message) => {
    const id = crypto.randomUUID()
    setItems((list) => [...list, { id, type, message }])
    setTimeout(() => setItems((list) => list.filter((t) => t.id !== id)), 5000)
  }, [])

  const api = useMemo(
    () => ({ success: (m) => push('success', m), error: (m) => push('error', m) }),
    [push],
  )

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div
        aria-live="polite"
        className="pointer-events-none fixed inset-x-0 top-20 z-[70] flex flex-col items-center gap-2 px-4"
      >
        {items.map((t) => (
          <div
            key={t.id}
            role="status"
            className={`pointer-events-auto flex max-w-lg animate-fade-up items-center gap-3 rounded-2xl border px-5 py-3.5 text-[15px] font-semibold shadow-float ${
              t.type === 'success'
                ? 'border-green-300 bg-green-50 text-green-900'
                : 'border-red-300 bg-red-50 text-red-900'
            }`}
          >
            {t.type === 'success' ? (
              <CheckCircle2 className="h-6 w-6 shrink-0 text-status-completed" aria-hidden="true" />
            ) : (
              <XCircle className="h-6 w-6 shrink-0 text-status-rejected" aria-hidden="true" />
            )}
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

import { useEffect, useRef } from 'react'
import { X } from 'lucide-react'

export default function Modal({ open, onClose, title, children, footer, wide = false }) {
  const panelRef = useRef(null)
  const onCloseRef = useRef(onClose)
  useEffect(() => {
    onCloseRef.current = onClose
  })

  useEffect(() => {
    if (!open) return
    const onKey = (e) => e.key === 'Escape' && onCloseRef.current()
    document.addEventListener('keydown', onKey)
    const previous = document.activeElement
    panelRef.current?.focus()
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.style.overflow = ''
      previous?.focus?.()
    }
  }, [open])

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center sm:p-4">
      <div className="absolute inset-0 bg-ink/30 backdrop-blur-[2px]" onClick={onClose} aria-hidden="true" />
      <div
        ref={panelRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className={`relative flex max-h-[92vh] w-full flex-col rounded-t-3xl border border-line bg-white shadow-float sm:rounded-card ${wide ? 'sm:max-w-2xl' : 'sm:max-w-md'}`}
      >
        <div className="flex items-center justify-between border-b border-line px-6 py-4">
          <h2 className="text-lg font-bold tracking-tight">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="rounded-full p-1.5 text-muted hover:bg-surface hover:text-ink"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="overflow-y-auto px-6 py-5">{children}</div>
        {footer && <div className="flex justify-end gap-2 border-t border-line px-6 py-4">{footer}</div>}
      </div>
    </div>
  )
}

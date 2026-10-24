import { ChevronLeft, ChevronRight } from 'lucide-react'
import Button from './Button'

export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null
  return (
    <nav className="mt-8 flex items-center justify-center gap-3" aria-label="Pagination">
      <Button variant="secondary" size="sm" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        <ChevronLeft className="h-4 w-4" aria-hidden="true" /> Previous
      </Button>
      <span className="text-sm text-muted">
        Page {page + 1} of {totalPages}
      </span>
      <Button variant="secondary" size="sm" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        Next <ChevronRight className="h-4 w-4" aria-hidden="true" />
      </Button>
    </nav>
  )
}

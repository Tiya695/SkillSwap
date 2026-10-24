import { Loader2 } from 'lucide-react'

export default function PageLoader() {
  return (
    <div className="flex min-h-[40vh] items-center justify-center" role="status" aria-label="Loading">
      <Loader2 className="h-6 w-6 animate-spin text-muted" />
    </div>
  )
}

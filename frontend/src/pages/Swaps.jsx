import { useState } from 'react'
import { ArrowLeftRight } from 'lucide-react'
import { listSwaps } from '../api/swaps'
import { useAuth } from '../auth/useAuth'
import SwapActions from '../components/domain/SwapActions'
import SwapCard from '../components/domain/SwapCard'
import PageHeader from '../components/layout/PageHeader'
import Button from '../components/ui/Button'
import Chip from '../components/ui/Chip'
import EmptyState from '../components/ui/EmptyState'
import Pagination from '../components/ui/Pagination'
import { CardSkeletonList } from '../components/ui/Skeleton'
import { statusLabel } from '../utils/format'
import { useFetch } from '../utils/useFetch'

const statuses = ['', 'PENDING', 'ACCEPTED', 'COMPLETED', 'REJECTED', 'CANCELLED']

export default function Swaps() {
  const { user } = useAuth()
  const [box, setBox] = useState('incoming')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)

  const changeBox = (value) => {
    setBox(value)
    setPage(0)
  }
  const changeStatus = (value) => {
    setStatus(value)
    setPage(0)
  }

  const { data, loading, error, reload } = useFetch(
    () => listSwaps({ box, status: status || undefined, page, size: 10 }),
    [box, status, page],
  )

  return (
    <>
      <PageHeader title="Swaps" subtitle="Requests you received and requests you sent." />

      <div className="mb-5 inline-flex rounded-full border border-line bg-white p-1" role="tablist" aria-label="Swap box">
        {[
          ['incoming', 'Incoming'],
          ['outgoing', 'Outgoing'],
        ].map(([value, label]) => (
          <button
            key={value}
            type="button"
            role="tab"
            aria-selected={box === value}
            onClick={() => changeBox(value)}
            className={`rounded-full px-5 py-1.5 text-sm font-semibold transition-colors ${
              box === value ? 'bg-brand text-white shadow-glow' : 'text-ink/70 hover:text-ink'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="mb-8 flex flex-wrap gap-2" role="group" aria-label="Filter by status">
        {statuses.map((s) => (
          <Chip key={s || 'all'} active={status === s} onClick={() => changeStatus(s)}>
            {s ? statusLabel(s) : 'All'}
          </Chip>
        ))}
      </div>

      {loading && <CardSkeletonList count={3} height="h-52" />}
      {error && (
        <EmptyState
          tone="error"
          title="Could not load swaps"
          text={error}
          action={<Button onClick={reload}>Try again</Button>}
        />
      )}
      {!loading && !error && data?.content.length === 0 && (
        <EmptyState
          icon={ArrowLeftRight}
          title={box === 'incoming' ? 'No incoming requests' : 'No outgoing requests'}
          text={
            box === 'incoming'
              ? 'When someone asks to swap with you, the request shows up here.'
              : 'Find a match and send your first swap request.'
          }
          action={box === 'outgoing' ? <Button to="/matches">Find a match</Button> : null}
        />
      )}
      {!loading && !error && data?.content.length > 0 && (
        <>
          <div className="grid gap-4 lg:grid-cols-2">
            {data.content.map((s) => (
              <SwapCard key={s.id} swap={s} meId={user.id} to={`/swaps/${s.id}`}>
                <SwapActions swap={s} meId={user.id} onChanged={reload} />
              </SwapCard>
            ))}
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </>
  )
}

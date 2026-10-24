import { useState } from 'react'
import { Handshake } from 'lucide-react'
import { listMatches } from '../api/matches'
import MatchCard from '../components/domain/MatchCard'
import SwapRequestModal from '../components/domain/SwapRequestModal'
import PageHeader from '../components/layout/PageHeader'
import Button from '../components/ui/Button'
import Chip from '../components/ui/Chip'
import EmptyState from '../components/ui/EmptyState'
import { CardSkeletonList } from '../components/ui/Skeleton'
import { useFetch } from '../utils/useFetch'

export default function Matches() {
  const { data, loading, error, reload } = useFetch(listMatches, [])
  const [filter, setFilter] = useState('ALL')
  const [requesting, setRequesting] = useState(null)

  const matches = (data || []).filter((m) => filter === 'ALL' || m.matchType === 'MUTUAL')
  const mutualCount = (data || []).filter((m) => m.matchType === 'MUTUAL').length

  return (
    <>
      <PageHeader
        title="Your matches"
        subtitle="Mutual matches can teach you something and want something you know."
      />

      <div className="mb-6 flex flex-wrap gap-2" role="group" aria-label="Filter matches">
        <Chip active={filter === 'ALL'} onClick={() => setFilter('ALL')}>
          All {data ? `(${data.length})` : ''}
        </Chip>
        <Chip active={filter === 'MUTUAL'} onClick={() => setFilter('MUTUAL')}>
          Mutual only {data ? `(${mutualCount})` : ''}
        </Chip>
      </div>

      {loading && <CardSkeletonList count={3} height="h-56" />}
      {error && (
        <EmptyState
          tone="error"
          title="Could not load matches"
          text={error}
          action={<Button onClick={reload}>Try again</Button>}
        />
      )}
      {!loading && !error && matches.length === 0 && (
        <EmptyState
          icon={Handshake}
          title={filter === 'MUTUAL' ? 'No mutual matches yet' : 'No matches yet'}
          text="Add the skills you can teach and the skills you want to learn. Matches appear as soon as someone fits."
          action={<Button to="/my-skills">Update my skills</Button>}
        />
      )}
      {!loading && !error && matches.length > 0 && (
        <div className="grid gap-4 lg:grid-cols-2">
          {matches.map((m) => (
            <MatchCard key={m.user.id} match={m} onRequest={setRequesting} />
          ))}
        </div>
      )}

      {requesting && (
        <SwapRequestModal
          receiver={requesting.user}
          suggest={{
            offeredId: requesting.youCanTeachThem[0]?.id,
            wantedId: requesting.theyCanTeachYou[0]?.id,
          }}
          onClose={() => setRequesting(null)}
        />
      )}
    </>
  )
}

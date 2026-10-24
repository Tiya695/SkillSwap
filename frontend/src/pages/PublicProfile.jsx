import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Send } from 'lucide-react'
import { listUserReviews } from '../api/reviews'
import { getProfile } from '../api/users'
import { useAuth } from '../auth/useAuth'
import StarRating from '../components/domain/StarRating'
import SwapRequestModal from '../components/domain/SwapRequestModal'
import Avatar from '../components/ui/Avatar'
import Badge from '../components/ui/Badge'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import EmptyState from '../components/ui/EmptyState'
import { Skeleton } from '../components/ui/Skeleton'
import { formatDate, levelLabel } from '../utils/format'
import { useFetch } from '../utils/useFetch'

export default function PublicProfile() {
  const { id } = useParams()
  const { user: me } = useAuth()
  const [requesting, setRequesting] = useState(false)
  const { data, loading, error } = useFetch(() => Promise.all([getProfile(id), listUserReviews(id)]), [id])
  const profile = data?.[0]
  const reviews = data?.[1] ?? []
  const isMe = profile && profile.user.id === me.id

  return (
    <>
      <Link to="/matches" className="mb-6 inline-flex items-center gap-1.5 text-sm font-semibold text-muted hover:text-ink">
        <ArrowLeft className="h-4 w-4" aria-hidden="true" /> Back
      </Link>

      {loading && <Skeleton className="h-64 rounded-card" />}
      {error && (
        <EmptyState tone="error" title="Could not open this profile" text={error} action={<Button to="/matches">Back to matches</Button>} />
      )}

      {profile && (
        <div className="mx-auto grid max-w-3xl gap-6">
          <Card className="p-6 sm:p-8">
            <div className="flex flex-wrap items-start gap-5">
              <Avatar name={profile.user.name} src={profile.user.avatarUrl} size="xl" />
              <div className="min-w-0 flex-1">
                <h1 className="text-3xl font-extrabold tracking-tightest">{profile.user.name}</h1>
                <div className="mt-2 flex flex-wrap items-center gap-3 text-sm text-muted">
                  {profile.reviewCount > 0 ? (
                    <span className="inline-flex items-center gap-2">
                      <StarRating value={profile.avgRating} size="sm" />
                      <span className="font-semibold text-ink">{profile.avgRating.toFixed(1)}</span>
                      <span>
                        ({profile.reviewCount} review{profile.reviewCount === 1 ? '' : 's'})
                      </span>
                    </span>
                  ) : (
                    <span>No reviews yet</span>
                  )}
                  <span>Member since {formatDate(profile.memberSince)}</span>
                </div>
                {profile.user.bio && <p className="mt-4 text-sm text-ink/80">{profile.user.bio}</p>}
              </div>
              {isMe ? (
                <Button to="/profile" variant="secondary">
                  Edit profile
                </Button>
              ) : (
                <Button onClick={() => setRequesting(true)}>
                  <Send className="h-4 w-4" aria-hidden="true" /> Request a swap
                </Button>
              )}
            </div>
          </Card>

          <div className="grid gap-6 md:grid-cols-2">
            <SkillsCard title="Can teach" skills={profile.offered} empty="Has not listed a skill to teach yet." />
            <SkillsCard title="Wants to learn" skills={profile.wanted} empty="Has not listed a skill to learn yet." />
          </div>

          <section aria-labelledby="reviews">
            <h2 id="reviews" className="mb-4 text-xl font-extrabold tracking-tight">
              Reviews
            </h2>
            {reviews.length === 0 ? (
              <EmptyState title="No reviews yet" text="Reviews appear here after a completed swap." />
            ) : (
              <div className="grid gap-3">
                {reviews.map((r) => (
                  <Card key={r.id} className="p-5">
                    <div className="flex items-center gap-3">
                      <Avatar name={r.reviewer.name} src={r.reviewer.avatarUrl} size="sm" />
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-bold">{r.reviewer.name}</p>
                        <p className="text-xs text-muted">{formatDate(r.createdAt)}</p>
                      </div>
                      <StarRating value={r.rating} size="sm" />
                    </div>
                    {r.comment && <p className="mt-3 text-sm text-ink/80">{r.comment}</p>}
                  </Card>
                ))}
              </div>
            )}
          </section>
        </div>
      )}

      {requesting && <SwapRequestModal receiver={profile.user} onClose={() => setRequesting(false)} />}
    </>
  )
}

function SkillsCard({ title, skills, empty }) {
  return (
    <Card className="p-6">
      <h2 className="mb-4 text-base font-bold">{title}</h2>
      {skills.length === 0 ? (
        <p className="text-sm text-muted">{empty}</p>
      ) : (
        <ul className="grid gap-2.5">
          {skills.map((s) => (
            <li key={s.id} className="flex items-center justify-between gap-3 rounded-xl border border-line px-3.5 py-2.5">
              <span className="min-w-0">
                <span className="block truncate text-sm font-bold">{s.skill.name}</span>
                <span className="block text-xs text-muted">{s.skill.category}</span>
              </span>
              <Badge>{levelLabel(s.level)}</Badge>
            </li>
          ))}
        </ul>
      )}
    </Card>
  )
}

import { useEffect, useState } from 'react'
import { ArrowLeftRight, BookOpen, Clock, GraduationCap, Handshake, Pencil, Plus, Star } from 'lucide-react'
import { Link } from 'react-router-dom'
import { listMatches } from '../api/matches'
import { listMySkills } from '../api/mySkills'
import { listSwaps } from '../api/swaps'
import { getProfile } from '../api/users'
import { useAuth } from '../auth/useAuth'
import AddSkillModal from '../components/domain/AddSkillModal'
import EditUserSkillModal from '../components/domain/EditUserSkillModal'
import PageHeader from '../components/layout/PageHeader'
import MatchCard from '../components/domain/MatchCard'
import StatCard from '../components/domain/StatCard'
import SwapCard from '../components/domain/SwapCard'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import EmptyState from '../components/ui/EmptyState'
import { CardSkeletonList, Skeleton } from '../components/ui/Skeleton'
import { firstName } from '../utils/format'
import { useFetch } from '../utils/useFetch'

export default function Dashboard() {
  const { user } = useAuth()
  const [adding, setAdding] = useState(false)
  const [editing, setEditing] = useState(null)

  const { data, loading, error, reload } = useFetch(
    () =>
      Promise.all([
        listSwaps({ box: 'incoming', size: 100 }),
        listSwaps({ box: 'outgoing', size: 100 }),
        listMatches(),
        getProfile(user.id),
        listMySkills(),
      ]),
    [user.id],
  )

  useEffect(() => {
    const onFocus = () => reload()
    window.addEventListener('focus', onFocus)
    return () => window.removeEventListener('focus', onFocus)
  }, [reload])

  const [incoming, outgoing, matches, profile, mySkills] = data || [null, null, [], null, []]
  const all = [...(incoming?.content || []), ...(outgoing?.content || [])]
  const active = all.filter((s) => s.status === 'ACCEPTED').length
  const pending = (incoming?.content || []).filter((s) => s.status === 'PENDING').length
  const mutual = matches.filter((m) => m.matchType === 'MUTUAL').length
  const recent = [...all].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)).slice(0, 4)
  const offered = mySkills.filter((s) => s.type === 'OFFERED')
  const wanted = mySkills.filter((s) => s.type === 'WANTED')

  const matchHint =
    mutual > 0
      ? `${mutual} mutual`
      : matches.length > 0
        ? `${matches.length} one-way`
        : null

  return (
    <>
      <PageHeader
        title="Dashboard"
        subtitle={`Welcome back, ${firstName(user.name)}`}
        action={
          <Button onClick={() => setAdding(true)}>
            <Plus className="h-4 w-4" aria-hidden="true" /> Add a skill
          </Button>
        }
      />

      {error && (
        <div>
          <EmptyState
            tone="error"
            title="Could not load your dashboard"
            text={error}
            action={<Button onClick={reload}>Try again</Button>}
          />
        </div>
      )}

      {!error && (
        <>
          <div className="scene grid grid-cols-2 gap-4 lg:grid-cols-4">
            <StatCard label="Active swaps" value={active} icon={ArrowLeftRight} loading={loading} delay={0} />
            <StatCard label="Pending requests" value={pending} icon={Clock} loading={loading} delay={80} />
            <StatCard
              label="Matches"
              value={matches.length}
              hint={matchHint}
              icon={Handshake}
              loading={loading}
              delay={160}
            />
            <StatCard
              label="Average rating"
              value={profile?.avgRating != null ? profile.avgRating.toFixed(1) : '-'}
              icon={Star}
              loading={loading}
              delay={240}
            />
          </div>

          <Card tilt gradient className="mt-8 p-6">
            <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="text-xl font-extrabold tracking-tight">Your skills</h2>
                <p className="text-xs text-muted">Click any skill to edit between teaching or learning</p>
              </div>
              <div className="flex gap-2">
                <Button size="sm" onClick={() => setAdding(true)}>
                  <Plus className="h-4 w-4" aria-hidden="true" /> Add skill
                </Button>
                <Button size="sm" variant="secondary" to="/my-skills">
                  Manage
                </Button>
              </div>
            </div>
            {loading ? (
              <Skeleton className="h-20" />
            ) : (
              <div className="grid gap-5 sm:grid-cols-2">
                <SkillGroup icon={GraduationCap} title="I can teach" items={offered} onEdit={setEditing} />
                <SkillGroup icon={BookOpen} title="I want to learn" items={wanted} onEdit={setEditing} />
              </div>
            )}
          </Card>

          <div className="mt-10 grid gap-10 lg:grid-cols-2">
            <section aria-labelledby="top-matches">
              <div className="mb-4 flex items-center justify-between">
                <h2 id="top-matches" className="text-xl font-extrabold tracking-tight">
                  Top matches for you
                </h2>
                <Link to="/matches" className="text-sm font-semibold text-brand hover:underline">
                  See all
                </Link>
              </div>
              {loading ? (
                <CardSkeletonList count={2} height="h-52" />
              ) : matches.length === 0 ? (
                <EmptyState
                  icon={Handshake}
                  title="No matches yet"
                  text="Add skills you can teach and skills you want to learn. Matches appear as soon as someone fits."
                  action={<Button onClick={() => setAdding(true)}>Add your skills</Button>}
                />
              ) : (
                <div className="scene grid gap-4">
                  {matches.slice(0, 3).map((m) => (
                    <MatchCard key={m.user.id} match={m} compact />
                  ))}
                </div>
              )}
            </section>

            <section aria-labelledby="recent-swaps">
              <div className="mb-4 flex items-center justify-between">
                <h2 id="recent-swaps" className="text-xl font-extrabold tracking-tight">
                  Recent swap activity
                </h2>
                <Link to="/swaps" className="text-sm font-semibold text-brand hover:underline">
                  See all
                </Link>
              </div>
              {loading ? (
                <CardSkeletonList count={2} height="h-44" />
              ) : recent.length === 0 ? (
                <EmptyState
                  icon={ArrowLeftRight}
                  title="No swaps yet"
                  text="When you send or receive a swap request it shows up here."
                  action={<Button to="/matches">Find a match</Button>}
                />
              ) : (
                <div className="scene grid gap-4">
                  {recent.map((s) => (
                    <SwapCard key={s.id} swap={s} meId={user.id} to={`/swaps/${s.id}`} />
                  ))}
                </div>
              )}
            </section>
          </div>
        </>
      )}

      {adding && <AddSkillModal onClose={() => setAdding(false)} onSaved={reload} />}
      {editing && (
        <EditUserSkillModal
          userSkill={editing}
          onClose={() => setEditing(null)}
          onSaved={reload}
        />
      )}
    </>
  )
}

function SkillGroup({ icon: Icon, title, items, onEdit }) {
  return (
    <div>
      <p className="mb-2.5 flex items-center gap-2 text-sm font-bold">
        <span className="flex h-7 w-7 items-center justify-center rounded-full bg-brand-100 text-brand-700">
          <Icon className="h-4 w-4" aria-hidden="true" />
        </span>
        {title}
      </p>
      {items.length === 0 ? (
        <p className="text-sm text-muted">Nothing added yet.</p>
      ) : (
        <div className="flex flex-wrap gap-2">
          {items.map((s) => (
            <button
              key={s.id}
              type="button"
              onClick={() => onEdit?.(s)}
              title={`Click to edit ${s.skill.name} (teach or learn, level)`}
              className="group inline-flex items-center gap-1.5 rounded-full border border-line bg-surface/90 px-3 py-1.5 text-xs font-semibold text-ink shadow-xs transition hover:border-brand hover:bg-brand-50/70 hover:text-brand cursor-pointer"
            >
              <span>{s.skill.name}</span>
              <Pencil className="h-3 w-3 text-muted transition group-hover:text-brand" aria-hidden="true" />
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

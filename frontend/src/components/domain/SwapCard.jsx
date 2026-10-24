import { Link } from 'react-router-dom'
import { ArrowLeftRight, ArrowLeft, ArrowRight } from 'lucide-react'
import Avatar from '../ui/Avatar'
import Card from '../ui/Card'
import { formatDate } from '../../utils/format'
import StatusBadge from './StatusBadge'

export default function SwapCard({ swap, meId, to, children, big = false }) {
  const iAmRequester = swap.requester.id === meId
  const me = iAmRequester ? swap.requester : swap.receiver
  const other = iAmRequester ? swap.receiver : swap.requester
  const iTeach = iAmRequester ? swap.skillOffered : swap.skillWanted
  const iLearn = iAmRequester ? swap.skillWanted : swap.skillOffered

  return (
    <Card tilt={!big} className={big ? 'p-6 sm:p-8' : 'p-5'}>
      <div className="flex items-center justify-between gap-3">
        <StatusBadge status={swap.status} />
        <span className="text-xs text-muted">{formatDate(swap.updatedAt || swap.createdAt)}</span>
      </div>

      <div className="mt-4 flex items-center gap-3 sm:gap-5">
        <Person user={me} label="You" big={big} />
        <span
          className={`flex shrink-0 items-center justify-center rounded-full border border-line bg-surface ${big ? 'h-12 w-12' : 'h-9 w-9'}`}
        >
          <ArrowLeftRight className={big ? 'h-5 w-5' : 'h-4 w-4'} aria-label="swaps with" />
        </span>
        <Person user={other} link big={big} />
      </div>

      <div className={`mt-4 grid gap-2 ${big ? 'sm:grid-cols-2' : ''}`}>
        <SkillLine icon={ArrowRight} verb="You teach" skill={iTeach.name} />
        <SkillLine icon={ArrowLeft} verb="You learn" skill={iLearn.name} />
      </div>

      <p className="mt-3 text-xs font-semibold text-muted">
        {swap.durationDays} day{swap.durationDays === 1 ? '' : 's'} of learning
        {swap.endDate && swap.status === 'ACCEPTED' ? ` - ends ${formatDate(swap.endDate)}` : ''}
      </p>

      {swap.message && (
        <p className="mt-4 rounded-xl bg-surface px-4 py-3 text-sm text-ink/80">&ldquo;{swap.message}&rdquo;</p>
      )}

      {(to || children) && (
        <div className="mt-5 flex flex-wrap items-center gap-2">
          {children}
          {to && (
            <Link to={to} className="ml-auto text-xs font-semibold text-muted hover:text-ink">
              View details
            </Link>
          )}
        </div>
      )}
    </Card>
  )
}

function Person({ user, label, link = false, big }) {
  const body = (
    <>
      <Avatar name={user.name} src={user.avatarUrl} size={big ? 'lg' : 'md'} />
      <span className="min-w-0 text-left">
        <span className={`block truncate font-bold ${big ? 'text-base' : 'text-sm'}`}>{label || user.name}</span>
        {label && <span className="block truncate text-xs text-muted">{user.name}</span>}
      </span>
    </>
  )
  const cls = 'flex min-w-0 flex-1 items-center gap-2.5'
  return link ? (
    <Link to={`/users/${user.id}`} className={`${cls} rounded-xl hover:opacity-80`}>
      {body}
    </Link>
  ) : (
    <div className={cls}>{body}</div>
  )
}

function SkillLine({ icon: Icon, verb, skill }) {
  return (
    <div className="flex items-center gap-2 rounded-xl border border-line bg-white px-3.5 py-2.5 text-sm">
      <Icon className="h-4 w-4 shrink-0 text-muted" aria-hidden="true" />
      <span className="text-muted">{verb}</span>
      <span className="truncate font-bold">{skill}</span>
    </div>
  )
}

import { Link } from 'react-router-dom'
import { Send } from 'lucide-react'
import Avatar from '../ui/Avatar'
import Badge from '../ui/Badge'
import Button from '../ui/Button'
import Card from '../ui/Card'
import Chip from '../ui/Chip'

export default function MatchCard({ match, onRequest, compact = false }) {
  const { user, matchType, theyCanTeachYou, youCanTeachThem, score } = match
  const mutual = matchType === 'MUTUAL'

  return (
    <Card tilt className="flex flex-col p-5">
      <div className="flex items-start gap-3">
        <Avatar name={user.name} src={user.avatarUrl} size="lg" />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <h3 className="truncate text-base font-bold">{user.name}</h3>
            <Badge tone={mutual ? 'dark' : 'neutral'}>{mutual ? 'Mutual match' : 'One-way'}</Badge>
          </div>
          {!compact && user.bio && <p className="mt-1 line-clamp-2 text-sm text-muted">{user.bio}</p>}
        </div>
        <div className="text-right">
          <p className="text-2xl font-extrabold leading-none tracking-tightest">{score}</p>
          <p className="text-[11px] text-muted">{score === 1 ? 'skill fits' : 'skills fit'}</p>
        </div>
      </div>

      <div className="mt-4 grid gap-3 text-sm">
        <SkillRow label="They can teach you" skills={theyCanTeachYou} />
        <SkillRow label="You can teach them" skills={youCanTeachThem} />
      </div>

      <div className="mt-5 flex flex-wrap gap-2">
        <Button to={`/users/${user.id}`} variant="secondary" size="sm">
          View profile
        </Button>
        {onRequest && (
          <Button size="sm" onClick={() => onRequest(match)}>
            <Send className="h-3.5 w-3.5" aria-hidden="true" /> Send swap request
          </Button>
        )}
        {!onRequest && (
          <Link to="/matches" className="self-center text-xs font-semibold text-muted hover:text-ink">
            All matches
          </Link>
        )}
      </div>
    </Card>
  )
}

function SkillRow({ label, skills }) {
  return (
    <div>
      <p className="mb-1.5 text-xs font-semibold text-muted">{label}</p>
      <div className="flex flex-wrap gap-1.5">
        {skills.length === 0 ? (
          <span className="text-xs text-muted">Nothing yet</span>
        ) : (
          skills.map((s) => <Chip key={s.id}>{s.name}</Chip>)
        )}
      </div>
    </div>
  )
}

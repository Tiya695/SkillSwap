import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Check, Circle, X } from 'lucide-react'
import { addReview } from '../api/reviews'
import { getSwap } from '../api/swaps'
import { useAuth } from '../auth/useAuth'
import StarRating from '../components/domain/StarRating'
import SwapActions from '../components/domain/SwapActions'
import SwapCard from '../components/domain/SwapCard'
import AssessmentSection from '../components/swap/AssessmentSection'
import CallPanel from '../components/swap/CallPanel'
import SwapChat from '../components/swap/SwapChat'
import SwapProgress from '../components/swap/SwapProgress'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import EmptyState from '../components/ui/EmptyState'
import { Textarea } from '../components/ui/Input'
import { Skeleton } from '../components/ui/Skeleton'
import { useToast } from '../components/ui/toastContext'
import { getErrorMessage, getFieldErrors } from '../utils/errors'
import { formatDate } from '../utils/format'
import { useFetch } from '../utils/useFetch'

export default function SwapDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const { data: swap, loading, error, reload } = useFetch(() => getSwap(id), [id])

  const active = swap?.status === 'ACCEPTED' || swap?.status === 'PENDING'
  useEffect(() => {
    if (!active) return undefined
    const timer = setInterval(reload, 6000)
    return () => clearInterval(timer)
  }, [active, reload])

  const other = swap ? (swap.requester.id === user.id ? swap.receiver : swap.requester) : null
  const working = swap && (swap.status === 'ACCEPTED' || swap.status === 'COMPLETED')

  return (
    <>
      <Link to="/swaps" className="mb-6 inline-flex items-center gap-1.5 text-sm font-semibold text-muted hover:text-ink">
        <ArrowLeft className="h-4 w-4" aria-hidden="true" /> All swaps
      </Link>

      {loading && !swap && <Skeleton className="h-72 rounded-card" />}
      {error && (
        <EmptyState
          tone="error"
          title="Could not open this swap"
          text={error}
          action={<Button to="/swaps">Back to swaps</Button>}
        />
      )}

      {swap && (
        <div className={`mx-auto grid gap-6 ${working ? 'max-w-5xl' : 'max-w-3xl'}`}>
          <SwapCard swap={swap} meId={user.id} big>
            <SwapActions swap={swap} meId={user.id} onChanged={reload} />
          </SwapCard>

          {!working && <Timeline swap={swap} />}

          {working && (
            <>
              <SwapProgress swap={swap} otherName={other.name} onChanged={reload} />

              <div className="grid gap-6 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,1fr)]">
                <SwapChat swapId={swap.id} meId={user.id} canWrite={swap.status === 'ACCEPTED'} />
                <CallPanel swap={swap} canEdit={swap.status === 'ACCEPTED'} onChanged={reload} />
              </div>

              <AssessmentSection swap={swap} otherName={other.name} onSwapChanged={reload} />
            </>
          )}

          {swap.status === 'COMPLETED' &&
            (swap.reviewedByMe ? (
              <Card className="p-6 text-sm text-muted">You have already reviewed this swap. Thank you.</Card>
            ) : (
              <ReviewForm swap={swap} reviewee={other} onDone={reload} />
            ))}
        </div>
      )}
    </>
  )
}

function Timeline({ swap }) {
  const ended = swap.status === 'REJECTED' || swap.status === 'CANCELLED'
  const steps = ended
    ? [
        { label: 'Requested', done: true, date: swap.createdAt },
        { label: swap.status === 'REJECTED' ? 'Rejected' : 'Cancelled', done: true, ended: true, date: swap.updatedAt },
      ]
    : [
        { label: 'Requested', done: true, date: swap.createdAt },
        { label: 'Accepted', done: false },
        { label: 'Final tests passed', done: false },
      ]

  return (
    <Card className="p-6">
      <h2 className="mb-5 text-base font-bold">Progress</h2>
      <ol className="grid gap-5 sm:grid-cols-3">
        {steps.map((s) => (
          <li key={s.label} className="flex items-start gap-3">
            <span
              className={`mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-full border ${
                s.ended
                  ? 'border-status-rejected bg-red-50 text-status-rejected'
                  : s.done
                    ? 'border-brand bg-brand text-white'
                    : 'border-line bg-surface text-muted'
              }`}
            >
              {s.ended ? <X className="h-4 w-4" /> : s.done ? <Check className="h-4 w-4" /> : <Circle className="h-3 w-3" />}
            </span>
            <span>
              <span className={`block text-sm font-bold ${s.done ? '' : 'text-muted'}`}>{s.label}</span>
              {s.date && <span className="block text-xs text-muted">{formatDate(s.date)}</span>}
            </span>
          </li>
        ))}
      </ol>
      {swap.status === 'PENDING' && (
        <p className="mt-5 text-sm text-muted">
          After the request is accepted you get a chat, a video call link and {swap.durationDays} days to learn from each other.
          Then you each give the other a final test.
        </p>
      )}
    </Card>
  )
}

function ReviewForm({ swap, reviewee, onDone }) {
  const toast = useToast()
  const [rating, setRating] = useState(0)
  const [comment, setComment] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setErrors({})
    setFormError('')
    try {
      await addReview(swap.id, { rating, comment })
      toast.success('Review submitted successfully. Thank you!')
      onDone()
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card className="p-6">
      <h2 className="text-base font-bold">How was your swap with {reviewee.name}?</h2>
      <form onSubmit={submit} className="mt-4 grid gap-4">
        <div>
          <StarRating value={rating} onChange={setRating} size="lg" />
          {errors.rating && (
            <p role="alert" className="mt-1 text-xs font-medium text-status-rejected">
              {errors.rating}
            </p>
          )}
        </div>
        <Textarea
          label="Comment (optional)"
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          error={errors.comment}
          maxLength={500}
        />
        {formError && !Object.keys(errors).length && (
          <p role="alert" className="text-sm font-medium text-status-rejected">
            {formError}
          </p>
        )}
        <div>
          <Button type="submit" loading={saving} disabled={rating === 0}>
            Submit review
          </Button>
        </div>
      </form>
    </Card>
  )
}

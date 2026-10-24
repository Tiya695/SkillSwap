import { useState } from 'react'
import { CalendarDays, CheckCircle2, Flag } from 'lucide-react'
import { finishEarly } from '../../api/swaps'
import { getErrorMessage } from '../../utils/errors'
import { formatDate } from '../../utils/format'
import Button from '../ui/Button'
import Card from '../ui/Card'
import { useToast } from '../ui/toastContext'

const DAY = 24 * 60 * 60 * 1000

export default function SwapProgress({ swap, otherName, onChanged }) {
  const toast = useToast()
  const [busy, setBusy] = useState(false)

  const start = swap.startDate ? new Date(swap.startDate + 'T00:00:00') : null
  const end = swap.endDate ? new Date(swap.endDate + 'T00:00:00') : null
  const today = new Date(new Date().toDateString())
  const total = Math.max(swap.durationDays, 1)
  const dayNumber = start ? Math.min(Math.max(Math.floor((today - start) / DAY) + 1, 1), total) : 0
  const daysLeft = end ? Math.max(Math.ceil((end - today) / DAY), 0) : 0
  const percent = swap.status === 'COMPLETED' || swap.assessmentUnlocked ? 100 : Math.round(((dayNumber - 1) / total) * 100)

  const finish = async () => {
    setBusy(true)
    try {
      onChanged(await finishEarly(swap.id))
      toast.success('Saved. The tests open when both of you are ready.')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  const steps = [
    { label: 'Request accepted', done: true },
    { label: 'Learning period', done: swap.assessmentUnlocked || swap.status === 'COMPLETED' },
    { label: 'Final tests', done: swap.status === 'COMPLETED' },
    { label: 'Completed', done: swap.status === 'COMPLETED' },
  ]

  return (
    <Card className="p-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold">Learning period</h2>
          <p className="mt-1 flex items-center gap-1.5 text-sm text-muted">
            <CalendarDays className="h-4 w-4" aria-hidden="true" />
            {formatDate(swap.startDate)} to {formatDate(swap.endDate)} ({swap.durationDays} days)
          </p>
        </div>
        {swap.status === 'ACCEPTED' && !swap.assessmentUnlocked && (
          <p className="rounded-full bg-brand-100 px-3.5 py-1.5 text-sm font-bold text-brand-700">
            Day {dayNumber} of {total} - {daysLeft} day{daysLeft === 1 ? '' : 's'} left
          </p>
        )}
      </div>

      <div className="mt-5 h-2.5 overflow-hidden rounded-full bg-brand-100" role="progressbar" aria-valuenow={percent} aria-valuemin={0} aria-valuemax={100} aria-label="Learning period progress">
        <div className="h-full rounded-full bg-gradient-to-r from-brand-400 to-brand transition-all" style={{ width: `${percent}%` }} />
      </div>

      <ol className="mt-5 grid gap-3 sm:grid-cols-4">
        {steps.map((s) => (
          <li key={s.label} className={`flex items-center gap-2 text-sm ${s.done ? 'font-bold text-ink' : 'text-muted'}`}>
            <CheckCircle2 className={`h-5 w-5 shrink-0 ${s.done ? 'text-brand' : 'text-line'}`} aria-hidden="true" />
            {s.label}
          </li>
        ))}
      </ol>

      {swap.status === 'ACCEPTED' && !swap.assessmentUnlocked && (
        <div className="mt-6 flex flex-wrap items-center gap-3 border-t border-line pt-5">
          {swap.finishedEarlyByMe ? (
            <p className="text-sm text-muted">
              You said you are ready. {swap.finishedEarlyByOther ? '' : `Waiting for ${otherName} to say the same.`}
            </p>
          ) : (
            <>
              <Button variant="secondary" size="sm" loading={busy} onClick={finish}>
                <Flag className="h-4 w-4" aria-hidden="true" /> We are ready early
              </Button>
              <p className="text-xs text-muted">
                {swap.finishedEarlyByOther
                  ? `${otherName} is ready. Press this to open the final tests now.`
                  : 'The final tests open on the end date, or sooner when both of you press this.'}
              </p>
            </>
          )}
        </div>
      )}
    </Card>
  )
}

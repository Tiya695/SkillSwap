import { useState } from 'react'
import { Link } from 'react-router-dom'
import { listMySkills } from '../../api/mySkills'
import { createSwap } from '../../api/swaps'
import { getProfile } from '../../api/users'
import { getErrorMessage, getFieldErrors } from '../../utils/errors'
import { useFetch } from '../../utils/useFetch'
import Button from '../ui/Button'
import { Input, Select, Textarea } from '../ui/Input'
import Modal from '../ui/Modal'
import { Skeleton } from '../ui/Skeleton'
import { useToast } from '../ui/toastContext'

export default function SwapRequestModal({ receiver, suggest = {}, onClose, onSent }) {
  const toast = useToast()
  const [offeredId, setOfferedId] = useState(suggest.offeredId ?? '')
  const [wantedId, setWantedId] = useState(suggest.wantedId ?? '')
  const [message, setMessage] = useState('')
  const [durationDays, setDurationDays] = useState('14')
  const [sending, setSending] = useState(false)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')

  const { data, loading, error } = useFetch(
    () => Promise.all([listMySkills('OFFERED'), getProfile(receiver.id)]),
    [receiver.id],
  )
  const myOffered = data?.[0] ?? []
  const theirOffered = data?.[1]?.offered ?? []

  const offeredValue = offeredId || myOffered[0]?.skill.id || ''
  const wantedValue = wantedId || theirOffered[0]?.skill.id || ''

  const submit = async (e) => {
    e.preventDefault()
    setSending(true)
    setErrors({})
    setFormError('')
    try {
      const swap = await createSwap({
        receiverId: receiver.id,
        skillOfferedId: Number(offeredValue),
        skillWantedId: Number(wantedValue),
        durationDays: durationDays === '' ? null : Number(durationDays),
        message,
      })
      toast.success(`Swap request sent to ${receiver.name} successfully`)
      onSent?.(swap)
      onClose()
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
    } finally {
      setSending(false)
    }
  }

  const blocked = !loading && !error && (myOffered.length === 0 || theirOffered.length === 0)

  return (
    <Modal
      open
      onClose={onClose}
      title={`Swap with ${receiver.name}`}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="swap-request-form" loading={sending} disabled={loading || blocked || Boolean(error)}>
            Send request
          </Button>
        </>
      }
    >
      {loading && (
        <div className="grid gap-4">
          <Skeleton className="h-16" />
          <Skeleton className="h-16" />
          <Skeleton className="h-24" />
        </div>
      )}
      {error && <p className="text-sm font-medium text-status-rejected">{error}</p>}
      {blocked && (
        <p className="text-sm text-muted">
          {myOffered.length === 0 ? (
            <>
              You need at least one skill you can teach before you can swap.{' '}
              <Link to="/my-skills" className="font-semibold text-ink underline">
                Add one in My Skills
              </Link>
              .
            </>
          ) : (
            <>{receiver.name} has not listed a skill to teach yet, so there is nothing to swap for.</>
          )}
        </p>
      )}
      {!loading && !error && !blocked && (
        <form id="swap-request-form" onSubmit={submit} className="grid gap-4">
          <Select
            label="You will teach"
            value={offeredValue}
            onChange={(e) => setOfferedId(e.target.value)}
            error={errors.skillOfferedId}
          >
            {myOffered.map((us) => (
              <option key={us.id} value={us.skill.id}>
                {us.skill.name}
              </option>
            ))}
          </Select>
          <Select
            label={`You want to learn from ${receiver.name}`}
            value={wantedValue}
            onChange={(e) => setWantedId(e.target.value)}
            error={errors.skillWantedId}
          >
            {theirOffered.map((us) => (
              <option key={us.id} value={us.skill.id}>
                {us.skill.name}
              </option>
            ))}
          </Select>
          <Input
            label="How many days will you teach each other?"
            type="number"
            min={1}
            max={365}
            value={durationDays}
            onChange={(e) => setDurationDays(e.target.value)}
            error={errors.durationDays}
            hint="After these days you each give the other a short final test"
            required
          />
          <Textarea
            label="Message (optional)"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            error={errors.message}
            maxLength={500}
            placeholder="Say hello and suggest how you could start"
          />
          {formError && !Object.keys(errors).length && (
            <p role="alert" className="text-sm font-medium text-status-rejected">
              {formError}
            </p>
          )}
        </form>
      )}
    </Modal>
  )
}

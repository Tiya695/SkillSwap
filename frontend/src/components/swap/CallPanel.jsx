import { useState } from 'react'
import { ExternalLink, Video } from 'lucide-react'
import { setMeetingLink } from '../../api/swaps'
import { getErrorMessage, getFieldErrors } from '../../utils/errors'
import Button from '../ui/Button'
import Card from '../ui/Card'
import { Input } from '../ui/Input'
import { useToast } from '../ui/toastContext'

export default function CallPanel({ swap, canEdit, onChanged }) {
  const toast = useToast()
  const [editing, setEditing] = useState(false)
  const [url, setUrl] = useState(swap.meetingUrl || '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const save = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      onChanged(await setMeetingLink(swap.id, url))
      toast.success('Call link updated successfully for both of you')
      setEditing(false)
    } catch (err) {
      setError(getFieldErrors(err).url || getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card gradient className="flex h-[440px] flex-col p-6">
      <h2 className="flex items-center gap-2 text-lg font-bold">
        <Video className="h-5 w-5 text-brand" aria-hidden="true" /> Video call
      </h2>
      <p className="mt-2 text-sm text-muted">
        Meet face to face to teach and learn. Both of you use the same link, so open it at the time you agree in the chat.
      </p>

      <div className="mt-5 rounded-2xl border border-line bg-white p-4">
        <p className="text-xs font-semibold text-muted">Call link</p>
        <p className="mt-1 break-all text-sm font-semibold">{swap.meetingUrl}</p>
      </div>

      <div className="mt-5 flex flex-wrap gap-2">
        <Button href={swap.meetingUrl} target="_blank" rel="noopener noreferrer" aria-label="Join the video call (opens in a new tab)">
          <ExternalLink className="h-4 w-4" aria-hidden="true" /> Join the call
        </Button>
        {canEdit && !editing && (
          <Button variant="secondary" onClick={() => setEditing(true)}>
            Use my own link
          </Button>
        )}
      </div>

      {editing && (
        <form onSubmit={save} className="mt-5 grid gap-3">
          <Input
            label="Your meeting link"
            type="url"
            placeholder="https://meet.google.com/..."
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            error={error}
            required
          />
          <div className="flex gap-2">
            <Button type="submit" size="sm" loading={saving}>
              Save link
            </Button>
            <Button variant="secondary" size="sm" onClick={() => setEditing(false)}>
              Cancel
            </Button>
          </div>
        </form>
      )}

      <p className="mt-auto pt-5 text-xs text-muted">
        The default room is on Jitsi Meet. If it asks the first person to sign in, paste a Google Meet or Zoom link instead.
      </p>
    </Card>
  )
}

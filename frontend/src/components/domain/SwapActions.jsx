import { useState } from 'react'
import { Check, X } from 'lucide-react'
import { updateSwapStatus } from '../../api/swaps'
import { getErrorMessage } from '../../utils/errors'
import Button from '../ui/Button'
import { useToast } from '../ui/toastContext'

export default function SwapActions({ swap, meId, onChanged }) {
  const toast = useToast()
  const [busy, setBusy] = useState(null)
  const isReceiver = swap.receiver.id === meId
  const isRequester = swap.requester.id === meId

  const change = async (status, doneMessage) => {
    setBusy(status)
    try {
      const updated = await updateSwapStatus(swap.id, status)
      toast.success(doneMessage)
      onChanged?.(updated)
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setBusy(null)
    }
  }

  const disabled = busy !== null

  if (swap.status === 'PENDING' && isReceiver) {
    return (
      <>
        <Button size="sm" loading={busy === 'ACCEPTED'} disabled={disabled} onClick={() => change('ACCEPTED', 'Request accepted successfully')}>
          <Check className="h-4 w-4" aria-hidden="true" /> Accept
        </Button>
        <Button
          size="sm"
          variant="secondary"
          loading={busy === 'REJECTED'}
          disabled={disabled}
          onClick={() => change('REJECTED', 'Request rejected successfully')}
        >
          <X className="h-4 w-4" aria-hidden="true" /> Reject
        </Button>
      </>
    )
  }
  if (swap.status === 'PENDING' && isRequester) {
    return (
      <Button
        size="sm"
        variant="secondary"
        loading={busy === 'CANCELLED'}
        disabled={disabled}
        onClick={() => change('CANCELLED', 'Request cancelled successfully')}
      >
        Cancel request
      </Button>
    )
  }
  if (swap.status === 'ACCEPTED') {

    return (
      <Button
        size="sm"
        variant="danger"
        loading={busy === 'CANCELLED'}
        disabled={disabled}
        onClick={() => {
          if (window.confirm('Cancel this swap? Both of you will lose the chat and tests.')) {
            change('CANCELLED', 'Swap cancelled successfully')
          }
        }}
      >
        Cancel swap
      </Button>
    )
  }
  return null
}

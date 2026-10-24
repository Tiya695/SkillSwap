import Badge from '../ui/Badge'
import { statusLabel } from '../../utils/format'

const tones = {
  PENDING: 'pending',
  ACCEPTED: 'accepted',
  COMPLETED: 'completed',
  REJECTED: 'rejected',
  CANCELLED: 'cancelled',
}

export default function StatusBadge({ status }) {
  return <Badge tone={tones[status] || 'neutral'}>{statusLabel(status)}</Badge>
}

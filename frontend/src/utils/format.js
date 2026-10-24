export function formatDate(value) {
  if (!value) return ''
  const plain = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)
  const date = plain ? new Date(Number(plain[1]), Number(plain[2]) - 1, Number(plain[3])) : new Date(value)
  return date.toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })
}

export function levelLabel(level) {
  return { BEGINNER: 'Beginner', INTERMEDIATE: 'Intermediate', ADVANCED: 'Advanced' }[level] || level
}

export function statusLabel(status) {
  return (
    {
      PENDING: 'Pending',
      ACCEPTED: 'Accepted',
      REJECTED: 'Rejected',
      CANCELLED: 'Cancelled',
      COMPLETED: 'Completed',
    }[status] || status
  )
}

export function assetUrl(path) {
  if (!path) return null
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  if (path.startsWith('/api/') && /^https?:\/\//.test(base)) return new URL(base).origin + path
  return path
}

export function firstName(name) {
  return name ? name.trim().split(/\s+/)[0] : ''
}

export function initials(name) {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/)
  return (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase()
}

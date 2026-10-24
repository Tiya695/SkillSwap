import { useState } from 'react'
import { assetUrl, initials } from '../../utils/format'

const sizes = {
  xs: 'h-6 w-6 text-[10px]',
  sm: 'h-8 w-8 text-xs',
  md: 'h-10 w-10 text-sm',
  lg: 'h-14 w-14 text-lg',
  xl: 'h-24 w-24 text-3xl',
}

export default function Avatar({ name, src, size = 'md', className = '' }) {
  const [failed, setFailed] = useState(null)
  const base = `inline-flex shrink-0 items-center justify-center rounded-full font-bold ${sizes[size]} ${className}`
  const url = assetUrl(src)

  if (url && failed !== url) {
    return (
      <img
        src={url}
        alt={name ? `${name}'s photo` : 'Profile photo'}
        onError={() => setFailed(url)}
        className={`${base} border border-line object-cover`}
      />
    )
  }
  return (
    <span className={`${base} border border-brand-200 bg-brand-100 text-brand-700`} role="img" aria-label={name || 'User'}>
      {initials(name)}
    </span>
  )
}

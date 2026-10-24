import { useTilt } from '../../utils/useTilt'

export default function Card({ children, gradient = false, tilt = false, className = '', as: Tag = 'div', ...rest }) {
  const tiltProps = useTilt()
  const base = `rounded-card border border-line shadow-soft ${gradient ? 'card-gradient' : 'bg-white'}`

  if (!tilt) {
    return (
      <Tag className={`${base} ${className}`} {...rest}>
        {children}
      </Tag>
    )
  }
  return (
    <Tag className={`tilt-card relative ${base} ${className}`} {...tiltProps} {...rest}>
      {children}
      <span className="tilt-glare" aria-hidden="true" />
    </Tag>
  )
}

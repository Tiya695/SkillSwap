import { Link } from 'react-router-dom'
import { ArrowLeftRight } from 'lucide-react'

export default function Logo({ to = '/' }) {
  return (
    <Link to={to} className="inline-flex items-center gap-2 font-extrabold tracking-tight" aria-label="SkillSwap home">
      <span className="icon-3d flex h-8 w-8 items-center justify-center rounded-[10px] text-white">
        <ArrowLeftRight className="h-4 w-4" aria-hidden="true" />
      </span>
      <span className="text-lg">SkillSwap</span>
    </Link>
  )
}

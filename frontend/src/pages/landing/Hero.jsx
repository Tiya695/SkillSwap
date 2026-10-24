import { Camera, Code, Dumbbell, Languages, Music, PenTool } from 'lucide-react'
import Button from '../../components/ui/Button'

const floating = [
  { label: 'Python', icon: Code, pos: 'left-[4%] top-[8%]', tone: 'bg-brand-100 text-brand-700', delay: '0s' },
  { label: 'Spanish', icon: Languages, pos: 'left-[6%] top-[50%]', tone: 'bg-pink-100 text-pink-600', delay: '-1.5s' },
  { label: 'UI Design', icon: PenTool, pos: 'left-[2%] top-[78%]', tone: 'bg-sky-100 text-sky-600', delay: '-3s' },
  { label: 'Guitar', icon: Music, pos: 'right-[5%] top-[10%]', tone: 'bg-amber-100 text-amber-600', delay: '-2s' },
  { label: 'Photography', icon: Camera, pos: 'right-[5%] top-[48%]', tone: 'bg-emerald-100 text-emerald-600', delay: '-4s' },
  { label: 'Yoga', icon: Dumbbell, pos: 'right-[2%] top-[76%]', tone: 'bg-brand-100 text-brand-700', delay: '-5s' },
]

export default function Hero() {
  return (
    <div className="relative mx-auto max-w-6xl px-4 pb-10 pt-14 text-center sm:px-6 sm:pt-20">
      <div className="pointer-events-none absolute inset-0 hidden lg:block" aria-hidden="true">
        {floating.map(({ label, icon: Icon, pos, tone, delay }) => (
          <span
            key={label}
            style={{ animationDelay: delay }}
            className={`absolute inline-flex animate-float items-center gap-2 rounded-full border border-line bg-white py-1.5 pl-1.5 pr-3.5 text-xs font-semibold shadow-soft ${pos}`}
          >
            <span className={`flex h-6 w-6 items-center justify-center rounded-full ${tone}`}>
              <Icon className="h-3.5 w-3.5" />
            </span>
            {label}
          </span>
        ))}
      </div>

      <h1 className="relative mx-auto max-w-3xl text-[2.6rem] font-extrabold leading-[1.05] tracking-tightest sm:text-6xl">
        Teach What You Know.
        <br />
        Learn What You Love.
      </h1>
      <p className="relative mx-auto mt-5 max-w-xl text-sm text-muted sm:text-base">
        Swap skills with people who want what you know. List what you can teach and what you want to learn, and
        SkillSwap finds the match.
      </p>
      <div className="relative mt-8 flex flex-wrap items-center justify-center gap-3">
        <Button to="/register" size="lg">
          Get Started Free
        </Button>
        <Button to="/login" variant="secondary" size="lg">
          Log in
        </Button>
      </div>
    </div>
  )
}

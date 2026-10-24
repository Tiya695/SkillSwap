import { Check, ChevronRight } from 'lucide-react'
import Avatar from '../../components/ui/Avatar'
import Badge from '../../components/ui/Badge'
import Card from '../../components/ui/Card'
import Chip from '../../components/ui/Chip'
import StarRating from '../../components/domain/StarRating'

export default function Features() {
  return (
    <section id="features" className="mx-auto max-w-6xl scroll-mt-20 px-4 pb-20 sm:px-6">
      <div className="text-center">
        <h2 className="section-title">Everything You Need To Swap Skills</h2>
        <p className="section-sub">From finding the right person to saying thanks, every step has its place.</p>
      </div>

      <div className="mt-12 grid gap-5 md:grid-cols-6">
        <FeatureCard
          className="md:col-span-2"
          title="Smart Mutual Matching"
          text="We pair you with people who can teach what you want and want what you teach."
        >
          <div className="rounded-2xl border border-line bg-white p-4">
            <div className="flex items-center gap-3">
              <Avatar name="Rahul Mehta" size="md" />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-bold">Rahul Mehta</p>
                <Badge tone="dark">Mutual match</Badge>
              </div>
            </div>
            <div className="mt-3 grid gap-2 text-xs">
              <div className="flex items-center justify-between gap-2">
                <span className="text-muted">Teaches</span>
                <Chip>Guitar</Chip>
              </div>
              <div className="flex items-center justify-between gap-2">
                <span className="text-muted">Wants</span>
                <Chip>Python</Chip>
              </div>
            </div>
          </div>
        </FeatureCard>

        <FeatureCard
          className="md:col-span-2"
          title="Easy Swap Requests"
          text="Send a request in a few clicks. The other person accepts or declines."
        >
          <div className="rounded-2xl border border-line bg-white p-4">
            <p className="text-sm font-bold">Python for Guitar</p>
            <p className="mt-0.5 text-xs text-muted">Waiting for your answer</p>
            <div className="mt-4 flex gap-2">
              <span className="inline-flex h-8 flex-1 items-center justify-center rounded-full bg-brand text-xs font-semibold text-white">
                Accept
              </span>
              <span className="inline-flex h-8 flex-1 items-center justify-center rounded-full border border-line text-xs font-semibold">
                Reject
              </span>
            </div>
          </div>
        </FeatureCard>

        <FeatureCard
          className="md:col-span-2"
          title="Reviews And Ratings"
          text="After a swap is complete, rate each other so the next person knows who to trust."
        >
          <div className="rounded-2xl border border-line bg-white p-4">
            <p className="text-xs font-semibold text-muted">Your rating</p>
            <div className="mt-1.5">
              <StarRating value={4} size="lg" />
            </div>
            <div className="mt-3 grid gap-1.5">
              <div className="h-2 w-full rounded-full bg-surface" />
              <div className="h-2 w-2/3 rounded-full bg-surface" />
            </div>
          </div>
        </FeatureCard>

        <FeatureCard
          className="md:col-span-4"
          title="Track Your Swaps"
          text="See where every swap stands, from the first request to the final review."
        >
          <div className="grid gap-2.5 rounded-2xl border border-line bg-white p-4 sm:grid-cols-4 sm:gap-0">
            {['Request sent', 'Accepted', 'Swap completed', 'Review left'].map((step, i) => (
              <div key={step} className="flex items-center gap-2.5 sm:flex-col sm:gap-2 sm:text-center">
                <span
                  className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full border text-xs font-bold ${
                    i < 3 ? 'border-brand bg-brand text-white' : 'border-line bg-surface text-muted'
                  }`}
                >
                  {i < 3 ? <Check className="h-4 w-4" aria-hidden="true" /> : i + 1}
                </span>
                <span className="text-xs font-semibold">{step}</span>
              </div>
            ))}
          </div>
        </FeatureCard>

        <FeatureCard
          className="md:col-span-2"
          title="Skill Profiles"
          text="Show what you can teach and what you want to learn, with your level for each."
        >
          <div className="grid gap-3 rounded-2xl border border-line bg-white p-4">
            <LevelRow name="Python" level={3} label="Advanced" />
            <LevelRow name="Yoga" level={1} label="Beginner" />
            <div className="flex items-center justify-between border-t border-line pt-3 text-xs">
              <span className="text-muted">Wants to learn</span>
              <span className="inline-flex items-center gap-1 font-semibold">
                Guitar <ChevronRight className="h-3 w-3" aria-hidden="true" />
              </span>
            </div>
          </div>
        </FeatureCard>
      </div>
    </section>
  )
}

function FeatureCard({ title, text, children, className = '' }) {
  return (
    <Card tilt gradient className={`flex flex-col p-6 ${className}`}>
      <h3 className="text-base font-bold">{title}</h3>
      <p className="mt-1.5 text-sm text-muted">{text}</p>
      <div className="mt-6 flex-1" aria-hidden="true">
        {children}
      </div>
    </Card>
  )
}

function LevelRow({ name, level, label }) {
  return (
    <div>
      <div className="mb-1.5 flex items-center justify-between text-xs">
        <span className="font-bold">{name}</span>
        <span className="text-muted">{label}</span>
      </div>
      <div className="grid grid-cols-3 gap-1.5">
        {[1, 2, 3].map((n) => (
          <span key={n} className={`h-1.5 rounded-full ${n <= level ? 'bg-brand' : 'bg-brand-100'}`} />
        ))}
      </div>
    </div>
  )
}

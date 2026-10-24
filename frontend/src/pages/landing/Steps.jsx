import Badge from '../../components/ui/Badge'
import Card from '../../components/ui/Card'
import Chip from '../../components/ui/Chip'

const steps = [
  { n: '01', title: 'List your skills', text: 'Add what you can teach and what you want to learn, with your level for each.' },
  { n: '02', title: 'Get matched', text: 'We find people who can teach what you want and want what you know.' },
  { n: '03', title: 'Swap and learn', text: 'Send a request, agree on a plan, then rate each other when you are done.' },
]

export default function Steps() {
  return (
    <section id="how-it-works" className="mx-auto max-w-6xl scroll-mt-20 px-4 pb-20 sm:px-6">
      <div className="text-center">
        <h2 className="section-title">Get Started In 3 Easy Steps</h2>
        <p className="section-sub">Set up your profile in minutes and start swapping.</p>
      </div>

      <div className="mt-12 grid gap-5 lg:grid-cols-[1.05fr_1fr]">

        <div
          className="rounded-[28px] border border-line bg-gradient-to-b from-surface to-white p-5 sm:p-8"
          aria-hidden="true"
        >
          <div className="rounded-2xl border border-line bg-white shadow-float">
            <div className="flex items-center gap-1.5 border-b border-line px-4 py-3">
              <span className="h-2.5 w-2.5 rounded-full bg-line" />
              <span className="h-2.5 w-2.5 rounded-full bg-line" />
              <span className="h-2.5 w-2.5 rounded-full bg-line" />
              <span className="ml-3 text-[11px] font-semibold text-muted">My Skills</span>
            </div>
            <div className="grid gap-5 p-5 sm:grid-cols-2">
              <SkillList
                title="I can teach"
                items={[
                  ['Python', 'Advanced'],
                  ['Yoga', 'Beginner'],
                ]}
              />
              <SkillList
                title="I want to learn"
                items={[
                  ['Guitar', 'Beginner'],
                  ['Spanish', 'Beginner'],
                ]}
              />
            </div>
            <div className="border-t border-line p-5">
              <p className="text-xs font-semibold text-muted">Best match for you</p>
              <div className="mt-2 flex items-center justify-between rounded-xl bg-surface px-3.5 py-2.5">
                <span className="text-sm font-bold">Guitar for Python</span>
                <Badge tone="dark">Mutual</Badge>
              </div>
            </div>
          </div>
        </div>

        <div className="grid content-center gap-4">
          {steps.map((s) => (
            <Card key={s.n} gradient className="flex items-start gap-4 p-5 sm:p-6">
              <span className="flex h-10 w-10 shrink-0 items-center justify-center icon-3d rounded-full text-sm font-bold text-white">
                {s.n}
              </span>
              <div>
                <h3 className="text-base font-bold">{s.title}</h3>
                <p className="mt-1 text-sm text-muted">{s.text}</p>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </section>
  )
}

function SkillList({ title, items }) {
  return (
    <div>
      <p className="mb-2.5 text-xs font-bold">{title}</p>
      <div className="grid gap-2">
        {items.map(([name, level]) => (
          <div key={name} className="flex items-center justify-between rounded-xl border border-line px-3 py-2">
            <span className="text-sm font-semibold">{name}</span>
            <Chip>{level}</Chip>
          </div>
        ))}
      </div>
    </div>
  )
}

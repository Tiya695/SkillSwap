import { ArrowLeftRight, LayoutGrid, List, Search } from 'lucide-react'
import Avatar from '../../components/ui/Avatar'
import Logo from '../../components/layout/Logo'

const columns = [
  {
    title: 'Pending',
    dot: 'bg-status-pending',
    cards: [
      { a: 'Tiya', b: 'Rahul', teach: 'Python', learn: 'Guitar' },
      { a: 'Isha', b: 'Kabir', teach: 'Baking', learn: 'Running' },
    ],
  },
  {
    title: 'Accepted',
    dot: 'bg-status-accepted',
    cards: [
      { a: 'Meera', b: 'Tiya', teach: 'Spanish', learn: 'Python' },
      { a: 'Dev', b: 'Sana', teach: 'Photography', learn: 'Yoga' },
    ],
  },
  {
    title: 'Completed',
    dot: 'bg-status-completed',
    cards: [
      { a: 'Arjun', b: 'Meera', teach: 'Java', learn: 'UI Design' },
      { a: 'Sana', b: 'Isha', teach: 'French', learn: 'Singing' },
    ],
  },
  {
    title: 'Cancelled',
    dot: 'bg-status-cancelled',
    cards: [{ a: 'Kabir', b: 'Dev', teach: 'Piano', learn: 'JavaScript' }],
  },
]

const people = ['Tiya', 'Rahul', 'Meera', 'Dev']

export default function AppPreview() {
  return (
    <div className="mx-auto max-w-5xl px-4 sm:px-6" aria-hidden="true">
      <div className="overflow-hidden rounded-[28px] border border-line bg-white/70 p-2 shadow-float backdrop-blur sm:p-3">
        <div className="rounded-[22px] border border-line bg-white">

          <div className="flex items-center justify-between gap-4 border-b border-line px-4 py-3 sm:px-5">
            <div className="flex items-center gap-6">
              <Logo to="/" />
              <div className="hidden items-center gap-4 text-[11px] font-semibold text-muted md:flex">
                <span className="rounded-full bg-brand px-3 py-1 text-white">Swaps</span>
                <span>Matches</span>
                <span>My Skills</span>
                <span>Browse</span>
              </div>
            </div>
            <div className="flex -space-x-2">
              {people.map((p) => (
                <Avatar key={p} name={p} size="xs" className="ring-2 ring-white" />
              ))}
            </div>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3 px-4 pt-4 sm:px-5">
            <p className="text-sm font-extrabold tracking-tight">Swap Board</p>
            <div className="flex items-center gap-2 text-[11px] font-semibold">
              <span className="inline-flex items-center gap-1 rounded-full bg-brand px-3 py-1 text-white">
                <LayoutGrid className="h-3 w-3" /> Board
              </span>
              <span className="inline-flex items-center gap-1 rounded-full border border-line px-3 py-1 text-muted">
                <List className="h-3 w-3" /> List
              </span>
              <span className="hidden items-center gap-1.5 rounded-full border border-line px-3 py-1 text-muted sm:inline-flex">
                <Search className="h-3 w-3" /> Search swaps
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3 p-4 sm:p-5 lg:grid-cols-4">
            {columns.map((col) => (
              <div key={col.title} className="min-w-0 rounded-2xl bg-surface/70 p-2.5">
                <div className="mb-2.5 flex items-center justify-between px-1.5 text-[11px] font-bold">
                  <span className="inline-flex items-center gap-1.5">
                    <span className={`h-2 w-2 rounded-full ${col.dot}`} />
                    {col.title}
                  </span>
                  <span className="text-muted">{col.cards.length}</span>
                </div>
                <div className="grid gap-2.5">
                  {col.cards.map((c) => (
                    <MiniSwap key={c.a + c.b} {...c} />
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

function MiniSwap({ a, b, teach, learn }) {
  return (
    <div className="min-w-0 overflow-hidden rounded-xl border border-line bg-white p-2.5 text-left shadow-soft">
      <div className="flex items-center gap-1.5">
        <Avatar name={a} size="xs" />
        <ArrowLeftRight className="h-3 w-3 shrink-0 text-muted" />
        <Avatar name={b} size="xs" />
        <span className="ml-1 hidden min-w-0 truncate text-[10px] font-semibold text-muted sm:block">
          {a} &amp; {b}
        </span>
      </div>
      <p className="mt-2 text-[11px] font-bold leading-tight">
        {teach} <span className="font-medium text-muted">for</span> {learn}
      </p>
    </div>
  )
}

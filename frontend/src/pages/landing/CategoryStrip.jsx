import { ChefHat, Code, Dumbbell, Languages, Music, Palette } from 'lucide-react'

const categories = [
  { name: 'Programming', icon: Code },
  { name: 'Music', icon: Music },
  { name: 'Languages', icon: Languages },
  { name: 'Design', icon: Palette },
  { name: 'Cooking', icon: ChefHat },
  { name: 'Fitness', icon: Dumbbell },
]

export default function CategoryStrip() {
  return (
    <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
      <div className="flex flex-col items-start gap-5 md:flex-row md:items-center">
        <p className="max-w-[10rem] shrink-0 text-xs font-medium leading-snug text-muted">
          People swap skills in these categories and more
        </p>
        <div className="grid w-full flex-1 grid-cols-2 gap-3 rounded-[28px] border border-line bg-white p-3 shadow-soft sm:grid-cols-3 lg:grid-cols-6">
          {categories.map(({ name, icon: Icon }) => (
            <span
              key={name}
              className="inline-flex h-11 items-center justify-center gap-2 rounded-full border border-line bg-white px-3 text-sm font-bold"
            >
              <Icon className="h-4 w-4" aria-hidden="true" />
              {name}
            </span>
          ))}
        </div>
      </div>
    </section>
  )
}

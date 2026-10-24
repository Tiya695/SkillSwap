import { useState } from 'react'
import { Plus, Search, SearchX } from 'lucide-react'
import { listCategories, listSkills } from '../api/skills'
import AddSkillModal from '../components/domain/AddSkillModal'
import SkillCard from '../components/domain/SkillCard'
import PageHeader from '../components/layout/PageHeader'
import Button from '../components/ui/Button'
import Chip from '../components/ui/Chip'
import EmptyState from '../components/ui/EmptyState'
import Pagination from '../components/ui/Pagination'
import { CardSkeletonList } from '../components/ui/Skeleton'
import { useDebounced, useFetch } from '../utils/useFetch'

export default function Skills() {
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [page, setPage] = useState(0)
  const [adding, setAdding] = useState(null)
  const q = useDebounced(search)

  const changeSearch = (value) => {
    setSearch(value)
    setPage(0)
  }
  const changeCategory = (value) => {
    setCategory(value)
    setPage(0)
  }

  const { data: categories, reload: reloadCategories } = useFetch(listCategories, [])
  const { data, loading, error, reload } = useFetch(
    () => listSkills({ q, category, page, size: 12 }),
    [q, category, page],
  )

  return (
    <>
      <PageHeader
        title="Browse skills"
        subtitle="Find a skill, then add it to what you can teach or want to learn."
        action={
          <Button onClick={() => setAdding({ create: true })}>
            <Plus className="h-4 w-4" aria-hidden="true" /> Add a new skill
          </Button>
        }
      />

      <div className="relative mb-4 max-w-md">
        <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden="true" />
        <input
          type="search"
          value={search}
          onChange={(e) => changeSearch(e.target.value)}
          placeholder="Search skills"
          aria-label="Search skills"
          className="w-full rounded-full border border-line bg-white py-2.5 pl-11 pr-4 text-sm placeholder:text-muted/70 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/15"
        />
      </div>

      <div className="mb-8 flex flex-wrap gap-2" role="group" aria-label="Filter by category">
        <Chip active={category === ''} onClick={() => changeCategory('')}>
          All
        </Chip>
        {(categories || []).map((c) => (
          <Chip key={c} active={category === c} onClick={() => changeCategory(c)}>
            {c}
          </Chip>
        ))}
      </div>

      {loading && <CardSkeletonList count={3} height="h-44" />}
      {error && (
        <EmptyState
          tone="error"
          icon={SearchX}
          title="Could not load skills"
          text={error}
          action={<Button onClick={reload}>Try again</Button>}
        />
      )}
      {!loading && !error && data?.content.length === 0 && (
        <EmptyState icon={SearchX} title="No skills found" text="Try a different search or category." />
      )}
      {!loading && !error && data?.content.length > 0 && (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((s) => (
              <SkillCard key={s.id} skill={s} onAdd={(skill, type) => setAdding({ skill, type })} />
            ))}
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}

      {adding && (
        <AddSkillModal
          preset={adding.create ? {} : adding}
          startCreating={Boolean(adding.create)}
          onClose={() => setAdding(null)}
          onSaved={(created) => {
            if (created) {

              setCategory('')
              changeSearch(created.name)
            }
            reload()
            reloadCategories()
          }}
        />
      )}
    </>
  )
}

import { useState } from 'react'
import { BookOpen, GraduationCap, Pencil, Plus, Trash2 } from 'lucide-react'
import { deleteMySkill, listMySkills } from '../api/mySkills'
import AddSkillModal from '../components/domain/AddSkillModal'
import EditUserSkillModal from '../components/domain/EditUserSkillModal'
import PageHeader from '../components/layout/PageHeader'
import Badge from '../components/ui/Badge'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import ConfirmModal from '../components/ui/ConfirmModal'
import EmptyState from '../components/ui/EmptyState'
import { CardSkeletonList } from '../components/ui/Skeleton'
import { useToast } from '../components/ui/toastContext'
import { getErrorMessage } from '../utils/errors'
import { levelLabel } from '../utils/format'
import { useFetch } from '../utils/useFetch'

export default function MySkills() {
  const toast = useToast()
  const { data, loading, error, reload } = useFetch(() => listMySkills(), [])
  const [adding, setAdding] = useState(false)
  const [editing, setEditing] = useState(null)
  const [removing, setRemoving] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const offered = (data || []).filter((s) => s.type === 'OFFERED')
  const wanted = (data || []).filter((s) => s.type === 'WANTED')

  const confirmDelete = async () => {
    setDeleting(true)
    try {
      await deleteMySkill(removing.id)
      toast.success(`${removing.skill.name} removed from your skills successfully`)
      setRemoving(null)
      reload()
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setDeleting(false)
    }
  }

  return (
    <>
      <PageHeader
        title="My skills"
        subtitle="What you can teach and what you want to learn decide who you match with."
        action={
          <Button onClick={() => setAdding(true)}>
            <Plus className="h-4 w-4" aria-hidden="true" /> Add skill
          </Button>
        }
      />

      {loading && <CardSkeletonList count={2} height="h-28" />}
      {error && (
        <EmptyState
          tone="error"
          title="Could not load your skills"
          text={error}
          action={<Button onClick={reload}>Try again</Button>}
        />
      )}

      {!loading && !error && (
        <div className="grid gap-8 md:grid-cols-2">
          <SkillColumn
            title="I can teach"
            icon={GraduationCap}
            items={offered}
            emptyText="Add a skill you could teach someone."
            onEdit={setEditing}
            onRemove={setRemoving}
          />
          <SkillColumn
            title="I want to learn"
            icon={BookOpen}
            items={wanted}
            emptyText="Add a skill you would like to learn."
            onEdit={setEditing}
            onRemove={setRemoving}
          />
        </div>
      )}

      {adding && <AddSkillModal onClose={() => setAdding(false)} onSaved={reload} />}
      {editing && <EditUserSkillModal userSkill={editing} onClose={() => setEditing(null)} onSaved={reload} />}
      <ConfirmModal
        open={Boolean(removing)}
        title="Remove this skill?"
        text={removing ? `${removing.skill.name} will be removed from your list. Matches update straight away.` : ''}
        confirmLabel="Remove"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setRemoving(null)}
      />
    </>
  )
}

function SkillColumn({ title, icon: Icon, items, emptyText, onEdit, onRemove }) {
  return (
    <section aria-label={title}>
      <h2 className="mb-4 flex items-center gap-2 text-xl font-extrabold tracking-tight">
        <Icon className="h-5 w-5" aria-hidden="true" /> {title}
        <span className="text-sm font-semibold text-muted">{items.length}</span>
      </h2>
      {items.length === 0 ? (
        <EmptyState title="Nothing here yet" text={emptyText} />
      ) : (
        <div className="grid gap-3">
          {items.map((us) => (
            <Card key={us.id} className="flex items-start justify-between gap-3 p-4">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <h3 className="font-bold">{us.skill.name}</h3>
                  <Badge>{levelLabel(us.level)}</Badge>
                </div>
                <p className="mt-0.5 text-xs text-muted">{us.skill.category}</p>
                {us.notes && <p className="mt-2 text-sm text-ink/80">{us.notes}</p>}
              </div>
              <div className="flex shrink-0 gap-1">
                <button
                  type="button"
                  onClick={() => onEdit(us)}
                  aria-label={`Edit ${us.skill.name}`}
                  className="rounded-full p-2 text-muted hover:bg-surface hover:text-ink"
                >
                  <Pencil className="h-4 w-4" />
                </button>
                <button
                  type="button"
                  onClick={() => onRemove(us)}
                  aria-label={`Remove ${us.skill.name}`}
                  className="rounded-full p-2 text-muted hover:bg-red-50 hover:text-status-rejected"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </section>
  )
}

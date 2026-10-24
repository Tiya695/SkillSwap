import { useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { createSkill, deleteSkill, listSkills, updateSkill } from '../api/skills'
import PageHeader from '../components/layout/PageHeader'
import Badge from '../components/ui/Badge'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import ConfirmModal from '../components/ui/ConfirmModal'
import EmptyState from '../components/ui/EmptyState'
import { Input, Textarea } from '../components/ui/Input'
import Modal from '../components/ui/Modal'
import { Skeleton } from '../components/ui/Skeleton'
import { useToast } from '../components/ui/toastContext'
import { getErrorMessage, getFieldErrors } from '../utils/errors'
import { useFetch } from '../utils/useFetch'

export default function AdminSkills() {
  const toast = useToast()
  const { data, loading, error, reload } = useFetch(() => listSkills({ size: 200 }), [])
  const [editing, setEditing] = useState(null)
  const [removing, setRemoving] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const confirmDelete = async () => {
    setDeleting(true)
    try {
      await deleteSkill(removing.id)
      toast.success(`Skill “${removing.name}” deleted successfully`)
      setRemoving(null)
      reload()
    } catch (err) {
      toast.error(getErrorMessage(err))
      setRemoving(null)
    } finally {
      setDeleting(false)
    }
  }

  return (
    <>
      <PageHeader
        title="Manage skills"
        subtitle="The catalog every member picks from."
        action={
          <Button onClick={() => setEditing('new')}>
            <Plus className="h-4 w-4" aria-hidden="true" /> Add skill
          </Button>
        }
      />

      {loading && <Skeleton className="h-80 rounded-card" />}
      {error && (
        <EmptyState tone="error" title="Could not load skills" text={error} action={<Button onClick={reload}>Try again</Button>} />
      )}
      {!loading && !error && data.content.length === 0 && (
        <EmptyState title="The catalog is empty" text="Add the first skill." />
      )}
      {!loading && !error && data.content.length > 0 && (
        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-line bg-surface text-xs font-semibold text-muted">
                <tr>
                  <th scope="col" className="px-5 py-3">Skill</th>
                  <th scope="col" className="px-5 py-3">Category</th>
                  <th scope="col" className="hidden px-5 py-3 md:table-cell">Description</th>
                  <th scope="col" className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {data.content.map((s) => (
                  <tr key={s.id}>
                    <td className="px-5 py-3 font-bold">{s.name}</td>
                    <td className="px-5 py-3">
                      <Badge>{s.category}</Badge>
                    </td>
                    <td className="hidden max-w-xs truncate px-5 py-3 text-muted md:table-cell">{s.description}</td>
                    <td className="px-5 py-3">
                      <div className="flex justify-end gap-1">
                        <button
                          type="button"
                          onClick={() => setEditing(s)}
                          aria-label={`Edit ${s.name}`}
                          className="rounded-full p-2 text-muted hover:bg-surface hover:text-ink"
                        >
                          <Pencil className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => setRemoving(s)}
                          aria-label={`Delete ${s.name}`}
                          className="rounded-full p-2 text-muted hover:bg-red-50 hover:text-status-rejected"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {editing && (
        <SkillFormModal skill={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSaved={reload} />
      )}
      <ConfirmModal
        open={Boolean(removing)}
        title="Delete this skill?"
        text={removing ? `${removing.name} will be removed from the catalog. This does not work while members still use it.` : ''}
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setRemoving(null)}
      />
    </>
  )
}

function SkillFormModal({ skill, onClose, onSaved }) {
  const toast = useToast()
  const [name, setName] = useState(skill?.name || '')
  const [category, setCategory] = useState(skill?.category || '')
  const [description, setDescription] = useState(skill?.description || '')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setErrors({})
    setFormError('')
    try {
      const body = { name, category, description }
      if (skill) await updateSkill(skill.id, body)
      else await createSkill(body)
      toast.success(`Skill “${name.trim()}” ${skill ? 'updated' : 'created'} successfully`)
      onSaved()
      onClose()
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal
      open
      onClose={onClose}
      title={skill ? 'Edit skill' : 'Add skill'}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="skill-form" loading={saving}>
            {skill ? 'Save changes' : 'Add skill'}
          </Button>
        </>
      }
    >
      <form id="skill-form" onSubmit={submit} className="grid gap-4" noValidate>
        <Input label="Name" value={name} onChange={(e) => setName(e.target.value)} error={errors.name} required />
        <Input
          label="Category"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          error={errors.category}
          hint="For example Programming, Music or Language"
          required
        />
        <Textarea
          label="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          error={errors.description}
          maxLength={500}
        />
        {formError && !Object.keys(errors).length && (
          <p role="alert" className="text-sm font-medium text-status-rejected">
            {formError}
          </p>
        )}
      </form>
    </Modal>
  )
}

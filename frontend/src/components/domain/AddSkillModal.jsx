import { useState } from 'react'
import { Plus } from 'lucide-react'
import { addMySkill } from '../../api/mySkills'
import { createSkill, listCategories, listSkills } from '../../api/skills'
import { getErrorMessage, getFieldErrors } from '../../utils/errors'
import { useFetch } from '../../utils/useFetch'
import Button from '../ui/Button'
import { Input, Select, Textarea } from '../ui/Input'
import Modal from '../ui/Modal'
import { useToast } from '../ui/toastContext'

export default function AddSkillModal({ preset = {}, startCreating = false, onClose, onSaved }) {
  const toast = useToast()
  const [creating, setCreating] = useState(startCreating)
  const [skillId, setSkillId] = useState(preset.skill?.id ?? '')
  const [newName, setNewName] = useState('')
  const [newCategory, setNewCategory] = useState('')
  const [newDescription, setNewDescription] = useState('')
  const [type, setType] = useState(preset.type ?? 'OFFERED')
  const [level, setLevel] = useState('BEGINNER')
  const [notes, setNotes] = useState('')
  const [saving, setSaving] = useState(false)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')

  const { data, loading } = useFetch(
    () => (preset.skill ? Promise.resolve(null) : Promise.all([listSkills({ size: 200 }), listCategories()])),
    [],
  )
  const catalog = data?.[0]?.content || []
  const categories = data?.[1] || []

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setErrors({})
    setFormError('')
    try {
      let chosenId = skillId ? Number(skillId) : null
      let created = null
      if (creating) {
        created = await createSkill({ name: newName, category: newCategory, description: newDescription })
        chosenId = created.id
      }
      await addMySkill({ skillId: chosenId, type, level, notes })
      const list = type === 'OFFERED' ? '“I can teach” list' : '“I want to learn” list'
      toast.success(
        creating
          ? `Skill “${created.name}” created successfully and added to your ${list}`
          : `Skill added successfully to your ${list}`,
      )
      onSaved?.(created)
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
      title={creating ? 'Create a new skill' : 'Add a skill'}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="add-skill-form" loading={saving}>
            {creating ? 'Create and add' : 'Add skill'}
          </Button>
        </>
      }
    >
      <form id="add-skill-form" onSubmit={submit} className="grid gap-4">
        {preset.skill && (
          <div>
            <p className="mb-1.5 text-sm font-semibold">Skill</p>
            <p className="rounded-xl border border-line bg-surface px-4 py-2.5 text-sm font-semibold">
              {preset.skill.name}
            </p>
          </div>
        )}

        {!preset.skill && !creating && (
          <>
            <Select
              label="Skill"
              value={skillId}
              onChange={(e) => setSkillId(e.target.value)}
              error={errors.skillId}
              disabled={loading}
            >
              <option value="">{loading ? 'Loading skills...' : 'Choose a skill'}</option>
              {Object.entries(groupByCategory(catalog)).map(([category, skills]) => (
                <optgroup key={category} label={category}>
                  {skills.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </optgroup>
              ))}
            </Select>
            <button
              type="button"
              onClick={() => setCreating(true)}
              className="-mt-2 inline-flex items-center gap-1.5 self-start text-sm font-semibold text-brand hover:underline"
            >
              <Plus className="h-4 w-4" aria-hidden="true" /> Cannot find it? Create a new skill
            </button>
          </>
        )}

        {creating && (
          <>
            <Input
              label="Skill name"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              error={errors.name}
              placeholder="For example Chess"
              required
            />
            <div>
              <Input
                label="Category"
                list="skill-categories"
                value={newCategory}
                onChange={(e) => setNewCategory(e.target.value)}
                error={errors.category}
                placeholder="Pick one or type a new one"
                required
              />
              <datalist id="skill-categories">
                {categories.map((c) => (
                  <option key={c} value={c} />
                ))}
              </datalist>
            </div>
            <Textarea
              label="Description (optional)"
              rows={2}
              value={newDescription}
              onChange={(e) => setNewDescription(e.target.value)}
              error={errors.description}
              maxLength={500}
            />
            {!startCreating && (
              <button
                type="button"
                onClick={() => setCreating(false)}
                className="-mt-2 self-start text-sm font-semibold text-brand hover:underline"
              >
                Back to the skill list
              </button>
            )}
          </>
        )}

        {!preset.type && (
          <Select label="This is a skill I..." value={type} onChange={(e) => setType(e.target.value)} error={errors.type}>
            <option value="OFFERED">can teach</option>
            <option value="WANTED">want to learn</option>
          </Select>
        )}

        <Select
          label={type === 'OFFERED' ? 'My level' : 'Level I am starting from'}
          value={level}
          onChange={(e) => setLevel(e.target.value)}
          error={errors.level}
        >
          <option value="BEGINNER">Beginner</option>
          <option value="INTERMEDIATE">Intermediate</option>
          <option value="ADVANCED">Advanced</option>
        </Select>

        <Textarea
          label="Notes (optional)"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          error={errors.notes}
          maxLength={300}
          placeholder={type === 'OFFERED' ? 'What can you help with?' : 'What do you want to get out of it?'}
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

function groupByCategory(skills) {
  return skills.reduce((groups, s) => {
    ;(groups[s.category] ||= []).push(s)
    return groups
  }, {})
}

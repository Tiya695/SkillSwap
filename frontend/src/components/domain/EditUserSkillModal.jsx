import { useState } from 'react'
import { updateMySkill } from '../../api/mySkills'
import { getErrorMessage, getFieldErrors } from '../../utils/errors'
import Button from '../ui/Button'
import { Select, Textarea } from '../ui/Input'
import Modal from '../ui/Modal'
import { useToast } from '../ui/toastContext'

export default function EditUserSkillModal({ userSkill, onClose, onSaved }) {
  const toast = useToast()
  const [type, setType] = useState(userSkill.type)
  const [level, setLevel] = useState(userSkill.level)
  const [notes, setNotes] = useState(userSkill.notes || '')
  const [saving, setSaving] = useState(false)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setErrors({})
    setFormError('')
    try {
      await updateMySkill(userSkill.id, { type, level, notes })
      toast.success(`${userSkill.skill.name} updated successfully`)
      onSaved?.()
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
      title={`Edit ${userSkill.skill.name}`}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="edit-user-skill-form" loading={saving}>
            Save changes
          </Button>
        </>
      }
    >
      <form id="edit-user-skill-form" onSubmit={submit} className="grid gap-4">
        <Select
          label="I want to"
          value={type}
          onChange={(e) => setType(e.target.value)}
          error={errors.type}
        >
          <option value="OFFERED">Teach this skill (I can teach)</option>
          <option value="WANTED">Learn this skill (I want to learn)</option>
        </Select>
        <Select label="Proficiency Level" value={level} onChange={(e) => setLevel(e.target.value)} error={errors.level}>
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

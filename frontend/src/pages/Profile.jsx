import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { Cake, ImagePlus, Mail, Phone, Trash2, TriangleAlert } from 'lucide-react'
import { removeAvatar, updateMe, uploadAvatar } from '../api/users'
import { useAuth } from '../auth/useAuth'
import DeleteAccountModal from '../components/domain/DeleteAccountModal'
import PageHeader from '../components/layout/PageHeader'
import Avatar from '../components/ui/Avatar'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import { Input, Textarea } from '../components/ui/Input'
import { useToast } from '../components/ui/toastContext'
import { getErrorMessage, getFieldErrors } from '../utils/errors'
import { formatDate } from '../utils/format'
import { isIndianMobile, todayIso } from '../utils/validation'

const MAX_PHOTO_BYTES = 2 * 1024 * 1024

export default function Profile() {
  const { user, setUser, isAdmin } = useAuth()
  const toast = useToast()
  const fileInput = useRef(null)
  const [name, setName] = useState(user.name)
  const [bio, setBio] = useState(user.bio || '')
  const [phone, setPhone] = useState(user.phone || '')
  const [dateOfBirth, setDateOfBirth] = useState(user.dateOfBirth || '')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [photoBusy, setPhotoBusy] = useState(false)
  const [photoError, setPhotoError] = useState('')
  const [deleting, setDeleting] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setFormError('')
    const found = {}
    if (!name.trim()) found.name = 'Name is required'
    if (!dateOfBirth) found.dateOfBirth = 'Date of birth is required'
    else if (dateOfBirth >= todayIso()) found.dateOfBirth = 'Date of birth must be in the past'
    if (phone.trim() && !isIndianMobile(phone)) {
      found.phone = 'Enter a valid 10 digit Indian mobile number, for example 9876543210 or +91 9876543210'
    }
    setErrors(found)
    if (Object.keys(found).length) return

    setSaving(true)
    try {
      const updated = await updateMe({ name, dateOfBirth, bio, phone })
      setUser(updated)
      setPhone(updated.phone || '')
      toast.success('Profile saved successfully')
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const onPickPhoto = async (e) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setPhotoError('')
    if (!['image/png', 'image/jpeg', 'image/webp'].includes(file.type)) {
      setPhotoError('Only PNG, JPG or WEBP photos are allowed')
      return
    }
    if (file.size > MAX_PHOTO_BYTES) {
      setPhotoError('The photo is too large. Please choose one under 2 MB.')
      return
    }
    setPhotoBusy(true)
    try {
      setUser(await uploadAvatar(file))
      toast.success('Profile photo updated successfully')
    } catch (err) {
      setPhotoError(getErrorMessage(err))
    } finally {
      setPhotoBusy(false)
    }
  }

  const onRemovePhoto = async () => {
    setPhotoBusy(true)
    setPhotoError('')
    try {
      setUser(await removeAvatar())
      toast.success('Profile photo removed successfully')
    } catch (err) {
      setPhotoError(getErrorMessage(err))
    } finally {
      setPhotoBusy(false)
    }
  }

  return (
    <>
      <PageHeader
        title="My profile"
        subtitle="This is what other people see when they find you."
        action={
          <Button to={`/users/${user.id}`} variant="secondary">
            View public profile
          </Button>
        }
      />

      <div className="grid gap-6 lg:grid-cols-[320px_minmax(0,1fr)]">
        <Card tilt gradient className="h-fit p-6 text-center">
          <div className="mx-auto w-fit rounded-full bg-gradient-to-br from-brand-300 to-brand p-1 shadow-glow">
            <Avatar name={user.name} src={user.avatarUrl} size="xl" className="border-4 border-white" />
          </div>
          <p className="mt-4 text-xl font-extrabold tracking-tight">{user.name}</p>

          <input
            ref={fileInput}
            type="file"
            accept="image/png,image/jpeg,image/webp"
            onChange={onPickPhoto}
            className="sr-only"
            aria-label="Choose a profile photo"
            tabIndex={-1}
          />
          <div className="mt-4 flex flex-wrap justify-center gap-2">
            <Button size="sm" loading={photoBusy} onClick={() => fileInput.current?.click()}>
              <ImagePlus className="h-4 w-4" aria-hidden="true" /> {user.avatarUrl ? 'Change photo' : 'Upload photo'}
            </Button>
            {user.avatarUrl && (
              <Button size="sm" variant="secondary" disabled={photoBusy} onClick={onRemovePhoto}>
                <Trash2 className="h-4 w-4" aria-hidden="true" /> Remove
              </Button>
            )}
          </div>
          <p className="mt-2 text-xs text-muted">PNG, JPG or WEBP, up to 2 MB</p>
          {photoError && (
            <p role="alert" className="mt-2 text-xs font-medium text-status-rejected">
              {photoError}
            </p>
          )}

          <ul className="mt-6 grid gap-2.5 border-t border-line pt-5 text-left text-sm">
            <Detail icon={Mail} label="Email" value={user.email} />
            <Detail icon={Phone} label="Contact" value={user.phone || 'Not added'} />
            <Detail icon={Cake} label="Date of birth" value={user.dateOfBirth ? formatDate(user.dateOfBirth) : 'Not set'} />
          </ul>
          <Link to="/my-skills" className="mt-5 inline-block text-sm font-semibold text-brand hover:underline">
            Edit my skills
          </Link>
        </Card>

        <Card className="p-6 sm:p-8">
          <h2 className="mb-5 text-lg font-bold">Edit details</h2>
          <form onSubmit={submit} className="grid gap-4" noValidate>
            <Input label="Name" value={name} onChange={(e) => setName(e.target.value)} error={errors.name} required />
            <Input
              label="Date of birth"
              type="date"
              max={todayIso()}
              value={dateOfBirth}
              onChange={(e) => setDateOfBirth(e.target.value)}
              error={errors.dateOfBirth}
              hint="Used to verify you when you reset a forgotten password"
              required
            />
            <Input
              label="Contact number"
              type="tel"
              inputMode="tel"
              autoComplete="tel"
              placeholder="9876543210 or +91 9876543210"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              error={errors.phone}
              hint="10 digit Indian mobile number. +91 is added for you."
            />
            <Textarea
              label="Bio"
              rows={4}
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              error={errors.bio}
              maxLength={500}
              hint="A line or two about you and what you like to learn"
            />
            {formError && !Object.keys(errors).length && (
              <p role="alert" className="text-sm font-medium text-status-rejected">
                {formError}
              </p>
            )}
            <div>
              <Button type="submit" loading={saving}>
                Save changes
              </Button>
            </div>
          </form>
        </Card>
      </div>

      {!isAdmin && (
        <Card className="mt-6 flex flex-wrap items-center justify-between gap-4 border-red-200 p-6">
          <div className="flex items-start gap-3">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-red-50 text-status-rejected">
              <TriangleAlert className="h-5 w-5" aria-hidden="true" />
            </span>
            <div>
              <h2 className="text-base font-bold">Delete my account</h2>
              <p className="mt-0.5 max-w-xl text-sm text-muted">
                Permanently removes your profile, skills and swaps. You will need your password to confirm.
              </p>
            </div>
          </div>
          <Button variant="danger" onClick={() => setDeleting(true)}>
            <Trash2 className="h-4 w-4" aria-hidden="true" /> Delete account
          </Button>
        </Card>
      )}

      {deleting && <DeleteAccountModal onClose={() => setDeleting(false)} />}
    </>
  )
}

function Detail({ icon: Icon, label, value }) {
  return (
    <li className="flex items-center gap-3">
      <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand-100 text-brand-700">
        <Icon className="h-4 w-4" aria-hidden="true" />
      </span>
      <span className="min-w-0">
        <span className="block text-[11px] font-semibold text-muted">{label}</span>
        <span className="block truncate font-semibold">{value}</span>
      </span>
    </li>
  )
}

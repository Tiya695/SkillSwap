import { useState } from 'react'
import { deleteAccount } from '../../api/users'
import { useAuth } from '../../auth/useAuth'
import { getErrorMessage, getFieldErrors } from '../../utils/errors'
import Button from '../ui/Button'
import { PasswordInput } from '../ui/Input'
import Modal from '../ui/Modal'

export default function DeleteAccountModal({ onClose }) {
  const { logout } = useAuth()
  const [password, setPassword] = useState('')
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setDeleting(true)
    setError('')
    try {
      await deleteAccount(password)
      logout()

      try {
        sessionStorage.setItem('skillswap_notice', 'Your account and all its data were deleted.')
      } catch {

      }
      window.location.assign('/')
    } catch (err) {
      setError(getFieldErrors(err).password || getErrorMessage(err))
      setDeleting(false)
    }
  }

  return (
    <Modal
      open
      onClose={onClose}
      title="Delete my account"
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Keep my account
          </Button>
          <Button type="submit" form="delete-account-form" variant="danger" loading={deleting} disabled={!password}>
            Delete forever
          </Button>
        </>
      }
    >
      <form id="delete-account-form" onSubmit={submit} className="grid gap-4">
        <p className="text-sm text-muted">This cannot be undone. These will be removed for good:</p>
        <ul className="list-disc space-y-1 pl-5 text-sm">
          <li>your profile, photo and skills</li>
          <li>every swap you are part of, with its chat, tests and reviews (the other person loses those swaps too)</li>
        </ul>
        <PasswordInput
          label="Enter your password to confirm"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={error}
        />
      </form>
    </Modal>
  )
}

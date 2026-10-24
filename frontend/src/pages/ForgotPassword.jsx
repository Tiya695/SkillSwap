import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { forgotPassword } from '../api/auth'
import { useAuth } from '../auth/useAuth'
import Button from '../components/ui/Button'
import { Input, PasswordInput } from '../components/ui/Input'
import PasswordChecklist from '../components/ui/PasswordChecklist'
import { getErrorMessage, getFieldErrors } from '../utils/errors'
import { GMAIL_MESSAGE, isGmail, isStrongPassword, todayIso } from '../utils/validation'
import AuthLayout from './AuthLayout'

export default function ForgotPassword() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState(location.state?.email || '')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/dashboard" replace />

  const submit = async (e) => {
    e.preventDefault()
    setFormError('')
    const found = {}
    if (!isGmail(email)) found.email = GMAIL_MESSAGE
    if (!dateOfBirth) found.dateOfBirth = 'Enter the date of birth you registered with'
    if (!isStrongPassword(newPassword)) found.newPassword = 'Password does not meet all the rules below'
    if (confirm !== newPassword) found.confirm = 'Passwords do not match'
    setErrors(found)
    if (Object.keys(found).length) return

    setSubmitting(true)
    try {
      const result = await forgotPassword({ email: email.trim(), dateOfBirth, newPassword })
      navigate('/login', { replace: true, state: { notice: result.message, email: email.trim() } })
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout
      title="Reset your password"
      subtitle="Enter your Gmail and the date of birth you registered with, then choose a new password."
      footer={
        <>
          Remembered it?{' '}
          <Link to="/login" className="font-semibold text-brand underline">
            Back to log in
          </Link>
        </>
      }
    >
      <form onSubmit={submit} className="grid gap-4" noValidate>
        <Input
          label="Gmail address"
          type="email"
          autoComplete="email"
          placeholder="name@gmail.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={errors.email}
          required
        />
        <Input
          label="Date of birth"
          type="date"
          max={todayIso()}
          value={dateOfBirth}
          onChange={(e) => setDateOfBirth(e.target.value)}
          error={errors.dateOfBirth}
          required
        />
        <div>
          <PasswordInput
            label="New password"
            autoComplete="new-password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            error={errors.newPassword}
            required
          />
          <PasswordChecklist password={newPassword} />
        </div>
        <PasswordInput
          label="Confirm new password"
          autoComplete="new-password"
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
          error={errors.confirm}
          required
        />
        {formError && !Object.keys(errors).length && (
          <p role="alert" className="text-sm font-medium text-status-rejected">
            {formError}
          </p>
        )}
        <Button type="submit" size="lg" loading={submitting} className="w-full">
          Update password
        </Button>
      </form>
    </AuthLayout>
  )
}

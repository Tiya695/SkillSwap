import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { CheckCircle2 } from 'lucide-react'
import { useAuth } from '../auth/useAuth'
import { useToast } from '../components/ui/toastContext'
import { firstName } from '../utils/format'
import Button from '../components/ui/Button'
import { Input, PasswordInput } from '../components/ui/Input'
import PasswordChecklist from '../components/ui/PasswordChecklist'
import { getErrorMessage, getFieldErrors } from '../utils/errors'
import { GMAIL_MESSAGE, isGmail, isStrongPassword, todayIso } from '../utils/validation'
import AuthLayout from './AuthLayout'

export default function Register() {
  const { user, register } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [registered, setRegistered] = useState(false)

  if (user) return <Navigate to="/dashboard" replace />

  const validate = () => {
    const found = {}
    if (!name.trim()) found.name = 'Name is required'
    if (!isGmail(email)) found.email = GMAIL_MESSAGE
    if (!dateOfBirth) found.dateOfBirth = 'Date of birth is required'
    else if (dateOfBirth >= todayIso()) found.dateOfBirth = 'Date of birth must be in the past'
    if (!isStrongPassword(password)) found.password = 'Password does not meet all the rules below'
    if (confirm !== password) found.confirm = 'Passwords do not match'
    return found
  }

  const submit = async (e) => {
    e.preventDefault()
    setFormError('')
    const found = validate()
    setErrors(found)
    if (Object.keys(found).length) return

    setSubmitting(true)
    try {
      await register(name.trim(), email.trim(), dateOfBirth, password)
      setRegistered(true)
      toast.success('Account created successfully! Please log in.')
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout
      title={registered ? 'Account created' : 'Create your account'}
      subtitle={
        registered
          ? 'Your registration is complete. Please log in with your credentials.'
          : 'Free to join. Tell us what you can teach and what you want to learn.'
      }
      footer={
        <>
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-brand underline">
            Log in
          </Link>
        </>
      }
    >
      {registered ? (
        <div className="grid gap-6 text-center py-4">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 shadow-sm animate-fade-in">
            <CheckCircle2 className="h-10 w-10" />
          </div>
          <div>
            <h3 className="text-xl font-extrabold text-ink">Account created successfully!</h3>
            <p className="mt-2 text-sm text-muted">
              Welcome, <span className="font-semibold text-ink">{firstName(name)}</span>! Your account has been registered with{' '}
              <span className="font-medium text-ink">{email}</span>.
            </p>
            <p className="mt-1 text-sm text-muted">
              You can now log in using your email and password.
            </p>
          </div>
          <Button
            size="lg"
            className="w-full"
            onClick={() =>
              navigate('/login', {
                state: { email, notice: 'Account created successfully! Please log in.' },
              })
            }
          >
            Go to Login
          </Button>
        </div>
      ) : (
      <form onSubmit={submit} className="grid gap-4" noValidate>
        <Input
          label="Full name"
          autoComplete="name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          error={errors.name}
          required
        />
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
          hint="You will need it if you ever forget your password"
          required
        />
        <div>
          <PasswordInput
            label="Password"
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={errors.password}
            required
          />
          <PasswordChecklist password={password} />
        </div>
        <PasswordInput
          label="Confirm password"
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
          Create account
        </Button>
      </form>
      )}
    </AuthLayout>
  )
}

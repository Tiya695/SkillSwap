import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { CheckCircle2 } from 'lucide-react'
import { useAuth } from '../auth/useAuth'
import Button from '../components/ui/Button'
import { Input, PasswordInput } from '../components/ui/Input'
import { getErrorMessage, getFieldErrors } from '../utils/errors'
import AuthLayout from './AuthLayout'

export default function Login() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState(location.state?.email || '')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const notice = location.state?.notice
  const destination = location.state?.from || '/dashboard'
  if (user) return <Navigate to={destination} replace />

  const submit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setErrors({})
    setFormError('')
    try {
      await login(email, password)
      navigate(destination, { replace: true })
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Log in to see your matches and swaps."
      footer={
        <>
          New to SkillSwap?{' '}
          <Link to="/register" className="font-semibold text-brand underline">
            Create an account
          </Link>
        </>
      }
    >
      {notice && (
        <p role="status" className="mb-5 flex items-start gap-2 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm font-medium text-status-completed">
          <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" /> {notice}
        </p>
      )}
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
        <div>
          <PasswordInput
            label="Password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={errors.password}
            required
          />
          <div className="mt-2 text-right">
            <Link to="/forgot-password" state={{ email }} className="text-sm font-semibold text-brand hover:underline">
              Forgot password?
            </Link>
          </div>
        </div>
        {formError && !Object.keys(errors).length && (
          <p role="alert" className="text-sm font-medium text-status-rejected">
            {formError}
          </p>
        )}
        <Button type="submit" size="lg" loading={submitting} className="w-full">
          Log in
        </Button>
      </form>
    </AuthLayout>
  )
}

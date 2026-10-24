
export const GMAIL_MESSAGE = 'Use your Gmail address, for example name@gmail.com'

export function isGmail(email) {
  return /^[A-Za-z0-9._%+-]+@gmail\.com$/i.test(email.trim())
}

export function passwordChecks(password) {
  return [
    { label: 'At least 6 characters', ok: password.length >= 6 },
    { label: 'One uppercase letter (A-Z)', ok: /[A-Z]/.test(password) },
    { label: 'One lowercase letter (a-z)', ok: /[a-z]/.test(password) },
    { label: 'One digit (0-9)', ok: /\d/.test(password) },
    { label: 'One special character (! @ # $ ...)', ok: /[^A-Za-z0-9\s]/.test(password) },
  ]
}

export function isStrongPassword(password) {
  return passwordChecks(password).every((c) => c.ok)
}

export function isIndianMobile(value) {
  return /^(\+91|91|0)?[6-9]\d{9}$/.test(value.replace(/[\s-]/g, ''))
}

export function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

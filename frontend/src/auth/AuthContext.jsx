import { useCallback, useEffect, useMemo, useState } from 'react'
import * as authApi from '../api/auth'
import * as usersApi from '../api/users'
import { AuthContext } from './context'
import { tokenStorage } from './tokenStorage'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(() => Boolean(tokenStorage.get()))

  useEffect(() => {
    if (!tokenStorage.get()) return
    usersApi
      .getMe()
      .then(setUser)
      .catch(() => tokenStorage.clear())
      .finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (email, password) => {
    const { token, user: loggedIn } = await authApi.login({ email, password })
    tokenStorage.set(token)
    setUser(loggedIn)
    return loggedIn
  }, [])

  const register = useCallback(
    async (name, email, dateOfBirth, password) => {
      return authApi.register({ name, email, dateOfBirth, password })
    },
    [],
  )

  const logout = useCallback(() => {
    tokenStorage.clear()
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ user, loading, isAdmin: user?.role === 'ADMIN', login, register, logout, setUser }),
    [user, loading, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

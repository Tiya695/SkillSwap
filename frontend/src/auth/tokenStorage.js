
const KEY = 'skillswap_token'

export const tokenStorage = {
  get() {
    try {
      return sessionStorage.getItem(KEY)
    } catch {
      return null
    }
  },
  set(token) {
    try {
      sessionStorage.setItem(KEY, token)
    } catch {

    }
  },
  clear() {
    try {
      sessionStorage.removeItem(KEY)
    } catch {

    }
  },
}

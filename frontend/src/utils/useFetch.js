import { useCallback, useEffect, useRef, useState } from 'react'
import { getErrorMessage } from './errors'

export function useFetch(fetcher, deps = []) {
  const [version, setVersion] = useState(0)
  const [result, setResult] = useState({ id: null, data: null, error: null })
  const fetcherRef = useRef(fetcher)
  useEffect(() => {
    fetcherRef.current = fetcher
  })

  const id = `${JSON.stringify(deps)}#${version}`

  useEffect(() => {
    let cancelled = false
    fetcherRef
      .current()
      .then((data) => !cancelled && setResult({ id, data, error: null }))
      .catch((err) => !cancelled && setResult({ id, data: null, error: getErrorMessage(err) }))
    return () => {
      cancelled = true
    }
  }, [id])

  const reload = useCallback(() => setVersion((v) => v + 1), [])

  return { data: result.data, error: result.id === id ? result.error : null, loading: result.id !== id, reload }
}

export function useDebounced(value, delay = 300) {
  const [debounced, setDebounced] = useState(value)
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay)
    return () => clearTimeout(id)
  }, [value, delay])
  return debounced
}

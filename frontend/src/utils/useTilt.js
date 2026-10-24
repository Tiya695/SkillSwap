import { useCallback, useRef } from 'react'

export function useTilt(max = 7) {
  const ref = useRef(null)

  const enabled = () =>
    typeof window !== 'undefined' &&
    window.matchMedia('(hover: hover)').matches &&
    !window.matchMedia('(prefers-reduced-motion: reduce)').matches

  const onMouseMove = useCallback(
    (e) => {
      const el = ref.current
      if (!el || !enabled()) return
      const rect = el.getBoundingClientRect()
      const x = (e.clientX - rect.left) / rect.width
      const y = (e.clientY - rect.top) / rect.height
      const rotateY = (x - 0.5) * 2 * max
      const rotateX = (0.5 - y) * 2 * max
      el.style.transform = `perspective(900px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) translateY(-3px)`
      el.style.setProperty('--mx', `${(x * 100).toFixed(0)}%`)
      el.style.setProperty('--my', `${(y * 100).toFixed(0)}%`)
    },
    [max],
  )

  const onMouseLeave = useCallback(() => {
    const el = ref.current
    if (el) el.style.transform = ''
  }, [])

  return { ref, onMouseMove, onMouseLeave }
}

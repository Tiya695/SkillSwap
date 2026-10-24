import { useEffect, useState } from 'react'

export function useCountUp(target, duration = 700) {
  const [value, setValue] = useState(0)

  useEffect(() => {
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (reduce || !Number.isFinite(target) || target === 0) {
      const id = requestAnimationFrame(() => setValue(target))
      return () => cancelAnimationFrame(id)
    }
    let frame
    const start = performance.now()
    const tick = (now) => {
      const progress = Math.min((now - start) / duration, 1)
      setValue(Math.round(target * (1 - Math.pow(1 - progress, 3))))
      if (progress < 1) frame = requestAnimationFrame(tick)
    }
    frame = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frame)
  }, [target, duration])

  return value
}

import { useEffect, useState } from 'react'
import { X } from 'lucide-react'
import AppPreview from './landing/AppPreview'
import CategoryStrip from './landing/CategoryStrip'
import Features from './landing/Features'
import FinalCta from './landing/FinalCta'
import Hero from './landing/Hero'
import Steps from './landing/Steps'
import WhySkillSwap from './landing/WhySkillSwap'

export default function Landing() {

  const [notice, setNotice] = useState(() => {
    try {
      return sessionStorage.getItem('skillswap_notice')
    } catch {
      return null
    }
  })
  useEffect(() => {
    try {
      sessionStorage.removeItem('skillswap_notice')
    } catch {

    }
  }, [])

  return (
    <>
      {notice && (
        <div role="status" className="flex items-center justify-center gap-3 bg-brand-100 px-4 py-2.5 text-sm font-semibold text-brand-700">
          {notice}
          <button type="button" onClick={() => setNotice(null)} className="rounded-full p-1 hover:bg-brand-200" aria-label="Dismiss message">
            <X className="h-4 w-4" />
          </button>
        </div>
      )}
      <div className="hero-glow">
        <Hero />
        <AppPreview />
      </div>
      <CategoryStrip />
      <Features />
      <WhySkillSwap />
      <Steps />
      <FinalCta />
    </>
  )
}

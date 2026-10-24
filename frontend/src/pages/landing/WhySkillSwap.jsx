const reasons = [
  { big: 'Mutual', text: 'A match counts as mutual only when you can each teach the other something.' },
  { big: '1 for 1', text: 'Every swap is between two people: one skill for one skill.' },
  { big: '5 stars', text: 'Rate each other after a completed swap so trust builds over time.' },
  { big: 'No fees', text: 'No money changes hands. You pay with what you know.' },
]

export default function WhySkillSwap() {
  return (
    <section id="why" className="mx-auto max-w-6xl scroll-mt-20 px-4 pb-24 sm:px-6">
      <div className="text-center">
        <h2 className="section-title">Why People Choose SkillSwap</h2>
        <p className="section-sub">Learning from a real person, in exchange for something you are good at.</p>
      </div>
      <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {reasons.map((r) => (
          <div key={r.big} className="glow-wrap">
            <div className="glow-card card-gradient flex h-64 flex-col justify-between rounded-card border border-line p-6 shadow-soft">
              <p className="text-4xl font-extrabold tracking-tightest">{r.big}</p>
              <p className="text-sm text-muted">{r.text}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

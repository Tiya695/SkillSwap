import Button from '../../components/ui/Button'

export default function FinalCta() {
  return (
    <section className="mx-auto max-w-6xl px-4 pb-24 sm:px-6">
      <div className="hero-glow rounded-[28px] border border-line bg-surface px-6 py-14 text-center sm:py-20">
        <h2 className="mx-auto max-w-2xl text-3xl font-extrabold tracking-tightest sm:text-5xl">
          Your next skill is one swap away.
        </h2>
        <p className="mx-auto mt-4 max-w-md text-sm text-muted sm:text-base">
          Create a free account, list what you know, and see who you match with.
        </p>
        <div className="mt-8 flex flex-wrap justify-center gap-3">
          <Button to="/register" size="lg">
            Get Started Free
          </Button>
          <Button to="/login" variant="secondary" size="lg">
            Log in
          </Button>
        </div>
      </div>
    </section>
  )
}

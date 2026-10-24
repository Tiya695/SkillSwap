import { useEffect, useState } from 'react'
import { Check, ClipboardCheck, Lock, Plus, Sparkles, Trash2, X } from 'lucide-react'
import { allowRetake, createAssessment, generateQuestions, getAssessments, submitAnswers } from '../../api/assessments'
import { getErrorMessage, getFieldErrors } from '../../utils/errors'
import { formatDate } from '../../utils/format'
import { useFetch } from '../../utils/useFetch'
import Badge from '../ui/Badge'
import Button from '../ui/Button'
import Card from '../ui/Card'
import { Input, Select, Textarea } from '../ui/Input'
import { Skeleton } from '../ui/Skeleton'
import { useToast } from '../ui/toastContext'

const emptyQuestion = () => ({ text: '', maxPoints: 10, options: ['', '', '', ''], correct: 0 })

export default function AssessmentSection({ swap, otherName, onSwapChanged }) {

  const { data: overview, error, reload } = useFetch(
    () => getAssessments(swap.id),
    [swap.id, swap.status, swap.assessmentUnlocked],
  )

  useEffect(() => {
    const timer = setInterval(reload, 8000)
    return () => clearInterval(timer)
  }, [reload])

  const changed = async () => {
    reload()
    onSwapChanged()
  }

  return (
    <section aria-labelledby="final-tests">
      <h2 id="final-tests" className="mb-1 flex items-center gap-2 text-xl font-extrabold tracking-tight">
        <ClipboardCheck className="h-5 w-5 text-brand" aria-hidden="true" /> Final tests
      </h2>
      <p className="mb-5 text-sm text-muted">
        Each of you writes a multiple choice test for the other. When the other person submits, the app marks it by itself and
        shows the points and whether they passed. 50% or more passes. When both of you pass, the swap is completed.
      </p>

      {error && <p className="text-sm font-medium text-status-rejected">{error}</p>}
      {!overview && !error && <Skeleton className="h-40 rounded-card" />}

      {overview && !overview.unlocked && (
        <Card className="flex items-start gap-3 p-6">
          <Lock className="mt-0.5 h-5 w-5 shrink-0 text-muted" aria-hidden="true" />
          <p className="text-sm text-muted">
            The tests open on <strong className="text-ink">{formatDate(swap.endDate)}</strong>, when the learning period ends, or
            sooner when both of you press &ldquo;We are ready early&rdquo;.
          </p>
        </Card>
      )}

      {overview?.unlocked && (
        <div className="grid gap-5 lg:grid-cols-2">
          <TeachSide swap={swap} view={overview.toTeach} learnerName={otherName} passMark={overview.passMark} onChanged={changed} />
          <TakeSide view={overview.toTake} teacherName={otherName} passMark={overview.passMark} onChanged={changed} />
        </div>
      )}
    </section>
  )
}

function TeachSide({ swap, view, learnerName, passMark, onChanged }) {
  const canWrite = swap.status === 'ACCEPTED'
  return (
    <Card className="p-6">
      <h3 className="text-base font-bold">Test you give to {learnerName}</h3>
      {view && <p className="mt-0.5 text-xs text-muted">Skill: {view.skill.name}</p>}

      {!view && canWrite && <TestWriter swapId={swap.id} learnerName={learnerName} onChanged={onChanged} />}
      {!view && !canWrite && <p className="mt-3 text-sm text-muted">No test was written.</p>}

      {view?.status === 'OPEN' && (
        <>
          <p className="mt-4 rounded-xl bg-brand-50 px-4 py-3 text-sm text-muted">
            Waiting for {learnerName} to take the test ({view.questions.length} question{view.questions.length === 1 ? '' : 's'}
            {view.attempt > 1 ? `, attempt ${view.attempt}` : ''}). It will be marked automatically.
          </p>
          <QuestionList questions={view.questions} showCorrect />
        </>
      )}
      {view?.status === 'GRADED' && (
        <Result view={view} passMark={passMark} personName={learnerName} teacherSide onChanged={onChanged} />
      )}
    </Card>
  )
}

function TestWriter({ swapId, learnerName, onChanged }) {
  const toast = useToast()
  const [questions, setQuestions] = useState([emptyQuestion()])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const updateQuestion = (qi, patch) => setQuestions((list) => list.map((q, i) => (i === qi ? { ...q, ...patch } : q)))
  const updateOption = (qi, oi, value) =>
    setQuestions((list) =>
      list.map((q, i) => (i === qi ? { ...q, options: q.options.map((o, j) => (j === oi ? value : o)) } : q)),
    )
  const addOption = (qi) =>
    setQuestions((list) => list.map((q, i) => (i === qi && q.options.length < 6 ? { ...q, options: [...q.options, ''] } : q)))
  const removeOption = (qi, oi) =>
    setQuestions((list) =>
      list.map((q, i) => {
        if (i !== qi || q.options.length <= 2) return q
        const options = q.options.filter((_, j) => j !== oi)

        const correct = q.correct === oi ? 0 : q.correct > oi ? q.correct - 1 : q.correct
        return { ...q, options, correct }
      }),
    )

  const [topic, setTopic] = useState('')
  const [count, setCount] = useState('10')
  const [generating, setGenerating] = useState(false)
  const [genError, setGenError] = useState('')
  const [drafted, setDrafted] = useState(false)

  const generate = async () => {
    setGenerating(true)
    setGenError('')
    try {
      const result = await generateQuestions(swapId, topic, Number(count))
      setQuestions(
        result.questions.map((q) => ({ text: q.text, maxPoints: q.maxPoints, options: q.options, correct: q.correctOption })),
      )
      setDrafted(true)
    } catch (err) {
      setGenError(getFieldErrors(err).topic || getErrorMessage(err))
    } finally {
      setGenerating(false)
    }
  }

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await createAssessment(
        swapId,
        questions.map((q) => ({
          text: q.text,
          maxPoints: Number(q.maxPoints),
          options: q.options.map((o) => o.trim()),
          correctOption: q.correct,
        })),
      )
      toast.success(`Test sent to ${learnerName} successfully`)
      await onChanged()
    } catch (err) {
      const fields = getFieldErrors(err)
      setError(Object.values(fields)[0] || getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={submit} className="mt-4 grid gap-4">
      <div className="rounded-2xl border border-brand-200 bg-gradient-to-br from-brand-50 to-white p-4">
        <p className="flex items-center gap-2 text-sm font-bold">
          <Sparkles className="h-4 w-4 text-brand" aria-hidden="true" /> Write the questions for me
        </p>
        <p className="mt-1 text-xs text-muted">
          Type the topic and a draft of multiple choice questions is written for you. Please check every question and mark the
          right answer. You can change the text and the choices, delete questions or add your own before you send the test.
        </p>
        <div className="mt-3 grid gap-3 sm:grid-cols-[1fr_120px]">
          <Input
            label="Topic"
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
            maxLength={200}
            placeholder="For example: lists and loops"
            disabled={generating}
          />
          <Select label="Questions" value={count} onChange={(e) => setCount(e.target.value)} disabled={generating}>
            {[3, 5, 7, 10].map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </Select>
        </div>
        <div className="mt-3 flex flex-wrap items-center gap-3">
          <Button size="sm" loading={generating} disabled={!topic.trim()} onClick={generate}>
            <Sparkles className="h-4 w-4" aria-hidden="true" /> {drafted ? 'Write again' : 'Generate questions'}
          </Button>
          {generating && (
            <p role="status" className="text-xs text-muted">
              Writing the questions. On a computer without a graphics card this can take one to two minutes.
            </p>
          )}
          {drafted && !generating && (
            <p role="status" className="text-xs font-semibold text-status-completed">
              Draft ready below. The AI can be wrong, so check the right answer of every question.
            </p>
          )}
        </div>
        {genError && (
          <p role="alert" className="mt-3 text-sm font-medium text-status-rejected">
            {genError}
          </p>
        )}
      </div>

      <p className="text-sm text-muted">Or write the questions yourself. Select the circle next to the correct answer.</p>

      {questions.map((q, qi) => (
        <fieldset key={qi} className="rounded-2xl border border-line bg-brand-50/40 p-4">
          <div className="mb-2 flex items-center justify-between">
            <legend className="text-xs font-bold text-brand-700">Question {qi + 1}</legend>
            {questions.length > 1 && (
              <button
                type="button"
                onClick={() => setQuestions((list) => list.filter((_, i) => i !== qi))}
                aria-label={`Remove question ${qi + 1}`}
                className="rounded-full p-1.5 text-muted hover:bg-red-50 hover:text-status-rejected"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            )}
          </div>
          <Textarea
            aria-label={`Question ${qi + 1}`}
            rows={2}
            maxLength={500}
            value={q.text}
            onChange={(e) => updateQuestion(qi, { text: e.target.value })}
            placeholder="Write the question"
            required
          />
          <div className="mt-3 grid gap-2">
            {q.options.map((option, oi) => (
              <div key={oi} className="flex items-center gap-2">
                <input
                  type="radio"
                  name={`correct-${qi}`}
                  checked={q.correct === oi}
                  onChange={() => updateQuestion(qi, { correct: oi })}
                  aria-label={`Choice ${oi + 1} is the correct answer`}
                  title="Correct answer"
                  className="h-4 w-4 shrink-0 accent-brand"
                />
                <input
                  value={option}
                  onChange={(e) => updateOption(qi, oi, e.target.value)}
                  maxLength={200}
                  placeholder={`Choice ${oi + 1}`}
                  aria-label={`Question ${qi + 1} choice ${oi + 1}`}
                  required
                  className={`min-w-0 flex-1 rounded-xl border bg-white px-3 py-2 text-sm focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/15 ${
                    q.correct === oi ? 'border-green-400 bg-green-50' : 'border-line'
                  }`}
                />
                {q.options.length > 2 && (
                  <button
                    type="button"
                    onClick={() => removeOption(qi, oi)}
                    aria-label={`Remove choice ${oi + 1} of question ${qi + 1}`}
                    className="rounded-full p-1.5 text-muted hover:bg-red-50 hover:text-status-rejected"
                  >
                    <X className="h-4 w-4" />
                  </button>
                )}
              </div>
            ))}
          </div>
          <div className="mt-3 flex flex-wrap items-end justify-between gap-3">
            {q.options.length < 6 ? (
              <button type="button" onClick={() => addOption(qi)} className="text-xs font-semibold text-brand hover:underline">
                + Add a choice
              </button>
            ) : (
              <span />
            )}
            <div className="w-[120px]">
              <Input
                label="Points"
                type="number"
                min={1}
                max={100}
                value={q.maxPoints}
                onChange={(e) => updateQuestion(qi, { maxPoints: e.target.value })}
                required
              />
            </div>
          </div>
        </fieldset>
      ))}

      {questions.length < 10 && (
        <button
          type="button"
          onClick={() => setQuestions((list) => [...list, emptyQuestion()])}
          className="inline-flex items-center gap-1.5 self-start text-sm font-semibold text-brand hover:underline"
        >
          <Plus className="h-4 w-4" aria-hidden="true" /> Add a question
        </button>
      )}
      {error && (
        <p role="alert" className="text-sm font-medium text-status-rejected">
          {error}
        </p>
      )}
      <div>
        <Button type="submit" loading={saving}>
          Send test to {learnerName}
        </Button>
      </div>
    </form>
  )
}

function TakeSide({ view, teacherName, passMark, onChanged }) {
  return (
    <Card className="p-6">
      <h3 className="text-base font-bold">Test you take from {teacherName}</h3>
      {view && <p className="mt-0.5 text-xs text-muted">Skill: {view.skill.name}</p>}

      {!view && <p className="mt-4 rounded-xl bg-brand-50 px-4 py-3 text-sm text-muted">{teacherName} has not written your test yet.</p>}
      {view?.status === 'OPEN' && <TakeForm view={view} onChanged={onChanged} />}
      {view?.status === 'GRADED' && <Result view={view} passMark={passMark} personName={teacherName} onChanged={onChanged} />}
    </Card>
  )
}

function TakeForm({ view, onChanged }) {
  const toast = useToast()
  const [picks, setPicks] = useState({})
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const answered = view.questions.filter((q) => picks[q.id] !== undefined).length
  const allAnswered = answered === view.questions.length

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const result = await submitAnswers(
        view.id,
        view.questions.map((q) => ({ questionId: q.id, selectedOption: picks[q.id] })),
      )
      toast.success(
        result.passed
          ? `Test marked: ${result.scorePercent}%. You passed!`
          : `Test marked: ${result.scorePercent}%. You did not pass this time.`,
      )
      await onChanged()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={submit} className="mt-4 grid gap-4">
      {view.attempt > 1 && <Badge tone="pending">Attempt {view.attempt}</Badge>}
      {view.questions.map((q) => (
        <fieldset key={q.id} className="rounded-2xl border border-line p-4">
          <legend className="px-1 text-sm font-bold">
            {q.position}. {q.text} <span className="font-normal text-muted">({q.maxPoints} points)</span>
          </legend>
          <div className="mt-2 grid gap-2">
            {q.options.map((option, oi) => (
              <label
                key={oi}
                className={`flex cursor-pointer items-center gap-3 rounded-xl border px-3.5 py-2.5 text-sm transition-colors ${
                  picks[q.id] === oi ? 'border-brand bg-brand-50 font-semibold' : 'border-line hover:border-brand/50'
                }`}
              >
                <input
                  type="radio"
                  name={`q-${q.id}`}
                  checked={picks[q.id] === oi}
                  onChange={() => setPicks((p) => ({ ...p, [q.id]: oi }))}
                  className="h-4 w-4 shrink-0 accent-brand"
                />
                {option}
              </label>
            ))}
          </div>
        </fieldset>
      ))}
      {error && (
        <p role="alert" className="text-sm font-medium text-status-rejected">
          {error}
        </p>
      )}
      <div className="flex flex-wrap items-center gap-3">
        <Button type="submit" loading={saving} disabled={!allAnswered}>
          Submit and see my result
        </Button>
        <p className="text-xs text-muted">
          {answered} of {view.questions.length} answered
        </p>
      </div>
    </form>
  )
}

function Result({ view, passMark, personName, teacherSide = false, onChanged }) {
  const toast = useToast()
  const [busy, setBusy] = useState(false)
  const earned = view.questions.reduce((sum, q) => sum + (q.awardedPoints || 0), 0)
  const total = view.questions.reduce((sum, q) => sum + q.maxPoints, 0)

  const retake = async () => {
    setBusy(true)
    try {
      await allowRetake(view.id)
      toast.success(`${personName} can now take the test again`)
      await onChanged()
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="mt-4 grid gap-4">
      <div className={`rounded-2xl border px-4 py-3 ${view.passed ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'}`}>
        <p className={`text-3xl font-extrabold tracking-tightest ${view.passed ? 'text-status-completed' : 'text-status-rejected'}`}>
          {view.scorePercent}%
        </p>
        <p className="text-sm font-semibold">
          {teacherSide ? `${personName}: ` : ''}
          {view.passed ? 'Passed' : `Not passed (pass mark ${passMark}%)`}
        </p>
        <p className="mt-0.5 text-xs text-muted">
          {earned} of {total} points{view.attempt > 1 ? ` - attempt ${view.attempt}` : ''}
        </p>
      </div>

      <QuestionList questions={view.questions} showCorrect showResult teacherSide={teacherSide} />

      {!view.passed && teacherSide && (
        <div>
          <Button variant="secondary" size="sm" loading={busy} onClick={retake}>
            Let {personName} try again
          </Button>
        </div>
      )}
      {!view.passed && !teacherSide && <p className="text-sm text-muted">{personName} can let you take the test again.</p>}
    </div>
  )
}

function QuestionList({ questions, showCorrect = false, showResult = false, teacherSide = false }) {
  return (
    <ul className="mt-4 grid gap-3">
      {questions.map((q) => (
        <li key={q.id} className="rounded-xl border border-line p-4 text-sm">
          <p className="font-bold">
            {q.position}. {q.text}
          </p>
          <ul className="mt-2 grid gap-1.5">
            {q.options.map((option, oi) => {
              const isCorrect = showCorrect && q.correctOption === oi
              const isPicked = showResult && q.selectedOption === oi
              const wrongPick = isPicked && !isCorrect
              return (
                <li
                  key={oi}
                  className={`flex items-center gap-2 rounded-lg border px-3 py-1.5 ${
                    isCorrect ? 'border-green-300 bg-green-50' : wrongPick ? 'border-red-300 bg-red-50' : 'border-line'
                  }`}
                >
                  {isCorrect && <Check className="h-4 w-4 shrink-0 text-status-completed" aria-label="Correct answer" />}
                  {wrongPick && <X className="h-4 w-4 shrink-0 text-status-rejected" aria-label="Wrong answer picked" />}
                  <span className="flex-1">{option}</span>
                  {isPicked && <span className="text-[11px] font-semibold text-muted">{teacherSide ? 'Their pick' : 'Your pick'}</span>}
                </li>
              )
            })}
          </ul>
          {showResult && (
            <p className="mt-2 text-xs font-semibold text-brand-700">
              {q.awardedPoints} / {q.maxPoints} points
            </p>
          )}
        </li>
      ))}
    </ul>
  )
}

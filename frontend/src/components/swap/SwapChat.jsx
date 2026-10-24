import { useEffect, useRef, useState } from 'react'
import { MessageCircle, Send } from 'lucide-react'
import { listMessages, sendMessage } from '../../api/messages'
import { getErrorMessage } from '../../utils/errors'
import Button from '../ui/Button'
import Card from '../ui/Card'
import { useToast } from '../ui/toastContext'

export default function SwapChat({ swapId, meId, canWrite }) {
  const toast = useToast()
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [sending, setSending] = useState(false)
  const lastId = useRef(0)
  const listRef = useRef(null)

  const addMessages = (incoming) => {
    if (!incoming.length) return
    setMessages((current) => {
      const known = new Set(current.map((m) => m.id))
      const fresh = incoming.filter((m) => !known.has(m.id))
      return fresh.length ? [...current, ...fresh] : current
    })
    lastId.current = Math.max(lastId.current, incoming[incoming.length - 1].id)
  }

  useEffect(() => {
    let stopped = false
    const load = async () => {
      try {
        const list = await listMessages(swapId, lastId.current)
        if (!stopped) addMessages(list)
      } catch {

      }
    }
    load()
    const timer = setInterval(load, 3000)
    return () => {
      stopped = true
      clearInterval(timer)
    }
  }, [swapId])

  useEffect(() => {
    const el = listRef.current
    if (el) el.scrollTop = el.scrollHeight
  }, [messages.length])

  const submit = async (e) => {
    e.preventDefault()
    if (!text.trim()) return
    setSending(true)
    try {
      addMessages([await sendMessage(swapId, text)])
      setText('')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setSending(false)
    }
  }

  return (
    <Card className="flex h-[440px] flex-col overflow-hidden">
      <h2 className="flex items-center gap-2 border-b border-line px-5 py-4 text-lg font-bold">
        <MessageCircle className="h-5 w-5 text-brand" aria-hidden="true" /> Chat
      </h2>
      <div ref={listRef} className="flex-1 space-y-3 overflow-y-auto px-5 py-4" aria-live="polite">
        {messages.length === 0 && <p className="py-10 text-center text-sm text-muted">No messages yet. Say hello.</p>}
        {messages.map((m) => {
          const mine = m.senderId === meId
          return (
            <div key={m.id} className={`flex ${mine ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[80%] rounded-2xl px-4 py-2.5 text-sm ${mine ? 'rounded-br-md bg-brand text-white' : 'rounded-bl-md bg-brand-100 text-ink'}`}>
                {!mine && <p className="mb-0.5 text-[11px] font-bold text-brand-700">{m.senderName}</p>}
                <p className="whitespace-pre-wrap break-words">{m.text}</p>
                <p className={`mt-1 text-[10px] ${mine ? 'text-white/70' : 'text-muted'}`}>
                  {new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </p>
              </div>
            </div>
          )
        })}
      </div>
      {canWrite ? (
        <form onSubmit={submit} className="flex gap-2 border-t border-line p-3">
          <input
            value={text}
            onChange={(e) => setText(e.target.value)}
            maxLength={1000}
            placeholder="Write a message"
            aria-label="Message"
            className="min-w-0 flex-1 rounded-full border border-line bg-white px-4 py-2 text-sm focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/15"
          />
          <Button type="submit" loading={sending} disabled={!text.trim()} aria-label="Send message">
            <Send className="h-4 w-4" aria-hidden="true" /> Send
          </Button>
        </form>
      ) : (
        <p className="border-t border-line px-5 py-3 text-center text-xs text-muted">This swap is finished, so the chat is read only.</p>
      )}
    </Card>
  )
}

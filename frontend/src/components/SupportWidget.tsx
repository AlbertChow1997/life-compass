import { useState, type FormEvent } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'

interface ChatEntry {
  question: string
  answer: string
}

/** Floating support widget (bottom-right): keyword-matched auto-replies, available signed in or out. */
export default function SupportWidget() {
  const [open, setOpen] = useState(false)
  const [question, setQuestion] = useState('')
  const [history, setHistory] = useState<ChatEntry[]>([])
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit(e: FormEvent) {
    e.preventDefault()
    const asked = question.trim()
    if (!asked) return
    setError(null)
    setSending(true)
    setQuestion('')
    try {
      const res = await api.post<ApiResult<{ answer: string; matched: boolean }>>('/support/ask', {
        question: asked,
      })
      const data = res.data.data
      if (data) {
        setHistory((h) => [...h, { question: asked, answer: data.answer }])
      }
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not send your question'))
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="support-widget">
      {open && (
        <div className="support-panel">
          <div className="support-panel-header">
            <span>Support</span>
            <button className="link-button" type="button" onClick={() => setOpen(false)} aria-label="Close">
              ✕
            </button>
          </div>

          <div className="support-history">
            {history.length === 0 && <p className="muted">Ask us anything about LifeCompass.</p>}
            {history.map((entry, i) => (
              <div key={i} className="support-entry">
                <p className="support-question">{entry.question}</p>
                <p className="support-answer">{entry.answer}</p>
              </div>
            ))}
          </div>

          {error && <div className="notice notice-error">{error}</div>}

          <form className="support-form" onSubmit={submit}>
            <input
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder="Type your question…"
              disabled={sending}
            />
            <button className="btn-primary" type="submit" disabled={sending}>
              Send
            </button>
          </form>
        </div>
      )}

      <button
        className="support-fab"
        type="button"
        onClick={() => setOpen((v) => !v)}
        aria-label={open ? 'Close support' : 'Open support'}
      >
        {open ? '✕' : '?'}
      </button>
    </div>
  )
}

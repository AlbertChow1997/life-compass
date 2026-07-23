import { useEffect, useState, type FormEvent } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'

interface SupportFaq {
  id: number
  keywords: string
  answer: string
  status: number
}

interface SupportMessage {
  id: number
  userId?: number
  question: string
  matchedFaqId?: number
  answerGiven?: string
  createTime?: string
}

/** Requirement (support): admins manage auto-reply FAQ entries and review customer questions. */
export default function AdminSupportPage() {
  const [faqs, setFaqs] = useState<SupportFaq[]>([])
  const [messages, setMessages] = useState<SupportMessage[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [keywords, setKeywords] = useState('')
  const [answer, setAnswer] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)

  async function load() {
    setLoading(true)
    try {
      const [faqRes, msgRes] = await Promise.all([
        api.get<ApiResult<SupportFaq[]>>('/admin/support/faq'),
        api.get<ApiResult<SupportMessage[]>>('/admin/support/messages'),
      ])
      setFaqs(faqRes.data.data ?? [])
      setMessages(msgRes.data.data ?? [])
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not load support data'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  function startEdit(faq: SupportFaq) {
    setEditingId(faq.id)
    setKeywords(faq.keywords)
    setAnswer(faq.answer)
  }

  function resetForm() {
    setEditingId(null)
    setKeywords('')
    setAnswer('')
  }

  async function submitFaq(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      if (editingId != null) {
        await api.put(`/admin/support/faq/${editingId}`, { keywords, answer })
      } else {
        await api.post('/admin/support/faq', { keywords, answer })
      }
      resetForm()
      load()
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not save FAQ entry'))
    }
  }

  async function deleteFaq(id: number) {
    if (!window.confirm('Delete this FAQ entry?')) return
    setError(null)
    try {
      await api.delete(`/admin/support/faq/${id}`)
      if (editingId === id) resetForm()
      load()
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not delete FAQ entry'))
    }
  }

  if (loading) {
    return (
      <section className="page">
        <p className="muted">Loading…</p>
      </section>
    )
  }

  return (
    <section className="page">
      <div className="hero">
        <h1>Support</h1>
        <p className="muted">Manage auto-reply FAQ entries and review questions customers have asked.</p>
      </div>

      {error && <div className="notice notice-error">{error}</div>}

      <h2>{editingId != null ? 'Edit FAQ entry' : 'Add FAQ entry'}</h2>
      <form className="auth-form new-post-form" onSubmit={submitFaq}>
        <label>
          Keywords (comma-separated)
          <input
            value={keywords}
            onChange={(e) => setKeywords(e.target.value)}
            placeholder="voucher, coupon, discount"
            required
          />
        </label>
        <label>
          Answer
          <textarea value={answer} onChange={(e) => setAnswer(e.target.value)} rows={3} required />
        </label>
        <div className="card-meta">
          <button className="btn-primary" type="submit">
            {editingId != null ? 'Save changes' : 'Add'}
          </button>
          {editingId != null && (
            <button className="link-button" type="button" onClick={resetForm}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <h2>FAQ entries</h2>
      <div className="posts">
        {faqs.map((faq) => (
          <article key={faq.id} className="post-card">
            <p className="post-author">{faq.keywords}</p>
            <p className="post-content">{faq.answer}</p>
            <div className="card-meta">
              <button className="btn-ghost" type="button" onClick={() => startEdit(faq)}>
                Edit
              </button>
              <button className="btn-ghost" type="button" onClick={() => deleteFaq(faq.id)}>
                Delete
              </button>
            </div>
          </article>
        ))}
        {faqs.length === 0 && <p className="muted">No FAQ entries yet.</p>}
      </div>

      <h2>Customer questions</h2>
      <div className="posts">
        {messages.map((m) => (
          <article key={m.id} className="post-card">
            <p className="post-content">{m.question}</p>
            <p className="muted">{m.matchedFaqId ? `Auto-answered: ${m.answerGiven}` : 'No match — needs a human reply'}</p>
          </article>
        ))}
        {messages.length === 0 && <p className="muted">No questions yet.</p>}
      </div>
    </section>
  )
}

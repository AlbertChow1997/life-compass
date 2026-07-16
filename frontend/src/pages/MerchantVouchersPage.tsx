import { useEffect, useState, type FormEvent } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { Shop, Voucher } from '../types'
import { useAuth } from '../context/AuthContext'
import { euro } from '../format'

/** Requirement 7: merchants manage vouchers for the shop(s) they own. */
export default function MerchantVouchersPage() {
  const { user } = useAuth()
  const [vouchers, setVouchers] = useState<Voucher[]>([])
  const [myShops, setMyShops] = useState<Shop[]>([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState<string | null>(null)

  const [shopId, setShopId] = useState<number | ''>('')
  const [title, setTitle] = useState('')
  const [payValue, setPayValue] = useState('')
  const [actualValue, setActualValue] = useState('')

  async function load() {
    setLoading(true)
    try {
      const [voucherRes, shopRes] = await Promise.all([
        api.get<ApiResult<Voucher[]>>('/merchant/voucher'),
        api.get<ApiResult<Shop[]>>('/shop'),
      ])
      setVouchers(voucherRes.data.data ?? [])
      const mine = (shopRes.data.data ?? []).filter((s) => s.ownerId === user?.userId)
      setMyShops(mine)
      setShopId((current) => (current === '' && mine.length > 0 ? mine[0].id : current))
    } catch (err) {
      setMessage(apiErrorMessage(err, 'Could not load your vouchers.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function toggleShelf(v: Voucher) {
    setMessage(null)
    try {
      await api.put(`/merchant/voucher/${v.id}/shelf?onShelf=${v.status !== 1}`)
      load()
    } catch (err) {
      setMessage(apiErrorMessage(err, 'Could not update voucher'))
    }
  }

  async function createVoucher(e: FormEvent) {
    e.preventDefault()
    setMessage(null)
    try {
      await api.post('/merchant/voucher', {
        shopId,
        title,
        payValue: Math.round(Number(payValue) * 100),
        actualValue: Math.round(Number(actualValue) * 100),
      })
      setTitle('')
      setPayValue('')
      setActualValue('')
      setMessage('Voucher created.')
      load()
    } catch (err) {
      setMessage(apiErrorMessage(err, 'Could not create voucher'))
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
        <h1>My vouchers</h1>
        <p className="muted">Manage vouchers for the shops you own.</p>
      </div>

      {message && <div className="notice">{message}</div>}

      {myShops.length === 0 ? (
        <p className="muted">You don't own any shops yet — ask an admin to assign one to your account.</p>
      ) : (
        <>
          <form className="auth-form" onSubmit={createVoucher}>
            <label>
              Shop
              <select value={shopId} onChange={(e) => setShopId(Number(e.target.value))}>
                {myShops.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Title
              <input value={title} onChange={(e) => setTitle(e.target.value)} required />
            </label>
            <label>
              Price paid by customer (EUR)
              <input type="number" step="0.01" min="0" value={payValue} onChange={(e) => setPayValue(e.target.value)} required />
            </label>
            <label>
              Face value (EUR)
              <input
                type="number"
                step="0.01"
                min="0"
                value={actualValue}
                onChange={(e) => setActualValue(e.target.value)}
                required
              />
            </label>
            <button className="btn-primary" type="submit">
              Create voucher
            </button>
          </form>

          <div className="grid">
            {vouchers.map((v) => (
              <article key={v.id} className="card">
                <div className="card-body">
                  <h3>{v.title}</h3>
                  <p className="muted">{v.status === 1 ? 'On shelf' : 'Off shelf'}</p>
                  <div className="card-meta">
                    <span className="price">{euro(v.payValue)}</span>
                    <span className="muted">worth {euro(v.actualValue)}</span>
                  </div>
                  <button className="btn-ghost" type="button" onClick={() => toggleShelf(v)}>
                    {v.status === 1 ? 'Take off shelf' : 'Put on shelf'}
                  </button>
                </div>
              </article>
            ))}
            {vouchers.length === 0 && <p className="muted">No vouchers yet — create one above.</p>}
          </div>
        </>
      )}
    </section>
  )
}

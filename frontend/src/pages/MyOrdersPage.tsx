import { useEffect, useState } from 'react'
import { api, apiErrorMessage, type ApiResult } from '../api/client'
import type { VoucherOrder } from '../types'

// Maps the backend's numeric order status codes to human-readable labels for display.
const STATUS_LABEL: Record<number, string> = {
  1: 'Unpaid',
  2: 'Paid',
  3: 'Used',
  4: 'Cancelled',
  5: 'Refunded',
}

/** Read-only history of the signed-in user's voucher purchases, with their current status. */
export default function MyOrdersPage() {
  const [orders, setOrders] = useState<VoucherOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<ApiResult<VoucherOrder[]>>('/user/orders')
      .then((res) => setOrders(res.data.data ?? []))
      .catch((err) => setError(apiErrorMessage(err, 'Could not load your orders.')))
      .finally(() => setLoading(false))
  }, [])

  return (
    <section className="page">
      <div className="hero">
        <h1>My orders</h1>
      </div>

      {loading && <p className="muted">Loading…</p>}
      {error && <div className="notice">{error}</div>}

      {!loading && !error && (
        <div className="posts">
          {orders.map((order) => (
            <article key={order.id} className="post-card">
              <h3>{order.voucherTitle ?? `Voucher #${order.voucherId}`}</h3>
              <p className="muted">{order.shopName}</p>
              <div className="card-meta">
                <span className="badge">{STATUS_LABEL[order.status] ?? 'Unknown'}</span>
                <span className="muted">{order.createTime?.slice(0, 10)}</span>
              </div>
            </article>
          ))}
          {orders.length === 0 && <p className="muted">You haven't bought any vouchers yet.</p>}
        </div>
      )}
    </section>
  )
}

import { useEffect, useState } from 'react'
import QRCode from 'qrcode'
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

const PAID = 2

/**
 * Read-only history of the signed-in user's voucher purchases, with their current status.
 * Clicking a paid (unredeemed) order expands it in place to show a QR code plus the same
 * verification code as text — a merchant scans or manually enters this to redeem the
 * voucher (see MerchantVouchersPage.tsx's redeem form).
 */
export default function MyOrdersPage() {
  const [orders, setOrders] = useState<VoucherOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [expanded, setExpanded] = useState<number | null>(null)
  const [qrDataUrls, setQrDataUrls] = useState<Record<number, string>>({})

  useEffect(() => {
    api
      .get<ApiResult<VoucherOrder[]>>('/user/orders')
      .then((res) => setOrders(res.data.data ?? []))
      .catch((err) => setError(apiErrorMessage(err, 'Could not load your orders.')))
      .finally(() => setLoading(false))
  }, [])

  // Generates the QR image once per order (cached in qrDataUrls) the first time it's
  // expanded, rather than on every render or for orders never clicked.
  function toggleExpand(order: VoucherOrder) {
    if (order.status !== PAID) return
    const next = expanded === order.id ? null : order.id
    setExpanded(next)
    if (next != null && order.verifyCode && !qrDataUrls[order.id]) {
      QRCode.toDataURL(order.verifyCode, { margin: 1, width: 200 }).then((url) => {
        setQrDataUrls((prev) => ({ ...prev, [order.id]: url }))
      })
    }
  }

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
            <article
              key={order.id}
              className={order.status === PAID ? 'post-card order-card-clickable' : 'post-card'}
              onClick={() => toggleExpand(order)}
            >
              <h3>{order.voucherTitle ?? `Voucher #${order.voucherId}`}</h3>
              <p className="muted">{order.shopName}</p>
              <div className="card-meta">
                <span className="badge">{STATUS_LABEL[order.status] ?? 'Unknown'}</span>
                <span className="muted">{order.createTime?.slice(0, 10)}</span>
              </div>
              {order.status === PAID && expanded === order.id && (
                <div className="order-qr-panel" onClick={(e) => e.stopPropagation()}>
                  {qrDataUrls[order.id] && <img src={qrDataUrls[order.id]} alt="Redemption QR code" />}
                  <div className="order-code">{order.verifyCode}</div>
                  <p className="muted">Show this to staff to redeem.</p>
                </div>
              )}
            </article>
          ))}
          {orders.length === 0 && <p className="muted">You haven't bought any vouchers yet.</p>}
        </div>
      )}
    </section>
  )
}

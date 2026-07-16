/** Formatting helpers shared across pages. */

/** Euro cents -> "€12.50". */
export function euro(cents?: number): string {
  if (cents == null) return '—'
  return `€${(cents / 100).toFixed(2)}`
}

/** score (rating x10, 0..50) -> "4.6". */
export function stars(score: number): string {
  return (score / 10).toFixed(1)
}

/** First URL from a comma-separated `images` field, or null if there isn't one. */
export function firstImage(images?: string): string | null {
  if (!images) return null
  const first = images.split(',')[0]?.trim()
  return first || null
}

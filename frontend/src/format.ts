/** Small formatting helpers shared across pages, for values the backend sends in raw form. */

/** Converts integer euro cents to a display string, e.g. 1250 -> "€12.50". Returns "—" when missing. */
export function euro(cents?: number): string {
  if (cents == null) return '—'
  return `€${(cents / 100).toFixed(2)}`
}

/** Converts a rating score (stored as rating x10, range 0..50) to a one-decimal string, e.g. 46 -> "4.6". */
export function stars(score: number): string {
  return (score / 10).toFixed(1)
}

/** Picks out the first URL from a comma-separated `images` field, or null if there isn't one. */
export function firstImage(images?: string): string | null {
  if (!images) return null
  const first = images.split(',')[0]?.trim()
  return first || null
}

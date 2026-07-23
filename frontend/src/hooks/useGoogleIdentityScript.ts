import { useEffect, useState } from 'react'

const SCRIPT_SRC = 'https://accounts.google.com/gsi/client'
// Module-level cache so the <script> tag is only ever injected once, even if
// multiple components using this hook mount at the same time.
let scriptPromise: Promise<void> | null = null

function loadScript(): Promise<void> {
  if (scriptPromise) return scriptPromise
  scriptPromise = new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${SCRIPT_SRC}"]`)) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = SCRIPT_SRC
    script.async = true
    script.defer = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Failed to load Google Identity Services'))
    document.head.appendChild(script)
  })
  return scriptPromise
}

/**
 * Lazily loads the Google Identity Services script (only when a component
 * that needs the "Sign in with Google" button actually mounts) and reports
 * once `window.google.accounts.id` is ready to use.
 */
export function useGoogleIdentityScript(): boolean {
  const [ready, setReady] = useState(() => Boolean(window.google?.accounts?.id))

  useEffect(() => {
    if (ready) return
    let cancelled = false
    loadScript()
      .then(() => {
        if (!cancelled) setReady(true)
      })
      .catch(() => {
        // Leave ready=false; the caller shows a fallback message.
      })
    return () => {
      cancelled = true
    }
  }, [ready])

  return ready
}

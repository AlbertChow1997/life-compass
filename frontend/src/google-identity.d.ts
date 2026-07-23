export {}

/**
 * Minimal ambient type declarations for the subset of Google Identity Services (GIS) we use.
 * The actual `google` object is attached to `window` at runtime by the GIS script loaded
 * in useGoogleIdentityScript; this file just tells TypeScript that shape exists.
 */
declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string
            callback: (response: { credential: string }) => void
          }) => void
          renderButton: (
            parent: HTMLElement,
            options: {
              type?: 'standard' | 'icon'
              theme?: 'outline' | 'filled_blue' | 'filled_black'
              size?: 'large' | 'medium' | 'small'
              width?: number
              text?: 'signin_with' | 'signup_with' | 'continue_with' | 'signin'
            },
          ) => void
          disableAutoSelect: () => void
        }
      }
    }
  }
}

/// <reference types="vite/client" />

/** Typed shape of the Vite env vars this app reads via `import.meta.env`. */
interface ImportMetaEnv {
  /** OAuth Web Client ID from Google Cloud Console. Required for Google sign-in. */
  readonly VITE_GOOGLE_CLIENT_ID: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

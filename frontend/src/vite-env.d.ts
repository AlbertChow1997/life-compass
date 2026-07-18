/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** OAuth Web Client ID from Google Cloud Console. Required for Google sign-in. */
  readonly VITE_GOOGLE_CLIENT_ID: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

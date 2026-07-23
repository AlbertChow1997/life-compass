import axios, { isAxiosError } from 'axios'

/**
 * Central place for talking to the backend: a preconfigured axios instance plus
 * helpers for unwrapping the backend's standard response envelope and error messages.
 * Requests hit `/api/*`, which Vite proxies to the Spring Boot backend in development
 * (see vite.config.ts).
 */
export const api = axios.create({
  baseURL: '/api',
  timeout: 10_000,
})

// Attach the JWT to every outgoing request so protected endpoints authenticate automatically.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/** Shape of the backend's uniform Result<T> envelope. */
export interface ApiResult<T> {
  success: boolean
  errorMsg?: string
  data?: T
  total?: number
}

/** Pulls the backend's Result.errorMsg out of a failed request, with a fallback. */
export function apiErrorMessage(err: unknown, fallback: string): string {
  if (isAxiosError(err)) {
    const msg = (err.response?.data as ApiResult<unknown> | undefined)?.errorMsg
    if (msg) return msg
  }
  return fallback
}

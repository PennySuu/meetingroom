import axios, { type AxiosError } from 'axios'
import { getActivePinia } from 'pinia'
import type { ApiEnvelope } from '@/types/api'

function readXsrfToken(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/)
  return match ? decodeURIComponent(match[1]) : null
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 30_000,
  withCredentials: true,
})

http.interceptors.request.use((config) => {
  config.headers.set('X-Request-ID', crypto.randomUUID())
  const method = config.method?.toLowerCase()
  if (method && ['post', 'put', 'patch', 'delete'].includes(method)) {
    const xsrf = readXsrfToken()
    if (xsrf) {
      config.headers.set('X-XSRF-TOKEN', xsrf)
    }
  }
  return config
})

http.interceptors.response.use(
  (res) => res,
  async (err) => {
    if (axios.isAxiosError(err) && err.response?.status === 401 && getActivePinia()) {
      const { useAuthStore } = await import('@/stores/auth')
      useAuthStore().clearUser()
    }
    return Promise.reject(err)
  },
)

/** 解析成功信封中的 data */
export function unwrap<T>(env: ApiEnvelope<T>): T {
  if (!env.success) {
    throw new Error(env.message || '请求失败')
  }
  return env.data
}

export function isAxiosEnvelopeError(err: unknown): err is AxiosError<ApiEnvelope<null>> {
  return axios.isAxiosError(err) && !!err.response?.data && typeof err.response.data === 'object'
}

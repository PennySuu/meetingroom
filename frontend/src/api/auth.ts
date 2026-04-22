import { http, unwrap } from '@/api/http'
import type { ApiEnvelope, PasswordParamsDto, UserSnapshot } from '@/types/api'

export async function fetchPasswordParams(): Promise<PasswordParamsDto> {
  const { data } = await http.get<ApiEnvelope<PasswordParamsDto>>('/v1/auth/password-params')
  return unwrap(data)
}

export async function register(payload: {
  username: string
  passwordPrehash: string
  displayName?: string
}): Promise<UserSnapshot> {
  const { data } = await http.post<ApiEnvelope<UserSnapshot>>('/v1/auth/register', payload)
  return unwrap(data)
}

export async function login(payload: {
  username: string
  passwordPrehash: string
}): Promise<UserSnapshot> {
  const { data } = await http.post<ApiEnvelope<UserSnapshot>>('/v1/auth/login', payload)
  return unwrap(data)
}

export async function logout(): Promise<void> {
  const { data } = await http.post<ApiEnvelope<null>>('/v1/auth/logout')
  unwrap(data)
}

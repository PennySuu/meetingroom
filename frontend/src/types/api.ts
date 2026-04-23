/** 统一 API 信封（对齐 openapi） */
export interface ApiEnvelope<T> {
  success: boolean
  code: string
  message: string
  data: T
}

export interface PasswordParamsDto {
  pepper: string
  formulaVersion: 1
}

export interface UserSnapshot {
  id: number
  username: string
  displayName: string | null
}

export interface RoomDto {
  id: number
  name: string
  location: string
  capacity: number
  amenities: string[]
}

export interface OccupiedSlot {
  startAt: string
  endAt: string
}

export interface OccupiedSlotsData {
  items: OccupiedSlot[]
}

export interface BookingDto {
  id: number
  roomId: number
  title: string
  startAt: string
  endAt: string
  status: 'ACTIVE' | 'CANCELLED'
}

export interface PageBooking {
  page: number
  size: number
  total: number
  items: BookingDto[]
}

export interface BookingConflictUserData {
  conflictingRoom: { id: number; name: string }
}

export interface ApiErrorBody {
  success: false
  code: string
  message: string
  data?: Record<string, unknown> | BookingConflictUserData | null
}

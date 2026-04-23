/** 访客提交前暂存的预约意图（sessionStorage） */

const KEY = 'meetingroom:bookingIntent'

export interface BookingIntent {
  roomId: number
  /** YYYY-MM-DD */
  date: string
  startAt: string
  endAt: string
  title: string
}

export function saveBookingIntent(intent: BookingIntent): void {
  sessionStorage.setItem(KEY, JSON.stringify(intent))
}

export function loadBookingIntent(): BookingIntent | null {
  const raw = sessionStorage.getItem(KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as BookingIntent
  } catch {
    return null
  }
}

export function clearBookingIntent(): void {
  sessionStorage.removeItem(KEY)
}

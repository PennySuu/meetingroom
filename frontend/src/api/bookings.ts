import { http, unwrap } from '@/api/http'
import type { ApiEnvelope, BookingDto, PageBooking } from '@/types/api'

export async function createBooking(
  idempotencyKey: string,
  body: { roomId: number; startAt: string; endAt: string; title: string },
): Promise<BookingDto> {
  const { data } = await http.post<ApiEnvelope<BookingDto>>('/v1/bookings', body, {
    headers: {
      'Idempotency-Key': idempotencyKey,
    },
  })
  return unwrap(data)
}

export async function listMyBookings(params: {
  roomId?: number
  dateFrom?: string
  dateTo?: string
  page?: number
  size?: number
  sort?: string
}): Promise<PageBooking> {
  const { data } = await http.get<ApiEnvelope<PageBooking>>('/v1/bookings/mine', {
    params,
  })
  return unwrap(data)
}

export async function cancelBooking(bookingId: number): Promise<BookingDto> {
  const { data } = await http.post<ApiEnvelope<BookingDto>>(
    `/v1/bookings/${bookingId}/cancel`,
  )
  return unwrap(data)
}

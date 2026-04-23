import { http, unwrap } from '@/api/http'
import type {
  ApiEnvelope,
  OccupiedSlotsData,
  RoomDto,
} from '@/types/api'

export async function listRooms(params: {
  capacityGte?: number
  amenities?: string[]
}): Promise<RoomDto[]> {
  const sp = new URLSearchParams()
  if (params.capacityGte != null) {
    sp.set('capacityGte', String(params.capacityGte))
  }
  for (const a of params.amenities ?? []) {
    sp.append('amenity', a)
  }
  const qs = sp.toString()
  const url = qs ? `/v1/rooms?${qs}` : '/v1/rooms'
  const { data } = await http.get<ApiEnvelope<RoomDto[]>>(url)
  return unwrap(data)
}

export async function getRoom(roomId: number): Promise<RoomDto> {
  const { data } = await http.get<ApiEnvelope<RoomDto>>(`/v1/rooms/${roomId}`)
  return unwrap(data)
}

export async function getRoomCalendar(
  roomId: number,
  date: string,
  timeZone = 'Asia/Shanghai',
): Promise<OccupiedSlotsData> {
  const { data } = await http.get<ApiEnvelope<OccupiedSlotsData>>(
    `/v1/rooms/${roomId}/calendar`,
    { params: { date, timeZone } },
  )
  return unwrap(data)
}

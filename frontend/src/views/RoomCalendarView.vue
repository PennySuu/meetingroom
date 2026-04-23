<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { getRoom, getRoomCalendar } from '@/api/rooms'
import { createBooking } from '@/api/bookings'
import { useAuthStore } from '@/stores/auth'
import {
  clearBookingIntent,
  loadBookingIntent,
  saveBookingIntent,
} from '@/composables/useBookingIntent'
import {
  buildShanghaiIso,
  GRID_END_MIN,
  GRID_START_MIN,
  SLOT_MINUTES,
  isoRangeToSlotIndexes,
  occupiedStyle,
  slotCount,
  slotIndexToMinutesFromMidnight,
} from '@/utils/timeGrid'
import type { BookingConflictUserData, OccupiedSlot, RoomDto } from '@/types/api'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const room = ref<RoomDto | null>(null)
const loadError = ref('')
const calendarItems = ref<OccupiedSlot[]>([])
const dateStr = ref<string>('')
const startSlot = ref<number | null>(null)
const endSlot = ref<number | null>(null)
const title = ref('')
const panelOpen = ref(false)
const idempotencyKey = ref(crypto.randomUUID())
const submitError = ref('')
const calendarLoading = ref(false)

const roomId = computed(() => Number(route.params.roomId))

function todayYmd(): string {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const minDate = todayYmd()

const slots = computed(() => {
  const n = slotCount()
  return Array.from({ length: n }, (_, i) => {
    const m = slotIndexToMinutesFromMidnight(i)
    const h = Math.floor(m / 60)
    const mm = m % 60
    return { index: i, label: `${String(h).padStart(2, '0')}:${String(mm).padStart(2, '0')}` }
  })
})

const selectedRangeLabel = computed(() => {
  if (startSlot.value == null || endSlot.value == null) return ''
  const s = buildShanghaiIso(dateStr.value, slotIndexToMinutesFromMidnight(startSlot.value))
  const e = buildShanghaiIso(
    dateStr.value,
    slotIndexToMinutesFromMidnight(endSlot.value) + SLOT_MINUTES,
  )
  return `${s} – ${e}`
})

const durationMinutes = computed(() => {
  if (startSlot.value == null || endSlot.value == null) return 0
  return (endSlot.value - startSlot.value + 1) * SLOT_MINUTES
})

function onSlotClick(index: number) {
  if (index < 0 || index >= slotCount()) return
  if (startSlot.value == null) {
    startSlot.value = index
    endSlot.value = null
    return
  }
  if (endSlot.value == null) {
    if (index < startSlot.value) {
      startSlot.value = index
      return
    }
    if ((index - startSlot.value + 1) * SLOT_MINUTES > 240) {
      submitError.value = '单次预约最长 4 小时'
      return
    }
    endSlot.value = index
    panelOpen.value = true
    idempotencyKey.value = crypto.randomUUID()
    submitError.value = ''
    return
  }
  startSlot.value = index
  endSlot.value = null
}

function closePanel() {
  panelOpen.value = false
}

function openLogin() {
  if (startSlot.value == null || endSlot.value == null) return
  const startAt = buildShanghaiIso(dateStr.value, slotIndexToMinutesFromMidnight(startSlot.value))
  const endAt = buildShanghaiIso(
    dateStr.value,
    slotIndexToMinutesFromMidnight(endSlot.value) + SLOT_MINUTES,
  )
  saveBookingIntent({
    roomId: roomId.value,
    date: dateStr.value,
    startAt,
    endAt,
    title: title.value,
  })
  router.push({
    name: 'login',
    query: { redirect: route.fullPath },
  })
}

async function submitBooking() {
  submitError.value = ''
  if (startSlot.value == null || endSlot.value == null) return
  if (!title.value.trim()) {
    submitError.value = '请填写会议主题'
    return
  }
  if (!auth.isAuthenticated) {
    openLogin()
    return
  }
  const startAt = buildShanghaiIso(dateStr.value, slotIndexToMinutesFromMidnight(startSlot.value))
  const endAt = buildShanghaiIso(
    dateStr.value,
    slotIndexToMinutesFromMidnight(endSlot.value) + SLOT_MINUTES,
  )
  if (new Date(startAt).getTime() < Date.now() - 30_000) {
    submitError.value = '开始时间不能早于当前时间'
    return
  }
  try {
    await createBooking(idempotencyKey.value, {
      roomId: roomId.value,
      startAt,
      endAt,
      title: title.value.trim(),
    })
    clearBookingIntent()
    closePanel()
    startSlot.value = null
    endSlot.value = null
    title.value = ''
    idempotencyKey.value = crypto.randomUUID()
    await loadCalendar()
  } catch (e: unknown) {
    if (axios.isAxiosError(e) && e.response?.data && typeof e.response.data === 'object') {
      const body = e.response.data as { code?: string; message?: string; data?: unknown }
      if (body.code === 'BOOKING_CONFLICT_USER' && body.data) {
        const d = body.data as BookingConflictUserData
        submitError.value = `该时段您已预约其他会议室：${d.conflictingRoom.name}，请先调整`
        return
      }
      if (body.code === 'BOOKING_CONFLICT_ROOM') {
        submitError.value = '该时段已被占用，请更换时间或其他会议室'
        await loadCalendar()
        return
      }
      if (body.message) {
        submitError.value = body.message
        return
      }
    }
    submitError.value = '预约失败，请重试'
  }
}

async function loadRoom() {
  try {
    room.value = await getRoom(roomId.value)
  } catch {
    loadError.value = '会议室不存在或加载失败'
  }
}

async function loadCalendar() {
  if (!dateStr.value) return
  calendarLoading.value = true
  try {
    const data = await getRoomCalendar(roomId.value, dateStr.value)
    calendarItems.value = data.items
  } catch {
    loadError.value = '日历加载失败'
  } finally {
    calendarLoading.value = false
  }
}

onMounted(async () => {
  const qd = route.query.date
  dateStr.value = typeof qd === 'string' && qd ? qd : todayYmd()
  await loadRoom()
  if (room.value) {
    await loadCalendar()
    const intent = loadBookingIntent()
    if (
      intent &&
      intent.roomId === roomId.value &&
      intent.date === dateStr.value &&
      auth.isAuthenticated
    ) {
      title.value = intent.title
      const idx = isoRangeToSlotIndexes(intent.startAt, intent.endAt)
      startSlot.value = idx.start
      endSlot.value = idx.end
      panelOpen.value = true
      submitError.value = ''
    }
  }
})

watch(dateStr, async () => {
  router.replace({
    query: { ...route.query, date: dateStr.value },
  })
  await loadCalendar()
})

watch(roomId, async () => {
  await loadRoom()
  await loadCalendar()
})
</script>

<template>
  <div>
    <div v-if="loadError && !room" class="rounded-lg border border-red-200 bg-red-50 p-4 text-red-800">
      {{ loadError }}
    </div>
    <template v-else-if="room">
      <div class="mb-4">
        <h1 class="text-2xl font-semibold text-slate-900">{{ room.name }}</h1>
        <p class="text-slate-600">{{ room.location }}</p>
      </div>

      <label class="mb-4 flex items-center gap-2 text-sm">
        <span class="text-slate-600">日期</span>
        <input v-model="dateStr" type="date" :min="minDate" class="rounded border border-slate-300 px-2 py-1" />
      </label>

      <p v-if="calendarLoading" class="text-slate-500">刷新日历…</p>

      <div class="relative flex gap-2">
        <div class="w-14 shrink-0 text-right text-xs text-slate-500">
          <div v-for="s in slots" :key="s.index" class="h-8 leading-8">{{ s.label }}</div>
        </div>
        <div class="relative min-h-[768px] flex-1 rounded border border-slate-200 bg-emerald-50">
          <!-- 占用块 -->
          <div
            v-for="(seg, idx) in calendarItems"
            :key="idx"
            class="pointer-events-none absolute left-0 right-0 bg-slate-400/70"
            :style="occupiedStyle(dateStr, seg.startAt, seg.endAt)"
          >
            <span
              class="absolute left-1 top-1 rounded bg-white/90 px-1 text-xs text-slate-700 shadow"
            >
              已占用
            </span>
          </div>
          <!-- 可点格 -->
          <button
            v-for="s in slots"
            :key="'b' + s.index"
            type="button"
            class="absolute left-0 right-0 box-border border-b border-emerald-200/60 text-left hover:bg-emerald-100/50"
            :class="{
              'bg-blue-200/60':
                startSlot != null &&
                endSlot != null &&
                s.index >= startSlot &&
                s.index <= endSlot,
              'ring-2 ring-blue-500': startSlot === s.index || endSlot === s.index,
            }"
            :style="{
              top: `${((s.index / slotCount()) * 100).toFixed(4)}%`,
              height: `${(100 / slotCount()).toFixed(4)}%`,
            }"
            @click="onSlotClick(s.index)"
          />
        </div>
      </div>

      <p class="mt-2 text-xs text-slate-500">
        工作时间 {{ GRID_START_MIN / 60 }}:00–{{ GRID_END_MIN / 60 }}:00，每 {{ SLOT_MINUTES }} 分钟一格；单击选择开始，再单击结束（最长 4 小时）。
      </p>

      <div
        v-if="panelOpen"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
        role="dialog"
        aria-modal="true"
      >
        <div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
          <h2 class="mb-2 text-lg font-semibold">确认预约</h2>
          <p class="mb-4 text-sm text-slate-600">{{ selectedRangeLabel }}</p>
          <p class="mb-2 text-sm text-slate-600">时长 {{ durationMinutes }} 分钟</p>
          <label class="mb-4 block text-sm">
            <span class="text-slate-700">会议主题</span>
            <input
              v-model="title"
              maxlength="200"
              class="mt-1 w-full rounded border border-slate-300 px-3 py-2"
              placeholder="必填"
            />
          </label>
          <p v-if="submitError" class="mb-2 text-sm text-red-600">{{ submitError }}</p>
          <div class="flex justify-end gap-2">
            <button
              type="button"
              class="rounded-md border border-slate-300 px-3 py-1.5 text-slate-700 hover:bg-slate-50"
              @click="closePanel"
            >
              取消
            </button>
            <button
              type="button"
              class="rounded-md bg-blue-600 px-3 py-1.5 text-white hover:bg-blue-700"
              @click="submitBooking"
            >
              确认预约
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

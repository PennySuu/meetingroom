<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import axios from 'axios'
import { cancelBooking, listMyBookings } from '@/api/bookings'
import { listRooms } from '@/api/rooms'
import type { BookingDto, RoomDto } from '@/types/api'

const rooms = ref<RoomDto[]>([])
const items = ref<BookingDto[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = 10
const roomFilter = ref<number | ''>('')
const dateFrom = ref('')
const dateTo = ref('')
const loading = ref(false)
const loadErr = ref('')
const cancelTarget = ref<BookingDto | null>(null)
const cancelling = ref(false)

async function loadRooms() {
  rooms.value = await listRooms({})
}

async function load() {
  loading.value = true
  loadErr.value = ''
  try {
    const roomId =
      roomFilter.value === '' || roomFilter.value === null ? undefined : Number(roomFilter.value)
    const df = dateFrom.value || undefined
    const dt = dateTo.value || undefined
    if ((df && !dt) || (!df && dt)) {
      loadErr.value = '请同时选择开始日期与结束日期，或清空两者'
      return
    }
    const data = await listMyBookings({
      roomId,
      dateFrom: df,
      dateTo: dt,
      page: page.value,
      size: pageSize,
      sort: 'startAt,desc',
    })
    items.value = data.items
    total.value = Number(data.total)
  } catch {
    loadErr.value = '加载失败'
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  roomFilter.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  page.value = 0
  load()
}

function canCancel(b: BookingDto): boolean {
  if (b.status !== 'ACTIVE') return false
  const start = new Date(b.startAt).getTime()
  return start > Date.now()
}

async function confirmCancel() {
  if (!cancelTarget.value) return
  cancelling.value = true
  try {
    await cancelBooking(cancelTarget.value.id)
    cancelTarget.value = null
    await load()
  } catch (e: unknown) {
    if (axios.isAxiosError(e) && e.response?.data && typeof e.response.data === 'object') {
      const msg = (e.response.data as { message?: string }).message
      if (msg) loadErr.value = msg
    }
  } finally {
    cancelling.value = false
  }
}

function formatRange(b: BookingDto): string {
  const s = new Date(b.startAt).toLocaleString()
  const e = new Date(b.endAt).toLocaleString()
  return `${s} ~ ${e}`
}

onMounted(async () => {
  await loadRooms()
  await load()
})

watch(page, load)
</script>

<template>
  <div>
    <h1 class="mb-4 text-2xl font-semibold text-slate-900">我的预约</h1>

    <div class="mb-6 flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-4">
      <label class="text-sm">
        <span class="text-slate-600">会议室</span>
        <select v-model="roomFilter" class="mt-1 block rounded border border-slate-300 px-2 py-1">
          <option value="">不限</option>
          <option v-for="r in rooms" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>
      </label>
      <label class="text-sm">
        <span class="text-slate-600">起始日</span>
        <input v-model="dateFrom" type="date" class="mt-1 rounded border border-slate-300 px-2 py-1" />
      </label>
      <label class="text-sm">
        <span class="text-slate-600">结束日</span>
        <input v-model="dateTo" type="date" class="mt-1 rounded border border-slate-300 px-2 py-1" />
      </label>
      <button
        type="button"
        class="rounded-md bg-blue-600 px-3 py-1.5 text-white hover:bg-blue-700"
        @click="
          page = 0;
          load();
        "
      >
        筛选
      </button>
      <button type="button" class="rounded-md border border-slate-300 px-3 py-1.5 hover:bg-slate-50" @click="resetFilters">
        重置
      </button>
    </div>

    <p v-if="loading" class="text-slate-500">加载中…</p>
    <p v-else-if="loadErr" class="text-red-600">{{ loadErr }}</p>
    <p v-else-if="items.length === 0" class="text-slate-600">暂无预约</p>

    <ul v-else class="space-y-3">
      <li
        v-for="b in items"
        :key="b.id"
        class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div class="font-medium text-slate-900">{{ b.title }}</div>
        <div class="text-sm text-slate-600">
          会议室 ID {{ b.roomId }} · {{ formatRange(b) }}
        </div>
        <div class="mt-2 text-xs text-slate-500">状态 {{ b.status }}</div>
        <div class="mt-3">
          <button
            v-if="canCancel(b)"
            type="button"
            class="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700 hover:bg-red-50"
            @click="cancelTarget = b"
          >
            取消预约
          </button>
          <span v-else-if="b.status === 'ACTIVE'" class="text-xs text-slate-500">
            会议已开始或进行中，无法取消
          </span>
        </div>
      </li>
    </ul>

    <div v-if="total > pageSize" class="mt-4 flex items-center gap-3">
      <button
        type="button"
        class="rounded border border-slate-300 px-3 py-1 disabled:opacity-40"
        :disabled="page <= 0"
        @click="
          page--;
          load();
        "
      >
        上一页
      </button>
      <span class="text-sm text-slate-600">
        第 {{ page + 1 }} 页 · 共 {{ total }} 条
      </span>
      <button
        type="button"
        class="rounded border border-slate-300 px-3 py-1 disabled:opacity-40"
        :disabled="(page + 1) * pageSize >= total"
        @click="
          page++;
          load();
        "
      >
        下一页
      </button>
    </div>

    <div
      v-if="cancelTarget"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      role="dialog"
      aria-modal="true"
    >
      <div class="w-full max-w-sm rounded-lg bg-white p-6 shadow-xl">
        <p class="mb-4 text-slate-800">确定取消该预约？时段将释放且不可自动恢复。</p>
        <div class="flex justify-end gap-2">
          <button type="button" class="rounded border border-slate-300 px-3 py-1" @click="cancelTarget = null">
            关闭
          </button>
          <button
            type="button"
            class="rounded bg-red-600 px-3 py-1 text-white disabled:opacity-60"
            :disabled="cancelling"
            @click="confirmCancel"
          >
            {{ cancelling ? '处理中…' : '确认取消' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listRooms } from '@/api/rooms'
import type { RoomDto } from '@/types/api'

const router = useRouter()
const rooms = ref<RoomDto[]>([])
const loadError = ref('')
const loading = ref(true)
const capacityGte = ref<number | ''>('')
const selectedAmenities = ref<string[]>([])

/** 与种子数据一致，避免筛选结果为空时筛选项消失 */
const AMENITY_OPTIONS = ['projector', 'whiteboard', 'video'] as const

const allAmenityTags = computed(() => {
  const s = new Set<string>(AMENITY_OPTIONS)
  for (const r of rooms.value) {
    for (const a of r.amenities) s.add(a)
  }
  return [...s].sort()
})

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    rooms.value = await listRooms({
      capacityGte: capacityGte.value === '' ? undefined : Number(capacityGte.value),
      amenities: selectedAmenities.value.length ? selectedAmenities.value : undefined,
    })
  } catch {
    loadError.value = '加载失败，请重试'
  } finally {
    loading.value = false
  }
}

onMounted(load)

function toggleAmenity(tag: string) {
  const i = selectedAmenities.value.indexOf(tag)
  if (i >= 0) selectedAmenities.value = selectedAmenities.value.filter((_, j) => j !== i)
  else selectedAmenities.value = [...selectedAmenities.value, tag]
}

function goCalendar(roomId: number) {
  router.push({ name: 'calendar', params: { roomId: String(roomId) } })
}
</script>

<template>
  <div>
    <h1 class="mb-4 text-2xl font-semibold text-slate-800">会议室列表</h1>

    <div class="mb-6 flex flex-wrap items-end gap-4 rounded-lg border border-slate-200 bg-white p-4">
      <label class="flex flex-col gap-1 text-sm">
        <span class="text-slate-600">最少容纳人数</span>
        <input
          v-model.number="capacityGte"
          type="number"
          min="1"
          placeholder="不限"
          class="w-32 rounded border border-slate-300 px-2 py-1"
          @change="load"
        />
      </label>
      <div class="flex flex-col gap-1">
        <span class="text-sm text-slate-600">设施（多选）</span>
        <div class="flex flex-wrap gap-2">
          <label v-for="tag in allAmenityTags" :key="tag" class="flex items-center gap-1 text-sm">
            <input
              type="checkbox"
              :checked="selectedAmenities.includes(tag)"
              @change="toggleAmenity(tag); load()"
            />
            {{ tag }}
          </label>
        </div>
      </div>
    </div>

    <p v-if="loading" class="text-slate-500">加载中…</p>
    <div
      v-else-if="loadError"
      class="rounded-lg border border-red-200 bg-red-50 p-4 text-red-800"
    >
      {{ loadError }}
      <button type="button" class="ml-2 underline" @click="load">重试</button>
    </div>
    <p v-else-if="rooms.length === 0" class="text-slate-600">
      没有符合条件的会议室，请放宽筛选。
    </p>
    <ul v-else class="space-y-3">
      <li
        v-for="room in rooms"
        :key="room.id"
        class="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div>
          <div class="font-medium text-slate-900">{{ room.name }}</div>
          <div class="text-sm text-slate-600">{{ room.location }} · 容纳 {{ room.capacity }} 人</div>
          <div class="mt-1 flex flex-wrap gap-1">
            <span
              v-for="a in room.amenities"
              :key="a"
              class="rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-700"
            >
              {{ a }}
            </span>
          </div>
        </div>
        <button
          type="button"
          class="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
          @click="goCalendar(room.id)"
        >
          去预约
        </button>
      </li>
    </ul>
  </div>
</template>

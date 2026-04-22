import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { UserSnapshot } from '@/types/api'

const STORAGE_KEY = 'mr_user_snapshot'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserSnapshot | null>(null)

  function hydrateFromStorage() {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return
    try {
      user.value = JSON.parse(raw) as UserSnapshot
    } catch {
      sessionStorage.removeItem(STORAGE_KEY)
    }
  }

  hydrateFromStorage()

  const isAuthenticated = computed(() => user.value !== null)

  function setUser(u: UserSnapshot | null) {
    user.value = u
    if (u) {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(u))
    } else {
      sessionStorage.removeItem(STORAGE_KEY)
    }
  }

  function clearUser() {
    setUser(null)
  }

  return { user, isAuthenticated, setUser, clearUser, hydrateFromStorage }
})

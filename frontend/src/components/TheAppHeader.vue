<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { logout as apiLogout } from '@/api/auth'

const router = useRouter()
const auth = useAuthStore()

async function onLogout() {
  try {
    await apiLogout()
  } catch {
    /* 退出接口失败仍清理本地态 */
  }
  auth.clearUser()
  router.push('/rooms')
}
</script>

<template>
  <header class="border-b border-slate-200 bg-white">
    <div class="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
      <router-link to="/rooms" class="text-lg font-semibold text-slate-800">
        会议室预约
      </router-link>
      <nav class="flex items-center gap-4 text-sm">
        <router-link to="/rooms" class="text-slate-600 hover:text-blue-600">会议室</router-link>
        <router-link
          v-if="auth.isAuthenticated"
          to="/bookings/mine"
          class="text-slate-600 hover:text-blue-600"
        >
          我的预约
        </router-link>
        <template v-if="!auth.isAuthenticated">
          <router-link to="/auth/login" class="text-slate-600 hover:text-blue-600">登录</router-link>
          <router-link
            to="/auth/register"
            class="rounded-md bg-blue-600 px-3 py-1.5 text-white hover:bg-blue-700"
          >
            注册
          </router-link>
        </template>
        <template v-else>
          <span class="text-slate-500">{{ auth.user?.username }}</span>
          <button
            type="button"
            class="rounded-md border border-slate-300 px-3 py-1.5 text-slate-700 hover:bg-slate-50"
            @click="onLogout"
          >
            退出
          </button>
        </template>
      </nav>
    </div>
  </header>
</template>

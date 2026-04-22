<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { fetchPasswordParams, login } from '@/api/auth'
import { computePasswordPrehash } from '@/composables/usePasswordHasher'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

onMounted(async () => {
  await fetchPasswordParams()
})

async function onSubmit() {
  errorMsg.value = ''
  loading.value = true
  try {
    const { pepper } = await fetchPasswordParams()
    const passwordPrehash = await computePasswordPrehash(pepper, password.value)
    const user = await login({ username: username.value, passwordPrehash })
    auth.setUser(user)
    const redir = typeof route.query.redirect === 'string' ? route.query.redirect : '/rooms'
    await router.replace(redir || '/rooms')
  } catch (e: unknown) {
    if (axios.isAxiosError(e) && e.response?.status === 429) {
      errorMsg.value = '访问过于频繁，请稍后再试'
    } else {
      errorMsg.value = '登录失败，请检查用户名或密码'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-md rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
    <h1 class="mb-4 text-xl font-semibold">登录</h1>
    <form class="space-y-4" @submit.prevent="onSubmit">
      <label class="block text-sm">
        <span class="text-slate-600">用户名</span>
        <input
          v-model="username"
          required
          autocomplete="username"
          class="mt-1 w-full rounded border border-slate-300 px-3 py-2"
        />
      </label>
      <label class="block text-sm">
        <span class="text-slate-600">密码</span>
        <input
          v-model="password"
          type="password"
          required
          autocomplete="current-password"
          class="mt-1 w-full rounded border border-slate-300 px-3 py-2"
        />
      </label>
      <p v-if="errorMsg" class="text-sm text-red-600">{{ errorMsg }}</p>
      <button
        type="submit"
        class="w-full rounded-md bg-blue-600 py-2 text-white hover:bg-blue-700 disabled:opacity-60"
        :disabled="loading"
      >
        {{ loading ? '登录中…' : '登录' }}
      </button>
      <p class="text-center text-sm text-slate-600">
        没有账号？
        <router-link :to="{ name: 'register', query: route.query }" class="text-blue-600 hover:underline">
          注册
        </router-link>
      </p>
    </form>
  </div>
</template>

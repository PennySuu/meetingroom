<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { fetchPasswordParams, register } from '@/api/auth'
import { computePasswordPrehash } from '@/composables/usePasswordHasher'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const confirm = ref('')
const displayName = ref('')
const errorMsg = ref('')
const loading = ref(false)

onMounted(async () => {
  await fetchPasswordParams()
})

async function onSubmit() {
  errorMsg.value = ''
  if (password.value !== confirm.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    const { pepper } = await fetchPasswordParams()
    const passwordPrehash = await computePasswordPrehash(pepper, password.value)
    const user = await register({
      username: username.value,
      passwordPrehash,
      displayName: displayName.value || undefined,
    })
    auth.setUser(user)
    const redir = typeof route.query.redirect === 'string' ? route.query.redirect : '/rooms'
    await router.replace(redir || '/rooms')
  } catch (e: unknown) {
    if (axios.isAxiosError(e) && e.response?.data && typeof e.response.data === 'object') {
      const code = (e.response.data as { code?: string }).code
      if (code === 'USER_USERNAME_CONFLICT') {
        errorMsg.value = '用户名已被占用'
        return
      }
    }
    errorMsg.value = '注册失败，请检查输入'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-md rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
    <h1 class="mb-4 text-xl font-semibold">注册</h1>
    <form class="space-y-4" @submit.prevent="onSubmit">
      <label class="block text-sm">
        <span class="text-slate-600">用户名</span>
        <input
          v-model="username"
          required
          minlength="3"
          maxlength="64"
          autocomplete="username"
          class="mt-1 w-full rounded border border-slate-300 px-3 py-2"
        />
      </label>
      <label class="block text-sm">
        <span class="text-slate-600">显示名称（可选）</span>
        <input v-model="displayName" maxlength="64" class="mt-1 w-full rounded border border-slate-300 px-3 py-2" />
      </label>
      <label class="block text-sm">
        <span class="text-slate-600">密码</span>
        <input
          v-model="password"
          type="password"
          required
          autocomplete="new-password"
          class="mt-1 w-full rounded border border-slate-300 px-3 py-2"
        />
      </label>
      <label class="block text-sm">
        <span class="text-slate-600">确认密码</span>
        <input
          v-model="confirm"
          type="password"
          required
          autocomplete="new-password"
          class="mt-1 w-full rounded border border-slate-300 px-3 py-2"
        />
      </label>
      <p v-if="errorMsg" class="text-sm text-red-600">{{ errorMsg }}</p>
      <button
        type="submit"
        class="w-full rounded-md bg-blue-600 py-2 text-white hover:bg-blue-700 disabled:opacity-60"
        :disabled="loading"
      >
        {{ loading ? '提交中…' : '注册' }}
      </button>
      <p class="text-center text-sm text-slate-600">
        已有账号？
        <router-link :to="{ name: 'login', query: route.query }" class="text-blue-600 hover:underline">
          登录
        </router-link>
      </p>
    </form>
  </div>
</template>

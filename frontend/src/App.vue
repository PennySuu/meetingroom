<script setup lang="ts">
import TheAppHeader from '@/components/TheAppHeader.vue'
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <TheAppHeader />
    <main class="mx-auto max-w-5xl px-4 py-6">
      <router-view />
    </main>
  </div>
</template>

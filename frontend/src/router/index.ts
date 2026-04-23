import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/rooms' },
    {
      path: '/rooms',
      name: 'rooms',
      component: () => import('@/views/RoomListView.vue'),
    },
    {
      path: '/rooms/:roomId/calendar',
      name: 'calendar',
      component: () => import('@/views/RoomCalendarView.vue'),
    },
    {
      path: '/bookings/mine',
      name: 'my-bookings',
      meta: { requiresAuth: true },
      component: () => import('@/views/MyBookingsView.vue'),
    },
    {
      path: '/auth/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/auth/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }
  return true
})

export default router

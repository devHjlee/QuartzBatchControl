import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTabsStore } from '@/stores/tabs'

import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import JobView from '@/views/JobView.vue'
import BatchView from '@/views/BatchView.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'Login', component: LoginView, meta: { title: '로그인' } },
  { path: '/dashboard', name: 'Dashboard', component: DashboardView, meta: { requiresAuth: true, title: '대시보드', closable: false } },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView, meta: { title: '페이지 없음' } },
  { path: '/batch', name: 'Batch 관리', component: BatchView, meta: { requiresAuth: true, title: 'Batch 관리', closable: true } },
  { path: '/job', name: 'Quartz 관리', component: JobView, meta: { requiresAuth: true, title: 'Quartz Job 관리', closable: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// ✅ 라우터 가드 (보호된 페이지는 로그인 필요)
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = !!authStore.token; // 스토어의 token 상태를 직접 사용

  if (to.name === 'Login' && isAuthenticated) {
    // 이미 로그인된 사용자가 로그인 페이지로 접근 시 대시보드로 리다이렉트
    next({ name: 'Dashboard' })
  } else if (to.meta.requiresAuth && !isAuthenticated) {
    // 인증이 필요한 페이지에 접근하려는데, 로그인되지 않은 경우 로그인 페이지로 리다이렉트
    // 사용자가 의도했던 경로를 저장하여 로그인 후 해당 경로로 리다이렉트할 수 있도록 함
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

router.afterEach((to: RouteLocationNormalized) => {
  const tabsStore = useTabsStore()
  if (to.name !== 'Login' && to.name !== 'NotFound') {
    tabsStore.addTab(to as any)
  }
})

export default router

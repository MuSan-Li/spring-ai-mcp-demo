import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
    {
      path: '/',
    redirect: '/tools'
  },
  {
    path: '/tools',
    name: 'Tools',
    component: () => import('@/views/ToolList.vue')
    },
    {
    path: '/tools/add',
    name: 'ToolAdd',
    component: () => import('@/views/ToolForm.vue')
  },
  {
    path: '/tools/edit/:id',
    name: 'ToolEdit',
    component: () => import('@/views/ToolForm.vue')
  },
  {
    path: '/markets',
    name: 'Markets',
    component: () => import('@/views/MarketList.vue')
  },
  {
    path: '/markets/add',
    name: 'MarketAdd',
    component: () => import('@/views/MarketForm.vue')
  },
  {
    path: '/markets/edit/:id',
    name: 'MarketEdit',
    component: () => import('@/views/MarketForm.vue')
  },
  {
    path: '/markets/:id',
    name: 'MarketDetail',
    component: () => import('@/views/MarketDetail.vue')
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/ChatView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

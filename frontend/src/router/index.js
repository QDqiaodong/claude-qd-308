import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/areas' },
  { path: '/areas', component: () => import('../views/Areas.vue'), meta: { label: '训练区' } },
  { path: '/machines', component: () => import('../views/Machines.vue'), meta: { label: '器械' } },
  { path: '/classes', component: () => import('../views/Classes.vue'), meta: { label: '团课' } },
  { path: '/members', component: () => import('../views/Members.vue'), meta: { label: '会员' } }
]

export const mods = routes.filter((r) => r.meta).map((r) => ({ path: r.path, label: r.meta.label }))

export default createRouter({ history: createWebHistory(), routes })

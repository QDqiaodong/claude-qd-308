import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/tickets' },
  { path: '/tickets', component: () => import('../views/TrialTickets.vue'), meta: { label: '体验入场' } },
  { path: '/areas', component: () => import('../views/Areas.vue'), meta: { label: '训练区' } },
  { path: '/machines', component: () => import('../views/Machines.vue'), meta: { label: '器械' } },
  { path: '/classes', component: () => import('../views/Classes.vue'), meta: { label: '团课' } },
  { path: '/members', component: () => import('../views/Members.vue'), meta: { label: '会员' } },
  { path: '/lockers', component: () => import('../views/Lockers.vue'), meta: { label: '更衣柜' } }
]

export const mods = routes.filter((r) => r.meta).map((r) => ({ path: r.path, label: r.meta.label }))

export default createRouter({ history: createWebHistory(), routes })

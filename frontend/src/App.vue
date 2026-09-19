<template>
  <div class="shell">
    <div class="row1">
      <span class="brand">健身房</span>
      <span class="sub">{{ mods.length }} 个模块 · {{ today }}</span>
    </div>
    <div class="row2">
      <router-link v-for="m in mods" :key="m.path" :to="m.path"
                   class="mk" :class="{ on: $route.path === m.path }">{{ m.label }}</router-link>
    </div>
    <main class="body"><router-view /></main>
  </div>
</template>

<script setup>
import { mods } from './router'
const today = new Date().toLocaleDateString('zh-CN')
</script>

<style>
html, body { margin: 0; }
body { background: #f6f4f3; font-family: -apple-system, 'PingFang SC', sans-serif; }
.row1 { display: flex; align-items: baseline; gap: 12px; background: var(--el-color-primary);
  color: #fff; padding: 13px 26px; }
.brand { font-size: 15px; font-weight: 700; }
.sub { font-size: 12px; opacity: .8; }
.row2 { display: flex; gap: 4px; background: #fff; padding: 0 22px; border-bottom: 1px solid #eee; }
.mk { padding: 13px 16px; text-decoration: none; color: #6d625c; font-size: 13px;
  border-bottom: 2px solid transparent; }
.mk.on { color: var(--el-color-primary-dark-2); font-weight: 600; border-bottom-color: var(--el-color-primary); }
.body { padding: 22px 26px; }
</style>

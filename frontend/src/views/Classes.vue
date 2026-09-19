<template>
  <div class="pane">
    <header class="hd"><h2>团课</h2><span class="sub">按日期分组，日期条吸顶；点「加一位」按名额报名，满了会被后端拦住</span>
      <button class="prime" @click="openNew">开团课</button></header>
    <div class="days">
      <template v-for="g in byDay" :key="g.date">
        <div class="day-bar">{{ g.date }} <small>{{ g.list.length }} 节</small></div>
        <div v-for="c in g.list" :key="c.id" class="c-row">
          <span class="c-code">{{ c.classCode }}</span>
          <span class="c-name">{{ c.className }}</span>
          <span class="c-coach">{{ c.coachName || '未排教练' }}</span>
          <span class="c-time">{{ c.startTime || '待定' }}</span>
          <div class="c-seat">
            <div class="seat-bar"><div class="seat-fill" :style="{ width: seatPct(c) + '%' }"></div></div>
            <span>{{ c.seatUsed }}/{{ c.seatTotal }}</span>
          </div>
          <span class="c-state">{{ c.classState }}</span>
          <span class="c-act">
            <button class="ghost" @click="join(c)">加一位</button>
          </span>
        </div>
      </template>
    </div>
    <el-dialog v-model="dialog" title="开一门团课" width="440px">
      <div class="fr"><label>课程编号</label><el-input v-model="form.classCode" /></div>
      <div class="fr"><label>课程名</label><el-input v-model="form.className" /></div>
      <div class="fr"><label>教练</label><el-input v-model="form.coachName" /></div>
      <div class="fr"><label>日期</label><el-input v-model="form.classDate" placeholder="2026-09-19" /></div>
      <div class="fr"><label>开始时间</label><el-input v-model="form.startTime" placeholder="19:00" /></div>
      <div class="fr"><label>名额</label><el-input v-model="form.seatTotal" /></div>
      <template #footer><el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { gymClassApi } from '../api'

const items = ref([])
const dialog = ref(false)
const form = ref({})

const byDay = computed(() => {
  const m = {}
  items.value.forEach((c) => {
    ;(m[c.classDate] = m[c.classDate] || []).push(c)
  })
  return Object.keys(m).sort().map((date) => ({ date, list: m[date] }))
})
function seatPct(c) {
  if (!c.seatTotal) return 0
  return Math.min(100, Math.round((c.seatUsed * 100) / c.seatTotal))
}
async function load() {
  items.value = await gymClassApi.list()
}
function openNew() {
  form.value = {}
  dialog.value = true
}
async function submit() {
  try {
    await gymClassApi.add(form.value)
    dialog.value = false
    await load()
    ElMessage.success('开好了')
  } catch (e) { ElMessage.error(e.message) }
}
async function join(c) {
  try {
    await gymClassApi.save(c.id, { seatUsed: c.seatUsed + 1 })
    await load()
    ElMessage.success('报上名了')
  } catch (e) { ElMessage.error(e.message) }
}
onMounted(load)
</script>

<style scoped>
.hd { display: flex; align-items: center; gap: 14px; margin-bottom: 18px; }
.hd h2 { margin: 0; font-size: 20px; }
.sub { flex: 1; color: #a2958f; font-size: 12px; }
.prime { background: var(--el-color-primary); color: #fff; border: none; border-radius: 8px;
  padding: 8px 18px; font-size: 13px; cursor: pointer; }
.days { background: #fff; border: 1px solid #efe9e6; border-radius: 12px; overflow: hidden; padding: 0 16px; }
.day-bar { position: sticky; top: 0; background: var(--el-color-primary-light-9); color: var(--el-color-primary-dark-2);
  font-weight: 600; font-size: 13px; padding: 9px 12px; margin: 10px -16px 4px; }
.day-bar small { color: #b09b93; font-weight: 400; margin-left: 6px; }
.c-row { display: grid; grid-template-columns: 96px 1.2fr 96px 80px 160px 90px 80px; gap: 8px;
  align-items: center; padding: 11px 12px; border-bottom: 1px solid #f6f2f0; font-size: 13px; }
.c-code { font-family: ui-monospace, Menlo, monospace; color: #a2958f; font-size: 12px; }
.c-coach { color: #7c6f68; font-size: 12px; }
.c-time { color: #a2958f; font-size: 12px; }
.c-seat { display: flex; align-items: center; gap: 8px; }
.seat-bar { flex: 1; height: 7px; background: #f4efec; border-radius: 4px; overflow: hidden; }
.seat-fill { height: 100%; background: var(--el-color-primary); border-radius: 4px; }
.c-seat span { font-size: 11px; color: #a2958f; min-width: 44px; text-align: right; }
.c-state { font-size: 12px; color: var(--el-color-primary-dark-2); }
.ghost { background: #fff; border: 1px solid var(--el-color-primary-light-7); color: var(--el-color-primary-dark-2);
  border-radius: 7px; padding: 4px 12px; font-size: 12px; cursor: pointer; }
.fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.fr label { width: 76px; text-align: right; font-size: 13px; color: #6d625c; }
</style>

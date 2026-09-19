<template>
  <div class="pane">
    <header class="hd"><h2>器械</h2><span class="sub">格子颜色是状态：可用深、占用浅、维修发红；点格子切状态</span>
      <button class="prime" @click="openNew">新增器械</button></header>
    <div class="heat">
      <div v-for="m in items" :key="m.id" class="cell" :class="tone(m)" @click="cycle(m)">
        <div class="c-code">{{ m.machineCode }}</div>
        <div class="c-name">{{ m.machineName }}</div>
        <div class="c-area">{{ m.areaName || '未归区' }}</div>
        <div class="c-state">{{ m.machineState }}</div>
      </div>
    </div>
    <el-dialog v-model="dialog" :title="form.id ? '修改器械' : '新增器械'" width="430px">
      <div class="fr"><label>编号</label><el-input v-model="form.machineCode" /></div>
      <div class="fr"><label>名称</label><el-input v-model="form.machineName" /></div>
      <div class="fr"><label>类型</label><el-input v-model="form.machineType" placeholder="有氧 / 力量 / 固定器械" /></div>
      <div class="fr"><label>训练区</label>
        <el-select v-model="form.areaId" style="flex:1">
          <el-option v-for="a in areas" :key="a.id" :label="a.areaName" :value="a.id" />
        </el-select></div>
      <div class="fr"><label>状态</label><el-input v-model="form.machineState" placeholder="可用 / 占用 / 维修" /></div>
      <template #footer><el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { areaApi, machineApi } from '../api'

const items = ref([])
const areas = ref([])
const dialog = ref(false)
const form = ref({})
const CYCLE = ['可用', '占用', '维修']

function tone(m) {
  if (m.machineState === '维修') return 'bad'
  return m.machineState === '占用' ? 'busy' : 'free'
}
async function load() {
  items.value = await machineApi.list()
  areas.value = await areaApi.list()
}
function openNew() {
  form.value = { machineState: '可用' }
  dialog.value = true
}
async function submit() {
  try {
    await machineApi.add(form.value)
    dialog.value = false
    await load()
    ElMessage.success('保存好了')
  } catch (e) { ElMessage.error(e.message) }
}
async function cycle(m) {
  const i = CYCLE.indexOf(m.machineState)
  try {
    await machineApi.save(m.id, { machineState: CYCLE[(i + 1) % CYCLE.length] })
    await load()
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
.heat { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px; }
.cell { border-radius: 10px; padding: 14px 12px; cursor: pointer; text-align: center; }
.cell.free { background: var(--el-color-primary); color: #fff; }
.cell.busy { background: var(--el-color-primary-light-7); color: var(--el-color-primary-dark-2); }
.cell.bad { background: #fbe9e7; color: #a63c2e; }
.c-code { font-size: 11px; opacity: .8; }
.c-name { font-size: 14px; font-weight: 600; margin: 5px 0; }
.c-area { font-size: 11px; opacity: .8; }
.c-state { font-size: 11px; margin-top: 6px; }
.fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.fr label { width: 66px; text-align: right; font-size: 13px; color: #6d625c; }
</style>

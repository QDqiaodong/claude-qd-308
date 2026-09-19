<template>
  <div class="pane">
    <header class="hd"><h2>训练区</h2><span class="sub">数字块里「在馆」是体验客加同时段团课的人头，停用区里没走完的人也算着</span>
      <button class="prime" @click="openNew">新增训练区</button></header>
    <div class="big-cards">
      <article v-for="a in items" :key="a.id" class="big" @click="openEdit(a)">
        <div class="b-top"><span class="badge">{{ a.areaCode }}</span><span class="chip">{{ a.areaState }}</span></div>
        <div class="b-name">{{ a.areaName }}</div>
        <div class="b-nums">
          <div class="num"><b>{{ a.floorSize ?? '-' }}</b><span>平方米</span></div>
          <div class="num"><b>{{ a.capacity ?? '-' }}</b><span>可容纳</span></div>
          <div class="num"><b>{{ inGymOf(a.id) }}</b><span>在馆</span></div>
          <div class="num"><b>{{ countOf(a.id) }}</b><span>器械</span></div>
        </div>
      </article>
    </div>
    <el-dialog v-model="dialog" :title="form.id ? '修改训练区' : '新增训练区'" width="420px">
      <div class="fr"><label>编号</label><el-input v-model="form.areaCode" /></div>
      <div class="fr"><label>名称</label><el-input v-model="form.areaName" /></div>
      <div class="fr"><label>面积</label><el-input v-model="form.floorSize" /></div>
      <div class="fr"><label>容纳人数</label><el-input v-model="form.capacity" /></div>
      <div class="fr"><label>状态</label><el-input v-model="form.areaState" placeholder="开放 / 停用" /></div>
      <template #footer><el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { areaApi, machineApi, trialTicketApi } from '../api'

const items = ref([])
const machines = ref([])
const board = ref([])
const dialog = ref(false)
const form = ref({})

function countOf(id) {
  return machines.value.filter((m) => m.areaId === id).length
}
function inGymOf(id) {
  const z = board.value.find((b) => b.areaId === id)
  return z ? z.inGym + z.classSeats : 0
}
async function load() {
  items.value = await areaApi.list()
  machines.value = await machineApi.list()
  board.value = await trialTicketApi.board()
}
function openNew() {
  form.value = { areaState: '开放' }
  dialog.value = true
}
function openEdit(row) {
  form.value = { ...row }
  dialog.value = true
}
async function submit() {
  try {
    if (form.value.id) await areaApi.save(form.value.id, form.value)
    else await areaApi.add(form.value)
    dialog.value = false
    await load()
    ElMessage.success('保存好了')
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
.big-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(272px, 1fr)); gap: 16px; }
.big { background: #fff; border: 1px solid #efe9e6; border-radius: 14px; padding: 18px 20px; cursor: pointer; }
.big:hover { box-shadow: 0 6px 18px rgba(0,0,0,.07); }
.b-top { display: flex; justify-content: space-between; align-items: center; }
.badge { background: var(--el-color-primary); color: #fff; font-size: 11px; border-radius: 10px; padding: 2px 10px; }
.chip { font-size: 12px; color: #a2958f; }
.b-name { font-size: 18px; font-weight: 600; margin: 12px 0 18px; }
.b-nums { display: flex; gap: 24px; }
.num b { display: block; font-size: 21px; color: var(--el-color-primary-dark-2); }
.num span { font-size: 11px; color: #a2958f; }
.fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.fr label { width: 76px; text-align: right; font-size: 13px; color: #6d625c; }
</style>

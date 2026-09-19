<template>
  <div class="pane">
    <header class="hd"><h2>更衣柜</h2>
      <span class="sub">格子颜色就是状态：空闲浅、使用中深、故障发红；占用情况跟着未离场的入场单走</span>
      <button class="prime" @click="openNew">新增柜子</button></header>
    <div class="cells">
      <div v-for="l in items" :key="l.id" class="lc" :class="tone(l)" @click="openEdit(l)">
        <b>{{ l.lockerCode }}</b>
        <span>{{ l.useState }}</span>
        <i v-if="l.occupant">{{ l.occupant }} · {{ l.visitCode }}</i>
        <i v-else>{{ l.lockerName || '　' }}</i>
      </div>
    </div>
    <el-dialog v-model="dialog" :title="form.id ? '修改柜子' : '新增柜子'" width="420px">
      <div class="fr"><label>编号</label><el-input v-model="form.lockerCode" :disabled="!!form.id" /></div>
      <div class="fr"><label>位置说明</label><el-input v-model="form.lockerName" placeholder="一层 A 排 01 柜" /></div>
      <div class="fr"><label>状态</label>
        <el-select v-model="form.lockerState" style="flex:1">
          <el-option label="正常" value="正常" /><el-option label="故障" value="故障" />
        </el-select></div>
      <template #footer><el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { lockerApi } from '../api'

const items = ref([])
const dialog = ref(false)
const form = ref({})

function tone(l) {
  if (l.useState === '故障') return 'bad'
  return l.useState === '使用中' ? 'busy' : 'free'
}
async function load() {
  items.value = await lockerApi.list()
}
function openNew() {
  form.value = { lockerState: '正常' }
  dialog.value = true
}
function openEdit(row) {
  form.value = { ...row }
  dialog.value = true
}
async function submit() {
  try {
    if (form.value.id) await lockerApi.save(form.value.id, form.value)
    else await lockerApi.add(form.value)
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
.cells { display: grid; grid-template-columns: repeat(auto-fill, minmax(132px, 1fr)); gap: 12px; }
.lc { border-radius: 10px; padding: 14px 10px; text-align: center; cursor: pointer;
  display: flex; flex-direction: column; gap: 4px; }
.lc.free { background: var(--el-color-primary-light-9); border: 1px solid var(--el-color-primary-light-7); }
.lc.busy { background: var(--el-color-primary); color: #fff; }
.lc.bad { background: #fbe9e7; color: #a63c2e; }
.lc b { font-size: 15px; }
.lc span { font-size: 12px; opacity: .85; }
.lc i { font-style: normal; font-size: 11px; opacity: .7; }
.fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.fr label { width: 66px; text-align: right; font-size: 13px; color: #6d625c; }
</style>

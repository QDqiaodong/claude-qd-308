<template>
  <div class="pg">
    <div class="hd">
      <h2>健身器械</h2>
      <span class="hint">每台设备一个格子，用颜色深浅表示忙碌程度。</span>
      <input class="search" v-model="kw" placeholder="搜索编号或名称" />
      <button class="btn solid" @click="openNew">新增</button>
    </div>
    <div class="heat">
      <div class="hcell" v-for="it in filtered" :key="it.id" :style="bg(it)" @click="openEdit(it)">
        <div class="hc-code">{{ it.code }}</div>
        <div class="hc-name">{{ it.name }}</div>
        <div class="hc-st">{{ it[ST] }}</div>
      </div>
    </div>
    <div class="legend">
      <span>空闲</span><i class="l0"></i><i class="l1"></i><i class="l2"></i><i class="l3"></i><span>满负荷</span>
    </div>
  </div>

    <el-dialog v-model="show" :title="form.id ? '修改' : '新增'" width="440px">
      <div class="frm">
        <div class="fr" v-for="fd in FORM_FIELDS" :key="fd.k">
          <label>{{ fd.l }}</label>
          <el-input v-model="form[fd.k]" :placeholder="'请填写' + fd.l" />
        </div>
      </div>
      <template #footer>
        <el-button @click="show = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { machineApi } from '../api'

const rows = ref([])
const show = ref(false)
const form = ref({})
const kw = ref('')
const FORM_FIELDS = [{"k":"code","l":"编号"},{"k":"name","l":"名称"},{"k":"type","l":"器械类型"},{"k":"areaId","l":"所在训练区"},{"k":"status","l":"状态"}]
const BODY_FIELDS = [{"k":"type","l":"器械类型"},{"k":"areaId","l":"所在训练区"}]
const ST = 'status'
const OPTS = ["可用","占用","维修"]

const picked = ref([])
const filtered = computed(() => {
  if (!kw.value) return rows.value
  const k = kw.value.toLowerCase()
  return rows.value.filter(r => (r.code || '').toLowerCase().includes(k) || (r.name || '').toLowerCase().includes(k))
})

async function load() { rows.value = await machineApi.list() }
function openNew() { form.value = {}; show.value = true }
function openEdit(it) { form.value = { ...it }; show.value = true }
async function save() {
  try {
    if (form.value.id) await machineApi.update(form.value.id, form.value)
    else await machineApi.create(form.value)
    show.value = false
    await load()
    ElMessage.success('已保存')
  } catch (e) { ElMessage.error(e.message) }
}
async function patch(it, key, value) {
  try {
    await machineApi.update(it.id, { [key]: value })
    await load()
    ElMessage.success('已更新')
  } catch (e) { ElMessage.error(e.message); await load() }
}
function togglePick(id) {
  const i = picked.value.indexOf(id)
  if (i >= 0) picked.value.splice(i, 1)
  else picked.value.push(id)
}
onMounted(load)
async function toggle(it) { await patch(it, ST, OPTS[(OPTS.indexOf(it[ST]) + 1) % OPTS.length]) }
function bg(it) {
  const i = OPTS.indexOf(it[ST])
  const light = ['#f7f7f8', 'var(--el-color-primary-light-8)', 'var(--el-color-primary-light-5)', 'var(--el-color-primary)']
  const dark = ['#555', '#333', '#fff', '#fff']
  const k = Math.max(0, Math.min(i, 3))
  return { background: light[k], color: dark[k] }
}
</script>
<style scoped>
.pg { padding: 4px 2px 40px; color: #303133; }
.pg h2 { margin: 0; font-size: 19px; }
.hd { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; flex-wrap: wrap; }
.hd .hint { color: #888; font-size: 13px; flex: 1; }
.btn { border: 1px solid var(--el-color-primary); background: #fff; color: var(--el-color-primary);
  border-radius: 6px; padding: 6px 14px; cursor: pointer; font-size: 13px; }
.btn:hover { background: var(--el-color-primary-light-9); }
.btn.solid { background: var(--el-color-primary); color: #fff; }
.btn.sm { padding: 3px 10px; font-size: 12px; }
.frm .fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.frm .fr label { width: 88px; text-align: right; color: #666; font-size: 13px; }
.blank { color: #bbb; padding: 30px; text-align: center; }
.search { border: 1px solid #e3e3e3; border-radius: 6px; padding: 6px 12px; font-size: 13px; width: 160px; }
.heat { display: grid; grid-template-columns: repeat(auto-fill, minmax(132px, 1fr)); gap: 10px; }
.hcell { border-radius: 10px; padding: 14px 12px; cursor: pointer; transition: .15s; }
.hcell:hover { transform: translateY(-2px); box-shadow: 0 5px 14px rgba(0,0,0,.1); }
.hc-code { font-size: 11px; opacity: .75; }
.hc-name { font-size: 14px; font-weight: 600; margin: 4px 0; }
.hc-st { font-size: 12px; opacity: .85; }
.legend { display: flex; align-items: center; gap: 6px; margin-top: 14px; font-size: 12px; color: #888; }
.legend i { width: 22px; height: 12px; border-radius: 3px; display: inline-block; }
.l0 { background: #f4f4f5; } .l1 { background: var(--el-color-primary-light-7); }
.l2 { background: var(--el-color-primary-light-3); } .l3 { background: var(--el-color-primary); }

</style>

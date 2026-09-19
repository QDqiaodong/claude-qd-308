<template>
  <div class="pg">
    <div class="hd">
      <h2>储物柜</h2>
      <span class="hint">按区画出柜格，格子颜色就是它的状态。</span>
      <input class="search" v-model="kw" placeholder="搜索编号或名称" />
      <button class="btn solid" @click="openNew">新增</button>
    </div>
    <div class="zone" v-for="z in zones" :key="z.name">
      <div class="zh">{{ z.name }}</div>
      <div class="cells">
        <div class="lc" v-for="it in z.items" :key="it.id" :class="lv(it)" @click="openEdit(it)">
          <b>{{ it.code }}</b>
          <span>{{ it[ST] }}</span>
        </div>
      </div>
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
import { lockerApi } from '../api'

const rows = ref([])
const show = ref(false)
const form = ref({})
const kw = ref('')
const FORM_FIELDS = [{"k":"code","l":"编号"},{"k":"zone","l":"所在区"},{"k":"size","l":"柜型"},{"k":"status","l":"状态"}]
const BODY_FIELDS = [{"k":"zone","l":"所在区"},{"k":"size","l":"柜型"}]
const ST = 'status'
const OPTS = ["使用中","空闲","故障"]

const picked = ref([])
const filtered = computed(() => {
  if (!kw.value) return rows.value
  const k = kw.value.toLowerCase()
  return rows.value.filter(r => (r.code || '').toLowerCase().includes(k) || (r.name || '').toLowerCase().includes(k))
})

async function load() { rows.value = await lockerApi.list() }
function openNew() { form.value = {}; show.value = true }
function openEdit(it) { form.value = { ...it }; show.value = true }
async function save() {
  try {
    if (form.value.id) await lockerApi.update(form.value.id, form.value)
    else await lockerApi.create(form.value)
    show.value = false
    await load()
    ElMessage.success('已保存')
  } catch (e) { ElMessage.error(e.message) }
}
async function patch(it, key, value) {
  try {
    await lockerApi.update(it.id, { [key]: value })
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
const ZK = (BODY_FIELDS.find(f => /区|柜|位/.test(f.l)) || BODY_FIELDS[0] || {}).k
const zones = computed(() => {
  const m = {}
  rows.value.forEach(r => { const k = (ZK && r[ZK]) || '未分区'; (m[k] = m[k] || []).push(r) })
  return Object.keys(m).map(k => ({ name: k, items: m[k] }))
})
function lv(it) {
  const i = OPTS.indexOf(it[ST])
  return i <= 0 ? '' : 's' + Math.min(i, 2)
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
.search { display: none; }
.zone { margin-bottom: 20px; }
.zh { font-size: 13px; color: #666; margin-bottom: 10px; }
.cells { display: grid; grid-template-columns: repeat(auto-fill, minmax(96px, 1fr)); gap: 8px; }
.lc { border-radius: 8px; padding: 14px 8px; text-align: center; cursor: pointer;
  background: var(--el-color-primary-light-9); border: 1px solid var(--el-color-primary-light-7); }
.lc b { display: block; font-size: 14px; }
.lc span { font-size: 11px; opacity: .8; }
.lc.s1 { background: #eef1f4; border-color: #dfe4e9; }
.lc.s2 { background: #fdf1f1; border-color: #f6dcdc; }

</style>

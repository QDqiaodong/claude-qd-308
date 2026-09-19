<template>
  <div class="pg">
    <div class="hd">
      <h2>团课排期</h2>
      <span class="hint">按日期分堆，日期条吸在顶上，滚到哪儿都知道是哪天。</span>
      <input class="search" v-model="kw" placeholder="搜索编号或名称" />
      <button class="btn solid" @click="openNew">新增</button>
    </div>
    <div class="sd">
      <template v-for="g in days" :key="g.key">
        <div class="dhead">{{ g.key }} <small>{{ g.items.length }} 条</small></div>
        <div class="drow" v-for="it in g.items" :key="it.id">
          <span class="dc">{{ it.code }}</span>
          <span class="dn">{{ it.name }}</span>
          <span class="ds">{{ it[ST] }}</span>
          <span class="dt">{{ timeOf(it) }}</span>
          <button class="btn sm" @click="openEdit(it)">改</button>
        </div>
      </template>
      <div class="blank" v-if="!days.length">还没有安排</div>
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
import { gymclassApi } from '../api'

const rows = ref([])
const show = ref(false)
const form = ref({})
const kw = ref('')
const FORM_FIELDS = [{"k":"code","l":"编号"},{"k":"name","l":"课程名称"},{"k":"coach","l":"教练"},{"k":"classDate","l":"上课日期"},{"k":"startTime","l":"开始时间"},{"k":"capacity","l":"名额"},{"k":"booked","l":"已报名"},{"k":"status","l":"状态"}]
const BODY_FIELDS = [{"k":"coach","l":"教练"},{"k":"classDate","l":"上课日期"},{"k":"startTime","l":"开始时间"},{"k":"capacity","l":"名额"},{"k":"booked","l":"已报名"}]
const ST = 'status'
const OPTS = ["待开课","已约满","已完成"]

const picked = ref([])
const filtered = computed(() => {
  if (!kw.value) return rows.value
  const k = kw.value.toLowerCase()
  return rows.value.filter(r => (r.code || '').toLowerCase().includes(k) || (r.name || '').toLowerCase().includes(k))
})

async function load() { rows.value = await gymclassApi.list() }
function openNew() { form.value = {}; show.value = true }
function openEdit(it) { form.value = { ...it }; show.value = true }
async function save() {
  try {
    if (form.value.id) await gymclassApi.update(form.value.id, form.value)
    else await gymclassApi.create(form.value)
    show.value = false
    await load()
    ElMessage.success('已保存')
  } catch (e) { ElMessage.error(e.message) }
}
async function patch(it, key, value) {
  try {
    await gymclassApi.update(it.id, { [key]: value })
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
const DK = (BODY_FIELDS.find(f => /日期/.test(f.l)) || BODY_FIELDS[0] || {}).k
const TK = (BODY_FIELDS.find(f => /时间/.test(f.l)) || {}).k
function timeOf(it) { return TK ? it[TK] : '' }
const days = computed(() => {
  const m = {}
  rows.value.forEach(r => { const k = DK ? String(r[DK] || '未定') : '未定'; (m[k] = m[k] || []).push(r) })
  return Object.keys(m).sort().map(k => ({ key: k, items: m[k] }))
})
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
.sd { background: #fff; border: 1px solid #eee; border-radius: 12px; overflow: hidden; padding: 0 16px; }
.dhead { position: sticky; top: 0; background: var(--el-color-primary-light-9); color: var(--el-color-primary-dark-2);
  font-weight: 600; font-size: 13px; padding: 9px 12px; margin: 10px -16px 4px; }
.dhead small { color: #aaa; font-weight: 400; margin-left: 6px; }
.drow { display: grid; grid-template-columns: 104px 1fr 96px 74px 56px; gap: 10px; align-items: center;
  padding: 10px 12px; border-bottom: 1px solid #f7f7f7; font-size: 13px; }
.dc { font-family: ui-monospace, monospace; color: #aaa; }
.ds { color: var(--el-color-primary-dark-2); }
.dt { color: #999; font-size: 12px; }

</style>

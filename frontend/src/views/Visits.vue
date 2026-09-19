<template>
  <div class="pane">
    <header class="hd"><h2>体验入场</h2>
      <span class="sub">开单一次写清称呼、训练区、更衣柜；在馆人头 = 在馆体验客 + 正在上课的团课报名</span>
      <el-select v-model="state" placeholder="全部状态" clearable style="width: 120px" @change="load">
        <el-option label="在馆" value="在馆" /><el-option label="已离场" value="已离场" />
      </el-select>
      <button class="prime" @click="openNew">开体验入场单</button>
    </header>

    <div class="board">
      <div v-for="b in board" :key="b.id" class="bd" :class="{ off: b.areaState === '停用', full: b.capacity && b.headcount >= b.capacity }">
        <span class="bd-name">{{ b.areaName }}<i v-if="b.areaState === '停用'">（停用）</i></span>
        <span class="bd-num"><b>{{ b.headcount }}</b><em>/ {{ b.capacity ?? '不限' }}</em></span>
        <span class="bd-sub">体验客 {{ b.guests }} + 团课 {{ b.classUsed }}</span>
      </div>
    </div>

    <el-table :data="items" class="tbl" empty-text="还没有体验入场单">
      <el-table-column prop="visitCode" label="单号" width="100" />
      <el-table-column prop="guestName" label="体验客" width="110" />
      <el-table-column prop="areaName" label="训练区" width="120" />
      <el-table-column prop="lockerCode" label="更衣柜" width="100" />
      <el-table-column label="介绍会员" width="120">
        <template #default="{ row }">{{ row.referrerName || '—' }}</template>
      </el-table-column>
      <el-table-column label="入场时间" width="150">
        <template #default="{ row }">{{ fmt(row.enterTime) }}</template>
      </el-table-column>
      <el-table-column label="离场时间" width="150">
        <template #default="{ row }">{{ row.leaveTime ? fmt(row.leaveTime) : '—' }}</template>
      </el-table-column>
      <el-table-column label="钥匙" width="90">
        <template #default="{ row }">
          <span v-if="row.visitState === '已离场'">已收回</span>
          <span v-else :class="row.keyReturned ? 'ok' : 'warn'">{{ row.keyReturned ? '已还' : '未还' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <span :class="row.visitState === '在馆' ? 'in' : 'out'">{{ row.visitState }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180">
        <template #default="{ row }">
          <template v-if="row.visitState === '在馆'">
            <button v-if="!row.keyReturned" class="ghost" @click="returnKey(row)">补记钥匙归还</button>
            <button class="ghost" :class="{ dim: !row.keyReturned }"
                    :title="row.keyReturned ? '' : '钥匙还没交回，先补记钥匙归还'"
                    @click="leave(row)">离场</button>
          </template>
          <span v-else class="done">已收口</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="开体验入场单" width="460px">
      <div class="fr"><label>体验客称呼</label><el-input v-model="form.guestName" placeholder="怎么称呼这位体验客" /></div>
      <div class="fr"><label>训练区</label>
        <el-select v-model="form.areaId" placeholder="只列开放的区" style="flex:1">
          <el-option v-for="a in openAreas" :key="a.id" :value="a.id"
                     :label="a.areaName + '（在馆 ' + a.headcount + '/' + (a.capacity ?? '不限') + '）'"
                     :disabled="a.capacity != null && a.headcount >= a.capacity" />
        </el-select></div>
      <div class="fr"><label>更衣柜</label>
        <el-select v-model="form.lockerId" placeholder="只列空闲的柜子" style="flex:1">
          <el-option v-for="l in freeLockers" :key="l.id" :value="l.id"
                     :label="l.lockerCode + ' · ' + (l.lockerName || '')" />
        </el-select></div>
      <div class="fr"><label>介绍会员</label>
        <el-select v-model="form.referrerId" placeholder="选填，只作备注" clearable style="flex:1">
          <el-option v-for="m in members" :key="m.id" :value="m.id"
                     :label="m.memberName + '（' + m.memberCode + (m.memberState === '已停卡' ? ' · 已停卡' : m.expired ? ' · 卡已过期' : '') + '）'" />
        </el-select></div>
      <div class="note">开单只做入场登记：不办卡、不把体验客写进会员档案，也不改介绍人的卡状态；办卡等体测走完、钥匙还清再谈。</div>
      <template #footer><el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">落单</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { lockerApi, memberApi, visitApi } from '../api'

const items = ref([])
const board = ref([])
const lockers = ref([])
const members = ref([])
const state = ref('')
const dialog = ref(false)
const form = ref({})

const openAreas = computed(() => board.value.filter((b) => b.areaState === '开放'))
const freeLockers = computed(() => lockers.value.filter((l) => l.useState === '空闲'))

function fmt(s) {
  return s ? String(s).replace('T', ' ').slice(0, 16) : ''
}
async function load() {
  const [v, b, l, m] = await Promise.all([
    visitApi.list({ state: state.value }), visitApi.board(), lockerApi.list(), memberApi.list()
  ])
  items.value = v
  board.value = b
  lockers.value = l
  members.value = m
}
function openNew() {
  form.value = {}
  dialog.value = true
}
async function submit() {
  if (!form.value.guestName || !String(form.value.guestName).trim()) {
    ElMessage.error('体验客称呼得填')
    return
  }
  if (!form.value.areaId) {
    ElMessage.error('训练区还没选，单子落不下去')
    return
  }
  if (!form.value.lockerId) {
    ElMessage.error('更衣柜还没选，单子落不下去')
    return
  }
  try {
    await visitApi.add(form.value)
    dialog.value = false
    await load()
    ElMessage.success('开好了，钥匙已交给体验客')
  } catch (e) { ElMessage.error(e.message) }
}
async function returnKey(row) {
  try {
    await visitApi.returnKey(row.id)
    await load()
    ElMessage.success('钥匙已登记收回')
  } catch (e) { ElMessage.error(e.message) }
}
async function leave(row) {
  if (!row.keyReturned) {
    ElMessage.error('钥匙还没交回，先补记钥匙归还再离场')
    return
  }
  try {
    await ElMessageBox.confirm(`确认 ${row.guestName} 离场？柜子 ${row.lockerCode} 会释放出来`, '离场收口')
  } catch { return }
  try {
    await visitApi.leave(row.id)
    await load()
    ElMessage.success('已离场，柜子释放了')
  } catch (e) { ElMessage.error(e.message) }
}
onMounted(load)
</script>

<style scoped>
.hd { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.hd h2 { margin: 0; font-size: 20px; }
.sub { flex: 1; color: #a2958f; font-size: 12px; }
.prime { background: var(--el-color-primary); color: #fff; border: none; border-radius: 8px;
  padding: 8px 18px; font-size: 13px; cursor: pointer; }
.board { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; margin-bottom: 16px; }
.bd { background: #fff; border: 1px solid #efe9e6; border-radius: 12px; padding: 12px 16px;
  display: flex; flex-direction: column; gap: 2px; }
.bd.off { opacity: .55; }
.bd.full { border-color: #f0c4bc; background: #fdf6f5; }
.bd-name { font-size: 13px; color: #6d625c; }
.bd-name i { font-style: normal; font-size: 11px; color: #b0a49e; }
.bd-num b { font-size: 22px; color: var(--el-color-primary-dark-2); }
.bd.full .bd-num b { color: #c0392b; }
.bd-num em { font-style: normal; font-size: 12px; color: #a2958f; margin-left: 2px; }
.bd-sub { font-size: 11px; color: #a2958f; }
.tbl { width: 100%; }
.warn { color: #c0392b; }
.ok { color: #2e7d32; }
.in { color: var(--el-color-primary-dark-2); font-weight: 600; }
.out { color: #a2958f; }
.done { color: #c9bfb9; font-size: 12px; }
.ghost { background: #fff; border: 1px solid var(--el-color-primary-light-7); color: var(--el-color-primary-dark-2);
  border-radius: 7px; padding: 4px 12px; font-size: 12px; cursor: pointer; margin-right: 6px; }
.ghost.dim { opacity: .45; cursor: not-allowed; }
.fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.fr label { width: 76px; text-align: right; font-size: 13px; color: #6d625c; }
.note { background: var(--el-color-primary-light-9); border-radius: 8px; padding: 10px 12px;
  font-size: 12px; color: #7c6f68; line-height: 1.7; }
</style>

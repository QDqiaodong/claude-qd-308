<template>
  <div class="pane">
    <header class="hd"><h2>体验入场</h2>
      <span class="sub">开单一次办清：选开放的训练区、占空闲的柜子，人头和柜门当场就对得上；钥匙还了才能离场</span>
      <button class="prime" @click="openNew">开入场单</button></header>

    <div class="board">
      <div v-for="z in board" :key="z.areaId" class="z" :class="{ off: z.areaState !== '开放', full: z.left !== null && z.left <= 0 }">
        <div class="z-top"><b>{{ z.areaName }}</b><span>{{ z.areaState }}</span></div>
        <div class="z-num">
          <b>{{ z.inGym + z.classSeats }}</b>
          <span>/ {{ z.capacity ?? '不限' }}</span>
        </div>
        <div class="z-sub">在馆 {{ z.inGym }} · 团课 {{ z.classSeats }}<template v-if="z.left !== null"> · 剩 {{ z.left }}</template></div>
      </div>
    </div>

    <div class="sheet">
      <div class="t-row t-head">
        <span>单号</span><span>称呼</span><span>训练区</span><span>柜子</span><span>入场</span><span>介绍人</span><span>钥匙</span><span>状态</span><span>操作</span>
      </div>
      <div v-for="t in items" :key="t.id" class="t-row" :class="{ gone: t.ticketState === '已离场' }">
        <span class="t-code">{{ t.ticketCode }}</span>
        <span class="t-name">{{ t.guestName }}</span>
        <span>{{ t.areaName }}<small v-if="t.areaState === '停用'" class="tag-off">已停用</small></span>
        <span class="t-code">{{ t.lockerCode }}</span>
        <span class="t-time">{{ t.visitDate }} · {{ t.timeSlot }}<small>{{ t.createdAt }}</small></span>
        <span>
          <template v-if="t.referrerName">{{ t.referrerName }}
            <small class="tag-ref" v-if="t.referrerState === '已停卡'">已停卡</small>
            <small class="tag-ref" v-else-if="refExpired(t)">卡过期</small>
          </template>
          <template v-else>—</template>
        </span>
        <span>
          <b v-if="t.keyReturned" class="key-ok">已还</b>
          <b v-else class="key-no">未还</b>
        </span>
        <span class="t-state">{{ t.ticketState }}<small v-if="t.leftAt">{{ t.leftAt }}</small></span>
        <span class="t-act">
          <template v-if="t.ticketState === '在馆'">
            <button v-if="!t.keyReturned" class="ghost" @click="returnKey(t)">记钥匙还</button>
            <button class="ghost" @click="leave(t)">离场</button>
          </template>
        </span>
      </div>
      <div v-if="!items.length" class="blank">今天还没有体验客入场单</div>
    </div>

    <el-dialog v-model="dialog" title="开体验入场单" width="460px">
      <div class="fr"><label>体验客称呼</label><el-input v-model="form.guestName" placeholder="怎么称呼这位客人" /></div>
      <div class="fr"><label>训练区</label>
        <el-select v-model="form.areaId" placeholder="选开放的训练区" style="flex:1">
          <el-option v-for="z in board" :key="z.areaId" :value="z.areaId"
                     :label="zoneLabel(z)" :disabled="z.areaState !== '开放' || (z.left !== null && z.left <= 0)" />
        </el-select></div>
      <div class="fr"><label>更衣柜</label>
        <el-select v-model="form.lockerId" placeholder="选空闲的柜子" style="flex:1">
          <el-option v-for="k in lockers" :key="k.id" :value="k.id"
                     :label="lockerLabel(k)" :disabled="k.state !== '空闲'" />
        </el-select></div>
      <div class="fr"><label>入场日期</label><el-input v-model="form.visitDate" placeholder="2026-09-19" /></div>
      <div class="fr"><label>入场时段</label>
        <el-select v-model="form.timeSlot" style="flex:1">
          <el-option v-for="s in ['上午', '下午', '晚上']" :key="s" :value="s" :label="s" />
        </el-select></div>
      <div class="fr"><label>介绍人</label>
        <el-select v-model="form.referrerId" clearable filterable placeholder="可空，只作备注" style="flex:1">
          <el-option v-for="m in members" :key="m.id" :value="m.id" :label="memberLabel(m)" />
        </el-select></div>
      <p class="note">介绍人只是备注：停卡、卡过期的会员也能当介绍人。开单不给体验客办卡，也不动介绍人的卡状态。</p>
      <template #footer><el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :disabled="!form.guestName || !form.areaId || !form.lockerId" @click="submit">开单</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { lockerApi, memberApi, trialTicketApi } from '../api'

const items = ref([])
const board = ref([])
const lockers = ref([])
const members = ref([])
const dialog = ref(false)
const form = ref({})

function today() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
function currentSlot() {
  const h = new Date().getHours()
  return h < 12 ? '上午' : h < 18 ? '下午' : '晚上'
}
function zoneLabel(z) {
  const cap = z.capacity == null ? '不限' : `上限 ${z.capacity}`
  const use = z.areaState === '开放' ? `现在在馆 ${z.inGym}+课 ${z.classSeats}` : '已停用'
  return `${z.areaName}（${use} / ${cap}）`
}
function lockerLabel(k) {
  if (k.state === '占用') return `${k.lockerCode}（${k.holdGuestName || '有人'}占着）`
  if (k.state === '故障') return `${k.lockerCode}（故障）`
  return k.lockerCode
}
function memberLabel(m) {
  const tags = []
  if (m.memberState === '已停卡') tags.push('已停卡')
  if (m.expired) tags.push('卡过期')
  return `${m.memberName} · ${m.memberCode}${tags.length ? '（' + tags.join('，') + '）' : ''}`
}
function refExpired(t) {
  return t.referrerExpire && t.referrerExpire < today()
}
async function load() {
  const [ts, b, ks, ms] = await Promise.all([
    trialTicketApi.list(), trialTicketApi.board(), lockerApi.list(), memberApi.list()
  ])
  items.value = ts
  board.value = b
  lockers.value = ks
  members.value = ms
}
function openNew() {
  form.value = { visitDate: today(), timeSlot: currentSlot(), referrerId: null }
  dialog.value = true
}
async function submit() {
  try {
    await trialTicketApi.add(form.value)
    dialog.value = false
    await load()
    ElMessage.success('单子开好了，柜子也占上了')
  } catch (e) { ElMessage.error(e.message) }
}
async function returnKey(t) {
  try {
    await trialTicketApi.returnKey(t.id)
    await load()
    ElMessage.success('钥匙记上了')
  } catch (e) { ElMessage.error(e.message) }
}
async function leave(t) {
  try {
    await trialTicketApi.leave(t.id)
    await load()
    ElMessage.success('离场了，柜子空出来了')
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
.board { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; margin-bottom: 18px; }
.z { background: #fff; border: 1px solid #efe9e6; border-radius: 12px; padding: 12px 16px; }
.z.off { opacity: .55; }
.z.full { border-color: #e5b8b2; }
.z-top { display: flex; justify-content: space-between; font-size: 12px; color: #a2958f; }
.z-top b { color: #3e2a25; font-size: 13px; }
.z-num b { font-size: 22px; color: var(--el-color-primary-dark-2); }
.z-num span { font-size: 12px; color: #a2958f; }
.z-sub { font-size: 11px; color: #a2958f; margin-top: 2px; }
.sheet { background: #fff; border: 1px solid #efe9e6; border-radius: 12px; overflow: hidden; }
.t-row { display: grid; grid-template-columns: 84px 90px 1.1fr 70px 1.4fr 1.1fr 56px 1fr 150px; gap: 8px;
  align-items: center; padding: 11px 16px; border-bottom: 1px solid #f6f2f0; font-size: 13px; }
.t-row:last-child { border-bottom: none; }
.t-head { background: var(--el-color-primary-light-9); color: var(--el-color-primary-dark-2);
  font-weight: 600; font-size: 12px; }
.t-row.gone { color: #b6aba6; }
.t-code { font-family: ui-monospace, Menlo, monospace; font-size: 12px; color: #a2958f; }
.t-name { font-weight: 600; }
.t-time small, .t-state small { display: block; font-size: 11px; color: #b6aba6; }
.tag-off, .tag-ref { font-size: 11px; color: #b0705e; margin-left: 4px; }
.key-ok { color: var(--el-color-primary-dark-2); font-size: 12px; }
.key-no { color: #b0705e; font-size: 12px; }
.t-act { display: flex; gap: 6px; }
.ghost { background: #fff; border: 1px solid var(--el-color-primary-light-7); color: var(--el-color-primary-dark-2);
  border-radius: 7px; padding: 4px 12px; font-size: 12px; cursor: pointer; }
.blank { padding: 30px; text-align: center; color: #b6aba6; font-size: 13px; }
.fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.fr label { width: 76px; text-align: right; font-size: 13px; color: #6d625c; flex-shrink: 0; }
.note { margin: 4px 0 0 86px; font-size: 11px; color: #a2958f; }
</style>

<template>
  <div class="pane">
    <header class="hd"><h2>会员</h2><span class="sub">卡片下面一条是「有效期还剩多少」，过期整卡发灰</span>
      <button class="prime" @click="openNew">登记会员</button></header>
    <div class="cards">
      <article v-for="m in items" :key="m.id" class="m-card" :class="{ gone: m.expired, off: m.memberState === '已停卡' }">
        <div class="m-top"><span class="m-code">{{ m.memberCode }}</span><span class="m-level">{{ m.cardLevel || '普通卡' }}</span></div>
        <div class="m-name">{{ m.memberName }}</div>
        <div class="m-phone">{{ m.phone || '未留电话' }}</div>
        <div class="m-bar"><div class="m-fill" :style="{ width: leftPct(m) + '%' }"></div></div>
        <div class="m-foot">
          <span>到期 {{ m.expireDate || '未设' }}</span>
          <b v-if="m.expired" class="late">已过期</b>
          <b v-else class="ok">剩 {{ daysLeft(m) }} 天</b>
        </div>
        <button class="ghost" @click="openEdit(m)">修改</button>
      </article>
    </div>
    <el-dialog v-model="dialog" :title="form.id ? '修改会员' : '登记会员'" width="430px">
      <div class="fr"><label>会员编号</label><el-input v-model="form.memberCode" /></div>
      <div class="fr"><label>姓名</label><el-input v-model="form.memberName" /></div>
      <div class="fr"><label>手机号</label><el-input v-model="form.phone" /></div>
      <div class="fr"><label>卡种</label><el-input v-model="form.cardLevel" placeholder="普通卡 / 银卡 / 金卡" /></div>
      <div class="fr"><label>到期日</label><el-input v-model="form.expireDate" placeholder="2027-05-01" /></div>
      <div class="fr"><label>状态</label><el-input v-model="form.memberState" placeholder="正常 / 已停卡" /></div>
      <template #footer><el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { memberApi } from '../api'

const items = ref([])
const dialog = ref(false)
const form = ref({})
const YEAR = 365

function daysLeft(m) {
  if (!m.expireDate) return 0
  return Math.max(0, Math.ceil((new Date(m.expireDate) - new Date()) / 86400000))
}
function leftPct(m) {
  return Math.min(100, Math.round((daysLeft(m) * 100) / YEAR))
}
async function load() {
  items.value = await memberApi.list()
}
function openNew() {
  form.value = { memberState: '正常' }
  dialog.value = true
}
function openEdit(row) {
  form.value = { ...row }
  dialog.value = true
}
async function submit() {
  try {
    if (form.value.id) await memberApi.save(form.value.id, form.value)
    else await memberApi.add(form.value)
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
.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(232px, 1fr)); gap: 14px; }
.m-card { background: #fff; border: 1px solid #efe9e6; border-radius: 12px; padding: 16px; }
.m-card.gone { background: #fafafa; border-color: #ece7e4; }
.m-card.off { opacity: .65; }
.m-top { display: flex; justify-content: space-between; font-size: 12px; }
.m-code { color: #a2958f; }
.m-level { color: var(--el-color-primary-dark-2); }
.m-name { font-size: 16px; font-weight: 600; margin: 8px 0 3px; }
.m-phone { font-size: 12px; color: #a2958f; margin-bottom: 12px; }
.m-bar { height: 8px; background: #f4efec; border-radius: 4px; overflow: hidden; }
.m-fill { height: 100%; background: var(--el-color-primary); border-radius: 4px; }
.m-card.gone .m-fill { background: #cfc4bf; }
.m-foot { display: flex; justify-content: space-between; align-items: center; font-size: 12px;
  color: #a2958f; margin: 8px 0 12px; }
.m-foot .late { color: #c0392b; }
.m-foot .ok { color: var(--el-color-primary-dark-2); }
.ghost { background: #fff; border: 1px solid var(--el-color-primary-light-7); color: var(--el-color-primary-dark-2);
  border-radius: 7px; padding: 5px 14px; font-size: 12px; cursor: pointer; }
.fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.fr label { width: 76px; text-align: right; font-size: 13px; color: #6d625c; }
</style>

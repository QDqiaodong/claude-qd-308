<template>
  <div class="pane">
    <header class="hd"><h2>更衣柜</h2>
      <span class="sub">「占用」不用手改 —— 有在馆的入场单就占着，单走离场流程才空出来；这里只管加柜子和标故障</span>
      <button class="prime" @click="openNew">新增柜子</button></header>
    <div class="cells">
      <div v-for="k in items" :key="k.id" class="lc" :class="k.state" @click="openEdit(k)">
        <b>{{ k.lockerCode }}</b>
        <span>{{ k.state }}</span>
        <small v-if="k.state === '占用'">{{ k.holdGuestName }} · {{ k.holdTicketCode }}</small>
      </div>
    </div>
    <el-dialog v-model="dialog" :title="form.id ? '修改柜子' : '新增柜子'" width="420px">
      <div class="fr"><label>编号</label><el-input v-model="form.lockerCode" :disabled="!!form.id" /></div>
      <div class="fr"><label>状态</label>
        <el-select v-model="form.lockerState" style="flex:1">
          <el-option value="空闲" label="空闲" />
          <el-option value="故障" label="故障" />
        </el-select></div>
      <p class="note" v-if="form.state === '占用'">这柜现在有人用着，得等那张入场单离场后才能标故障。</p>
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

async function load() {
  items.value = await lockerApi.list()
}
function openNew() {
  form.value = { lockerState: '空闲' }
  dialog.value = true
}
function openEdit(k) {
  form.value = { ...k, lockerState: k.state === '故障' ? '故障' : '空闲' }
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
.cells { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 12px; }
.lc { border-radius: 10px; padding: 16px 10px; text-align: center; cursor: pointer;
  background: var(--el-color-primary-light-9); border: 1px solid var(--el-color-primary-light-7); }
.lc b { display: block; font-size: 15px; }
.lc span { font-size: 11px; opacity: .8; }
.lc small { display: block; font-size: 10px; margin-top: 4px; opacity: .75; }
.lc.占用 { background: var(--el-color-primary); border-color: var(--el-color-primary); color: #fff; }
.lc.故障 { background: #fbe9e7; border-color: #f0d5d0; color: #a63c2e; }
.fr { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.fr label { width: 56px; text-align: right; font-size: 13px; color: #6d625c; }
.note { margin: 4px 0 0 66px; font-size: 11px; color: #a2958f; }
</style>

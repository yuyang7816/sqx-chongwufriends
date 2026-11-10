<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  exportWithdrawPageApi,
  getPaymentStatsApi,
  getRechargePageApi,
  getWithdrawPageApi,
  getWithdrawStatsApi,
  offlineTransferWithdrawApi,
  refundRechargeApi,
  rejectWithdrawApi,
  transferWithdrawApi,
} from '@/api/modules/finance-center'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const route = useRoute()
const router = useRouter()

const activeTab = ref('withdraw-manage')

const withdrawLoading = ref(false)
const withdrawPage = ref(1)
const withdrawSize = ref(10)
const withdrawTotal = ref(0)
const withdrawList = ref([])
const selectedWithdrawRows = ref([])
const exportLoading = ref(false)
const rejectDialogVisible = ref(false)
const rejectDialogLoading = ref(false)

const statFlag = ref(1)
const statDate = ref(getDefaultStatDate())
const withdrawStats = ref({})
const paymentStats = ref({})
const withdrawStatsLoading = ref(false)
const paymentStatsLoading = ref(false)

const rechargeLoading = ref(false)
const rechargePage = ref(1)
const rechargeSize = ref(10)
const rechargeTotal = ref(0)
const rechargeList = ref([])
const refundDialogVisible = ref(false)
const refundDialogLoading = ref(false)

const withdrawFilters = reactive({
  zhifubao: '',
  zhifubaoName: '',
  classify: '',
  userType: '',
  state: '',
  startTime: '',
  endTime: '',
})

const rejectForm = reactive({
  cashId: 0,
  content: '',
})

const rechargeFilters = reactive({
  type: '',
  state: -1,
  userName: '',
  phone: '',
  orderId: '',
  classify: '',
  startTime: getDefaultStartDate(),
  endTime: getDefaultEndDate(),
})

const refundForm = reactive({
  payDetailsId: 0,
  refundMoney: '',
  maxRefundMoney: 0,
})

const withdrawTypeOptions = [
  { label: '全部', value: '' },
  { label: '支付宝', value: 1 },
  { label: '微信', value: 2 },
]

const userTypeOptions = [
  { label: '全部', value: '' },
  { label: '用户', value: 1 },
  { label: '商家', value: 2 },
]

const withdrawStateOptions = [
  { label: '全部', value: '' },
  { label: '待提现', value: 0 },
  { label: '提现成功', value: 1 },
  { label: '拒绝提现', value: -1 },
]

const statFlagOptions = [
  { label: '按天查询', value: 1 },
  { label: '按月查询', value: 2 },
  { label: '按年查询', value: 3 },
]

const paymentRecordTypeOptions = [
  { label: '全部', value: '' },
  { label: '会员卡充值', value: 1 },
  { label: '订单', value: 2 },
  { label: '预约', value: 3 },
  { label: '优惠券', value: 4 },
  { label: '结束', value: 5 },
  { label: '补费', value: 6 },
  { label: '续费', value: 9 },
  { label: '周期卡', value: 13 },
]

const paymentRecordStateOptions = [
  { label: '全部', value: -1 },
  { label: '待支付', value: 0 },
  { label: '支付成功', value: 1 },
  { label: '支付失败', value: 2 },
]

const paymentMethodOptions = [
  { label: '全部', value: '' },
  { label: 'app微信', value: 1 },
  { label: '微信公众号', value: 2 },
  { label: '微信小程序', value: 3 },
  { label: '支付宝', value: 4 },
  { label: '支付宝H5', value: 5 },
  { label: '系统充值', value: 6 },
]

const statPickerType = computed(() => {
  if (statFlag.value === 2) return 'month'
  if (statFlag.value === 3) return 'year'
  return 'date'
})

const statValueFormat = computed(() => {
  if (statFlag.value === 2) return 'YYYY-MM'
  if (statFlag.value === 3) return 'YYYY'
  return 'YYYY-MM-DD'
})

function getDefaultStatDate(flag = 1) {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  if (flag === 2) return `${year}-${month}`
  if (flag === 3) return `${year}`
  return `${year}-${month}-${day}`
}

function getDefaultStartDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}-01`
}

function getDefaultEndDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatMoney(value) {
  const amount = Number(value || 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

function buildWithdrawParams() {
  const params = {
    page: withdrawPage.value,
    limit: withdrawSize.value,
    zhifubaoName: withdrawFilters.zhifubaoName.trim(),
    zhifubao: withdrawFilters.zhifubao.trim(),
    classify: withdrawFilters.classify,
    state: withdrawFilters.state,
    startTime: withdrawFilters.startTime,
    endTime: withdrawFilters.endTime ? `${withdrawFilters.endTime} 23:59:59` : '',
    userType: withdrawFilters.userType,
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') delete params[key]
  })
  return params
}

function buildRechargeParams() {
  const params = {
    page: rechargePage.value,
    limit: rechargeSize.value,
    state: rechargeFilters.state,
    startTime: rechargeFilters.startTime || getDefaultStartDate(),
    endTime: rechargeFilters.endTime || getDefaultEndDate(),
    userName: rechargeFilters.userName.trim(),
    phone: rechargeFilters.phone.trim(),
    orderId: rechargeFilters.orderId.trim(),
    type: rechargeFilters.type,
    classify: rechargeFilters.classify,
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') delete params[key]
  })
  return params
}

function buildStatParams() {
  return {
    time: statDate.value,
    flag: statFlag.value,
  }
}

function withdrawClassifyText(value) {
  const map = { 1: '支付宝', 2: '微信小程序', 3: '微信公众号' }
  return map[Number(value)] || '暂无'
}

function withdrawStateText(value) {
  const map = { 0: '待转账', 1: '已转账', '-1': '已拒绝' }
  return map[Number(value)] || '-'
}

function withdrawUserTypeText(value) {
  const map = { 1: '用户', 2: '商家' }
  return map[Number(value)] || '-'
}

function paymentMethodText(value) {
  const map = { 1: 'app微信', 2: '微信公众号', 3: '微信小程序', 4: '支付宝', 5: '支付宝H5', 6: '系统充值' }
  return map[Number(value)] || '暂无'
}

function paymentStateText(value) {
  const map = { 0: '待支付', 1: '支付成功', 2: '支付失败' }
  return map[Number(value)] || '-'
}

function paymentTypeText(value) {
  const map = { 1: '会员卡充值', 2: '订单', 3: '预约', 4: '优惠券', 5: '结束', 6: '补费', 9: '续费', 13: '周期卡' }
  return map[Number(value)] || '-'
}

function goToUser(row) {
  if (!row?.userId) {
    showError('当前记录未绑定用户')
    return
  }
  router.push(`/users/${row.userId}`)
}

async function loadWithdrawList() {
  withdrawLoading.value = true
  try {
    const data = await getWithdrawPageApi(buildWithdrawParams())
    withdrawList.value = data.list || []
    withdrawTotal.value = Number(data.total || 0)
  } finally {
    withdrawLoading.value = false
  }
}

async function loadWithdrawStats() {
  withdrawStatsLoading.value = true
  try {
    withdrawStats.value = await getWithdrawStatsApi(buildStatParams())
  } finally {
    withdrawStatsLoading.value = false
  }
}

async function loadPaymentStats() {
  paymentStatsLoading.value = true
  try {
    paymentStats.value = await getPaymentStatsApi(buildStatParams())
  } finally {
    paymentStatsLoading.value = false
  }
}

async function loadRechargeList() {
  rechargeLoading.value = true
  try {
    const data = await getRechargePageApi(buildRechargeParams())
    rechargeList.value = data.list || []
    rechargeTotal.value = Number(data.total || 0)
  } finally {
    rechargeLoading.value = false
  }
}

function handleWithdrawSearch() {
  withdrawPage.value = 1
  loadWithdrawList()
}

function resetWithdrawFilters() {
  withdrawFilters.zhifubao = ''
  withdrawFilters.zhifubaoName = ''
  withdrawFilters.classify = ''
  withdrawFilters.userType = ''
  withdrawFilters.state = ''
  withdrawFilters.startTime = ''
  withdrawFilters.endTime = ''
  withdrawPage.value = 1
  loadWithdrawList()
}

async function exportWithdraw() {
  exportLoading.value = true
  try {
    const blobData = await exportWithdrawPageApi(buildWithdrawParams())
    const blob = new Blob([blobData], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '财务列表.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } finally {
    exportLoading.value = false
  }
}

function handleWithdrawSelectionChange(value) {
  selectedWithdrawRows.value = value
}

async function transferOne(row) {
  if (Number(row.state) === 1) {
    showError('已转账，请勿重复操作')
    return
  }
  await confirmAction('确定转账吗？')
  await transferWithdrawApi(row.id)
  showSuccess('转账成功')
  await loadWithdrawList()
}

async function transferBatch() {
  if (!selectedWithdrawRows.value.length) return
  await confirmAction('确定批量转账吗？')
  const rows = selectedWithdrawRows.value.filter((item) => Number(item.state) === 0)
  if (!rows.length) {
    showError('未选中可转账记录')
    return
  }
  const results = await Promise.allSettled(rows.map((item) => transferWithdrawApi(item.id)))
  const successCount = results.filter((item) => item.status === 'fulfilled').length
  if (!successCount) {
    showError('批量转账失败')
  } else {
    showSuccess(`批量转账完成，成功 ${successCount} 条`)
  }
  await loadWithdrawList()
}

async function offlineTransferOne(row) {
  if (Number(row.state) === 1) {
    showError('已转账，请勿重复操作')
    return
  }
  await confirmAction('确定线下转账吗？')
  await offlineTransferWithdrawApi(String(row.id))
  showSuccess('操作成功')
  await loadWithdrawList()
}

async function offlineTransferBatch() {
  if (!selectedWithdrawRows.value.length) return
  await confirmAction('确定批量线下转账吗？')
  const ids = selectedWithdrawRows.value.filter((item) => Number(item.state) === 0).map((item) => item.id)
  if (!ids.length) {
    showError('未选中可转账记录')
    return
  }
  await offlineTransferWithdrawApi(ids.join(','))
  showSuccess('操作成功')
  await loadWithdrawList()
}

function openRejectDialog(row) {
  if (Number(row.state) === -1) {
    showError('已拒绝，请勿重复操作')
    return
  }
  rejectForm.cashId = row.id
  rejectForm.content = ''
  rejectDialogVisible.value = true
}

async function submitRejectDialog() {
  if (!rejectForm.content.trim()) {
    showError('请输入拒绝原因')
    return
  }
  rejectDialogLoading.value = true
  try {
    await rejectWithdrawApi(rejectForm.cashId, rejectForm.content.trim())
    rejectDialogVisible.value = false
    showSuccess('操作成功')
    rejectForm.content = ''
    await loadWithdrawList()
  } finally {
    rejectDialogLoading.value = false
  }
}

function handleStatFlagChange() {
  statDate.value = getDefaultStatDate(statFlag.value)
  if (activeTab.value === 'withdraw-stats') loadWithdrawStats()
  if (activeTab.value === 'payment-stats') loadPaymentStats()
}

function handleRechargeSearch() {
  rechargePage.value = 1
  loadRechargeList()
}

function resetRechargeFilters() {
  rechargeFilters.type = ''
  rechargeFilters.state = -1
  rechargeFilters.userName = ''
  rechargeFilters.phone = ''
  rechargeFilters.orderId = ''
  rechargeFilters.classify = ''
  rechargeFilters.startTime = getDefaultStartDate()
  rechargeFilters.endTime = getDefaultEndDate()
  rechargePage.value = 1
  loadRechargeList()
}

function openRefundDialog(row) {
  refundForm.payDetailsId = row.id
  refundForm.maxRefundMoney = Number(row.money || 0) - Number(row.refundMoney || 0)
  refundForm.refundMoney = String(refundForm.maxRefundMoney || '')
  refundDialogVisible.value = true
}

async function submitRefundDialog() {
  const amount = Number(refundForm.refundMoney)
  if (!Number.isFinite(amount)) {
    showError('请输入有效退款金额')
    return
  }
  if (amount <= 0) {
    showError('退款金额必须大于0')
    return
  }
  if (amount > Number(refundForm.maxRefundMoney)) {
    showError(`退款金额不可高于${Number(refundForm.maxRefundMoney)}`)
    return
  }
  refundDialogLoading.value = true
  try {
    await confirmAction('确定退款吗？')
    await refundRechargeApi({ payDetailsId: refundForm.payDetailsId, refundMoney: amount })
    refundDialogVisible.value = false
    showSuccess('操作成功')
    await Promise.all([loadRechargeList(), loadPaymentStats()])
  } finally {
    refundDialogLoading.value = false
  }
}

async function loadCurrentTab() {
  if (activeTab.value === 'withdraw-manage') return loadWithdrawList()
  if (activeTab.value === 'withdraw-stats') return loadWithdrawStats()
  if (activeTab.value === 'payment-stats') return loadPaymentStats()
  if (activeTab.value === 'payment-records') return loadRechargeList()
  return Promise.resolve()
}

function syncRouteStatus() {
  const status = route.query.status
  if (typeof status !== 'undefined' && status !== null && status !== '') {
    withdrawFilters.state = Number(status)
  } else {
    withdrawFilters.state = ''
  }
}

watch(() => route.fullPath, async () => {
  syncRouteStatus()
  if (activeTab.value === 'withdraw-manage') {
    withdrawPage.value = 1
    await loadWithdrawList()
  }
})

onMounted(async () => {
  syncRouteStatus()
  await loadCurrentTab()
})
</script>

<template>
  <div class="fc">
    <header class="fc-hero nf-page-hero">
      <span class="fc-hero__badge">财务管理</span>
      <h1 class="fc-hero__title">财务中心</h1>
      <p class="fc-hero__desc">提现审核与转账、提现/支付统计及支付记录查询，支持批量操作与 Excel 导出</p>
    </header>

    <div v-if="activeTab === 'withdraw-manage'" class="fc-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="fc-filter__form" @submit.prevent>
        <el-form-item label="支付宝账号">
          <el-input v-model="withdrawFilters.zhifubao" clearable placeholder="请输入支付宝账号" @keyup.enter="handleWithdrawSearch" />
        </el-form-item>
        <el-form-item label="支付宝名称">
          <el-input v-model="withdrawFilters.zhifubaoName" clearable placeholder="请输入支付宝名称" @keyup.enter="handleWithdrawSearch" />
        </el-form-item>
        <el-form-item label="提现类型">
          <el-select v-model="withdrawFilters.classify" clearable placeholder="请选择提现类型" @change="handleWithdrawSearch">
            <el-option v-for="item in withdrawTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="withdrawFilters.userType" clearable placeholder="请选择用户类型" @change="handleWithdrawSearch">
            <el-option v-for="item in userTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="withdrawFilters.state" clearable placeholder="请选择状态" @change="handleWithdrawSearch">
            <el-option v-for="item in withdrawStateOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="withdrawFilters.startTime" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" placeholder="选择开始时间" clearable />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="withdrawFilters.endTime" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" placeholder="选择截止时间" clearable />
        </el-form-item>
        <el-form-item class="fc-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleWithdrawSearch">查询</el-button>
            <el-button @click="resetWithdrawFilters">重置</el-button>
            <el-button type="warning" :loading="exportLoading" @click="exportWithdraw">导出Excel</el-button>
            <el-button type="primary" :disabled="selectedWithdrawRows.length <= 0 || !isAuth('financeList:transfer')" @click="transferBatch">批量转账</el-button>
            <el-button type="primary" :disabled="selectedWithdrawRows.length <= 0 || !isAuth('financeList:transfer')" @click="offlineTransferBatch">批量线下转账</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div v-else-if="activeTab === 'withdraw-stats' || activeTab === 'payment-stats'" class="fc-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>统计筛选</span>
      </div>
      <el-form :inline="true" class="fc-filter__form fc-filter__form--stat" @submit.prevent>
        <el-form-item label="查询维度">
          <el-select v-model="statFlag" style="width: 150px" @change="handleStatFlagChange">
            <el-option v-for="item in statFlagOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="statDate" :type="statPickerType" :value-format="statValueFormat" :format="statValueFormat" placeholder="选择时间" clearable @change="activeTab === 'withdraw-stats' ? loadWithdrawStats() : loadPaymentStats()" />
        </el-form-item>
      </el-form>
    </div>

    <div v-else-if="activeTab === 'payment-records'" class="fc-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="fc-filter__form" @submit.prevent>
        <el-form-item label="支付类型">
          <el-select v-model="rechargeFilters.type" clearable placeholder="请选择支付类型" @change="handleRechargeSearch">
            <el-option v-for="item in paymentRecordTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="rechargeFilters.state" placeholder="请选择状态" @change="handleRechargeSearch">
            <el-option v-for="item in paymentRecordStateOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名称">
          <el-input v-model="rechargeFilters.userName" clearable placeholder="请输入用户名称" @keyup.enter="handleRechargeSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="rechargeFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleRechargeSearch" />
        </el-form-item>
        <el-form-item label="支付订单号">
          <el-input v-model="rechargeFilters.orderId" clearable placeholder="请输入支付订单号" @keyup.enter="handleRechargeSearch" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="rechargeFilters.classify" clearable placeholder="请选择支付方式" @change="handleRechargeSearch">
            <el-option v-for="item in paymentMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="rechargeFilters.startTime" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" placeholder="选择开始时间" clearable />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="rechargeFilters.endTime" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" placeholder="选择截止时间" clearable />
        </el-form-item>
        <el-form-item class="fc-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleRechargeSearch">查询</el-button>
            <el-button @click="resetRechargeFilters">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="fc-tabs">
      <el-tabs v-model="activeTab" @tab-change="loadCurrentTab">
        <el-tab-pane label="提现管理" name="withdraw-manage">
          <div class="fc-table nf-table-card">
            <div class="fc-notices">
              <div class="notice warning-red">* 线下转账提示：支付宝线下转账成功并核对状态后再点击线下转账，请勿重复操作。</div>
              <div class="notice warning-orange">* 导出订单提示：建议先按时间或状态筛选后再导出，避免数据量过大导致卡顿。</div>
            </div>

            <el-table v-loading="withdrawLoading" :data="withdrawList" border @selection-change="handleWithdrawSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="id" label="编号" width="90" fixed="left" />
          <el-table-column label="支付宝账号" min-width="180">
            <template #default="{ row }">
              <span class="user-link" @click="goToUser(row)">{{ row.zhifubao || '暂无' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="zhifubaoName" label="支付宝名称" width="120" />
          <el-table-column label="收款二维码" width="110">
            <template #default="{ row }">
              <el-image v-if="row.wxImg" :src="row.wxImg" :preview-src-list="[row.wxImg]" fit="cover" class="thumb-image" preview-teleported />
              <span v-else>暂无图片</span>
            </template>
          </el-table-column>
          <el-table-column label="用户类型" width="100">
            <template #default="{ row }">{{ withdrawUserTypeText(row.userType) }}</template>
          </el-table-column>
          <el-table-column label="提现方式" width="110">
            <template #default="{ row }">{{ withdrawClassifyText(row.classify) }}</template>
          </el-table-column>
          <el-table-column prop="money" label="提现金额" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ withdrawStateText(row.state) }}</template>
          </el-table-column>
          <el-table-column prop="refund" label="拒绝原因" min-width="220" show-overflow-tooltip />
          <el-table-column prop="createAt" label="申请时间" width="180" />
          <el-table-column prop="outAt" label="转账/拒绝时间" width="180" />
          <el-table-column prop="orderNumber" label="转账订单号" width="170" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" type="primary" :disabled="!isAuth('financeList:transfer') || Number(row.state) !== 0" @click="transferOne(row)">转账</el-button>
                <el-button size="small" type="warning" :disabled="!isAuth('financeList:refund') || Number(row.state) !== 0" @click="openRejectDialog(row)">拒绝</el-button>
                <el-button size="small" type="primary" plain :disabled="!isAuth('financeList:transfer') || Number(row.state) !== 0" @click="offlineTransferOne(row)">线下转账</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

            <BasePagination
              :current-page="withdrawPage"
              :page-size="withdrawSize"
              :page-sizes="[10, 20, 30, 50, 100]"
              :total="withdrawTotal"
              @update:currentPage="withdrawPage = $event; loadWithdrawList()"
              @update:pageSize="withdrawSize = $event; withdrawPage = 1; loadWithdrawList()"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="提现统计" name="withdraw-stats">
          <div class="fc-stats nf-table-card">
            <el-row v-loading="withdrawStatsLoading" :gutter="12">
              <el-col :xs="24" :sm="12" :lg="6">
                <div class="stat-card">
                  <div class="stat-title">累计提现金额</div>
                  <div class="stat-value">{{ withdrawStats.sumMoney || 0 }}</div>
                  <div class="stat-unit">元</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="6">
                <div class="stat-card">
                  <div class="stat-title">提现次数</div>
                  <div class="stat-value">{{ withdrawStats.countMoney || 0 }}</div>
                  <div class="stat-unit">笔</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="6">
                <div class="stat-card">
                  <div class="stat-title">待转账次数</div>
                  <div class="stat-value">{{ withdrawStats.stayMoney || 0 }}</div>
                  <div class="stat-unit">笔</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="支付统计" name="payment-stats">
          <div class="fc-stats nf-table-card">
            <el-row v-loading="paymentStatsLoading" :gutter="12">
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">支付总金额</div><div class="stat-value">{{ paymentStats.sumMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">微信小程序支付金额</div><div class="stat-value">{{ paymentStats.weiXinXCXMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">微信APP支付金额</div><div class="stat-value">{{ paymentStats.weiXinAppMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">微信公众号支付金额</div><div class="stat-value">{{ paymentStats.weiXinGZHMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">支付宝APP支付金额</div><div class="stat-value">{{ paymentStats.zhiFuBaoAppMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">支付宝H5支付金额</div><div class="stat-value">{{ paymentStats.zhiFuBaoH5Money || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">系统充值</div><div class="stat-value">{{ paymentStats.xiTongMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">退款金额</div><div class="stat-value">{{ paymentStats.refundMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="支付记录" name="payment-records">
          <div class="fc-table nf-table-card">
            <el-table v-loading="rechargeLoading" :data="rechargeList" border>
          <el-table-column prop="id" label="编号" width="90" fixed="left" />
          <el-table-column label="支付类型" width="120">
            <template #default="{ row }">{{ paymentTypeText(row.type) }}</template>
          </el-table-column>
          <el-table-column label="用户名称" min-width="140">
            <template #default="{ row }">
              <span class="user-link" @click="goToUser(row)">{{ row.userName || '未绑定' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="160" />
          <el-table-column prop="orderId" label="支付订单号" min-width="220" />
          <el-table-column prop="money" label="支付金额" width="100" />
          <el-table-column prop="refundMoney" label="退款金额" width="100" />
          <el-table-column label="支付方式" width="120">
            <template #default="{ row }">{{ paymentMethodText(row.classify) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ paymentStateText(row.state) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="payTime" label="支付时间" width="180" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="Number(row.refundMoney || 0) < Number(row.money || 0) && Number(row.state) === 1 && Number(row.classify) !== 6"
                link
                type="primary"
                :disabled="!isAuth('financeList:transfer')"
                @click="openRefundDialog(row)"
              >退款</el-button>
            </template>
          </el-table-column>
        </el-table>

            <BasePagination
              :current-page="rechargePage"
              :page-size="rechargeSize"
              :page-sizes="[5, 10, 15]"
              :total="rechargeTotal"
              @update:currentPage="rechargePage = $event; loadRechargeList()"
              @update:pageSize="rechargeSize = $event; rechargePage = 1; loadRechargeList()"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="rejectDialogVisible" title="拒绝" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form label-width="100px" @submit.prevent>
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.content" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="rejectDialogLoading" @click="submitRejectDialog">确定</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="refundDialogVisible" title="退款" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form label-width="100px" @submit.prevent>
        <el-form-item label="退款金额">
          <div class="dialog-field">
            <el-input v-model="refundForm.refundMoney" type="number" placeholder="请输入退款金额" />
            <div class="hint-text">当前可退款金额为：{{ refundForm.maxRefundMoney }}</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="refundDialogLoading" @click="submitRefundDialog">确定</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.fc {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.fc-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.fc-hero__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--nf-text-primary);
  background: var(--nf-brand-lite);
  border: 1px solid var(--nf-brand);
  border-radius: var(--nf-radius-sm);
}

.fc-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.fc-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.fc-filter.nf-filter-card {
  margin-bottom: 0;
}

.fc-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.fc-filter__form--stat {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.fc-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.fc-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
    padding: 0 16px;
    background: #fff;
    border: 1px solid var(--nf-border);
    border-radius: 8px 8px 0 0;
    box-shadow: var(--nf-shadow-card);
  }

  :deep(.el-tabs__content) {
    overflow: visible;
  }

  :deep(.el-tab-pane) {
    padding-top: 0;
  }
}

.fc-table.nf-table-card,
.fc-stats.nf-table-card {
  margin-bottom: 0;
  margin-top: 0;
  padding-bottom: 12px;
  border-radius: 0 0 8px 8px;
  border-top: none;
  box-shadow: var(--nf-shadow-card);
}

.fc-table.nf-table-card {
  :deep(.el-table) {
    --el-table-border-color: var(--nf-border);
    --el-table-header-bg-color: var(--nf-sidebar-bg-alt, #f7f8fa);
  }

  :deep(.el-table th.el-table__cell) {
    font-weight: 600;
    color: var(--nf-text-primary);
  }

  :deep(.el-table td.el-table__cell) {
    background: #fff;
  }
}

.fc-notices {
  margin-bottom: 12px;
}

.thumb-image {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  display: block;
}

.user-link {
  color: var(--nf-brand-deep);
  cursor: pointer;
}

.notice {
  margin-bottom: 6px;
  font-size: 13px;
  line-height: 1.5;
}

.warning-red {
  color: #f56c6c;
}

.warning-orange {
  color: #e6a23c;
}

.stat-card {
  height: 100%;
  padding: 16px;
  border: 1px solid var(--nf-border);
  border-radius: var(--nf-radius-md);
  background: #fff;
  margin-bottom: 12px;
  box-shadow: var(--nf-shadow-card);
}

.stat-title {
  color: var(--nf-text-secondary);
  font-size: 13px;
  margin-bottom: 10px;
  line-height: 1.4;
}

.stat-value {
  color: var(--nf-brand-deep);
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.stat-unit {
  color: var(--nf-text-secondary);
  font-size: 12px;
  margin-top: 8px;
}

.dialog-field {
  width: 100%;
}

.hint-text {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 6px;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.action-buttons :deep(.el-button) {
  width: 100%;
  margin-left: 0;
}

@media (max-width: 768px) {
  .fc-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }
}
</style>

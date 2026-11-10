<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { getUserCenterPage } from '@/api/modules/user-center'
import {
  changeOrderRoomApi,
  closeOrderApi,
  exportOrderCenterApi,
  getGoodsPageApi,
  getOrderCenterPageApi,
  getOrderRenewRecordsApi,
  getOrderVipDetailsApi,
  refundOrderApi,
  updateHangOrderStatusApi,
  updateOrderApi,
} from '@/api/modules/order-center'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const userPickerVisible = ref(false)
const userPickerLoading = ref(false)
const goodsPickerVisible = ref(false)
const goodsPickerLoading = ref(false)
const refundDialogVisible = ref(false)
const refundDialogLoading = ref(false)
const supplementDialogVisible = ref(false)
const supplementDialogLoading = ref(false)
const otherAmountVisible = ref(false)
const otherAmountLoading = ref(false)
const renewVisible = ref(false)
const renewLoading = ref(false)
const merchantCloseVisible = ref(false)
const merchantCloseLoading = ref(false)
const hangVisible = ref(false)
const hangLoading = ref(false)
const exportLoading = ref(false)

const totalMoney = ref(0)
const totalCashPledge = ref(0)
const totalMinuteMoney = ref(0)
const totalCouponMoney = ref(0)

const userPickerPage = ref(1)
const userPickerSize = ref(10)
const userPickerTotal = ref(0)
const userPickerData = ref([])

const renewPage = ref(1)
const renewSize = ref(10)
const renewTotal = ref(0)
const renewData = ref([])

const goodsData = ref([])
const otherAmountData = ref({})

const currentOrder = ref(null)
const goodsPickerMode = ref('change')
const currentGoodsId = ref(0)
const currentTearoomId = ref(0)

const filters = reactive({
  isShop: '',
  tearoomName: '',
  payPhone: '',
  payUserName: '',
  goodsName: '',
  status: '',
  platformType: '',
  payWay: '',
  money: '',
  ordersNo: '',
  startTime: '',
  endTime: '',
})

const userFilters = reactive({
  phone: '',
  userName: '',
})

const goodsFilters = reactive({
  goodsCategory: '',
})

const refundForm = reactive({
  ordersNo: '',
  refundMoney: '',
  refundRemark: '',
  maxRefundMoney: 0,
})

const supplementForm = reactive({
  ordersId: 0,
  amount: '',
})

const merchantCloseForm = reactive({
  ordersId: 0,
  shopDiscount: 1,
  payWay: '',
  userRemark: '',
  payMoney: 0,
})

const hangForm = reactive({
  ordersId: 0,
  ordersGoodsId: 0,
  ordersGoodsName: '',
})

const orderStatusOptions = [
  { label: '全部', value: '' },
  { label: '待支付', value: 1 },
  { label: '预约中', value: 2 },
  { label: '使用中', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 },
  { label: '待评价', value: 6 },
  { label: '待补费', value: 7 },
]

const payWayOptions = [
  { label: '全部', value: '' },
  { label: '微信', value: 1 },
  { label: '支付宝', value: 2 },
  { label: '零钱', value: 3 },
]

const needSupplementOptions = [
  { label: '全部', value: '' },
  { label: '是', value: 1 },
  { label: '否', value: 0 },
]

const platformOptions = [
  { label: '全部', value: '' },
  { label: '美团', value: 1 },
  { label: '抖音', value: 2 },
  { label: '快手', value: 3 },
]

const openTypeOptions = [
  { label: '全部', value: '' },
  { label: '商家开台', value: 1 },
  { label: '用户开台', value: 2 },
]

const goodsCategoryOptions = [
  { label: '全部', value: '' },
  { label: '羽毛球', value: 1 },
  { label: '棋牌桌', value: 2 },
  { label: '其他桌', value: 3 },
  { label: '柜子', value: 4 },
]

const merchantPayWayOptions = [
  { label: '微信', value: 1 },
  { label: '现金', value: 2 },
  { label: '门店会员', value: 3 },
  { label: '美团核销', value: 4 },
  { label: '快手核销', value: 5 },
  { label: '抖音核销', value: 6 },
]

const discountOptions = [1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1]

const summaryText = computed(() => `本页累计支付统计：${totalMoney.value.toFixed(2)}元；押金统计：${totalCashPledge.value.toFixed(2)}元；超出费用统计：${totalMinuteMoney.value.toFixed(2)}元；优惠券金额统计：${totalCouponMoney.value.toFixed(2)}元；`)

const merchantFinalPay = computed(() => (Number(merchantCloseForm.payMoney || 0) * Number(merchantCloseForm.shopDiscount || 1)).toFixed(2))

function buildQueryParams() {
  const params = {
    page: pageNum.value,
    limit: pageSize.value,
    payWay: filters.payWay,
    payPhone: filters.payPhone.trim(),
    payUserName: filters.payUserName.trim(),
    goodsName: filters.goodsName.trim(),
    ordersNo: filters.ordersNo.trim(),
    status: filters.status,
    startTime: filters.startTime,
    endTime: filters.endTime,
    money: filters.money,
    tearoomName: filters.tearoomName.trim(),
    platformType: filters.platformType,
    isShop: filters.isShop,
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') delete params[key]
  })
  return params
}

function normalizeOrderRow(item = {}) {
  const payMoney = Number(item.payMoney || 0)
  const refundMoney = Number(item.refundMoney || 0)
  return {
    ...item,
    payMoney,
    refundMoney,
    refundMoneySf: payMoney - refundMoney,
  }
}

function calcPageSummary(list = []) {
  totalMoney.value = 0
  totalCashPledge.value = 0
  totalMinuteMoney.value = 0
  totalCouponMoney.value = 0
  list.forEach((item) => {
    totalMoney.value += Number(item.payMoney || 0)
    totalCashPledge.value += Number(item.cashPledge || 0)
    totalMinuteMoney.value += Number(item.minuteMoney || 0)
    totalCouponMoney.value += Number(item.couponMoney || 0)
  })
}

function formatMoney(value) {
  const amount = Number(value || 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

function firstImage(value) {
  return String(value || '').split(',').map((item) => item.trim()).filter(Boolean)[0] || ''
}

function openTypeText(value) {
  return Number(value) === 1 ? '商家开台' : '用户开台'
}

function orderTypeText(value) {
  const map = { 1: '开台', 2: '-', 3: '预约' }
  return map[Number(value)] || '-'
}

function platformText(value) {
  const map = { 1: '美团', 2: '抖音', 3: '快手' }
  return map[Number(value)] || '-'
}

function payWayText(value) {
  const map = { 1: '微信', 2: '支付宝', 3: '零钱', 4: '美团核销', 5: '快手核销', 6: '抖音核销' }
  return map[Number(value)] || '-'
}

function orderStatusText(value) {
  const map = { 1: '待支付', 2: '预约中', 3: '使用中', 4: '已完成', 5: '已取消', 6: '待评价', 7: '待补费', 8: '挂账' }
  return map[Number(value)] || '-'
}

function canRefund(row) {
  return [2, 3, 4].includes(Number(row.status)) && Number(row.payMoney || 0) > Number(row.refundMoney || 0)
}

function needsSupplement(row) {
  return Number(row.sumMoney || 0) < 0
}

function goToUser(userId) {
  if (!userId) {
    showError('当前记录未绑定用户')
    return
  }
  router.push(`/users/${userId}`)
}

async function loadData() {
  loading.value = true
  try {
    const data = await getOrderCenterPageApi(buildQueryParams())
    tableData.value = (data.list || []).map(normalizeOrderRow)
    total.value = Number(data.total || 0)
    calcPageSummary(tableData.value)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  pageSize.value = 10
  loadData()
}

function handleReset() {
  filters.isShop = ''
  filters.tearoomName = ''
  filters.payPhone = ''
  filters.payUserName = ''
  filters.goodsName = ''
  filters.status = ''
  filters.platformType = ''
  filters.payWay = ''
  filters.money = ''
  filters.ordersNo = ''
  filters.startTime = ''
  filters.endTime = ''
  pageNum.value = 1
  pageSize.value = 10
  loadData()
}

async function exportData() {
  exportLoading.value = true
  try {
    const blobData = await exportOrderCenterApi(buildQueryParams())
    const blob = new Blob([blobData], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '订单列表.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } finally {
    exportLoading.value = false
  }
}

async function openUserPicker(row) {
  currentOrder.value = row
  userFilters.phone = ''
  userFilters.userName = ''
  userPickerPage.value = 1
  userPickerVisible.value = true
  await loadUserPicker()
}

async function loadUserPicker() {
  userPickerLoading.value = true
  try {
    const data = await getUserCenterPage({
      page: userPickerPage.value,
      limit: userPickerSize.value,
      phone: userFilters.phone.trim(),
      userName: userFilters.userName.trim(),
    })
    userPickerData.value = data.list || []
    userPickerTotal.value = Number(data.total || 0)
  } finally {
    userPickerLoading.value = false
  }
}

async function confirmTransferUser(row) {
  if (!currentOrder.value?.ordersId) return
  await updateOrderApi({
    ordersId: currentOrder.value.ordersId,
    userId: row.userId,
    userNam: row.userName,
    phone: row.phone,
  })
  userPickerVisible.value = false
  showSuccess('转单成功')
  await loadData()
}

function openRefundDialog(row) {
  refundForm.ordersNo = row.ordersNo
  refundForm.refundMoney = ''
  refundForm.refundRemark = ''
  refundForm.maxRefundMoney = Number(row.refundMoneySf || 0)
  refundDialogVisible.value = true
}

async function refundAll(row) {
  const refundMoney = Number(row.refundMoneySf || 0)
  await confirmAction('确定退全款吗？')
  await refundOrderApi({ ordersNo: row.ordersNo, refundMoney })
  showSuccess('退款成功')
  await loadData()
}

async function submitRefundDialog() {
  const amount = Number(refundForm.refundMoney)
  if (!Number.isFinite(amount) || amount <= 0) {
    showError('退款金额必须大于0')
    return
  }
  if (amount > Number(refundForm.maxRefundMoney)) {
    showError('退款金额不能大于最大可退款金额')
    return
  }
  refundDialogLoading.value = true
  try {
    await refundOrderApi({
      ordersNo: refundForm.ordersNo,
      refundMoney: amount,
      refundRemark: refundForm.refundRemark.trim(),
    })
    refundDialogVisible.value = false
    showSuccess('退款成功')
    await loadData()
  } finally {
    refundDialogLoading.value = false
  }
}

async function endOrder(row) {
  await confirmAction('确定要结束订单吗？')
  await closeOrderApi({ ordersId: row.ordersId })
  showSuccess('操作成功')
  await loadData()
}

function openMerchantCloseDialog(row) {
  merchantCloseForm.ordersId = row.ordersId
  merchantCloseForm.shopDiscount = 1
  merchantCloseForm.payWay = ''
  merchantCloseForm.userRemark = row.userRemark || ''
  merchantCloseForm.payMoney = Number(row.payMoney || 0)
  merchantCloseVisible.value = true
}

async function submitMerchantCloseDialog() {
  if (!merchantCloseForm.payWay) {
    showError('请选择支付方式')
    return
  }
  merchantCloseLoading.value = true
  try {
    await closeOrderApi({
      ordersId: merchantCloseForm.ordersId,
      shopDiscount: merchantCloseForm.shopDiscount,
      payWay: merchantCloseForm.payWay,
      userRemark: merchantCloseForm.userRemark.trim(),
    })
    merchantCloseVisible.value = false
    showSuccess('操作成功')
    await loadData()
  } finally {
    merchantCloseLoading.value = false
  }
}

function openSupplementDialog(row) {
  supplementForm.ordersId = row.ordersId
  supplementForm.amount = String(Math.abs(Number(row.sumMoney || 0)) || 0)
  supplementDialogVisible.value = true
}

async function submitSupplementDialog() {
  const amount = Number(supplementForm.amount)
  if (!Number.isFinite(amount) || amount < 0) {
    showError('补费金额不能小于0')
    return
  }
  supplementDialogLoading.value = true
  try {
    await updateOrderApi({ ordersId: supplementForm.ordersId, sumMoney: -amount })
    supplementDialogVisible.value = false
    showSuccess('修改成功')
    await loadData()
  } finally {
    supplementDialogLoading.value = false
  }
}

async function finishSupplement(row) {
  await confirmAction(`补费金额为：${Math.abs(Number(row.sumMoney || 0))}，确定要补费吗？`)
  await updateOrderApi({ ordersId: row.ordersId, status: 4 })
  showSuccess('操作成功')
  await loadData()
}

async function openOtherAmountDialog(row) {
  otherAmountLoading.value = true
  otherAmountVisible.value = true
  try {
    otherAmountData.value = await getOrderVipDetailsApi(row.ordersNo)
  } finally {
    otherAmountLoading.value = false
  }
}

async function openRenewDialog(row) {
  renewPage.value = 1
  renewSize.value = 10
  currentOrder.value = row
  renewVisible.value = true
  await loadRenewData()
}

async function loadRenewData() {
  if (!currentOrder.value?.ordersNo) return
  renewLoading.value = true
  try {
    const data = await getOrderRenewRecordsApi({
      page: renewPage.value,
      limit: renewSize.value,
      content: currentOrder.value.ordersNo,
      state: 400,
    })
    renewData.value = data.list || []
    renewTotal.value = Number(data.total || 0)
  } finally {
    renewLoading.value = false
  }
}

function openGoodsPicker(row, mode) {
  currentOrder.value = row
  currentTearoomId.value = Number(row.tearoomId || 0)
  currentGoodsId.value = Number(row.goodsId || 0)
  goodsPickerMode.value = mode
  goodsFilters.goodsCategory = ''
  if (mode === 'hang') {
    hangForm.ordersId = row.ordersId
    hangForm.ordersGoodsId = 0
    hangForm.ordersGoodsName = ''
  }
  goodsPickerVisible.value = true
  loadGoodsData()
}

async function loadGoodsData() {
  goodsPickerLoading.value = true
  try {
    const data = await getGoodsPageApi({
      page: 1,
      limit: 100,
      goodsName: '',
      status: '',
      tearoomId: currentTearoomId.value,
      goodsCategory: goodsFilters.goodsCategory,
    })
    goodsData.value = data.list || []
  } finally {
    goodsPickerLoading.value = false
  }
}

async function selectGoods(row) {
  if (goodsPickerMode.value === 'change') {
    if (!currentOrder.value?.ordersId) return
    await changeOrderRoomApi({ ordersId: currentOrder.value.ordersId, goodsId: row.goodsId })
    goodsPickerVisible.value = false
    showSuccess('换台成功')
    await loadData()
    return
  }
  hangForm.ordersGoodsId = row.goodsId
  hangForm.ordersGoodsName = row.goodsName || ''
  goodsPickerVisible.value = false
  hangVisible.value = true
}

function openHangDialog(row) {
  hangVisible.value = false
  openGoodsPicker(row, 'hang')
}

async function submitHangDialog() {
  if (!hangForm.ordersGoodsId) {
    showError('请选择羽毛球场')
    return
  }
  hangLoading.value = true
  try {
    await updateHangOrderStatusApi({ ordersId: hangForm.ordersId, ordersGoodsId: hangForm.ordersGoodsId })
    hangVisible.value = false
    showSuccess('操作成功')
    await loadData()
  } finally {
    hangLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="oc">
    <header class="oc-hero nf-page-hero">
      <span class="oc-hero__badge">订单管理</span>
      <h1 class="oc-hero__title">订单中心</h1>
      <p class="oc-hero__desc">查询与管理开台订单，支持多条件筛选、导出、退款、换台、转单与补费等操作</p>
    </header>

    <div class="oc-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="oc-filter__form" @submit.prevent>
      <el-form-item label="开台类型">
        <el-select v-model="filters.isShop" clearable placeholder="请选择" @change="handleSearch">
          <el-option v-for="item in openTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="羽毛球馆名称">
        <el-input v-model="filters.tearoomName" clearable placeholder="请输入羽毛球馆名称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="filters.payPhone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="用户昵称">
        <el-input v-model="filters.payUserName" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="羽毛球场名称">
        <el-input v-model="filters.goodsName" clearable placeholder="请输入羽毛球场名称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="请选择" @change="handleSearch">
          <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优惠券平台">
        <el-select v-model="filters.platformType" clearable placeholder="请选择" @change="handleSearch">
          <el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式">
        <el-select v-model="filters.payWay" clearable placeholder="请选择" @change="handleSearch">
          <el-option v-for="item in payWayOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否需要补费">
        <el-select v-model="filters.money" clearable placeholder="请选择" @change="handleSearch">
          <el-option v-for="item in needSupplementOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单编号">
        <el-input v-model="filters.ordersNo" clearable placeholder="请输入订单编号" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker v-model="filters.startTime" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" placeholder="选择开始时间" clearable />
      </el-form-item>
      <el-form-item label="截止时间">
        <el-date-picker v-model="filters.endTime" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" placeholder="选择截止时间" clearable />
      </el-form-item>
      <el-form-item class="oc-filter__actions">
        <el-space wrap>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" :loading="exportLoading" @click="exportData">导出</el-button>
        </el-space>
      </el-form-item>
      </el-form>
    </div>

    <div class="oc-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="ordersId" label="编号" width="90" fixed="left" />
      <el-table-column prop="tearoomName" label="羽毛球馆名称" min-width="180" fixed="left" />
      <el-table-column label="羽毛球馆图片" width="110">
        <template #default="{ row }">
          <el-image
            v-if="firstImage(row.tearoomTitleImage)"
            :src="firstImage(row.tearoomTitleImage)"
            :preview-src-list="[firstImage(row.tearoomTitleImage)]"
            fit="cover"
            class="thumb-image"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="开台类型" width="100">
        <template #default="{ row }">{{ openTypeText(row.isShop) }}</template>
      </el-table-column>
      <el-table-column prop="goodsName" label="羽毛球场名称" min-width="160" />
      <el-table-column prop="ordersNo" label="订单号" min-width="190" />
      <el-table-column label="订单类型" width="90">
        <template #default="{ row }">{{ orderTypeText(row.ordersType) }}</template>
      </el-table-column>
      <el-table-column label="用户昵称" width="130">
        <template #default="{ row }">
          <span v-if="row.userId" class="user-link" @click="goToUser(row.userId)">{{ row.payUserName || '未绑定' }}</span>
          <span v-else class="text-danger">商家开台</span>
        </template>
      </el-table-column>
      <el-table-column prop="payPhone" label="手机号" width="130" />
      <el-table-column label="开台用户" width="130">
        <template #default="{ row }">
          <span class="user-link" @click="goToUser(row.openUserId)">{{ row.openUserName || '未绑定' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="关台用户" width="130">
        <template #default="{ row }">
          <span class="user-link" @click="goToUser(row.closeUserId)">{{ row.closeUserName || '未绑定' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="vip.vipName" label="会员等级" width="100" />
      <el-table-column label="每半小时价格" width="120">
        <template #default="{ row }">{{ row.hourMoney ? `${row.hourMoney}元` : '-' }}</template>
      </el-table-column>
      <el-table-column label="小时数" width="90">
        <template #default="{ row }">{{ row.productId ? '-' : row.hour || '-' }}</template>
      </el-table-column>
      <el-table-column label="优惠券平台" width="110">
        <template #default="{ row }">{{ platformText(row.platformType) }}</template>
      </el-table-column>
      <el-table-column prop="couponName" label="优惠券名称" min-width="140" />
      <el-table-column label="优惠券金额" width="110">
        <template #default="{ row }"><span class="text-danger">{{ row.couponMoney || 0 }}元</span></template>
      </el-table-column>
      <el-table-column prop="couponNames" label="续费优惠券名称" min-width="150" />
      <el-table-column label="续费优惠券抵扣金额" width="150">
        <template #default="{ row }"><span class="text-danger">{{ row.couponMoneys || 0 }}元</span></template>
      </el-table-column>
      <el-table-column label="会员优惠金额" width="120">
        <template #default="{ row }"><span class="text-danger">{{ row.vipMoney || 0 }}元</span></template>
      </el-table-column>
      <el-table-column label="支付金额" width="160">
        <template #default="{ row }">
          <div><span class="text-danger">{{ row.payMoney || 0 }}元</span></div>
          <div class="minor-text">最终实付金额：{{ formatMoney(row.refundMoneySf) }}</div>
          <el-button link type="primary" @click="openOtherAmountDialog(row)">其他金额</el-button>
        </template>
      </el-table-column>
      <el-table-column label="已退款金额" width="120">
        <template #default="{ row }">{{ row.refundMoney ? `${row.refundMoney}元` : '-' }}</template>
      </el-table-column>
      <el-table-column prop="refundRemark" label="退款原因" min-width="160" show-overflow-tooltip />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="minuteNum" label="超出时间(分钟)" width="120" />
      <el-table-column prop="minuteMoney" label="超出费用" width="100" />
      <el-table-column prop="cashPledge" label="押金" width="90" />
      <el-table-column prop="shopMoney" label="商家总收益" width="110" />
      <el-table-column prop="payMinuteMoney" label="实际分钟计费" width="120" />
      <el-table-column label="补费金额" width="130">
        <template #default="{ row }">
          <div>
            <span class="text-danger">{{ needsSupplement(row) ? `${Math.abs(Number(row.sumMoney || 0))}元` : '0' }}</span>
          </div>
          <el-button link type="danger" :disabled="!isAuth('orderAdmin:update')" @click="openSupplementDialog(row)">修改</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="userRemark" label="订单备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="支付方式" width="100">
        <template #default="{ row }">{{ payWayText(row.payWay) }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" fixed="right">
        <template #default="{ row }">{{ orderStatusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="是否需要补费" width="110" fixed="right">
        <template #default="{ row }">
          <span :class="needsSupplement(row) ? 'text-success' : 'text-danger'">{{ needsSupplement(row) ? '是' : '否' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="付款时间" width="170">
        <template #default="{ row }">{{ row.payTime || '未付款' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="action-group">
            <el-button size="small" type="primary" plain @click="openRenewDialog(row)">续费记录</el-button>
            <el-button v-if="Number(row.status) === 3" size="small" type="primary" :disabled="!isAuth('orderAdmin:huantai')" @click="openGoodsPicker(row, 'change')">换台</el-button>
            <el-button v-if="!row.userId && Number(row.status) !== 5" size="small" type="danger" :disabled="!isAuth('orderAdmin:zhuandan')" @click="openUserPicker(row)">转单</el-button>
            <el-button v-if="canRefund(row)" size="small" type="warning" :disabled="!isAuth('orderAdmin:refund')" @click="openRefundDialog(row)">部分退款</el-button>
            <el-button v-if="canRefund(row)" size="small" type="warning" plain :disabled="!isAuth('orderAdmin:refund')" @click="refundAll(row)">退全款</el-button>
            <el-button v-if="Number(row.isShop) === 1 && [3, 8].includes(Number(row.status))" size="small" type="primary" :disabled="!isAuth('orderAdmin:refund')" @click="openMerchantCloseDialog(row)">结束订单</el-button>
            <el-button v-if="Number(row.isShop) === 1 && Number(row.status) === 3" size="small" type="primary" plain :disabled="!isAuth('orderAdmin:refund')" @click="openHangDialog(row)">挂账</el-button>
            <el-button v-if="Number(row.isShop) !== 1 && [3, 8].includes(Number(row.status))" size="small" type="primary" :disabled="!isAuth('orderAdmin:refund')" @click="endOrder(row)">结束订单</el-button>
            <el-button v-if="Number(row.status) === 7" size="small" type="danger" :disabled="!isAuth('orderAdmin:refund')" @click="finishSupplement(row)">完成补费</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

      <div class="summary-bar">{{ summaryText }}</div>

      <BasePagination
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 30, 50, 100]"
        :total="total"
        @update:currentPage="pageNum = $event; loadData()"
        @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
      />
    </div>

    <el-dialog v-model="userPickerVisible" title="用户列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid search-grid--compact" @submit.prevent>
        <el-form-item label="手机号">
          <el-input v-model="userFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="userPickerPage = 1; loadUserPicker()" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userFilters.userName" clearable placeholder="请输入昵称" @keyup.enter="userPickerPage = 1; loadUserPicker()" />
        </el-form-item>
        <el-form-item>
          <el-space>
            <el-button type="primary" @click="userPickerPage = 1; loadUserPicker()">查询</el-button>
            <el-button @click="userFilters.phone = ''; userFilters.userName = ''; userPickerPage = 1; loadUserPicker()">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
      <el-table v-loading="userPickerLoading" :data="userPickerData" border>
        <el-table-column prop="userId" label="编号" width="90" />
        <el-table-column prop="userName" label="昵称" min-width="160" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="confirmTransferUser(row)">确定</el-button>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination
        :current-page="userPickerPage"
        :page-size="userPickerSize"
        :page-sizes="[10, 20, 30, 50, 100]"
        :total="userPickerTotal"
        @update:currentPage="userPickerPage = $event; loadUserPicker()"
        @update:pageSize="userPickerSize = $event; userPickerPage = 1; loadUserPicker()"
      />
    </el-dialog>

    <el-dialog v-model="refundDialogVisible" title="部分退款" width="640px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form label-width="100px" @submit.prevent>
        <el-form-item label="退款金额">
          <div class="dialog-field">
            <el-input v-model="refundForm.refundMoney" type="number" min="0" placeholder="请输入退款金额" />
            <div class="hint-text">最大可退款金额为：{{ formatMoney(refundForm.maxRefundMoney) }}</div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="refundForm.refundRemark" type="textarea" :rows="4" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="refundDialogLoading" @click="submitRefundDialog">确定</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="supplementDialogVisible" title="修改补费金额" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form label-width="100px" @submit.prevent>
        <el-form-item label="补费金额">
          <el-input v-model="supplementForm.amount" type="number" min="0" placeholder="请输入补费金额" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="supplementDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="supplementDialogLoading" @click="submitSupplementDialog">确定</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="otherAmountVisible" title="其他会员金额" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-row v-loading="otherAmountLoading" :gutter="16">
        <el-col v-for="(item, index) in otherAmountData.vipList || []" :key="`${item.vipName}-${index}`" :xs="24" :sm="12" :lg="6">
          <div class="stat-card stat-card--compact">
            <div class="stat-title">{{ item.vipName }}需支付金额</div>
            <div class="stat-value stat-value--small">{{ formatMoney(item.payMoney) }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card stat-card--compact">
            <div class="stat-title">点餐金额</div>
            <div class="stat-value stat-value--small">{{ formatMoney(otherAmountData.tbOrder?.orderMoney || 0) }}</div>
          </div>
        </el-col>
        <el-col v-for="(item, index) in otherAmountData.vipList || []" :key="`${item.vipName}-total-${index}`" :xs="24" :sm="12" :lg="6">
          <div class="stat-card stat-card--compact stat-card--accent">
            <div class="stat-title">{{ item.vipName }}+点餐总支付金额</div>
            <div class="stat-value stat-value--small text-danger">{{ formatMoney(Number(item.payMoney || 0) + Number(otherAmountData.tbOrder?.orderMoney || 0)) }}</div>
          </div>
        </el-col>
      </el-row>
    </el-dialog>

    <el-dialog v-model="renewVisible" title="续费记录" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="renewLoading" :data="renewData" border>
        <el-table-column prop="id" label="编号" width="90" />
        <el-table-column label="续费类型" width="100">
          <template #default="{ row }">{{ Number(row.type) === 1 ? '优惠券' : Number(row.type) === 2 ? '会员卡' : Number(row.type) === 3 ? '微信' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="优惠券名称" min-width="180">
          <template #default="{ row }">{{ Number(row.type) === 1 ? row.selfCouponUser?.couponName || '-' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="url" label="金额" width="100" />
        <el-table-column prop="createAt" label="操作时间" width="170" />
      </el-table>
      <BasePagination
        :current-page="renewPage"
        :page-size="renewSize"
        :page-sizes="[10, 20, 30, 50, 100]"
        :total="renewTotal"
        @update:currentPage="renewPage = $event; loadRenewData()"
        @update:pageSize="renewSize = $event; renewPage = 1; loadRenewData()"
      />
    </el-dialog>

    <el-dialog v-model="merchantCloseVisible" title="结束订单" width="760px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form label-width="100px" @submit.prevent>
        <el-form-item label="折扣比例">
          <el-radio-group v-model="merchantCloseForm.shopDiscount">
            <el-radio v-for="item in discountOptions" :key="item" :label="item">{{ item === 1 ? '不打折' : `${Math.round(item * 10)}折` }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="支付金额">
          <div class="dialog-field-inline">
            <span class="text-danger">{{ merchantFinalPay }}</span>
            <span class="minor-text">(原价：{{ merchantCloseForm.payMoney }})</span>
          </div>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="merchantCloseForm.payWay">
            <el-radio v-for="item in merchantPayWayOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="merchantCloseForm.userRemark" type="textarea" :rows="4" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="merchantCloseVisible = false">取消</el-button>
          <el-button type="primary" :loading="merchantCloseLoading" @click="submitMerchantCloseDialog">确定</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="hangVisible" title="挂账" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form label-width="120px" @submit.prevent>
        <el-form-item label="选择羽毛球场">
          <el-input v-model="hangForm.ordersGoodsName" readonly placeholder="请选择羽毛球场" @click="openGoodsPicker(currentOrder, 'hang')">
            <template #append>
              <el-button @click="openGoodsPicker(currentOrder, 'hang')">选择</el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="hangVisible = false">取消</el-button>
          <el-button type="primary" :loading="hangLoading" @click="submitHangDialog">确定</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="goodsPickerVisible" title="羽毛球场列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid search-grid--compact" @submit.prevent>
        <el-form-item label="羽毛球场类型">
          <el-select v-model="goodsFilters.goodsCategory" clearable placeholder="请选择羽毛球场类型" @change="loadGoodsData">
            <el-option v-for="item in goodsCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table v-loading="goodsPickerLoading" :data="goodsData" border>
        <el-table-column prop="goodsId" label="编号" width="90" />
        <el-table-column prop="goodsName" label="羽毛球场名称" min-width="160" />
        <el-table-column label="开台前图片" width="100">
          <template #default="{ row }"><el-image :src="row.goodsCoverImage" :preview-src-list="[row.goodsCoverImage]" fit="cover" class="thumb-image" preview-teleported /></template>
        </el-table-column>
        <el-table-column label="开台后图片" width="100">
          <template #default="{ row }"><el-image :src="row.goodsTitleImage" :preview-src-list="[row.goodsTitleImage]" fit="cover" class="thumb-image" preview-teleported /></template>
        </el-table-column>
        <el-table-column prop="goodsType" label="羽毛球场信息" min-width="180" />
        <el-table-column label="羽毛球场图标" width="100">
          <template #default="{ row }"><el-image :src="row.iconImg" :preview-src-list="[row.iconImg]" fit="cover" class="thumb-image" preview-teleported /></template>
        </el-table-column>
        <el-table-column label="半小时价" width="100">
          <template #default="{ row }">{{ formatMoney(row.hourPrice) }}</template>
        </el-table-column>
        <el-table-column label="分钟价" width="100">
          <template #default="{ row }">{{ formatMoney(row.minutePrice) }}</template>
        </el-table-column>
        <el-table-column label="押金" width="90">
          <template #default="{ row }">{{ formatMoney(row.cashPledge) }}</template>
        </el-table-column>
        <el-table-column label="预约价格" width="100">
          <template #default="{ row }">{{ formatMoney(row.subscription) }}</template>
        </el-table-column>
        <el-table-column label="销量" width="90">
          <template #default="{ row }">{{ row.sales || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="(goodsPickerMode === 'change' && Number(row.hourPrice) === Number(currentOrder?.hourMoney) && Number(row.goodsId) !== Number(currentGoodsId)) || (goodsPickerMode === 'hang' && Number(row.goodsId) !== Number(currentGoodsId))"
              link
              type="primary"
              @click="selectGoods(row)"
            >确定</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.oc {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.oc-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.oc-hero__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--nf-text-primary);
  background: var(--nf-brand-lite);
  border: 1px solid var(--nf-brand);
  border-radius: var(--nf-radius-sm, 6px);
}

.oc-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.oc-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.oc-filter.nf-filter-card {
  margin-bottom: 0;
}

.oc-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }

  :deep(.el-date-editor) {
    width: 180px;
  }
}

.oc-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.oc-table.nf-table-card {
  margin-bottom: 0;
  padding-bottom: 12px;

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

.thumb-image {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: block;
}

.user-link {
  color: var(--el-color-primary);
  cursor: pointer;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #13ce66;
}

.minor-text {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.summary-bar {
  color: #b94a48;
  font-size: 16px;
  font-weight: 600;
  margin: 12px 0 8px;
  line-height: 1.5;
}

.action-group {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.action-group :deep(.el-button) {
  width: 100%;
  margin-left: 0;
}

.search-grid--compact {
  margin-bottom: 16px;
}

.dialog-field {
  width: 100%;
}

.hint-text {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 6px;
}

.dialog-field-inline {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-card {
  height: 100%;
  padding: 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 14px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
}

.stat-card--compact {
  margin-bottom: 16px;
}

.stat-card--accent {
  background: linear-gradient(135deg, #fff9f5 0%, #fff1eb 100%);
}

.stat-title {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 12px;
}

.stat-value {
  color: var(--nf-brand-deep);
  font-size: 28px;
  font-weight: 700;
}

.stat-value--small {
  font-size: 22px;
}

@media (max-width: 768px) {
  .oc-filter__form {
    :deep(.el-select),
    :deep(.el-input),
    :deep(.el-date-editor) {
      width: 100%;
    }
  }
}
</style>

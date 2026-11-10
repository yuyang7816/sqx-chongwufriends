<script setup>
import { computed, ref, reactive } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { closeOrderApi, exportOrderCenterApi, getOrderCenterPageApi, getOrderVipDetailsApi, refundOrderApi } from '@/api/modules/order-center'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderList = ref([])

const filters = reactive({
  tearoomName: '',
  payPhone: '',
  payUserName: '',
  goodsName: '',
  smallHardwareName: '',
  ordersNo: '',
  status: '',
  payWay: '',
  startTime: '',
  endTime: '',
})

const statusOptions = [
  { value: '', label: '全部' },
  { value: 1, label: '待支付' },
  { value: 2, label: '预约中' },
  { value: 3, label: '使用中' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已取消' },
  { value: 6, label: '待评价' },
  { value: 7, label: '待补费' },
]

const payWayOptions = [
  { value: '', label: '全部' },
  { value: 1, label: '微信' },
  { value: 2, label: '支付宝' },
  { value: 3, label: '零钱' },
]

const pageTotalMoney = ref(0)
const pageTotalCouponMoney = ref(0)

const vipDialogVisible = ref(false)
const vipDetail = ref({})

const partialRefundDialogVisible = ref(false)
const partialRefundLoading = ref(false)
const partialRefundForm = reactive({
  ordersNo: '',
  refundMoney: '',
  refundRemark: '',
  maxRefundMoney: 0,
})

function statusText(status) {
  if (Number(status) === 1) return '待支付'
  if (Number(status) === 2) return '预约中'
  if (Number(status) === 3) return '使用中'
  if (Number(status) === 4) return '已完成'
  if (Number(status) === 5) return '已取消'
  if (Number(status) === 6) return '待评价'
  if (Number(status) === 7) return '待补费'
  if (Number(status) === 8) return '挂账'
  return '-'
}

function payWayText(payWay) {
  if (Number(payWay) === 1) return '微信'
  if (Number(payWay) === 2) return '支付宝'
  if (Number(payWay) === 3) return '零钱'
  return '-'
}

function splitImages(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function finalPaidMoney(row) {
  const payMoney = Number(row.payMoney || 0)
  const refundMoney = Number(row.refundMoney || 0)
  return payMoney - refundMoney
}

async function loadOrders() {
  loading.value = true
  try {
    const data = await getOrderCenterPageApi({
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
      tearoomName: filters.tearoomName.trim(),
      ordersType: 2,
      smallHardwareName: filters.smallHardwareName.trim(),
    })
    orderList.value = (data.list || []).map((item) => ({
      ...item,
      refundMoneySf: finalPaidMoney(item),
    }))
    total.value = data.total || 0
    pageTotalMoney.value = orderList.value.reduce((sum, item) => sum + Number(item.payMoney || 0), 0)
    pageTotalCouponMoney.value = orderList.value.reduce((sum, item) => sum + Number(item.couponMoney || 0), 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadOrders()
}

function handleReset() {
  Object.assign(filters, {
    tearoomName: '',
    payPhone: '',
    payUserName: '',
    goodsName: '',
    smallHardwareName: '',
    ordersNo: '',
    status: '',
    payWay: '',
    startTime: '',
    endTime: '',
  })
  pageNum.value = 1
  loadOrders()
}

async function handleExport() {
  const blobData = await exportOrderCenterApi({
    payWay: filters.payWay,
    payPhone: filters.payPhone.trim(),
    payUserName: filters.payUserName.trim(),
    goodsName: filters.goodsName.trim(),
    ordersNo: filters.ordersNo.trim(),
    status: filters.status,
    startTime: filters.startTime,
    endTime: filters.endTime,
    tearoomName: filters.tearoomName.trim(),
    ordersType: 2,
    smallHardwareName: filters.smallHardwareName.trim(),
  })
  const blob = new Blob([blobData], { type: 'application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '订单列表.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

async function openVipDialog(row) {
  vipDetail.value = await getOrderVipDetailsApi(row.ordersNo)
  vipDialogVisible.value = true
}

async function handleFullRefund(row) {
  const payMoney = row.refundMoney ? Number(row.payMoney) - Number(row.refundMoney) : Number(row.payMoney)
  await confirmAction('确定退全款?')
  await refundOrderApi({ ordersNo: row.ordersNo, refundMoney: payMoney })
  showSuccess('退款成功')
  await loadOrders()
}

function openPartialRefund(row) {
  partialRefundForm.ordersNo = row.ordersNo
  partialRefundForm.refundRemark = ''
  partialRefundForm.refundMoney = ''
  partialRefundForm.maxRefundMoney = row.refundMoney ? Number(row.payMoney) - Number(row.refundMoney) : Number(row.payMoney)
  partialRefundDialogVisible.value = true
}

async function submitPartialRefund() {
  if (partialRefundForm.refundMoney === '') return showError('请输入退款金额')
  if (Number(partialRefundForm.refundMoney) <= 0) return showError('退款金额必须大于0')
  if (Number(partialRefundForm.refundMoney) > Number(partialRefundForm.maxRefundMoney)) return showError('退款金额不能大于支付金额')

  partialRefundLoading.value = true
  try {
    await refundOrderApi({
      ordersNo: partialRefundForm.ordersNo,
      refundMoney: partialRefundForm.refundMoney,
      refundRemark: partialRefundForm.refundRemark,
    })
    showSuccess('退款成功')
    partialRefundDialogVisible.value = false
    await loadOrders()
  } finally {
    partialRefundLoading.value = false
  }
}

async function handleCloseOrder(row) {
  await confirmAction('确定要结束订单?')
  await closeOrderApi({ ordersId: row.ordersId })
  showSuccess('操作成功')
  await loadOrders()
}

const pageSummaryText = computed(() => `本页累计支付统计：${pageTotalMoney.value.toFixed(2)}元；优惠券金额统计：${pageTotalCouponMoney.value.toFixed(2)}元；`)

loadOrders()
</script>

<template>
  <div class="cab-order">
    <header class="cab-order-hero nf-page-hero">
      <span class="cab-order-hero__badge">展示柜</span>
      <h1 class="cab-order-hero__title">展示柜订单</h1>
      <p class="cab-order-hero__desc">查看与管理展示柜商品订单，支持多条件筛选、导出与退款操作</p>
    </header>

    <div class="cab-order-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="cab-order-filter__form" @submit.prevent>
        <el-form-item label="羽毛球馆名称">
          <el-input v-model="filters.tearoomName" placeholder="请输入羽毛球馆名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filters.payPhone" placeholder="请输入手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="filters.payUserName" placeholder="请输入用户昵称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="柜子名称">
          <el-input v-model="filters.smallHardwareName" placeholder="请输入柜子名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="柜子商品名称">
          <el-input v-model="filters.goodsName" placeholder="请输入柜子商品名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="filters.payWay" placeholder="请选择支付方式" clearable @change="handleSearch">
            <el-option v-for="item in payWayOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="filters.ordersNo" placeholder="请输入订单编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="filters.startTime" type="datetime" value-format="YYYY-MM-DD" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="filters.endTime" type="datetime" value-format="YYYY-MM-DD" placeholder="选择截止时间" />
        </el-form-item>
        <el-form-item class="cab-order-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleExport">导出</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="cab-order-table nf-table-card">
      <el-table v-loading="loading" :data="orderList" border>
        <el-table-column prop="ordersId" label="编号" width="80" fixed="left" />
        <el-table-column prop="tearoomName" label="羽毛球馆名称" width="160" fixed="left" />
        <el-table-column label="羽毛球馆图片" width="100" fixed="left">
          <template #default="{ row }">
            <div v-if="splitImages(row.tearoomTitleImage).length">
              <el-image :src="splitImages(row.tearoomTitleImage)[0]" class="thumb-image" :preview-src-list="splitImages(row.tearoomTitleImage)" preview-teleported />
            </div>
            <div v-else>暂无图片</div>
          </template>
        </el-table-column>
        <el-table-column prop="smallHardwareName" label="柜子名称" width="160" />
        <el-table-column prop="goodsName" label="柜子商品名称" width="160" />
        <el-table-column prop="ordersNo" label="订单号" width="200" />
        <el-table-column prop="payUserName" label="用户昵称" width="120">
          <template #default="{ row }">
            <el-link v-if="row.userId" type="primary" @click="$router.push(`/users/${row.userId}`)">{{ row.payUserName || '未绑定' }}</el-link>
            <span v-else>商家开台</span>
          </template>
        </el-table-column>
        <el-table-column prop="payPhone" label="手机号" width="120" />
        <el-table-column prop="couponName" label="优惠券名称" width="120" />
        <el-table-column prop="couponMoney" label="优惠券金额" width="120">
          <template #default="{ row }"><span class="text-danger">{{ row.couponMoney || 0 }}元</span></template>
        </el-table-column>
        <el-table-column prop="couponNames" label="续费优惠券名称" width="120" />
        <el-table-column prop="couponMoneys" label="续费优惠券抵扣金额" width="140">
          <template #default="{ row }"><span class="text-danger">{{ row.couponMoneys || 0 }}元</span></template>
        </el-table-column>
        <el-table-column prop="vipMoney" label="会员优惠金额" width="120">
          <template #default="{ row }"><span class="text-danger">{{ row.vipMoney || 0 }}元</span></template>
        </el-table-column>
        <el-table-column prop="payMoney" label="支付金额" width="180">
          <template #default="{ row }">
            <div class="money-cell">
              <span class="text-danger">{{ row.payMoney || 0 }}元</span>
              <span>(最终实付金额:{{ Number(row.refundMoneySf || 0).toFixed(2) }})</span>
              <el-button size="small" type="primary" plain @click="openVipDialog(row)">其他金额</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="refundMoney" label="已退款金额" width="120">
          <template #default="{ row }"><span class="text-danger">{{ row.refundMoney ? `${row.refundMoney}元` : '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="refundRemark" label="退款原因" min-width="160" />
        <el-table-column prop="userRemark" label="订单备注" min-width="180" />
        <el-table-column prop="payWay" label="支付方式" width="100">
          <template #default="{ row }">{{ payWayText(row.payWay) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="payTime" label="付款时间" width="160">
          <template #default="{ row }">{{ row.payTime || '未付款' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button v-if="[2,3,4].includes(Number(row.status))" size="small" type="warning" plain :disabled="!isAuth('orderAdmin:refund') || Number(row.payMoney) === Number(row.refundMoney || 0)" @click="openPartialRefund(row)">部分退款</el-button>
              <el-button v-if="[2,3,4].includes(Number(row.status))" size="small" type="warning" :disabled="!isAuth('orderAdmin:refund') || Number(row.payMoney) === Number(row.refundMoney || 0)" @click="handleFullRefund(row)">退全款</el-button>
              <el-button v-if="[3,8].includes(Number(row.status))" size="small" type="primary" :disabled="!isAuth('orderAdmin:refund')" @click="handleCloseOrder(row)">结束订单</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-total">{{ pageSummaryText }}</div>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadOrders()" @update:pageSize="pageSize = $event; pageNum = 1; loadOrders()" />
    </div>

    <el-dialog v-model="vipDialogVisible" title="其他会员金额" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="vip-grid">
        <template v-if="vipDetail.vipList?.length">
          <div v-for="(item, index) in vipDetail.vipList" :key="index" class="stat-card">
            <div class="stat-title">{{ item.vipName }}需支付金额</div>
            <div class="stat-value">{{ Number(item.payMoney || 0).toFixed(2) }}</div>
          </div>
        </template>
        <div class="stat-card">
          <div class="stat-title">点餐金额</div>
          <div class="stat-value">{{ vipDetail.tbOrder ? Number(vipDetail.tbOrder.orderMoney || 0).toFixed(2) : '0.00' }}</div>
        </div>
        <template v-if="vipDetail.vipList?.length">
          <div v-for="(item, index) in vipDetail.vipList" :key="`sum-${index}`" class="stat-card accent-card">
            <div class="stat-title">{{ item.vipName }}+点餐总支付金额</div>
            <div class="stat-value">{{ (Number(item.payMoney || 0) + Number(vipDetail.tbOrder?.orderMoney || 0)).toFixed(2) }}</div>
          </div>
        </template>
      </div>
    </el-dialog>

    <el-dialog v-model="partialRefundDialogVisible" title="部分退款" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">退款金额</span><div class="dialog-field"><el-input v-model="partialRefundForm.refundMoney" type="number" min="0" placeholder="请输入退款金额" /><div class="hint-text">* 最大可退款金额为：{{ partialRefundForm.maxRefundMoney.toFixed(2) }}</div></div></div>
        <div class="form-row"><span class="form-label">备注</span><el-input v-model="partialRefundForm.refundRemark" class="dialog-field" type="textarea" :rows="4" placeholder="请输入备注" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="partialRefundDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="partialRefundLoading" @click="submitPartialRefund">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.cab-order {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.cab-order-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cab-order-hero__badge {
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

.cab-order-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.cab-order-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.cab-order-filter.nf-filter-card {
  margin-bottom: 0;
}

.cab-order-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }

  :deep(.el-date-editor) {
    width: 180px;
  }
}

.cab-order-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.cab-order-table.nf-table-card {
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
  width: 50px;
  height: 50px;
  border-radius: 8px;
}

.money-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.text-danger {
  color: var(--el-color-danger);
}

.page-total {
  color: var(--el-color-danger);
  font-size: 18px;
  margin-top: 12px;
}

.vip-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.stat-card {
  padding: 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
}

.accent-card {
  background: var(--el-fill-color-extra-light);
}

.stat-title {
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
}

.stat-value {
  font-size: 22px;
  color: var(--el-color-primary);
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.form-label {
  width: 100px;
  line-height: 32px;
  text-align: right;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
}

.hint-text {
  color: var(--el-color-danger);
  margin-top: 6px;
  font-size: 12px;
}

@media (max-width: 768px) {
  .cab-order-filter__form {
    :deep(.el-select),
    :deep(.el-input),
    :deep(.el-date-editor) {
      width: 100%;
    }
  }

  .form-row {
    flex-direction: column;
  }

  .form-label {
    width: auto;
    text-align: left;
  }
}
</style>

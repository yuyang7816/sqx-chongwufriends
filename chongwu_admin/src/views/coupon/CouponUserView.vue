<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  getCouponUserPageApi,
  getCouponUserTearoomOptionsApi,
  invalidateCouponUserApi,
  verifyCouponUserApi,
} from '@/api/modules/coupon-user'
import { confirmAction, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()

const activeTab = ref('second')
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const couponUsers = ref([])
const tearoomOptions = ref([])

const filters = reactive({
  platformType: '',
  type: '',
  couponName: '',
  nickName: '',
  phone: '',
  tearoomId: '',
  status: '',
})

const platformOptions = [
  { label: '全部', value: '' },
  { label: '美团', value: 1 },
  { label: '抖音', value: 2 },
  { label: '快手', value: 3 },
]

const typeOptions = [
  { label: '全部', value: '' },
  { label: '优惠券', value: 2 },
  { label: '实物', value: 3 },
  { label: '定时券', value: 4 },
  { label: '周期卡', value: 5 },
  { label: '充值卡', value: 6 },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '未使用', value: 1 },
  { label: '已使用', value: 2 },
  { label: '已过期', value: 3 },
]

function platformText(value) {
  if (Number(value) === 1) return '美团'
  if (Number(value) === 2) return '抖音'
  if (Number(value) === 3) return '快手'
  return '-'
}

function typeText(value) {
  if (Number(value) === 2) return '优惠券'
  if (Number(value) === 3) return '实物'
  if (Number(value) === 4) return '定时券'
  if (Number(value) === 5) return '周期卡'
  if (Number(value) === 6) return '充值卡'
  return '-'
}

function sourceText(value) {
  if (Number(value) === 1) return '新用户领取'
  if (Number(value) === 2) return '邀请新用户'
  if (Number(value) === 3) return '邀请用户下单'
  if (Number(value) === 4) return '购买'
  if (Number(value) === 5) return '兑换'
  if (Number(value) === 6) return '商家赠送'
  return '后台赠送'
}

function payWayText(value) {
  if (Number(value) === 1) return '微信app'
  if (Number(value) === 2) return '微信公众号'
  if (Number(value) === 3) return '微信小程序'
  if (Number(value) === 4) return '支付宝app'
  if (Number(value) === 5) return '支付宝H5'
  if (Number(value) === 6) return '零钱'
  return '-'
}

function statusText(value) {
  if (Number(value) === 1) return '未使用'
  if (Number(value) === 2) return '已使用'
  if (Number(value) === 3) return '已过期'
  return '-'
}

function normalizeRow(item = {}) {
  return {
    ...item,
    couponUserId: item.couponUserId,
    platformType: Number(item.platformType || 0),
    type: Number(item.type || 0),
    tearoom: item.tearoom || {},
    couponName: item.couponName || '',
    nickName: item.nickName || '',
    phone: item.phone || '',
    platform: item.platform,
    payWay: item.payWay,
    couponId: item.couponId || '',
    buyRule: item.buyRule || '',
    cdkCode: item.cdkCode || '',
    hour: item.hour || '',
    failureTime: item.failureTime || '',
    status: Number(item.status || 0),
    createTime: item.createTime || item.createdAt || '',
    userId: item.userId || 0,
  }
}

async function loadTearoomOptions() {
  tearoomOptions.value = await getCouponUserTearoomOptionsApi()
}

async function loadCouponUsers() {
  loading.value = true
  try {
    const data = await getCouponUserPageApi({
      page: pageNum.value - 1,
      size: pageSize.value,
      couponName: filters.couponName.trim(),
      type: filters.type,
      nickName: filters.nickName.trim(),
      ordersId: '',
      status: filters.status,
      phone: filters.phone.trim(),
      platformType: filters.platformType,
      tearoomId: filters.tearoomId,
    })
    couponUsers.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadCouponUsers()
}

function handleReset() {
  pageNum.value = 1
  filters.platformType = ''
  filters.type = ''
  filters.couponName = ''
  filters.nickName = ''
  filters.phone = ''
  filters.tearoomId = ''
  filters.status = ''
  loadCouponUsers()
}

function goUserDetail(row) {
  if (!row.userId) return
  router.push(`/users/${row.userId}`)
}

async function handleInvalidate(row) {
  await confirmAction('确定要作废?')
  await invalidateCouponUserApi(row.couponUserId)
  showSuccess('操作成功')
  await loadCouponUsers()
}

async function handleVerify(row) {
  await confirmAction('确定要核销?')
  await verifyCouponUserApi(row.cdkCode)
  showSuccess('操作成功')
  await loadCouponUsers()
}

loadCouponUsers()
loadTearoomOptions()
</script>

<template>
  <div class="cu">
    <header class="cu-hero nf-page-hero">
      <span class="cu-hero__badge">优惠券</span>
      <h1 class="cu-hero__title">优惠券领取记录</h1>
      <p class="cu-hero__desc">查看用户优惠券领取与使用记录，支持多条件筛选、作废与核销操作</p>
    </header>

    <div class="cu-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="cu-filter__form" @submit.prevent>
        <el-form-item label="优惠券平台"><el-select v-model="filters.platformType" clearable @change="handleSearch"><el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="优惠券类型"><el-select v-model="filters.type" clearable @change="handleSearch"><el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="优惠券名称"><el-input v-model="filters.couponName" clearable placeholder="请输入优惠券名称" @keyup.enter="handleSearch" /></el-form-item>
        <el-form-item label="用户昵称"><el-input v-model="filters.nickName" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="filters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" /></el-form-item>
        <el-form-item label="羽毛球馆名称"><el-select v-model="filters.tearoomId" class="cu-filter__tearoom" clearable filterable placeholder="请选择羽毛球馆" @change="handleSearch"><el-option v-for="item in tearoomOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="filters.status" clearable @change="handleSearch"><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item class="cu-filter__actions"><el-space wrap><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-space></el-form-item>
      </el-form>
    </div>

    <div class="cu-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="领取列表" name="second">
          <div class="cu-table nf-table-card">
          <el-table v-loading="loading" :data="couponUsers" border>
            <el-table-column prop="couponUserId" label="编号" width="80" fixed="left" />
            <el-table-column label="优惠券平台" width="100"><template #default="{ row }">{{ platformText(row.platformType) }}</template></el-table-column>
            <el-table-column label="优惠券类型" width="100"><template #default="{ row }">{{ typeText(row.type) }}</template></el-table-column>
            <el-table-column prop="tearoom.tearoomName" label="羽毛球馆名称" width="160" />
            <el-table-column prop="couponName" label="优惠券名称" width="150" fixed="left" />
            <el-table-column prop="nickName" label="用户昵称" width="150"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row)">{{ row.nickName || '未绑定' }}</span></template></el-table-column>
            <el-table-column prop="phone" label="手机号" width="160" />
            <el-table-column label="来源" width="110"><template #default="{ row }">{{ sourceText(row.platform) }}</template></el-table-column>
            <el-table-column label="支付类型" width="110"><template #default="{ row }">{{ payWayText(row.payWay) }}</template></el-table-column>
            <el-table-column prop="couponId" label="优惠劵编号" width="160" />
            <el-table-column label="购买须知" width="200"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.buyRule }}</div></template><div>{{ row.buyRule }}</div></el-popover></template></el-table-column>
            <el-table-column prop="cdkCode" label="核销码" width="140" />
            <el-table-column prop="hour" label="使用时长(半小时)" width="140" />
            <el-table-column prop="failureTime" label="有效期限" width="150" />
            <el-table-column label="状态" width="90"><template #default="{ row }"><span :class="row.status === 1 ? 'status-primary' : ''">{{ statusText(row.status) }}</span></template></el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="150" />
            <el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-space wrap><el-button v-if="row.status === 1" size="small" type="primary" plain :disabled="!isAuth('couponuser:zuofei')" @click="handleInvalidate(row)">作废</el-button><el-button v-if="row.status === 1" size="small" type="warning" plain :disabled="!isAuth('couponuser:hexiao')" @click="handleVerify(row)">核销</el-button></el-space></template></el-table-column>
          </el-table>

          <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadCouponUsers()" @update:pageSize="pageSize = $event; loadCouponUsers()" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped lang="scss">
.cu {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.cu-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cu-hero__badge {
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

.cu-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.cu-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.cu-filter.nf-filter-card {
  margin-bottom: 0;
}

.cu-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 150px;
  }
}

.cu-filter__tearoom {
  width: 220px;
}

.cu-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.cu-tabs {
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

.cu-table.nf-table-card {
  margin-bottom: 0;
  margin-top: 0;
  padding-bottom: 12px;
  border-radius: 0 0 8px 8px;
  border-top: none;
  box-shadow: var(--nf-shadow-card);

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

.link-text {
  color: var(--nf-brand-deep);
  cursor: pointer;
}

.status-primary {
  color: var(--nf-brand-deep);
}

.clamp-text {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  max-height: 80px;
}

@media (max-width: 768px) {
  .cu-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }

  .cu-filter__tearoom {
    width: 100%;
  }
}
</style>

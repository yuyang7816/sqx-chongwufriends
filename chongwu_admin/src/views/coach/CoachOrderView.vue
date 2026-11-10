<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { accomplishCoachOrderApi, cancelCoachOrderApi, getCoachOrderPageApi } from '@/api/modules/coach-order'
import { confirmAction, showSuccess } from '@/utils/modal'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderList = ref([])
const pageTotalMoney = ref(0)

const filters = reactive({
  status: '',
  userName: '',
  phone: '',
  serviceName: '',
  tearoomName: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 2 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 },
]

function statusText(status) {
  if (Number(status) === 0) return '待支付'
  if (Number(status) === 2) return '已支付'
  if (Number(status) === 4) return '已完成'
  if (Number(status) === 5) return '已取消'
  return '-'
}

function statusClass(status) {
  if (Number(status) === 0 || Number(status) === 2 || Number(status) === 4) return 'text-primary'
  if (Number(status) === 5) return 'text-muted'
  return 'text-muted'
}

async function loadOrders() {
  loading.value = true
  try {
    const data = await getCoachOrderPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      userName: filters.userName.trim(),
      phone: filters.phone.trim(),
      status: filters.status,
      tearoomName: filters.tearoomName.trim(),
      serviceName: filters.serviceName.trim(),
    })
    orderList.value = data.list || []
    total.value = data.total || 0
    pageTotalMoney.value = orderList.value.reduce((sum, item) => sum + Number(item.payMoney || 0), 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadOrders()
}

function handleReset() {
  filters.status = ''
  filters.userName = ''
  filters.phone = ''
  filters.serviceName = ''
  filters.tearoomName = ''
  pageNum.value = 1
  loadOrders()
}

function goUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

async function handleAccomplish(row) {
  await confirmAction('确认订单已完成?')
  await accomplishCoachOrderApi(row.helpOrderId)
  showSuccess('操作成功')
  await loadOrders()
}

async function handleCancel(row) {
  await confirmAction('确定取消订单?')
  await cancelCoachOrderApi(row.helpOrderId)
  showSuccess('取消成功')
  await loadOrders()
}

const pageMoneyText = computed(() => `${pageTotalMoney.value.toFixed(2)}元`)

loadOrders()
</script>

<template>
  <div class="coach-ord">
    <header class="coach-ord-hero nf-page-hero">
      <span class="coach-ord-hero__badge">教练管理</span>
      <h1 class="coach-ord-hero__title">教练订单</h1>
      <p class="coach-ord-hero__desc">查看与管理教练服务订单，支持多条件筛选与订单操作</p>
    </header>

    <div class="coach-ord-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="coach-ord-filter__form" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单用户">
          <el-input v-model="filters.userName" placeholder="请输入下单用户" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="下单人手机号">
          <el-input v-model="filters.phone" placeholder="请输入下单人手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="服务名称">
          <el-input v-model="filters.serviceName" placeholder="请输入服务名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="羽毛球馆名称">
          <el-input v-model="filters.tearoomName" placeholder="请输入羽毛球馆名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="coach-ord-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="coach-ord-table nf-table-card">
      <el-table v-loading="loading" :data="orderList" border>
        <el-table-column prop="helpOrderId" label="编号" width="80" fixed="left" />
        <el-table-column prop="userName" label="下单用户" width="120" fixed="left">
          <template #default="{ row }">
            <el-link type="primary" @click="goUserDetail(row.userId)">{{ row.userName || '未绑定' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="下单人手机号" width="130" />
        <el-table-column prop="tearoomName" label="羽毛球馆名称" min-width="160" />
        <el-table-column prop="helpService.serviceName" label="服务名称" min-width="160" />
        <el-table-column label="规格" min-width="220">
          <template #default="{ row }">
            <div v-if="row.helpServiceSku">
              <div>规格：{{ row.helpServiceSku.skuName }}</div>
              <div>价格：{{ row.helpServiceSku.money }}￥</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="num" label="下单数量" width="90" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="statusClass(row.status)">{{ statusText(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button v-if="Number(row.status) === 2" size="small" type="primary" @click="handleAccomplish(row)">确认订单</el-button>
              <el-button v-if="Number(row.status) === 2 || Number(row.status) === 4" size="small" type="warning" @click="handleCancel(row)">取消订单</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-total">本页累计总金额统计：{{ pageMoneyText }}</div>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 40]" :total="total" @update:currentPage="pageNum = $event; loadOrders()" @update:pageSize="pageSize = $event; pageNum = 1; loadOrders()" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.coach-ord {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.coach-ord-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.coach-ord-hero__badge {
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

.coach-ord-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.coach-ord-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.coach-ord-filter.nf-filter-card {
  margin-bottom: 0;
}

.coach-ord-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.coach-ord-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.coach-ord-table.nf-table-card {
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

.page-total {
  margin: 12px 0 0;
  color: var(--el-color-danger);
  font-size: 16px;
  font-weight: 500;
}

.text-primary {
  color: var(--nf-brand-deep);
}

.text-muted {
  color: var(--nf-text-secondary);
}

@media (max-width: 768px) {
  .coach-ord-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }
}
</style>

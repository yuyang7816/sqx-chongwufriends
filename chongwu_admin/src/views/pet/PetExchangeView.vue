<script setup>
import { ref, computed } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import UserFilterFields from '@/components/common/UserFilterFields.vue'
import { getPetOrderPageApi } from '@/api/modules/pet-order'
import { updatePetExchangeStatusApi } from '@/api/modules/pet-exchange'
import { useRouteUserFilter } from '@/utils/user-filter'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const orderTypeMap = [
  { value: '', label: '全部' },
  { value: 'product', label: '商品订单' },
  { value: 'exchange', label: '积分兑换' }
]
const exchangeStatusMap = [
  { value: '', label: '全部' },
  { value: 'pending', label: '进行中' },
  { value: 'done', label: '已完成' }
]

const { filters, appendUserFilter } = useRouteUserFilter()
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const orderType = ref('')
const exchangeStatus = ref('')

function formatPoints(value) {
  const num = Number(value)
  if (!Number.isFinite(num) || num < 1000) return String(num ?? 0)
  const k = num / 1000
  const rounded = Math.round(k * 10) / 10
  return `${rounded % 1 === 0 ? rounded.toFixed(0) : rounded.toFixed(1)}k`
}

function isExchangeRow(row) {
  return Number(row.bizType) === 4 || Number(row.payType) === 2
}

function rowTypeLabel(row) {
  return isExchangeRow(row) ? '积分兑换' : '商品订单'
}

function rowStatusText(row) {
  const s = Number(row.status)
  if (s === 2) return '已完成'
  if (s === 3) return '已取消'
  if (s === 4) return '已退款'
  return '进行中'
}

function rowDate(row) {
  return row.createTime ? String(row.createTime).slice(0, 10) : ''
}

async function loadData() {
  loading.value = true
  try {
    const params = appendUserFilter({ page: pageNum.value, limit: pageSize.value })
    if (orderType.value) params.orderType = orderType.value
    if (exchangeStatus.value) params.exchangeStatus = exchangeStatus.value
    const data = await getPetOrderPageApi(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

async function markDone(row) {
  await confirmAction('确认标记为已完成？')
  try {
    await updatePetExchangeStatusApi({ id: row.id, status: 2 })
    showSuccess('状态已更新')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '更新失败')
  }
}

loadData()
</script>

<template>
  <div class="pexchange">
    <header class="pexchange-hero nf-page-hero">
      <span class="pexchange-hero__badge">宠物业务</span>
      <h1 class="pexchange-hero__title">商品订单</h1>
      <p class="pexchange-hero__desc">查看与管理商品订单与积分兑换订单，支持按类型、状态与用户筛选</p>
    </header>

    <div class="pexchange-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pexchange-filter__form" @submit.prevent>
        <UserFilterFields v-model:phone="filters.phone" v-model:username="filters.username" @search="handleSearch" />
        <el-form-item label="类型">
          <el-select v-model="orderType" placeholder="全部类型" clearable style="width: 160px" @change="handleSearch">
            <el-option v-for="item in orderTypeMap" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="exchangeStatus" placeholder="全部状态" clearable style="width: 160px" @change="handleSearch">
            <el-option v-for="item in exchangeStatusMap" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="pexchange-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pexchange-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column label="商品" min-width="150">
          <template #default="{ row }">{{ row.spuName || '-' }}</template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="isExchangeRow(row) ? 'warning' : 'success'" effect="light" round>
              {{ rowTypeLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额/积分" width="120">
          <template #default="{ row }">
            <span v-if="isExchangeRow(row)" class="pexchange-points">🌸 {{ formatPoints(row.pointAmount) }} 积分</span>
            <span v-else class="pexchange-cash">¥ {{ Number(row.payAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="日期" width="120">
          <template #default="{ row }">{{ rowDate(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ rowStatusText(row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="isAuth('pet:exchange:update') && isExchangeRow(row) && rowStatusText(row) !== '已完成'"
              link
              type="success"
              @click="markDone(row)"
            >标记完成</el-button>
            <span v-else class="pexchange-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.pexchange {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pexchange-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pexchange-hero__badge {
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

.pexchange-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pexchange-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pexchange-filter.nf-filter-card {
  margin-bottom: 0;
}

.pexchange-filter__form {
  margin-bottom: 0;
}

.pexchange-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pexchange-table.nf-table-card {
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

.pexchange-points {
  font-weight: 700;
  color: #ce8f4c;
}

.pexchange-cash {
  font-weight: 700;
  color: #ce8f4c;
}

.pexchange-muted {
  color: var(--nf-text-secondary);
}
</style>

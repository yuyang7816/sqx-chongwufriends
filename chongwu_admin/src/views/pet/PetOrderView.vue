<script setup>
import { ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import UserFilterFields from '@/components/common/UserFilterFields.vue'
import {
  cancelPetOrderApi,
  getPetOrderInfoApi,
  getPetOrderPageApi,
  refundPetOrderApi,
} from '@/api/modules/pet-order'
import { useRouteUserFilter } from '@/utils/user-filter'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

const bizTypeMap = { 1: '商城', 2: '投喂', 3: '次卡', 4: '积分兑换' }
const payStatusMap = { 1: '待支付', 2: '已支付', 3: '已支付', 6: '已退款' }
const statusMap = { 1: '待支付', 2: '已支付', 3: '已取消', 4: '已退款' }

const { filters, appendUserFilter } = useRouteUserFilter()
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const orderNo = ref('')
const bizType = ref('')
const status = ref('')
const payStatus = ref('')
const detailVisible = ref(false)
const detail = ref(null)

async function loadData() {
  loading.value = true
  try {
    const params = appendUserFilter({ page: pageNum.value, limit: pageSize.value })
    if (orderNo.value.trim()) params.orderNo = orderNo.value.trim()
    if (bizType.value !== '') params.bizType = bizType.value
    if (status.value !== '') params.status = status.value
    if (payStatus.value !== '') params.payStatus = payStatus.value
    const data = await getPetOrderPageApi(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载商城订单失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.phone = ''
  filters.username = ''
  orderNo.value = ''
  bizType.value = ''
  status.value = ''
  payStatus.value = ''
  handleSearch()
}

async function showDetail(row) {
  try {
    detail.value = await getPetOrderInfoApi(row.id)
    detailVisible.value = true
  } catch {
    showError('获取订单详情失败')
  }
}

async function cancelRow(row) {
  await confirmAction('确认取消该订单？')
  await cancelPetOrderApi({ id: row.id })
  showSuccess('订单已取消')
  await loadData()
}

async function refundRow(row) {
  await confirmAction('确认发起退款？（占位接口）')
  try {
    await refundPetOrderApi({ id: row.id })
    showSuccess('退款请求已提交')
    await loadData()
  } catch {
    showError('退款接口尚未就绪')
  }
}

loadData()
</script>

<template>
  <div class="porder">
    <header class="porder-hero nf-page-hero">
      <span class="porder-hero__badge">商城管理</span>
      <h1 class="porder-hero__title">商城订单</h1>
      <p class="porder-hero__desc">查看与管理商城、积分兑换等订单，支持按订单号、用户、状态筛选</p>
    </header>

    <div class="porder-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="porder-filter__form" @submit.prevent>
        <el-form-item label="订单号">
          <el-input
            v-model="orderNo"
            clearable
            placeholder="订单号"
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <UserFilterFields v-model:phone="filters.phone" v-model:username="filters.username" @search="handleSearch" />
        <el-form-item label="业务类型">
          <el-select v-model="bizType" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option v-for="(label, key) in bizTypeMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="status" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="payStatus" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option label="待支付" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="已退款" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item class="porder-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="porder-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border empty-text="暂无订单">
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="spuName" label="商品" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.spuName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="payAmount" label="金额" width="90" />
        <el-table-column prop="pointAmount" label="积分" width="80" />
        <el-table-column label="业务类型" width="100">
          <template #default="{ row }">{{ bizTypeMap[row.bizType] || row.bizType || '-' }}</template>
        </el-table-column>
        <el-table-column label="支付" width="90">
          <template #default="{ row }">{{ payStatusMap[row.payStatus] ?? row.payStatus ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ statusMap[row.status] ?? row.status ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" @click="showDetail(row)">详情</el-button>
              <el-button size="small" type="warning" @click="cancelRow(row)">取消</el-button>
              <el-button size="small" type="danger" @click="refundRow(row)">退款</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="520px" class="nf-pro-dialog" align-center>
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ detail.spuName || detail.spuId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.porder {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.porder-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.porder-hero__badge {
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

.porder-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.porder-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.porder-filter.nf-filter-card {
  margin-bottom: 0;
}

.porder-filter__form {
  margin-bottom: 0;
}

.porder-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.porder-table.nf-table-card {
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
</style>

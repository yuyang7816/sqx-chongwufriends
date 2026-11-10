<script setup>
import { ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import UserFilterFields from '@/components/common/UserFilterFields.vue'
import { getPaymentOrderPageApi } from '@/api/modules/pet-payment'
import { useRouteUserFilter } from '@/utils/user-filter'

const statusMap = { 1: '待支付', 2: '已支付', 3: '已关闭' }
const businessTypeMap = {
  mall_order: '商城订单',
  feed_order: '投喂订单',
  vip: '会员',
}
const { filters, appendUserFilter } = useRouteUserFilter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

async function loadData() {
  loading.value = true
  try {
    const data = await getPaymentOrderPageApi(appendUserFilter({ page: pageNum.value, limit: pageSize.value }))
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

loadData()
</script>

<template>
  <div class="ppayorder">
    <header class="ppayorder-hero nf-page-hero">
      <span class="ppayorder-hero__badge">宠物业务</span>
      <h1 class="ppayorder-hero__title">支付单</h1>
      <p class="ppayorder-hero__desc">统一收银台流水：对应商城订单 / 投喂订单等业务单的微信/支付宝支付记录（非业务订单本身）</p>
    </header>

    <div class="ppayorder-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="ppayorder-filter__form" @submit.prevent>
        <UserFilterFields v-model:phone="filters.phone" v-model:username="filters.username" @search="handleSearch" />
        <el-form-item class="ppayorder-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="ppayorder-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="payNo" label="支付单号" min-width="160" />
        <el-table-column prop="bizOrderNo" label="业务单号" min-width="160" />
        <el-table-column label="业务类型" width="110">
          <template #default="{ row }">{{ businessTypeMap[row.businessType] || row.businessType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="payChannel" label="渠道" width="100" />
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ statusMap[row.status] ?? row.status }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
      </el-table>
      <BasePagination
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        @update:current-page="pageNum = $event; loadData()"
        @update:page-size="pageSize = $event; pageNum = 1; loadData()"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.ppayorder {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.ppayorder-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ppayorder-hero__badge {
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

.ppayorder-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.ppayorder-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.ppayorder-filter.nf-filter-card {
  margin-bottom: 0;
}

.ppayorder-filter__form {
  margin-bottom: 0;
}

.ppayorder-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.ppayorder-table.nf-table-card {
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

<script setup>
import { ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getPaymentCallbackLogPageApi } from '@/api/modules/pet-payment'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const statusMap = { 1: '成功', 2: '失败' }

async function loadData() {
  loading.value = true
  try {
    const data = await getPaymentCallbackLogPageApi({ page: pageNum.value, limit: pageSize.value })
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

loadData()
</script>

<template>
  <div class="ppaylog">
    <header class="ppaylog-hero nf-page-hero">
      <span class="ppaylog-hero__badge">宠物业务</span>
      <h1 class="ppaylog-hero__title">支付回调日志</h1>
      <p class="ppaylog-hero__desc">查看微信支付等渠道回调记录，用于排查支付成功/失败状态</p>
    </header>

    <div class="ppaylog-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>快捷操作</span>
      </div>
      <el-form :inline="true" class="ppaylog-filter__form" @submit.prevent>
        <el-form-item class="ppaylog-filter__actions">
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="ppaylog-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="channel" label="渠道" width="100" />
        <el-table-column prop="tradeType" label="类型" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ statusMap[row.status] ?? row.status }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>
      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.ppaylog {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.ppaylog-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ppaylog-hero__badge {
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

.ppaylog-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.ppaylog-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.ppaylog-filter.nf-filter-card {
  margin-bottom: 0;
}

.ppaylog-filter__form {
  margin-bottom: 0;
}

.ppaylog-filter__actions {
  margin-bottom: 0;

  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.ppaylog-table.nf-table-card {
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

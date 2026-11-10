<script setup>
import { ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getLoveRecordRankingPageApi, getLoveRecordTearoomOptionsApi } from '@/api/modules/love-record-rankings'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rankingList = ref([])
const tearoomOptions = ref([])
const tearoomName = ref('')

function normalizeRow(item = {}) {
  return {
    ...item,
    id: item.id,
    tearoomName: item.tearoomName || '',
    money: item.money ?? 0,
    donationMoney: item.donationMoney ?? 0,
  }
}

async function loadRankingList() {
  loading.value = true
  try {
    const data = await getLoveRecordRankingPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      tearoomName: tearoomName.value,
    })
    rankingList.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadTearoomOptions() {
  tearoomOptions.value = await getLoveRecordTearoomOptionsApi()
}

function handleSearch() {
  pageNum.value = 1
  loadRankingList()
}

loadRankingList()
loadTearoomOptions()
</script>

<template>
  <div class="lwrank">
    <header class="lwrank-hero nf-page-hero">
      <span class="lwrank-hero__badge">排行榜</span>
      <h1 class="lwrank-hero__title">爱心钱包排行榜</h1>
      <p class="lwrank-hero__desc">按门店统计爱心钱包余额与已捐款金额排行</p>
    </header>

    <div class="lwrank-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="lwrank-filter__form" @submit.prevent>
        <el-form-item label="门店名称">
          <el-select v-model="tearoomName" style="width: 220px" clearable filterable placeholder="请选择门店名称" @change="handleSearch">
            <el-option v-for="item in tearoomOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomName" />
          </el-select>
        </el-form-item>
        <el-form-item class="lwrank-filter__actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="lwrank-table nf-table-card">
      <el-table v-loading="loading" :data="rankingList" border>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="tearoomName" label="门店名称" min-width="180" />
        <el-table-column prop="money" label="爱心钱包" min-width="120" />
        <el-table-column prop="donationMoney" label="已捐款" min-width="120" />
      </el-table>
      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadRankingList()" @update:pageSize="pageSize = $event; loadRankingList()" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.lwrank {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.lwrank-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.lwrank-hero__badge {
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

.lwrank-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.lwrank-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.lwrank-filter.nf-filter-card {
  margin-bottom: 0;
}

.lwrank-filter__form {
  margin-bottom: 0;
}

.lwrank-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.lwrank-table.nf-table-card {
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

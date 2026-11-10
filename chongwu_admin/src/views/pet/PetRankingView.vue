<script setup>
import { ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  getPetRankingPageApi,
  getPetRankingPeriodsApi,
} from '@/api/modules/pet-ranking'
import { showError } from '@/utils/modal'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const weekOffset = ref(0)
const periodOverview = ref(null)

async function loadPeriods() {
  try {
    periodOverview.value = await getPetRankingPeriodsApi()
  } catch (error) {
    periodOverview.value = null
  }
}

function handleWeekChange() {
  pageNum.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetRankingPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      weekOffset: weekOffset.value,
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载榜单失败')
  } finally {
    loading.value = false
  }
}

async function initPage() {
  await loadPeriods()
  await loadData()
}

initPage()
</script>

<template>
  <div class="prank">
    <header class="prank-hero nf-page-hero">
      <span class="prank-hero__badge">营销管理</span>
      <h1 class="prank-hero__title">打榜排行</h1>
      <p class="prank-hero__desc">查看本周/上周宠物爱心榜。打榜次数与规则请在「系统配置 → 全局配置」中维护</p>
    </header>

    <div class="prank-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>

      <div class="prank-filter__toolbar">
        <el-radio-group v-model="weekOffset" @change="handleWeekChange">
          <el-radio-button :value="0">本周</el-radio-button>
          <el-radio-button :value="-1">上周</el-radio-button>
        </el-radio-group>
        <el-button type="primary" plain @click="initPage">刷新</el-button>
      </div>

      <el-alert
        v-if="periodOverview?.currentWeek"
        type="info"
        :closable="false"
        show-icon
        class="prank-filter__alert"
        :title="`当前周期：${periodOverview.currentWeek.name}（ID ${periodOverview.currentWeek.id}）`"
      />
    </div>

    <div class="prank-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border empty-text="暂无榜单数据">
        <el-table-column prop="rankNo" label="排名" width="80" />
        <el-table-column prop="petId" label="宠物ID" width="100" />
        <el-table-column prop="petName" label="宠物名称" min-width="120">
          <template #default="{ row }">{{ row.petName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="storeName" label="门店" min-width="120">
          <template #default="{ row }">{{ row.storeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="likeCount" label="爱心值" width="100" />
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
.prank {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.prank-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.prank-hero__badge {
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

.prank-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.prank-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.prank-filter.nf-filter-card {
  margin-bottom: 0;
}

.prank-filter__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.prank-filter__alert {
  margin-bottom: 0;
}

.prank-table.nf-table-card {
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

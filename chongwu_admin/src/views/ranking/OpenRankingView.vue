<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { getOpenRankingPageApi, getOpenRankingTearoomOptionsApi } from '@/api/modules/open-rankings'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rankingList = ref([])
const tearoomOptions = ref([])

const filters = reactive({
  title: '',
  scoreType: '',
  tearoomName: '',
  city: '',
  startTime: '',
  endTime: '',
})

const scoreTypeOptions = [
  { value: '', label: '全国' },
  { value: 2, label: '城市' },
  { value: 3, label: '店铺' },
]

function formatDuration(value) {
  let seconds = Number(value || 0)
  if (!Number.isFinite(seconds) || seconds < 0) {
    return '-'
  }
  const hours = Math.floor(seconds / 3600)
  seconds %= 3600
  const minutes = Math.floor(seconds / 60)
  seconds %= 60
  return `${String(hours).padStart(2, '0')}时${String(minutes).padStart(2, '0')}分${String(seconds).padStart(2, '0')}秒`
}

function normalizeRow(item = {}) {
  return {
    ...item,
    userId: item.userId || '',
    userName: item.userName || '',
    avatar: item.avatar || '',
    ordersCount: item.ordersCount ?? 0,
    durationHms: formatDuration(item.duration),
  }
}

function avatarText(row) {
  return String(row.userName || '未').slice(0, 1)
}

async function loadRankingList() {
  loading.value = true
  try {
    const data = await getOpenRankingPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      userName: filters.title.trim(),
      phone: '',
      city: filters.city.trim(),
      startTime: filters.startTime,
      endTime: filters.endTime,
      tearoomName: filters.tearoomName,
    })
    rankingList.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadTearoomOptions() {
  tearoomOptions.value = await getOpenRankingTearoomOptionsApi()
}

function handleSearch() {
  if (filters.scoreType === '') {
    filters.tearoomName = ''
    filters.city = ''
  }
  pageNum.value = 1
  loadRankingList()
}

function handleReset() {
  filters.title = ''
  filters.startTime = ''
  filters.endTime = ''
  filters.tearoomName = ''
  filters.city = ''
  filters.scoreType = ''
  pageNum.value = 1
  loadRankingList()
  loadTearoomOptions()
}

function goUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

loadRankingList()
loadTearoomOptions()
</script>

<template>
  <div class="orank">
    <header class="orank-hero nf-page-hero">
      <span class="orank-hero__badge">排行榜</span>
      <h1 class="orank-hero__title">开台排行榜</h1>
      <p class="orank-hero__desc">按全国、城市或店铺维度统计用户开台订单数与开台总时长</p>
    </header>

    <div class="orank-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="orank-filter__form" @submit.prevent>
        <el-form-item label="用户昵称">
          <el-input v-model="filters.title" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.scoreType" style="width: 150px" @change="handleSearch">
            <el-option v-for="item in scoreTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="filters.scoreType === 2" label="城市">
          <el-input v-model="filters.city" clearable placeholder="请输入城市" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item v-if="filters.scoreType === 3" label="羽毛球馆名称">
          <el-select v-model="filters.tearoomName" style="width: 220px" filterable @change="handleSearch">
            <el-option v-for="item in tearoomOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomName" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="filters.startTime" type="datetime" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="filters.endTime" type="datetime" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择截止时间" />
        </el-form-item>
        <el-form-item class="orank-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="orank-table nf-table-card">
      <el-table v-loading="loading" :data="rankingList" border>
        <el-table-column label="序号" width="80">
          <template #default="{ $index }">{{ $index + 1 + (pageNum - 1) * pageSize }}</template>
        </el-table-column>
        <el-table-column prop="userName" label="用户昵称" min-width="160">
          <template #default="{ row }">
            <span class="orank-link" @click="goUserDetail(row.userId)">{{ row.userName || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图像" width="90">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :src="row.avatar" :size="40" />
            <el-avatar v-else :size="40">{{ avatarText(row) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="ordersCount" label="订单总数" width="120" />
        <el-table-column prop="durationHms" label="开台总时长" min-width="160" />
      </el-table>
      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadRankingList()" @update:pageSize="pageSize = $event; loadRankingList()" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.orank {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.orank-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.orank-hero__badge {
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

.orank-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.orank-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.orank-filter.nf-filter-card {
  margin-bottom: 0;
}

.orank-filter__form {
  margin-bottom: 0;
}

.orank-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.orank-table.nf-table-card {
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

.orank-link {
  color: var(--nf-brand-deep);
  cursor: pointer;
}
</style>

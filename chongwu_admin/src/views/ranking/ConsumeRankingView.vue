<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { getConsumeRankingPageApi } from '@/api/modules/consume-rankings'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rankingList = ref([])

const filters = reactive({
  title: '',
  phone: '',
  startTime: '',
  endTime: '',
})

function normalizeRow(item = {}) {
  return {
    ...item,
    userId: item.userId || '',
    userName: item.userName || '',
    phone: item.phone || '',
    avatar: item.avatar || '',
    money: item.money ?? 0,
  }
}

function avatarText(row) {
  return String(row.userName || '未').slice(0, 1)
}

async function loadRankingList() {
  loading.value = true
  try {
    const data = await getConsumeRankingPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      userName: filters.title.trim(),
      startTime: filters.startTime,
      endTime: filters.endTime,
      phone: filters.phone.trim(),
    })
    rankingList.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadRankingList()
}

function handleReset() {
  filters.title = ''
  filters.phone = ''
  filters.startTime = ''
  filters.endTime = ''
  pageNum.value = 1
  loadRankingList()
}

function goUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

loadRankingList()
</script>

<template>
  <div class="crank">
    <header class="crank-hero nf-page-hero">
      <span class="crank-hero__badge">排行榜</span>
      <h1 class="crank-hero__title">消费排行</h1>
      <p class="crank-hero__desc">按用户昵称、手机号与时间范围统计订单消费金额排行</p>
    </header>

    <div class="crank-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="crank-filter__form" @submit.prevent>
        <el-form-item label="用户昵称">
          <el-input v-model="filters.title" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="filters.startTime" type="datetime" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="filters.endTime" type="datetime" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择截止时间" />
        </el-form-item>
        <el-form-item class="crank-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="crank-table nf-table-card">
      <el-table v-loading="loading" :data="rankingList" border>
        <el-table-column label="序号" width="80">
          <template #default="{ $index }">{{ $index + 1 + (pageNum - 1) * pageSize }}</template>
        </el-table-column>
        <el-table-column prop="userName" label="用户昵称" min-width="160">
          <template #default="{ row }">
            <span class="crank-link" @click="goUserDetail(row.userId)">{{ row.userName || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="图像" width="90">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :src="row.avatar" :size="40" />
            <el-avatar v-else :size="40">{{ avatarText(row) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="money" label="订单金额" min-width="120" />
      </el-table>
      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadRankingList()" @update:pageSize="pageSize = $event; loadRankingList()" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.crank {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.crank-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.crank-hero__badge {
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

.crank-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.crank-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.crank-filter.nf-filter-card {
  margin-bottom: 0;
}

.crank-filter__form {
  margin-bottom: 0;
}

.crank-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.crank-table.nf-table-card {
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

.crank-link {
  color: var(--nf-brand-deep);
  cursor: pointer;
}
</style>

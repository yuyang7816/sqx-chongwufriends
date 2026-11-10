<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  getPetCommunityComplaintPageApi,
  handlePetCommunityComplaintApi,
} from '@/api/modules/pet-community'
import { confirmAction, showSuccess } from '@/utils/modal'

const router = useRouter()

const statusMap = { 1: '待处理', 2: '已处理' }
const statusTagType = { 1: 'warning', 2: 'success' }
const targetTypeMap = { 1: '动态', 2: '评论' }

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const filters = ref({
  targetType: '',
  status: '',
  userName: '',
  keyword: '',
})

const summary = computed(() => ({
  pending: tableData.value.filter((row) => Number(row.status) === 1).length,
  handled: tableData.value.filter((row) => Number(row.status) === 2).length,
}))

function statusText(value) {
  return statusMap[Number(value)] ?? value ?? '-'
}

function targetTypeText(value) {
  return targetTypeMap[Number(value)] ?? value ?? '-'
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pageNum.value,
      limit: pageSize.value,
      keyword: filters.value.keyword.trim(),
      userName: filters.value.userName.trim(),
    }
    if (filters.value.targetType !== '') params.targetType = Number(filters.value.targetType)
    if (filters.value.status !== '') params.status = Number(filters.value.status)
    const data = await getPetCommunityComplaintPageApi(params)
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

function handleReset() {
  filters.value = { targetType: '', status: '', userName: '', keyword: '' }
  pageNum.value = 1
  loadData()
}

function goToUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

async function handleRow(row) {
  await confirmAction('确认将该投诉标记为已处理？')
  await handlePetCommunityComplaintApi({ id: row.id, status: 2 })
  showSuccess('已处理')
  await loadData()
}

loadData()
</script>

<template>
  <div class="pcommc">
    <header class="pcommc-hero nf-page-hero">
      <span class="pcommc-hero__badge">宠物业务</span>
      <h1 class="pcommc-hero__title">朋友圈投诉管理</h1>
      <p class="pcommc-hero__desc">处理用户提交的动态/评论投诉</p>
    </header>

    <div class="pcommc-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
        <el-tag v-if="summary.pending" type="warning" size="small">本页待处理 {{ summary.pending }}</el-tag>
        <el-tag v-if="summary.handled" type="success" size="small">本页已处理 {{ summary.handled }}</el-tag>
      </div>
      <el-form :inline="true" class="pcommc-filter__form" @submit.prevent>
        <el-form-item label="投诉类型">
          <el-select v-model="filters.targetType" clearable placeholder="全部">
            <el-option label="动态" :value="1" />
            <el-option label="评论" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option label="待处理" :value="1" />
            <el-option label="已处理" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="filters.userName" clearable placeholder="投诉人昵称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" clearable placeholder="投诉原因" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
        <el-form-item class="pcommc-filter__actions">
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="pcommc-table nf-table-card">
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="76" />
      <el-table-column label="投诉人" width="120">
        <template #default="{ row }">
          <span v-if="row.userId" class="user-link" @click="goToUserDetail(row.userId)">
            {{ row.userName || `用户${row.userId}` }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">{{ row.targetTypeText || targetTypeText(row.targetType) }}</template>
      </el-table-column>
      <el-table-column prop="targetId" label="对象ID" width="90" />
      <el-table-column label="对象摘要" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.targetSummary || '-' }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="投诉原因" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.status] || 'info'" size="small">
            {{ row.statusText || statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="168" />
      <el-table-column label="操作" width="110" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            v-if="Number(row.status) === 1"
            type="primary"
            link
            @click="handleRow(row)"
          >
            标记已处理
          </el-button>
          <span v-else class="muted-text">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />
    </div>
  </div>
</template>

<style scoped lang="scss">
.pcommc {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pcommc-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pcommc-hero__badge {
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

.pcommc-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pcommc-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pcommc-filter.nf-filter-card {
  margin-bottom: 0;
}

.pcommc-filter__form {
  margin-bottom: 0;

  :deep(.el-select) {
    width: 140px;
  }
}

.pcommc-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pcommc-table.nf-table-card {
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

.user-link {
  color: var(--el-color-primary);
  cursor: pointer;
}

.muted-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>

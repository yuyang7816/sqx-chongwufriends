<script setup>
import { ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import UserFilterFields from '@/components/common/UserFilterFields.vue'
import {
  getAdoptPageApi,
  getAdoptVisitPageApi,
  saveAdoptVisitApi,
  updateAdoptStatusApi,
} from '@/api/modules/pet-adopt'
import { useRouteUserFilter } from '@/utils/user-filter'
import { showError, showSuccess } from '@/utils/modal'

/** 三态：申请中(1~5) / 通过(6) / 拒绝(7) */
const statusMap = {
  1: '申请中',
  2: '申请中',
  3: '申请中',
  4: '申请中',
  5: '申请中',
  6: '通过',
  7: '拒绝',
}

function resolveStatusLabel(status) {
  const n = Number(status)
  if (n >= 1 && n <= 5) return '申请中'
  return statusMap[n] || '未知'
}

const { filters, appendUserFilter } = useRouteUserFilter()
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const updatingId = ref(null)

const visitVisible = ref(false)
const visitLoading = ref(false)
const visitSaving = ref(false)
const visitList = ref([])
const currentRow = ref(null)
const visitForm = ref({
  visitTime: '',
  content: '',
})

const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)

function canAudit(status) {
  const n = Number(status)
  return n >= 1 && n <= 5
}

function isFinal(status) {
  return status === 6 || status === 7
}

async function loadData() {
  loading.value = true
  try {
    const params = appendUserFilter({ page: pageNum.value, limit: pageSize.value })
    delete params.username
    const data = await getAdoptPageApi(params)
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

async function applyStatus(row, status) {
  if (status === 7) {
    rejectTarget.value = row
    rejectReason.value = row.rejectReason || ''
    rejectVisible.value = true
    return
  }
  updatingId.value = row.id
  try {
    await updateAdoptStatusApi({ id: row.id, status })
    showSuccess(status === 6 ? '已通过，宠物已归属用户' : '状态已更新')
    await loadData()
  } finally {
    updatingId.value = null
  }
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    showError('请填写拒绝原因')
    return
  }
  updatingId.value = rejectTarget.value.id
  try {
    await updateAdoptStatusApi({
      id: rejectTarget.value.id,
      status: 7,
      rejectReason: rejectReason.value.trim(),
    })
    rejectVisible.value = false
    showSuccess('已拒绝申请')
    await loadData()
  } finally {
    updatingId.value = null
  }
}

async function openVisitDialog(row) {
  currentRow.value = row
  visitVisible.value = true
  visitLoading.value = true
  visitList.value = []
  visitForm.value = {
    visitTime: new Date().toISOString().slice(0, 16).replace('T', ' '),
    content: '',
  }
  try {
    const data = await getAdoptVisitPageApi({ applicationId: row.id, page: 1, limit: 50 })
    visitList.value = data.list || []
  } finally {
    visitLoading.value = false
  }
}

async function submitVisit() {
  if (!visitForm.value.content.trim()) {
    showError('请填写回访内容')
    return
  }
  visitSaving.value = true
  try {
    await saveAdoptVisitApi({
      applicationId: currentRow.value.id,
      visitTime: visitForm.value.visitTime.replace(' ', 'T'),
      content: visitForm.value.content.trim(),
    })
    showSuccess('回访记录已保存')
    visitForm.value.content = ''
    const data = await getAdoptVisitPageApi({ applicationId: currentRow.value.id, page: 1, limit: 50 })
    visitList.value = data.list || []
  } finally {
    visitSaving.value = false
  }
}

loadData()
</script>

<template>
  <div class="padopt">
    <header class="padopt-hero nf-page-hero">
      <span class="padopt-hero__badge">宠物业务</span>
      <h1 class="padopt-hero__title">领养审核</h1>
      <p class="padopt-hero__desc">审核状态：申请中 / 通过 / 拒绝。通过后宠物归属用户并可设为岛宅常驻</p>
    </header>

    <div class="padopt-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="padopt-filter__form" @submit.prevent>
        <UserFilterFields v-model:phone="filters.phone" v-model:username="filters.username" @search="handleSearch" />
        <el-form-item class="padopt-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="padopt-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="realName" label="申请人" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="petName" label="宠物" width="100" />
        <el-table-column prop="petId" label="宠物ID" width="90" />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="reason" label="申请摘要" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.status === 6 ? 'success' : row.status === 7 ? 'danger' : 'warning'"
            >
              {{ row.statusLabel || resolveStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" min-width="120" show-overflow-tooltip />
        <el-table-column label="审核操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="padopt-actions">
              <template v-if="isFinal(row.status)">
                <span class="padopt-actions__done">{{ row.status === 7 ? '已拒绝' : '已完成' }}</span>
              </template>
              <template v-else-if="canAudit(row.status)">
                <el-button
                  size="small"
                  type="success"
                  :loading="updatingId === row.id"
                  @click="applyStatus(row, 6)"
                >
                  通过
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  :disabled="updatingId === row.id"
                  @click="applyStatus(row, 7)"
                >
                  拒绝
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="回访" width="90" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openVisitDialog(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="rejectVisible" title="拒绝领养申请" width="480px" class="nf-pro-dialog" align-center>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请填写拒绝原因，将通知申请人"
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="warning" :disabled="!rejectReason.trim()" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="visitVisible" :title="`回访记录 - ${currentRow?.realName || ''}`" width="720px" class="nf-pro-dialog" align-center>
      <el-form label-width="90px" class="visit-form">
        <el-form-item label="回访时间">
          <el-input v-model="visitForm.visitTime" placeholder="yyyy-MM-dd HH:mm" />
        </el-form-item>
        <el-form-item label="回访内容">
          <el-input v-model="visitForm.content" type="textarea" :rows="3" placeholder="填写本次回访情况" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="visitSaving" @click="submitVisit">新增回访</el-button>
        </el-form-item>
      </el-form>
      <el-divider />
      <el-table v-loading="visitLoading" :data="visitList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="visitTime" label="回访时间" width="170" />
        <el-table-column prop="operatorId" label="操作人ID" width="120" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!visitLoading && !visitList.length" description="暂无回访记录" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.padopt {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.padopt-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.padopt-hero__badge {
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

.padopt-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.padopt-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.padopt-filter.nf-filter-card {
  margin-bottom: 0;
}

.padopt-filter__form {
  margin-bottom: 0;
}

.padopt-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.padopt-table.nf-table-card {
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

.padopt-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.padopt-actions__done {
  font-size: 12px;
  color: var(--nf-text-secondary);
}

.visit-form {
  margin-bottom: 8px;
}
</style>

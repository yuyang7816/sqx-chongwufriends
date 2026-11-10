<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  auditCertificationApi,
  deleteCertificationApi,
  getCertificationPageApi,
  updateCertificationApi,
} from '@/api/modules/certification'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const editDialogVisible = ref(false)
const editLoading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const rejectUserId = ref('')

const filters = reactive({
  status: '',
  name: '',
  userName: '',
  phone: '',
})

const rejectForm = reactive({
  content: '',
})

const editForm = reactive({
  id: '',
  userId: '',
  name: '',
  idNumber: '',
  details: '',
  front: '',
  back: '',
  birth: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待审核', value: 0 },
  { label: '审核成功', value: 1 },
  { label: '拒绝', value: 2 },
]

function formatStatus(value) {
  const map = {
    0: '待审核',
    1: '审核成功',
    2: '已拒绝',
  }
  return map[Number(value)] || '-'
}

function buildQueryParams() {
  const params = {
    page: pageNum.value,
    limit: pageSize.value,
    status: filters.status,
    userName: filters.userName.trim(),
    name: filters.name.trim(),
    phone: filters.phone.trim(),
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
      delete params[key]
    }
  })
  return params
}

function resetEditForm() {
  editForm.id = ''
  editForm.userId = ''
  editForm.name = ''
  editForm.idNumber = ''
  editForm.details = ''
  editForm.front = ''
  editForm.back = ''
  editForm.birth = ''
}

function normalizeRow(item = {}) {
  return {
    ...item,
    userId: item.userId || item.userEntity?.userId || '',
    userEntity: item.userEntity || null,
    createTime: item.createTime || item.createdAt || '',
    remek: item.remek || item.remark || '',
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getCertificationPageApi(buildQueryParams())
    tableData.value = (data.list || []).map(normalizeRow)
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.status = ''
  filters.name = ''
  filters.userName = ''
  filters.phone = ''
  pageNum.value = 1
  pageSize.value = 10
  loadData()
}

function goToUserDetail(row) {
  if (!row?.userId) {
    showError('当前记录未绑定用户')
    return
  }
  router.push(`/users/${row.userId}`)
}

async function handlePass(row) {
  await auditCertificationApi({
    status: 1,
    userId: row.userId,
    remek: '',
  })
  showSuccess('操作成功')
  await loadData()
}

function openRejectDialog(row) {
  rejectUserId.value = row.userId
  rejectForm.content = ''
  rejectDialogVisible.value = true
}

async function submitReject() {
  if (!rejectForm.content.trim()) {
    showError('请输入拒绝理由')
    return
  }
  rejectLoading.value = true
  try {
    await auditCertificationApi({
      status: 2,
      userId: rejectUserId.value,
      remek: rejectForm.content.trim(),
    })
    rejectDialogVisible.value = false
    showSuccess('操作成功')
    await loadData()
  } finally {
    rejectLoading.value = false
  }
}

function openEditDialog(row) {
  editForm.id = row.id || ''
  editForm.userId = row.userId || ''
  editForm.name = row.name || ''
  editForm.idNumber = row.idNumber || ''
  editForm.details = row.details || ''
  editForm.front = row.front || ''
  editForm.back = row.back || ''
  editForm.birth = row.birth || ''
  editDialogVisible.value = true
}

async function submitEdit() {
  editLoading.value = true
  try {
    await updateCertificationApi({
      userId: editForm.userId,
      name: editForm.name.trim(),
      details: editForm.details,
      idNumber: editForm.idNumber.trim(),
      front: editForm.front,
      back: editForm.back,
      birth: editForm.birth,
      id: editForm.id,
    })
    editDialogVisible.value = false
    showSuccess('操作成功')
    await loadData()
  } finally {
    editLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确定要删除此条信息?删除后无法恢复！')
  await deleteCertificationApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

watch(
  () => route.query.status,
  (value) => {
    filters.status = value === undefined ? '' : Number(value)
    pageNum.value = 1
    loadData()
  },
)

onMounted(() => {
  filters.status = route.query.status === undefined ? '' : Number(route.query.status)
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>认证管理</strong>
      </div>
    </template>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="全部" @change="handleSearch">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="filters.name" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="用户昵称">
        <el-input v-model="filters.userName" placeholder="请输入用户昵称" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="filters.phone" placeholder="请输入电话" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="编号" width="80" fixed="left" />
      <el-table-column label="用户昵称" min-width="160">
        <template #default="{ row }">
          <span v-if="row.userEntity" class="user-link" @click="goToUserDetail(row)">
            {{ row.userEntity.userName || '未绑定' }}
          </span>
          <span v-else class="muted-text">未绑定</span>
        </template>
      </el-table-column>
      <el-table-column label="姓名" min-width="160">
        <template #default="{ row }">
          <span class="user-link" @click="goToUserDetail(row)">{{ row.name || '未绑定' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="电话" min-width="150">
        <template #default="{ row }">
          <span>{{ row.userEntity?.phone || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="idNumber" label="身份证号码" min-width="200" />
      <el-table-column prop="remek" label="审核内容" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="状态" width="120" fixed="right">
        <template #default="{ row }">
          <span>{{ formatStatus(row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button
              size="small"
              type="primary"
              :disabled="row.status !== 0 || !isAuth('autonym:shenhe')"
              @click="openRejectDialog(row)"
            >
              拒绝
            </el-button>
            <el-button
              size="small"
              type="primary"
              :disabled="row.status !== 0 || !isAuth('autonym:shenhe')"
              @click="handlePass(row)"
            >
              通过
            </el-button>
            <el-button size="small" type="warning" :disabled="!isAuth('autonym:update')" @click="openEditDialog(row)">
              修改
            </el-button>
            <el-button size="small" type="danger" :disabled="!isAuth('autonym:delete')" @click="handleDelete(row)">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[5, 10, 20, 30, 50, 100]"
      :total="total"
      @update:currentPage="pageNum = $event; loadData()"
      @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
    />

    <el-dialog v-model="rejectDialogVisible" title="审核拒绝" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form label-width="88px">
        <el-form-item label="拒绝理由">
          <el-input v-model="rejectForm.content" type="textarea" :rows="4" placeholder="请输入拒绝理由" />
        </el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="rejectLoading" @click="submitReject">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改认证信息" width="720px" destroy-on-close @closed="resetEditForm" class="nf-pro-dialog" align-center>
<el-form label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="editForm.idNumber" placeholder="请输入身份证号" />
        </el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editLoading" @click="submitEdit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.action-buttons {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.action-buttons :deep(.el-button) {
  width: 100%;
  margin-left: 0;
}

.user-link {
  color: var(--el-color-primary);
  cursor: pointer;
}

.muted-text {
  color: var(--el-text-color-secondary);
}
</style>

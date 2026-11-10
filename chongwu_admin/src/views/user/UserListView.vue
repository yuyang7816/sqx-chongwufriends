<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  deleteUserApi,
  getUserPageApi,
  updateUserStatusApi,
  updateUserApi,
} from '@/api/modules/users'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { DEFAULT_USER_AVATAR, isAuth } from '@/utils/tool'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const editForm = reactive({
  userId: 0,
  userName: '',
  phone: '',
  sex: '',
  birthday: '',
  wechat: '',
  userType: 1,
  status: 1,
})

const filters = reactive({
  userName: '',
  phone: '',
  invitationCode: '',
  status: '',
  userType: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '正常', value: 1 },
  { label: '禁用', value: 2 },
]

const userTypeOptions = [
  { label: '全部', value: '' },
  { label: '普通用户', value: 1 },
  { label: 'VIP用户', value: 2 },
]

const sexOptions = [
  { label: '未设置', value: '' },
  { label: '男', value: 1 },
  { label: '女', value: 2 },
]

function sexText(value) {
  if (Number(value) === 1) return '男'
  if (Number(value) === 2) return '女'
  return '未设置'
}

function userTypeText(value) {
  return Number(value) === 2 ? 'VIP用户' : '普通用户'
}

function vipText(value) {
  return Number(value) === 1 ? '是' : '否'
}

function platformText(value) {
  return value || '-'
}

async function loadData() {
  loading.value = true
  try {
    const data = await getUserPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      userName: filters.userName.trim(),
      phone: filters.phone.trim(),
      invitationCode: filters.invitationCode.trim(),
      status: filters.status,
      userType: filters.userType,
    })
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
  filters.userName = ''
  filters.phone = ''
  filters.invitationCode = ''
  filters.status = ''
  filters.userType = ''
  pageNum.value = 1
  loadData()
}

function goToDetail(userId, tab) {
  router.push(tab ? { path: `/users/${userId}`, query: { tab } } : `/users/${userId}`)
}

function openEditDialog(row) {
  editForm.userId = row.userId
  editForm.userName = row.userName || ''
  editForm.phone = row.phone || ''
  editForm.sex = row.sex ?? ''
  editForm.birthday = row.birthday || ''
  editForm.wechat = row.wechat || ''
  editForm.userType = Number(row.userType || 1)
  editForm.status = Number(row.status || 1)
  editDialogVisible.value = true
}

async function submitEditDialog() {
  if (!editForm.userName.trim()) {
    showError('请输入用户昵称')
    return
  }
  editDialogLoading.value = true
  try {
    await updateUserApi({
      userId: editForm.userId,
      userName: editForm.userName.trim(),
      sex: editForm.sex === '' ? undefined : Number(editForm.sex),
      birthday: editForm.birthday || undefined,
      wechat: editForm.wechat.trim() || undefined,
      userType: Number(editForm.userType),
      status: Number(editForm.status),
    })
    editDialogVisible.value = false
    showSuccess('修改成功')
    await loadData()
  } finally {
    editDialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确定删除该用户吗？')
  await deleteUserApi(row.userId)
  showSuccess('删除成功')
  await loadData()
}

async function handleStatusChange(row, previousValue) {
  try {
    await updateUserStatusApi(row.userId, row.status)
    showSuccess('状态已更新')
    await loadData()
  } catch (error) {
    row.status = previousValue
  }
}

onMounted(loadData)
</script>

<template>
  <div class="ulist">
    <header class="ulist-hero nf-page-hero">
      <span class="ulist-hero__badge">用户中心</span>
      <h1 class="ulist-hero__title">用户管理</h1>
      <p class="ulist-hero__desc">查看与管理平台注册用户，支持昵称、手机号、邀请码等多条件筛选与状态操作</p>
    </header>

    <div class="ulist-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="ulist-filter__form" @submit.prevent>
        <el-form-item label="用户昵称">
          <el-input v-model="filters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="邀请码">
          <el-input v-model="filters.invitationCode" clearable placeholder="请输入邀请码" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option v-for="item in statusOptions" :key="`status-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="filters.userType" clearable placeholder="全部">
            <el-option v-for="item in userTypeOptions" :key="`type-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="ulist-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="ulist-table nf-table-card">
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="userId" label="编号" width="80" fixed />
      <el-table-column label="昵称" min-width="140">
        <template #default="{ row }">
          <span class="nick-name">{{ row.userName || '未设置' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="头像" width="82">
        <template #default="{ row }">
          <img :src="row.avatar || DEFAULT_USER_AVATAR" alt="头像" class="avatar-image" />
        </template>
      </el-table-column>
      <el-table-column label="手机号" width="140">
        <template #default="{ row }">
          <span class="click-text" @click="goToDetail(row.userId)">{{ row.phone || '未绑定' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="90">
        <template #default="{ row }">{{ sexText(row.sex) }}</template>
      </el-table-column>
      <el-table-column prop="birthday" label="生日" width="120" />
      <el-table-column label="用户类型" width="110">
        <template #default="{ row }">{{ userTypeText(row.userType) }}</template>
      </el-table-column>
      <el-table-column label="是否会员" width="100">
        <template #default="{ row }">{{ vipText(row.isVip) }}</template>
      </el-table-column>
      <el-table-column prop="invitationCode" label="邀请码" min-width="120" />
      <el-table-column prop="superiorInvitationCode" label="上级邀请码" min-width="120" />
      <el-table-column label="来源" width="100">
        <template #default="{ row }">{{ platformText(row.platform) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="状态" width="90" fixed="right">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="2"
            :disabled="!isAuth('userList:delete')"
            @change="handleStatusChange(row, row.status === 1 ? 2 : 1)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-space wrap>
            <el-button size="small" type="primary" :disabled="!isAuth('userList:info')" @click="goToDetail(row.userId)">详情</el-button>
            <el-button size="small" :disabled="!isAuth('userList:update')" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" :disabled="!isAuth('userList:delete')" @click="handleDelete(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[5, 10, 15, 20]"
      :total="total"
      @update:currentPage="pageNum = $event; loadData()"
      @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
    />
    </div>

    <el-dialog v-model="editDialogVisible" title="编辑用户" width="520px" class="nf-pro-dialog" align-center>
<el-form label-width="100px">
        <el-form-item label="用户昵称">
          <el-input v-model="editForm.userName" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" disabled />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.sex">
            <el-option v-for="item in sexOptions" :key="`sex-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="editForm.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择生日" />
        </el-form-item>
        <el-form-item label="微信号">
          <el-input v-model="editForm.wechat" placeholder="请输入微信号" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="editForm.userType">
            <el-option v-for="item in userTypeOptions.filter((item) => item.value !== '')" :key="`edit-type-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status">
            <el-option v-for="item in statusOptions.filter((item) => item.value !== '')" :key="`edit-status-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitEditDialog">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.ulist {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.ulist-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ulist-hero__badge {
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

.ulist-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.ulist-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.ulist-filter.nf-filter-card {
  margin-bottom: 0;
}

.ulist-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.ulist-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.ulist-table.nf-table-card {
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

.nick-name {
  color: #f56c6c;
}

.click-text {
  color: var(--nf-brand-deep);
  cursor: pointer;
}

.avatar-image,
.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

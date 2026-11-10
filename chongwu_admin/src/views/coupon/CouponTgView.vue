<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { adminConversionCouponApi, getCouponTgPageApi, getCouponTgUserPageApi } from '@/api/modules/coupon-tg'
import { showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const recordList = ref([])

const filters = reactive({
  userName: '',
  phone: '',
  content: '',
  classify: '',
})

const platformOptions = [
  { label: '全部', value: '' },
  { label: '美团', value: 1 },
  { label: '抖音', value: 2 },
  { label: '快手', value: 3 },
]

const exchangeDialogVisible = ref(false)
const exchangeDialogLoading = ref(false)
const exchangeForm = reactive({
  userId: '',
  userName: '',
  code: '',
})

const userDialogVisible = ref(false)
const userLoading = ref(false)
const userPage = ref(1)
const userSize = ref(10)
const userTotal = ref(0)
const userList = ref([])
const userFilters = reactive({
  phone: '',
  nickName: '',
})

function platformText(value) {
  if (Number(value) === 1) return '美团'
  if (Number(value) === 2) return '抖音'
  if (Number(value) === 3) return '快手'
  return '-'
}

function normalizeRow(item = {}) {
  return {
    ...item,
    id: item.id,
    userId: item.userId || 0,
    userName: item.userName || '',
    phone: item.phone || '',
    image: item.image || '',
    type: Number(item.type || 0),
    content: item.content || '',
    createAt: item.createAt || item.createTime || '',
  }
}

function normalizeUserRow(item = {}) {
  return {
    ...item,
    userId: item.userId,
    userName: item.userName || '未设置',
    phone: item.phone || '未绑定',
    createTime: item.createTime || item.createdAt || '',
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const data = await getCouponTgPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      type: filters.classify,
      state: 100,
      userName: filters.userName.trim(),
      phone: filters.phone.trim(),
      content: filters.content.trim(),
    })
    recordList.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadRecords()
}

function handleReset() {
  pageNum.value = 1
  filters.classify = ''
  filters.userName = ''
  filters.phone = ''
  filters.content = ''
  loadRecords()
}

function goUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

function openExchangeDialog() {
  exchangeForm.userId = ''
  exchangeForm.userName = ''
  exchangeForm.code = ''
  exchangeDialogVisible.value = true
}

async function loadUsers() {
  userLoading.value = true
  try {
    const data = await getCouponTgUserPageApi({
      page: userPage.value,
      limit: userSize.value,
      userName: userFilters.nickName.trim(),
      inviterCode: '',
      startTime: '',
      endTime: '',
      isAuthentication: '',
      phone: userFilters.phone.trim(),
    })
    userList.value = (data.list || []).map(normalizeUserRow)
    userTotal.value = data.total || 0
  } finally {
    userLoading.value = false
  }
}

async function openUserDialog() {
  userPage.value = 1
  userFilters.phone = ''
  userFilters.nickName = ''
  userDialogVisible.value = true
  await loadUsers()
}

function chooseUser(row) {
  exchangeForm.userId = row.userId
  exchangeForm.userName = row.userName || ''
  userDialogVisible.value = false
}

async function submitExchange() {
  if (!exchangeForm.userId) return showError('请选择用户')
  if (!exchangeForm.code.trim()) return showError('请输入兑换码')

  exchangeDialogLoading.value = true
  try {
    await adminConversionCouponApi({
      userId: exchangeForm.userId,
      key: exchangeForm.code.trim(),
    })
    showSuccess('操作成功')
    exchangeDialogVisible.value = false
    await loadRecords()
  } finally {
    exchangeDialogLoading.value = false
  }
}

loadRecords()
</script>

<template>
  <div class="ctg">
    <header class="ctg-hero nf-page-hero">
      <span class="ctg-hero__badge">优惠券</span>
      <h1 class="ctg-hero__title">验券列表</h1>
      <p class="ctg-hero__desc">查看用户平台兑换验券记录，支持多条件筛选与管理员代兑换操作</p>
    </header>

    <div class="ctg-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="ctg-filter__form" @submit.prevent>
        <el-form-item label="用户昵称"><el-input v-model="filters.userName" style="width: 150px" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="filters.phone" style="width: 150px" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" /></el-form-item>
        <el-form-item label="兑换码"><el-input v-model="filters.content" style="width: 150px" clearable placeholder="请输入兑换码" @keyup.enter="handleSearch" /></el-form-item>
        <el-form-item label="平台"><el-select v-model="filters.classify" style="width: 150px" clearable placeholder="请选择平台" @change="handleSearch"><el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item class="ctg-filter__actions"><el-space wrap><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button><el-button type="primary" plain :disabled="!isAuth('couponTg:yanquan')" @click="openExchangeDialog">兑换</el-button></el-space></el-form-item>
      </el-form>
    </div>

    <div class="ctg-table nf-table-card">
      <el-table v-loading="loading" :data="recordList" border>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="userName" label="用户昵称" min-width="140"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '-' }}</span></template></el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="用户头像" width="100"><template #default="{ row }"><el-image v-if="row.image" :src="row.image" class="thumb-image" :preview-src-list="[row.image]" preview-teleported /></template></el-table-column>
        <el-table-column label="平台" width="100"><template #default="{ row }">{{ platformText(row.type) }}</template></el-table-column>
        <el-table-column prop="content" label="兑换码" min-width="160" />
        <el-table-column prop="createAt" label="创建时间" width="160" />
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadRecords()" @update:pageSize="pageSize = $event; loadRecords()" />
    </div>

    <el-dialog v-model="exchangeDialogVisible" title="兑换信息确认" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">用户昵称</span><el-input v-model="exchangeForm.userName" class="dialog-field" readonly placeholder="请选择用户" @click="openUserDialog" /></div>
        <div class="form-row"><span class="form-label">兑换码</span><el-input v-model="exchangeForm.code" class="dialog-field" placeholder="请输入兑换码" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="exchangeDialogVisible = false">取消</el-button><el-button type="primary" :loading="exchangeDialogLoading" @click="submitExchange">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="userDialogVisible" title="用户列表" width="70%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item><el-input v-model="userFilters.phone" style="width: 150px" clearable placeholder="请输入手机号" @keyup.enter="userPage = 1; loadUsers()" /></el-form-item>
        <el-form-item><el-input v-model="userFilters.nickName" style="width: 150px" clearable placeholder="请输入昵称" @keyup.enter="userPage = 1; loadUsers()" /></el-form-item>
        <el-form-item><el-space><el-button type="primary" @click="userPage = 1; loadUsers()">查询</el-button><el-button @click="userFilters.phone = ''; userFilters.nickName = ''; userPage = 1; loadUsers()">重置</el-button></el-space></el-form-item>
      </el-form>
      <el-table v-loading="userLoading" :data="userList" border>
        <el-table-column prop="userId" width="80" label="编号" />
        <el-table-column prop="userName" label="昵称"><template #default="{ row }"><span class="text-danger">{{ row.userName }}</span></template></el-table-column>
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="120"><template #default="{ row }"><el-button size="small" type="primary" @click="chooseUser(row)">确定</el-button></template></el-table-column>
      </el-table>
      <BasePagination :current-page="userPage" :page-size="userSize" :page-sizes="[10, 20, 30, 50, 100]" :total="userTotal" @update:currentPage="userPage = $event; loadUsers()" @update:pageSize="userSize = $event; loadUsers()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.ctg {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.ctg-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ctg-hero__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--nf-text-primary);
  background: var(--nf-brand-lite);
  border: 1px solid var(--nf-brand);
  border-radius: var(--nf-radius-sm);
}

.ctg-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.ctg-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.ctg-filter.nf-filter-card {
  margin-bottom: 0;
}

.ctg-filter__form {
  margin-bottom: 0;
}

.ctg-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.ctg-table.nf-table-card {
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

.search-grid {
  margin-bottom: 16px;
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-label {
  width: 100px;
  text-align: right;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
}

.link-text {
  color: var(--nf-brand-deep);
  cursor: pointer;
}

.text-danger {
  color: #f56c6c;
}

.thumb-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .ctg-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }

  .form-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-label {
    width: auto;
    text-align: left;
  }
}
</style>

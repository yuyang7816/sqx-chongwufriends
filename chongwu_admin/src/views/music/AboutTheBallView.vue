<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  createAboutBallApi,
  deleteAboutBallApi,
  getAboutBallCommonOptionsApi,
  getAboutBallPageApi,
  getAboutBallPeoplePageApi,
  getAboutBallUserPageApi,
  getAboutBallVenueOptionsApi,
  updateAboutBallApi,
} from '@/api/modules/about-the-ball'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const recordList = ref([])

const filters = reactive({
  status: '',
  userName: '',
  phone: '',
  tearoomName: '',
  dateBallClassify: '',
  ballType: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待开始', value: 1 },
  { label: '进行中', value: 2 },
  { label: '已结束', value: 3 },
  { label: '已取消', value: 4 },
]

const dateBallClassifyOptions = [
  { label: '全部', value: '' },
  { label: '公开', value: 1 },
  { label: '单约', value: 2 },
]

const ballTypeOptions = ref([])
const ballNumOptions = ref([])
const payTypeOptions = ref([])
const venueOptions = ref([])

const memberDialogVisible = ref(false)
const memberLoading = ref(false)
const memberPage = ref(1)
const memberSize = ref(10)
const memberTotal = ref(0)
const memberList = ref([])
const currentDateBallId = ref('')

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const dialogTitle = computed(() => (form.dateBallId ? '修改' : '添加'))

const form = reactive({
  userName: '',
  userId: '',
  tearoomId: '',
  status: 1,
  phone: '',
  peopleNum: '',
  dateBallId: '',
  dateBallClassify: 1,
  ballType: '',
  ballTime: '',
  ballNum: '',
  payType: '',
})

const userDialogVisible = ref(false)
const userLoading = ref(false)
const userPage = ref(1)
const userSize = ref(10)
const userTotal = ref(0)
const userList = ref([])
const userFilters = reactive({
  userName: '',
  phone: '',
})

function classifyText(value) {
  if (Number(value) === 1) return '公开'
  if (Number(value) === 2) return '单约'
  return '-'
}

function statusText(value) {
  if (Number(value) === 1) return '待开始'
  if (Number(value) === 2) return '进行中'
  if (Number(value) === 3) return '已结束'
  if (Number(value) === 4) return '已取消'
  return '-'
}

function memberStatusText(value) {
  if (Number(value) === 1) return '待签到'
  if (Number(value) === 2) return '已签到'
  if (Number(value) === 3) return '已结束'
  if (Number(value) === 4) return '已取消'
  return '-'
}

function sexText(value) {
  if (Number(value) === 1) return '男'
  if (Number(value) === 2) return '女'
  return '未设置'
}

function userAvatarText(row) {
  return String(row.userName || '未').slice(0, 1)
}

function resetForm() {
  Object.assign(form, {
    userName: '',
    userId: '',
    tearoomId: '',
    status: 1,
    phone: '',
    peopleNum: '',
    dateBallId: '',
    dateBallClassify: 1,
    ballType: '',
    ballTime: '',
    ballNum: '',
    payType: '',
  })
}

function normalizeRecord(item = {}) {
  return {
    ...item,
    dateBallId: item.dateBallId,
    userId: item.userId || '',
    userName: item.userName || '',
    phone: item.phone || '',
    tearoomId: item.tearoomId || '',
    tearoomName: item.tearoomName || '',
    dateBallClassify: Number(item.dateBallClassify || 0),
    ballType: item.ballType || '',
    ballNum: item.ballNum || '',
    payType: item.payType || '',
    ballTime: item.ballTime || '',
    peopleNum: item.peopleNum || '',
    createTime: item.createTime || item.createdAt || '',
    status: Number(item.status || 0),
  }
}

function normalizeMember(item = {}) {
  return {
    ...item,
    dateBallId: item.dateBallId,
    userId: item.userId || '',
    userName: item.userName || '',
    avatar: item.avatar || '',
    createTime: item.createTime || item.createdAt || '',
    status: Number(item.status || 0),
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const data = await getAboutBallPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      status: filters.status,
      ballType: filters.ballType,
      tearoomName: filters.tearoomName.trim(),
      userName: filters.userName.trim(),
      phone: filters.phone.trim(),
      dateBallClassify: filters.dateBallClassify,
    })
    recordList.value = (data.list || []).map(normalizeRecord)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  pageSize.value = 10
  loadRecords()
}

async function handleReset() {
  filters.status = ''
  filters.userName = ''
  filters.phone = ''
  filters.tearoomName = ''
  filters.dateBallClassify = ''
  filters.ballType = ''
  pageNum.value = 1
  await loadRecords()
  await loadOptions()
}

function goUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

async function handleCancel(row) {
  await confirmAction('确定取消此订单')
  await updateAboutBallApi({ dateBallId: row.dateBallId, status: 4 })
  showSuccess('操作成功')
  await loadRecords()
}

async function handleDelete(row) {
  await confirmAction('确定删除此数据')
  await deleteAboutBallApi(row.dateBallId)
  if (recordList.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1
  }
  showSuccess('操作成功')
  await loadRecords()
}

async function openMembersDialog(row) {
  currentDateBallId.value = row.dateBallId
  memberPage.value = 1
  memberDialogVisible.value = true
  await loadMembers()
}

async function loadMembers() {
  memberLoading.value = true
  try {
    const data = await getAboutBallPeoplePageApi({
      page: memberPage.value,
      limit: memberSize.value,
      dateBallId: currentDateBallId.value,
      userName: '',
      phone: '',
      userId: '',
    })
    memberList.value = (data.list || []).map(normalizeMember)
    memberTotal.value = data.total || 0
  } finally {
    memberLoading.value = false
  }
}

async function openCreateDialog() {
  resetForm()
  await ensureVenueOptions()
  editDialogVisible.value = true
}

async function openEditDialog(row) {
  Object.assign(form, {
    userName: row.userName || '',
    userId: row.userId || '',
    tearoomId: row.tearoomId || '',
    status: Number(row.status || 1),
    phone: row.phone || '',
    peopleNum: row.peopleNum || '',
    dateBallId: row.dateBallId || '',
    dateBallClassify: Number(row.dateBallClassify || 1),
    ballType: row.ballType || '',
    ballTime: row.ballTime || '',
    ballNum: row.ballNum || '',
    payType: row.payType || '',
  })
  await ensureVenueOptions()
  editDialogVisible.value = true
}

async function ensureVenueOptions() {
  if (venueOptions.value.length) return
  venueOptions.value = await getAboutBallVenueOptionsApi()
}

async function loadOptions() {
  const [ballTypes, ballNums, payTypes] = await Promise.all([
    getAboutBallCommonOptionsApi(922),
    getAboutBallCommonOptionsApi(933),
    getAboutBallCommonOptionsApi(939),
  ])
  ballTypeOptions.value = ballTypes
  ballNumOptions.value = ballNums
  payTypeOptions.value = payTypes
}

async function submitRecord() {
  if (!form.userId) return showError('请选择用户')
  if (!form.phone.trim()) return showError('请输入联系电话')
  if (form.peopleNum === '') return showError('请选输入邀请人数')
  if (!form.ballType) return showError('请选择约球类型')
  if (!form.ballNum) return showError('请选择约球局数')
  if (!form.ballTime) return showError('请选择约球时间')
  if (!form.tearoomId) return showError('请选择羽毛球馆')

  editDialogLoading.value = true
  try {
    const payload = {
      userId: form.userId,
      tearoomId: form.tearoomId,
      status: form.status,
      phone: form.phone.trim(),
      peopleNum: form.peopleNum,
      dateBallId: form.dateBallId || undefined,
      dateBallClassify: form.dateBallClassify,
      ballType: form.ballType,
      ballTime: form.ballTime,
      ballNum: form.ballNum,
      payType: form.payType,
    }
    if (form.dateBallId) {
      await updateAboutBallApi(payload)
    } else {
      delete payload.dateBallId
      await createAboutBallApi(payload)
    }
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadRecords()
  } finally {
    editDialogLoading.value = false
  }
}

async function openUserDialog() {
  userFilters.userName = ''
  userFilters.phone = ''
  userPage.value = 1
  userDialogVisible.value = true
  await loadUsers()
}

async function loadUsers() {
  userLoading.value = true
  try {
    const data = await getAboutBallUserPageApi({
      page: userPage.value,
      limit: userSize.value,
      isVip: '',
      userName: userFilters.userName.trim(),
      inviterCode: '',
      startTime: '',
      endTime: '',
      isAuthentication: '',
      phone: userFilters.phone.trim(),
      status: '',
      isRate: '',
      moneyUser: '',
      vipIndex: '',
    })
    userList.value = data.list || []
    userTotal.value = data.total || 0
  } finally {
    userLoading.value = false
  }
}

function chooseUser(row) {
  form.userName = row.userName || ''
  form.userId = row.userId || ''
  userDialogVisible.value = false
}

loadRecords()
loadOptions()
</script>

<template>
  <div class="atb">
    <header class="atb-hero nf-page-hero">
      <span class="atb-hero__badge">约球管理</span>
      <h1 class="atb-hero__title">约球记录</h1>
      <p class="atb-hero__desc">查看与管理用户约球订单，支持筛选查询、添加记录与成员查看</p>
    </header>

    <div class="atb-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="atb-filter__form" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="filters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="羽毛球馆名称">
          <el-input v-model="filters.tearoomName" clearable placeholder="请输入羽毛球馆名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="对局类型">
          <el-select v-model="filters.dateBallClassify" placeholder="请选择对局类型" clearable @change="handleSearch">
            <el-option v-for="item in dateBallClassifyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="约球类型">
          <el-select v-model="filters.ballType" placeholder="请选择约球类型" clearable @change="handleSearch">
            <el-option v-for="item in ballTypeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item class="atb-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">刷新</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :disabled="!isAuth('aboutTheBall:add')" @click="openCreateDialog">添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="atb-table nf-table-card">
      <el-table v-loading="loading" :data="recordList" border>
        <el-table-column prop="dateBallId" label="编号" width="80" fixed="left" />
        <el-table-column prop="userName" label="发起用户" min-width="140" fixed="left">
          <template #default="{ row }">
            <el-link v-if="row.userName" type="primary" @click="goUserDetail(row.userId)">{{ row.userName }}</el-link>
            <span v-else>未绑定</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" min-width="130" />
        <el-table-column prop="tearoomName" label="羽毛球馆名称" min-width="160" />
        <el-table-column label="对局类型" width="100">
          <template #default="{ row }">
            <span :class="row.dateBallClassify === 1 ? 'status-danger' : 'status-primary'">{{ classifyText(row.dateBallClassify) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ballType" label="约球类型" min-width="120" />
        <el-table-column prop="ballNum" label="约球局数" width="100" />
        <el-table-column prop="payType" label="付费方式" min-width="120" />
        <el-table-column prop="ballTime" label="约球时间" min-width="170" />
        <el-table-column prop="peopleNum" label="邀请人数" width="100" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span
              :class="[
                row.status === 1 || row.status === 3 ? 'status-danger' : '',
                row.status === 2 ? 'status-primary' : '',
                row.status === 4 ? 'status-muted' : '',
              ]"
            >
              {{ statusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="warning" @click="openMembersDialog(row)">约球成员</el-button>
              <el-button size="small" type="primary" :disabled="!isAuth('aboutTheBall:update')" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('aboutTheBall:delete')" @click="handleDelete(row)">删除</el-button>
              <el-button v-if="row.status === 1" size="small" type="primary" plain :disabled="!isAuth('aboutTheBall:quxiao')" @click="handleCancel(row)">取消</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadRecords()" @update:pageSize="pageSize = $event; pageNum = 1; loadRecords()" />
    </div>

    <el-dialog v-model="memberDialogVisible" title="约球成员" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="toolbar-row">
        <el-button type="primary" @click="memberPage = 1; loadMembers()">刷新</el-button>
      </div>
      <el-table v-loading="memberLoading" :data="memberList" border>
        <el-table-column prop="dateBallId" label="编号" width="80" />
        <el-table-column prop="userName" label="昵称" min-width="140">
          <template #default="{ row }">
            <el-link type="danger" @click="goUserDetail(row.userId)">{{ row.userName || '未绑定' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="图像" width="90">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :src="row.avatar" :size="40" />
            <el-avatar v-else :size="40">{{ userAvatarText(row) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span
              :class="[
                row.status === 1 || row.status === 3 ? 'status-danger' : '',
                row.status === 2 ? 'status-primary' : '',
                row.status === 4 ? 'status-muted' : '',
              ]"
            >
              {{ memberStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination :current-page="memberPage" :page-size="memberSize" :page-sizes="[5, 10, 15, 20]" :total="memberTotal" @update:currentPage="memberPage = $event; loadMembers()" @update:pageSize="memberSize = $event; memberPage = 1; loadMembers()" />
    </el-dialog>

    <el-dialog v-model="editDialogVisible" :title="dialogTitle" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">选择用户</span><el-input v-model="form.userName" class="dialog-field" placeholder="请选择选择用户" readonly @click="openUserDialog" /></div>
        <div class="form-row"><span class="form-label">对局类型</span><el-radio-group v-model="form.dateBallClassify"><el-radio :value="1">公开</el-radio><el-radio :value="2">单约</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">约球类型</span><el-radio-group v-model="form.ballType"><el-radio v-for="item in ballTypeOptions" :key="item" :value="item">{{ item }}</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">约球局数</span><el-radio-group v-model="form.ballNum"><el-radio v-for="item in ballNumOptions" :key="item" :value="item">{{ item }}</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">付费方式</span><el-radio-group v-model="form.payType"><el-radio v-for="item in payTypeOptions" :key="item" :value="item">{{ item }}</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">约球时间</span><el-date-picker v-model="form.ballTime" class="dialog-field" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择自动发布时间" /></div>
        <div class="form-row"><span class="form-label">羽毛球馆</span><el-select v-model="form.tearoomId" class="dialog-field" placeholder="请选择羽毛球馆"><el-option v-for="item in venueOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" /></el-select></div>
        <div class="form-row"><span class="form-label">联系电话</span><el-input v-model="form.phone" class="dialog-field" placeholder="请输入联系电话" /></div>
        <div class="form-row"><span class="form-label">邀请人数</span><el-input v-model="form.peopleNum" class="dialog-field" placeholder="请输入邀请人数" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitRecord">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="userDialogVisible" title="用户列表" width="75%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="用户昵称">
          <el-input v-model="userFilters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="userPage = 1; loadUsers()" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="userPage = 1; loadUsers()" />
        </el-form-item>
        <el-form-item>
          <el-space>
            <el-button type="primary" @click="userPage = 1; loadUsers()">查询</el-button>
            <el-button @click="userFilters.userName = ''; userFilters.phone = ''; userPage = 1; loadUsers()">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
      <el-table v-loading="userLoading" :data="userList" border height="600px">
        <el-table-column prop="userId" label="编号" width="80" fixed="left" />
        <el-table-column prop="userName" label="昵称" width="150" fixed="left">
          <template #default="{ row }">
            <el-link type="danger" @click="goUserDetail(row.userId)">{{ row.userName || '未绑定' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="图像" width="90">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :src="row.avatar" :size="40" />
            <el-avatar v-else :size="40">{{ userAvatarText(row) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="sex" label="性别" width="90">
          <template #default="{ row }">{{ sexText(row.sex) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140">
          <template #default="{ row }">{{ row.phone || '未绑定' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }"><el-button size="small" type="primary" @click="chooseUser(row)">选择</el-button></template>
        </el-table-column>
      </el-table>
      <BasePagination :current-page="userPage" :page-size="userSize" :page-sizes="[5, 10, 15, 20]" :total="userTotal" @update:currentPage="userPage = $event; loadUsers()" @update:pageSize="userSize = $event; userPage = 1; loadUsers()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.atb {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.atb-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.atb-hero__badge {
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

.atb-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.atb-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.atb-filter.nf-filter-card {
  margin-bottom: 0;
}

.atb-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.atb-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.atb-table.nf-table-card {
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

.toolbar-row {
  margin-bottom: 12px;
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.form-label {
  width: 100px;
  line-height: 32px;
  text-align: right;
  color: var(--nf-text-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
}

.status-danger {
  color: var(--el-color-danger);
}

.status-primary {
  color: var(--nf-brand-deep);
}

.status-muted {
  color: var(--nf-text-secondary);
}

@media (max-width: 768px) {
  .atb-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }

  .form-row {
    flex-direction: column;
  }

  .form-label {
    width: auto;
    text-align: left;
  }
}
</style>

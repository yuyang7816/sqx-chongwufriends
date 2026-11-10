<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getMessageCenterListApi, auditMessageApi, sendMessageApi } from '@/api/modules/message-center'
import { getUserCenterPage } from '@/api/modules/user-center'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()
const route = useRoute()

const activeTab = ref('complaint')
const loading = ref(false)
const pushDialogVisible = ref(false)
const pushDialogLoading = ref(false)
const complaintDialogVisible = ref(false)
const complaintDialogLoading = ref(false)
const userPickerVisible = ref(false)
const userPickerLoading = ref(false)

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const complaintTypeOptions = [
  { label: '全部', value: '' },
  { label: '用户投诉', value: 1 },
  { label: '动态投诉', value: 3 },
  { label: '活动投诉', value: 4 },
  { label: '评论投诉', value: 5 },
  { label: '店铺投诉', value: 6 },
]

const complaintStatusOptions = [
  { label: '全部', value: '' },
  { label: '待处理', value: 0 },
  { label: '不处理', value: 1 },
  { label: '已封号', value: 2 },
  { label: '已删除', value: 3 },
  { label: '已处理', value: 4 },
]

const complaintFilters = reactive({
  platform: '',
  status: '',
})

const pushForm = reactive({
  type: '',
  title: '',
  content: '',
})

const selectedUsers = ref([])

const complaintForm = reactive({
  radio: '',
  disposeType: '',
  content: '',
  messageId: '',
  platform: '',
})

const pickerFilters = reactive({
  inviterCode: '',
  phone: '',
  userName: '',
  dateRange: [],
})

const pickerPage = ref(1)
const pickerSize = ref(10)
const pickerTotal = ref(0)
const pickerData = ref([])
const pickerSelection = ref([])

const currentState = computed(() => {
  if (activeTab.value === 'feedback') return 2
  if (activeTab.value === 'user-message') return 5
  return 3
})

const complaintResultText = computed(() => (value) => {
  const map = { 0: '待处理', 1: '不处理', 2: '已封号', 3: '已删除', 4: '已处理' }
  return map[Number(value)] || '-'
})

function complaintTypeText(value) {
  const map = { 1: '用户投诉', 3: '动态投诉', 4: '活动投诉', 5: '评论投诉', 6: '店铺投诉' }
  return map[Number(value)] || '-'
}

function complaintAuditStatusText(value) {
  return Number(value) === 0 ? '待审核' : '已审核'
}

function normalizeImages(value) {
  if (!value) return []
  if (Array.isArray(value)) return value.filter(Boolean)
  return String(value).split(',').map((item) => item.trim()).filter(Boolean)
}

function normalizeMessageRow(item = {}) {
  const row = { ...item }
  row.imageList = normalizeImages(item.image)
  if (currentState.value === 2 && item.content) {
    const raw = String(item.content)
    if (raw !== '我要注销账号！') {
      try {
        row.content = JSON.parse(raw)?.content || raw
      } catch {
        row.content = raw
      }
    }
  }
  return row
}

function buildQueryParams() {
  const params = {
    page: pageNum.value,
    limit: pageSize.value,
    state: currentState.value,
  }
  if (activeTab.value === 'complaint') {
    params.platform = complaintFilters.platform
    params.status = complaintFilters.status
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
      delete params[key]
    }
  })
  return params
}

async function loadData() {
  loading.value = true
  try {
    const data = await getMessageCenterListApi(buildQueryParams())
    const list = data?.list || data?.records || []
    tableData.value = list.map(normalizeMessageRow)
    total.value = Number(data?.totalCount || data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  complaintFilters.platform = ''
  complaintFilters.status = ''
  pageNum.value = 1
  loadData()
}

function handleTabChange() {
  pageNum.value = 1
  pageSize.value = activeTab.value === 'complaint' ? 10 : 5
  if (activeTab.value !== 'complaint') {
    complaintFilters.platform = ''
    complaintFilters.status = ''
  }
  loadData()
}

function goToUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

function openPushDialog() {
  pushForm.type = ''
  pushForm.title = ''
  pushForm.content = ''
  selectedUsers.value = []
  pickerSelection.value = []
  pushDialogVisible.value = true
}

async function submitPushDialog() {
  if (!pushForm.type) {
    showError('请选择类型')
    return
  }
  if (!pushForm.title.trim()) {
    showError('请输入消息标题')
    return
  }
  if (!pushForm.content.trim()) {
    showError('请输入消息内容')
    return
  }
  if (Number(pushForm.type) === 1 && !selectedUsers.value.length) {
    showError('请选择用户')
    return
  }
  pushDialogLoading.value = true
  try {
    await sendMessageApi({
      title: pushForm.title.trim(),
      content: pushForm.content.trim(),
      flag: Number(pushForm.type),
      state: currentState.value === 3 ? 5 : currentState.value,
      userIds: selectedUsers.value.map((item) => item.userId).join(','),
      type: Number(pushForm.type),
      phone: '',
    })
    pushDialogVisible.value = false
    showSuccess('消息推送成功')
    await loadData()
  } finally {
    pushDialogLoading.value = false
  }
}

function openComplaintDialog(row) {
  complaintForm.radio = ''
  complaintForm.disposeType = ''
  complaintForm.content = ''
  complaintForm.messageId = row.id
  complaintForm.platform = row.platform
  complaintDialogVisible.value = true
}

async function submitComplaintDialog() {
  if (complaintForm.radio === '') {
    showError('请选择审核结果')
    return
  }
  if (Number(complaintForm.radio) === 1 && !complaintForm.content.trim()) {
    showError('请输入驳回理由')
    return
  }
  if (Number(complaintForm.radio) === 0 && complaintForm.disposeType === '') {
    showError('请选择处理结果')
    return
  }
  if (Number(complaintForm.radio) === 0 && Number(complaintForm.disposeType) === 2 && !complaintForm.content.trim()) {
    showError('请输入处理说明')
    return
  }
  let status = 1
  if (Number(complaintForm.radio) === 0) {
    if (Number(complaintForm.disposeType) === 0) status = 2
    if (Number(complaintForm.disposeType) === 1) status = 3
    if (Number(complaintForm.disposeType) === 2) status = 4
  }
  complaintDialogLoading.value = true
  try {
    await auditMessageApi({
      messageId: complaintForm.messageId,
      status,
      auditContent: complaintForm.content.trim(),
    })
    complaintDialogVisible.value = false
    showSuccess('投诉处理成功')
    await loadData()
  } finally {
    complaintDialogLoading.value = false
  }
}

function buildPickerQueryParams() {
  const params = {
    page: pickerPage.value,
    limit: pickerSize.value,
    inviterCode: pickerFilters.inviterCode.trim(),
    phone: pickerFilters.phone.trim(),
    userName: pickerFilters.userName.trim(),
    isAuthentication: '',
    isRate: '',
    isVip: '',
    moneyUser: '',
    vipIndex: '',
    vipNo: '',
    isBirthday: '',
    birthdayMonth: '',
    integralNum: '',
  }
  if (pickerFilters.dateRange?.length === 2) {
    params.startTime = pickerFilters.dateRange[0]
    params.endTime = pickerFilters.dateRange[1]
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
      delete params[key]
    }
  })
  return params
}

async function loadPickerData() {
  userPickerLoading.value = true
  try {
    const data = await getUserCenterPage(buildPickerQueryParams())
    pickerData.value = data.list || []
    pickerTotal.value = Number(data.total || 0)
  } finally {
    userPickerLoading.value = false
  }
}

function openUserPicker() {
  pickerFilters.inviterCode = ''
  pickerFilters.phone = ''
  pickerFilters.userName = ''
  pickerFilters.dateRange = []
  pickerPage.value = 1
  pickerSelection.value = []
  userPickerVisible.value = true
  loadPickerData()
}

function handlePickerSearch() {
  pickerPage.value = 1
  loadPickerData()
}

function handlePickerReset() {
  pickerFilters.inviterCode = ''
  pickerFilters.phone = ''
  pickerFilters.userName = ''
  pickerFilters.dateRange = []
  pickerPage.value = 1
  loadPickerData()
}

function handlePickerSelectionChange(rows) {
  pickerSelection.value = rows
}

function confirmUserPicker() {
  if (!pickerSelection.value.length) {
    showError('请选择用户')
    return
  }
  const selectedMap = new Map(selectedUsers.value.map((item) => [item.userId, item]))
  pickerSelection.value.forEach((item) => {
    selectedMap.set(item.userId, item)
  })
  selectedUsers.value = Array.from(selectedMap.values())
  userPickerVisible.value = false
}

function removeSelectedUser(userId) {
  if (selectedUsers.value.length <= 1) {
    showError('最少需要选中一个用户')
    return
  }
  selectedUsers.value = selectedUsers.value.filter((item) => item.userId !== userId)
}

watch(
  () => route.query.status,
  (value) => {
    complaintFilters.status = value === undefined ? '' : Number(value)
    if (activeTab.value === 'complaint') {
      pageNum.value = 1
      loadData()
    }
  },
)

onMounted(() => {
  complaintFilters.status = route.query.status === undefined ? '' : Number(route.query.status)
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>消息中心</strong>
      </div>
    </template>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="投诉消息" name="complaint">
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="投诉类型">
            <el-select v-model="complaintFilters.platform" clearable placeholder="全部" @change="handleSearch">
              <el-option v-for="item in complaintTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="complaintFilters.status" clearable placeholder="全部" @change="handleSearch">
              <el-option v-for="item in complaintStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-space>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-space>
          </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="tableData" border>
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column label="投诉用户" min-width="140">
            <template #default="{ row }">
              <span class="user-link" @click="goToUserDetail(row.userId)">{{ row.userName || '未绑定' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="被投诉用户" min-width="140">
            <template #default="{ row }">
              <span class="user-link" @click="goToUserDetail(row.byUserId)">{{ row.byUserName || '未绑定' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="投诉类型" width="110">
            <template #default="{ row }">{{ complaintTypeText(row.platform) }}</template>
          </el-table-column>
          <el-table-column prop="content" label="投诉内容" min-width="220" show-overflow-tooltip />
          <el-table-column label="图片" width="180">
            <template #default="{ row }">
              <div v-if="row.imageList.length" class="image-list">
                <el-popover v-for="item in row.imageList" :key="item" placement="top-start" trigger="hover" width="320">
                  <img :src="item" class="complaint-image-preview" alt="投诉图片" />
                  <template #reference>
                    <img :src="item" class="complaint-image-thumb" alt="投诉图片" />
                  </template>
                </el-popover>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ complaintAuditStatusText(row.status) }}</template>
          </el-table-column>
          <el-table-column label="处理结果" width="100">
            <template #default="{ row }">{{ complaintResultText(row.status) }}</template>
          </el-table-column>
          <el-table-column prop="auditContent" label="审核内容" min-width="180" show-overflow-tooltip />
          <el-table-column prop="createAt" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button v-if="Number(row.status) === 0" size="small" type="primary" @click="openComplaintDialog(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="用户反馈" name="feedback">
        <el-table v-loading="loading" :data="tableData" border>
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="title" label="联系方式" min-width="180" />
          <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
          <el-table-column prop="createAt" label="创建时间" width="180" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="用户消息" name="user-message">
        <div class="toolbar-row-end">
          <el-button :disabled="!isAuth('message:push')" type="primary" @click="openPushDialog">消息推送</el-button>
        </div>
        <el-table v-loading="loading" :data="tableData" border>
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column label="用户名称" min-width="160">
            <template #default="{ row }">
              <span class="user-link" @click="goToUserDetail(row.userId)">{{ row.userName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="消息标题" min-width="180" />
          <el-table-column prop="content" label="消息内容" min-width="260" show-overflow-tooltip />
          <el-table-column prop="createAt" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" :disabled="!isAuth('userList:viewDetails') && !isAuth('userList:list')" @click="goToUserDetail(row.userId)">用户详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[5, 10, 20, 50, 100]"
      :total="total"
      @update:currentPage="pageNum = $event; loadData()"
      @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
    />

    <AppDialog
      :model-value="pushDialogVisible"
      title="消息推送"
      :loading="pushDialogLoading"
      width="760px"
      @update:modelValue="pushDialogVisible = $event"
      @confirm="submitPushDialog"
    >
      <el-form label-width="96px">
        <el-form-item label="类型">
          <el-radio-group v-model="pushForm.type">
            <el-radio :value="1">指定用户</el-radio>
            <el-radio :value="2">所有用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="Number(pushForm.type) === 1" label="选择用户">
          <div class="selected-users-wrap">
            <el-tag v-for="item in selectedUsers" :key="item.userId" closable @close="removeSelectedUser(item.userId)">
              {{ item.userName || item.phone || item.userId }}
            </el-tag>
            <el-button size="small" @click="openUserPicker">添加</el-button>
          </div>
        </el-form-item>
        <el-form-item label="消息标题">
          <el-input v-model="pushForm.title" placeholder="请输入消息标题" @keyup.enter="submitPushDialog" />
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input v-model="pushForm.content" type="textarea" :rows="4" placeholder="请输入消息内容" />
        </el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog
      :model-value="complaintDialogVisible"
      title="投诉处理"
      :loading="complaintDialogLoading"
      width="620px"
      @update:modelValue="complaintDialogVisible = $event"
      @confirm="submitComplaintDialog"
    >
      <el-form label-width="96px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="complaintForm.radio">
            <el-radio :value="0">通过</el-radio>
            <el-radio :value="1">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="Number(complaintForm.radio) === 1" label="驳回理由">
          <el-input v-model="complaintForm.content" type="textarea" :rows="4" placeholder="请输入驳回理由" />
        </el-form-item>
        <el-form-item v-if="Number(complaintForm.radio) === 0" label="处理结果">
          <el-radio-group v-model="complaintForm.disposeType">
            <el-radio :value="0">封号</el-radio>
            <el-radio v-if="[3, 4].includes(Number(complaintForm.platform))" :value="1">删除</el-radio>
            <el-radio :value="2">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="Number(complaintForm.radio) === 0 && Number(complaintForm.disposeType) === 2" label="处理说明">
          <el-input v-model="complaintForm.content" type="textarea" :rows="4" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
    </AppDialog>

    <el-dialog v-model="userPickerVisible" title="用户列表" width="78%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="邀请人邀请码">
          <el-input v-model="pickerFilters.inviterCode" clearable placeholder="请输入邀请人邀请码" @keyup.enter="handlePickerSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="pickerFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="handlePickerSearch" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="pickerFilters.userName" clearable placeholder="请输入昵称" @keyup.enter="handlePickerSearch" />
        </el-form-item>
        <el-form-item label="注册时间">
          <el-date-picker v-model="pickerFilters.dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始时间" end-placeholder="截止时间" />
        </el-form-item>
        <el-form-item>
          <el-space>
            <el-button type="primary" @click="handlePickerSearch">查询</el-button>
            <el-button @click="handlePickerReset">重置</el-button>
            <el-button type="primary" :disabled="pickerSelection.length <= 0" @click="confirmUserPicker">确定</el-button>
          </el-space>
        </el-form-item>
      </el-form>

      <el-table v-loading="userPickerLoading" :data="pickerData" border @selection-change="handlePickerSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column label="昵称" min-width="140">
          <template #default="{ row }">
            <span class="user-link" @click="goToUserDetail(row.userId)">{{ row.userName || '未设置' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="头像" width="90" align="center">
          <template #default="{ row }">
            <el-image :src="row.avatar || 'https://www.gomyorder.cn/logo.png'" fit="cover" class="picker-avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="inviterCode" label="邀请人邀请码" min-width="140" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
      </el-table>

      <BasePagination
        :current-page="pickerPage"
        :page-size="pickerSize"
        :page-sizes="[10, 20, 30, 50, 100]"
        :total="pickerTotal"
        @update:currentPage="pickerPage = $event; loadPickerData()"
        @update:pageSize="pickerSize = $event; pickerPage = 1; loadPickerData()"
      />
    </el-dialog>
  </el-card>
</template>

<style scoped>
.user-link {
  color: var(--el-color-primary);
  cursor: pointer;
}

.toolbar-row-end {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.complaint-image-thumb {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--el-border-color-light);
}

.complaint-image-preview {
  width: 100%;
  height: auto;
}

.selected-users-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
}

.picker-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
}
</style>

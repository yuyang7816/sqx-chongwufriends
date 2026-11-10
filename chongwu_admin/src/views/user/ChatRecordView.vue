<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  createChatRecordApi,
  deleteChatRecordApi,
  getChatRecordPageApi,
  updateChatRecordApi,
} from '@/api/modules/chat-record'
import { getUserCenterPage } from '@/api/modules/user-center'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const userPickerVisible = ref(false)
const userPickerLoading = ref(false)

const pickerPage = ref(1)
const pickerSize = ref(10)
const pickerTotal = ref(0)
const pickerData = ref([])

const filters = reactive({
  nickName: '',
  prompt: '',
  answer: '',
})

const form = reactive({
  chatId: '',
  userId: '',
  prompt: '',
  answer: '',
  messageType: 1,
})

const pickerFilters = reactive({
  phone: '',
  userName: '',
  invitationCode: '',
})

const selectedUserName = ref('')

const dialogTitle = computed(() => (form.chatId ? '修改聊天记录' : '添加聊天记录'))

function normalizeChatRow(item = {}) {
  const userEntity = item.userEntity || {}
  return {
    ...item,
    userId: item.userId || userEntity.userId || '',
    userName: userEntity.userName || item.userName || item.nickName || '',
    avatar: userEntity.avatar || item.avatar || '',
    prompt: item.prompt || '',
    answer: item.answer || '',
    createTime: item.createTime || '',
  }
}

function buildQueryParams() {
  const params = {
    page: pageNum.value,
    limit: pageSize.value,
    nickName: filters.nickName.trim(),
    prompt: filters.prompt.trim(),
    answer: filters.answer.trim(),
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
      delete params[key]
    }
  })
  return params
}

function buildUserPickerParams() {
  const params = {
    page: pickerPage.value,
    limit: pickerSize.value,
    phone: pickerFilters.phone.trim(),
    userName: pickerFilters.userName.trim(),
    invitationCode: pickerFilters.invitationCode.trim(),
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
    const data = await getChatRecordPageApi(buildQueryParams())
    tableData.value = (data.list || []).map(normalizeChatRow)
    total.value = Number(data.total || 0)
    if (pageNum.value > 1 && !tableData.value.length) {
      pageNum.value -= 1
      await loadData()
    }
  } finally {
    loading.value = false
  }
}

async function loadUserPicker() {
  userPickerLoading.value = true
  try {
    const data = await getUserCenterPage(buildUserPickerParams())
    pickerData.value = data.list || []
    pickerTotal.value = Number(data.total || 0)
  } finally {
    userPickerLoading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.nickName = ''
  filters.prompt = ''
  filters.answer = ''
  pageNum.value = 1
  loadData()
}

function goToUserDetail(row) {
  if (!row?.userId) {
    showError('当前记录未绑定用户')
    return
  }
  router.push(`/users/${row.userId}`)
}

async function copyText(text) {
  const value = String(text || '')
  if (!value) {
    showError('没有可复制的内容')
    return
  }
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(value)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = value
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    showSuccess('复制成功')
  } catch {
    showError('复制失败，请手动复制')
  }
}

function resetForm() {
  form.chatId = ''
  form.userId = ''
  form.prompt = ''
  form.answer = ''
  form.messageType = 1
  selectedUserName.value = ''
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  form.chatId = row.chatId || ''
  form.userId = row.userId || ''
  form.prompt = row.prompt || ''
  form.answer = row.answer || ''
  form.messageType = 1
  selectedUserName.value = row.userName || ''
  dialogVisible.value = true
}

async function submitDialog() {
  if (!form.userId) {
    showError('请选择用户')
    return
  }
  if (!form.prompt.trim()) {
    showError('请输入问题')
    return
  }
  if (!form.answer.trim()) {
    showError('请输入答案')
    return
  }
  dialogLoading.value = true
  try {
    const payload = {
      chatId: form.chatId || undefined,
      userId: form.userId,
      prompt: form.prompt.trim(),
      answer: form.answer.trim(),
      messageType: 1,
    }
    if (form.chatId) {
      await updateChatRecordApi(payload)
    } else {
      delete payload.chatId
      await createChatRecordApi(payload)
    }
    dialogVisible.value = false
    showSuccess('操作成功')
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确定删除此条聊天记录吗？')
  await deleteChatRecordApi(row.chatId)
  showSuccess('删除成功')
  await loadData()
}

function openUserPicker() {
  userPickerVisible.value = true
  pickerPage.value = 1
  loadUserPicker()
}

function handleUserPickerSearch() {
  pickerPage.value = 1
  loadUserPicker()
}

function handleUserPickerReset() {
  pickerFilters.phone = ''
  pickerFilters.userName = ''
  pickerFilters.invitationCode = ''
  pickerPage.value = 1
  loadUserPicker()
}

function chooseUser(row) {
  form.userId = row.userId
  selectedUserName.value = row.userName || row.nickName || ''
  userPickerVisible.value = false
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>聊天记录</strong>
      </div>
    </template>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="用户昵称">
        <el-input v-model="filters.nickName" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="问题">
        <el-input v-model="filters.prompt" clearable placeholder="请输入问题" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="答案">
        <el-input v-model="filters.answer" clearable placeholder="请输入答案" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" :disabled="!isAuth('chatRecord:add')" @click="openCreateDialog">添加</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="chatId" label="编号" width="90" />
      <el-table-column label="用户昵称" min-width="160">
        <template #default="{ row }">
          <span class="user-link" @click="goToUserDetail(row)">{{ row.userName || '未绑定' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户头像" width="100">
        <template #default="{ row }">
          <el-image
            v-if="row.avatar"
            :src="row.avatar"
            :preview-src-list="[row.avatar]"
            fit="cover"
            class="avatar-image"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="问题" min-width="260">
        <template #default="{ row }">
          <div class="content-cell">
            <el-tooltip :content="row.prompt" placement="top-start" :show-after="300">
              <div class="content-text">{{ row.prompt || '-' }}</div>
            </el-tooltip>
            <el-button link type="primary" @click="copyText(row.prompt)">复制</el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="答案" min-width="320">
        <template #default="{ row }">
          <div class="content-cell">
            <el-tooltip :content="row.answer" placement="top-start" :show-after="300">
              <div class="content-text content-text--answer">{{ row.answer || '-' }}</div>
            </el-tooltip>
            <el-button link type="primary" @click="copyText(row.answer)">复制</el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-space>
            <el-button link type="primary" :disabled="!isAuth('chatRecord:update')" @click="openEditDialog(row)">修改</el-button>
            <el-button link type="danger" :disabled="!isAuth('chatRecord:delete')" @click="handleDelete(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 30, 40]"
      :total="total"
      @update:currentPage="pageNum = $event; loadData()"
      @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
    />

    <AppDialog v-model="dialogVisible" :title="dialogTitle" width="760px" :loading="dialogLoading" @confirm="submitDialog">
      <el-form label-width="100px" @submit.prevent>
        <el-form-item label="用户昵称">
          <el-input v-model="selectedUserName" readonly placeholder="请选择用户" @click="openUserPicker">
            <template #append>
              <el-button @click="openUserPicker">选择</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="问题">
          <el-input v-model="form.prompt" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="请输入问题" />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" type="textarea" :rows="5" maxlength="4000" show-word-limit placeholder="请输入答案" />
        </el-form-item>
      </el-form>
    </AppDialog>

    <el-dialog v-model="userPickerVisible" title="用户列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid search-grid--compact" @submit.prevent>
        <el-form-item label="手机号">
          <el-input v-model="pickerFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleUserPickerSearch" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="pickerFilters.userName" clearable placeholder="请输入昵称" @keyup.enter="handleUserPickerSearch" />
        </el-form-item>
        <el-form-item label="邀请码">
          <el-input v-model="pickerFilters.invitationCode" clearable placeholder="请输入邀请码" @keyup.enter="handleUserPickerSearch" />
        </el-form-item>
        <el-form-item>
          <el-space>
            <el-button type="primary" @click="handleUserPickerSearch">查询</el-button>
            <el-button @click="handleUserPickerReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>

      <el-table v-loading="userPickerLoading" :data="pickerData" border>
        <el-table-column prop="userId" label="编号" width="90" />
        <el-table-column prop="userName" label="昵称" min-width="180" />
        <el-table-column prop="phone" label="手机号" min-width="160" />
        <el-table-column prop="invitationCode" label="邀请码" min-width="140" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="chooseUser(row)">确定</el-button>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination
        :current-page="pickerPage"
        :page-size="pickerSize"
        :page-sizes="[5, 10, 15, 20]"
        :total="pickerTotal"
        @update:currentPage="pickerPage = $event; loadUserPicker()"
        @update:pageSize="pickerSize = $event; pickerPage = 1; loadUserPicker()"
      />
    </el-dialog>
  </el-card>
</template>

<style scoped>
.user-link {
  color: var(--el-color-primary);
  cursor: pointer;
}

.avatar-image {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  display: block;
}

.content-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.content-text {
  flex: 1;
  min-width: 0;
  line-height: 1.6;
  word-break: break-all;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.content-text--answer {
  -webkit-line-clamp: 4;
}

.search-grid--compact {
  margin-bottom: 16px;
}
</style>

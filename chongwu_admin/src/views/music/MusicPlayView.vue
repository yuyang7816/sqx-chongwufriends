<script setup>
import { computed, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { getUserCenterPage } from '@/api/modules/user-center'
import { getMusicTypePageApi } from '@/api/modules/merchant-center'
import { getMusicLibraryPageApi } from '@/api/modules/music-library'
import {
  createMusicPlayApi,
  deleteMusicPlayApi,
  getMusicPlayPageApi,
  updateMusicPlayApi,
} from '@/api/modules/music-play'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const playList = ref([])

const categoryOptions = ref([])

const filters = reactive({
  status: '',
  userName: '',
  tearoomName: '',
  phone: '',
  musicName: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '已播放', value: 1 },
  { label: '未播放', value: 2 },
]

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const dialogTitle = computed(() => (form.musicPlayId ? '修改歌曲' : '添加歌曲'))

const form = reactive({
  musicPlayId: '',
  musicLibraryId: '',
  musicTypeId: '',
  musicName: '',
  musicUrl: '',
  musicPicUrl: '',
  musicAuthor: '',
  tearoomId: '',
  userId: '',
  userName: '',
  status: 2,
})

const userDialogVisible = ref(false)
const userLoading = ref(false)
const userPage = ref(1)
const userSize = ref(10)
const userTotal = ref(0)
const userList = ref([])
const userFilters = reactive({
  phone: '',
  userName: '',
})

const libraryDialogVisible = ref(false)
const libraryLoading = ref(false)
const libraryPage = ref(1)
const librarySize = ref(10)
const libraryTotal = ref(0)
const libraryList = ref([])
const libraryFilters = reactive({
  musicTypeId: '',
  musicName: '',
  musicAuthor: '',
})

function statusText(status) {
  if (Number(status) === 1) return '是'
  if (Number(status) === 2) return '否'
  return '-'
}

function splitImages(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

async function loadCategories() {
  const data = await getMusicTypePageApi({ page: 1, limit: 50 })
  categoryOptions.value = data?.records || data?.list || []
}

async function loadPlayList() {
  loading.value = true
  try {
    const data = await getMusicPlayPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      status: filters.status,
      musicName: filters.musicName.trim(),
      userName: filters.userName.trim(),
      tearoomName: filters.tearoomName.trim(),
      phone: filters.phone.trim(),
    })
    playList.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadPlayList()
}

function handleReset() {
  filters.status = ''
  filters.userName = ''
  filters.tearoomName = ''
  filters.phone = ''
  filters.musicName = ''
  pageNum.value = 1
  loadPlayList()
}

function resetForm() {
  Object.assign(form, {
    musicPlayId: '',
    musicLibraryId: '',
    musicTypeId: '',
    musicName: '',
    musicUrl: '',
    musicPicUrl: '',
    musicAuthor: '',
    tearoomId: '',
    userId: '',
    userName: '',
    status: 2,
  })
}

function openCreateDialog() {
  resetForm()
  editDialogVisible.value = true
}

function openEditDialog(row) {
  Object.assign(form, {
    musicPlayId: row.musicPlayId,
    musicLibraryId: row.musicLibraryId || '',
    musicTypeId: row.musicTypeId || '',
    musicName: row.musicName || '',
    musicUrl: row.musicUrl || '',
    musicPicUrl: row.musicPicUrl || '',
    musicAuthor: row.musicAuthor || '',
    tearoomId: row.tearoomId || '',
    userId: row.userId || '',
    userName: row.userName || '',
    status: Number(row.status || 2),
  })
  editDialogVisible.value = true
}

async function submitPlay() {
  if (!form.userId) return showError('请选择用户')
  if (!form.musicName.trim()) return showError('请输入歌曲名称')
  if (!form.musicPicUrl) return showError('请上传歌曲封面图')

  editDialogLoading.value = true
  try {
    const payload = {
      musicLibraryId: form.musicLibraryId,
      musicTypeId: form.musicTypeId,
      musicName: form.musicName.trim(),
      musicUrl: form.musicUrl,
      musicPicUrl: form.musicPicUrl,
      musicAuthor: form.musicAuthor,
      tearoomId: form.tearoomId,
      musicPlayId: form.musicPlayId || undefined,
      userId: form.userId,
      status: form.status,
    }
    if (form.musicPlayId) {
      await updateMusicPlayApi(payload)
    } else {
      delete payload.musicPlayId
      await createMusicPlayApi(payload)
    }
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadPlayList()
  } finally {
    editDialogLoading.value = false
  }
}

async function removePlay(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMusicPlayApi(row.musicPlayId)
  showSuccess('删除成功')
  await loadPlayList()
}

function goUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

async function openUserDialog() {
  userPage.value = 1
  userFilters.phone = ''
  userFilters.userName = ''
  userDialogVisible.value = true
  await loadUsers()
}

async function loadUsers() {
  userLoading.value = true
  try {
    const data = await getUserCenterPage({
      page: userPage.value,
      limit: userSize.value,
      phone: userFilters.phone.trim(),
      userName: userFilters.userName.trim(),
      invitationCode: '',
      startTime: '',
      endTime: '',
      isAuthentication: '',
    })
    userList.value = data.list || []
    userTotal.value = data.total || 0
  } finally {
    userLoading.value = false
  }
}

function chooseUser(row) {
  form.userId = row.userId
  form.userName = row.userName || ''
  userDialogVisible.value = false
}

async function openLibraryDialog() {
  libraryPage.value = 1
  libraryFilters.musicTypeId = ''
  libraryFilters.musicName = ''
  libraryFilters.musicAuthor = ''
  libraryDialogVisible.value = true
  await loadLibrary()
}

async function loadLibrary() {
  libraryLoading.value = true
  try {
    const data = await getMusicLibraryPageApi({
      page: libraryPage.value,
      limit: librarySize.value,
      musicTypeId: libraryFilters.musicTypeId,
      musicName: libraryFilters.musicName.trim(),
      musicAuthor: libraryFilters.musicAuthor.trim(),
      tearoomName: '',
    })
    libraryList.value = data.list || []
    libraryTotal.value = data.total || 0
  } finally {
    libraryLoading.value = false
  }
}

function chooseLibrary(row) {
  Object.assign(form, {
    musicLibraryId: row.musicLibraryId,
    musicTypeId: row.musicTypeId || '',
    musicName: row.musicName || '',
    musicUrl: row.musicUrl || '',
    musicPicUrl: row.musicPicUrl || '',
    musicAuthor: row.musicAuthor || '',
    tearoomId: row.tearoomId || '',
  })
  libraryDialogVisible.value = false
}

loadCategories()
loadPlayList()
</script>

<template>
  <div class="mplay">
    <header class="mplay-hero nf-page-hero">
      <span class="mplay-hero__badge">音乐管理</span>
      <h1 class="mplay-hero__title">点歌列表</h1>
      <p class="mplay-hero__desc">查看与管理用户点歌记录，支持多条件筛选、添加点歌与关联用户/歌曲选择</p>
    </header>

    <div class="mplay-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="mplay-filter__form" @submit.prevent>
        <el-form-item label="是否已播放">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="filters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="门店名称">
          <el-input v-model="filters.tearoomName" clearable placeholder="请输入门店名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="歌曲名称">
          <el-input v-model="filters.musicName" clearable placeholder="请输入歌曲名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="mplay-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :disabled="!isAuth('musicPlay:add')" @click="openCreateDialog">添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="mplay-table nf-table-card">
      <el-table v-loading="loading" :data="playList" border>
        <el-table-column prop="musicPlayId" label="编号" width="80" fixed="left" />
        <el-table-column prop="tearoomName" label="门店名称" min-width="140" fixed="left" />
        <el-table-column prop="userName" label="用户昵称" min-width="140" fixed="left">
          <template #default="{ row }">
            <el-link type="primary" @click="goUserDetail(row.userId)">{{ row.userName || '未绑定' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="musicName" label="歌曲名称" min-width="150" />
        <el-table-column prop="musicUrl" label="歌曲地址" min-width="320">
          <template #default="{ row }">
            <audio v-if="row.musicUrl" :src="row.musicUrl" controls preload="metadata" class="audio-player"></audio>
            <span v-else>暂无语音</span>
          </template>
        </el-table-column>
        <el-table-column prop="musicAuthor" label="歌曲作者" min-width="140" />
        <el-table-column label="歌曲封面图" width="100">
          <template #default="{ row }">
            <el-image v-if="row.musicPicUrl" :src="row.musicPicUrl" class="thumb-image" :preview-src-list="[row.musicPicUrl]" preview-teleported />
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="是否已播放" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" :disabled="!isAuth('musicPlay:update')" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('musicPlay:delete')" @click="removePlay(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadPlayList()" @update:pageSize="pageSize = $event; pageNum = 1; loadPlayList()" />
    </div>

    <el-dialog v-model="editDialogVisible" :title="dialogTitle" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">用户昵称</span><el-input v-model="form.userName" class="dialog-field" placeholder="请选择绑定用户" readonly @click="openUserDialog" /></div>
        <div class="form-row"><span class="form-label">歌曲名称</span><el-input v-model="form.musicName" class="dialog-field" placeholder="请选择歌曲" readonly @click="openLibraryDialog" /></div>
        <div class="form-row"><span class="form-label">门店名称</span><el-input :model-value="playList.find((item) => item.tearoomId === form.tearoomId)?.tearoomName || ''" class="dialog-field" disabled /></div>
        <div class="form-row"><span class="form-label">歌曲地址</span><el-input v-model="form.musicUrl" class="dialog-field" disabled /></div>
        <div class="form-row"><span class="form-label">歌曲作者</span><el-input v-model="form.musicAuthor" class="dialog-field" disabled /></div>
        <div class="form-row"><span class="form-label">歌曲封面图</span><div class="cover-box"><el-image v-if="form.musicPicUrl" :src="form.musicPicUrl" class="large-thumb" :preview-src-list="[form.musicPicUrl]" preview-teleported /></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitPlay">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="userDialogVisible" title="用户列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="手机号">
          <el-input v-model="userFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="userPage = 1; loadUsers()" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userFilters.userName" clearable placeholder="请输入昵称" @keyup.enter="userPage = 1; loadUsers()" />
        </el-form-item>
        <el-form-item>
          <el-space>
            <el-button type="primary" @click="userPage = 1; loadUsers()">查询</el-button>
            <el-button @click="userFilters.phone = ''; userFilters.userName = ''; userPage = 1; loadUsers()">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
      <el-table v-loading="userLoading" :data="userList" border>
        <el-table-column prop="userId" label="编号" width="80" />
        <el-table-column prop="userName" label="昵称" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="sex" label="性别" width="80">
          <template #default="{ row }">{{ row.sex === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button size="small" type="primary" @click="chooseUser(row)">确定</el-button></template>
        </el-table-column>
      </el-table>
      <BasePagination :current-page="userPage" :page-size="userSize" :page-sizes="[5, 10, 15, 20]" :total="userTotal" @update:currentPage="userPage = $event; loadUsers()" @update:pageSize="userSize = $event; userPage = 1; loadUsers()" />
    </el-dialog>

    <el-dialog v-model="libraryDialogVisible" title="音乐库列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="分类名称">
          <el-select v-model="libraryFilters.musicTypeId" placeholder="请选择分类" clearable @change="libraryPage = 1; loadLibrary()">
            <el-option v-for="item in categoryOptions" :key="item.musicTypeId" :label="item.musicTypeName" :value="item.musicTypeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="歌曲名称">
          <el-input v-model="libraryFilters.musicName" clearable placeholder="请输入歌曲名称" @keyup.enter="libraryPage = 1; loadLibrary()" />
        </el-form-item>
        <el-form-item label="歌曲作者">
          <el-input v-model="libraryFilters.musicAuthor" clearable placeholder="请输入歌曲作者" @keyup.enter="libraryPage = 1; loadLibrary()" />
        </el-form-item>
        <el-form-item>
          <el-space>
            <el-button type="primary" @click="libraryPage = 1; loadLibrary()">查询</el-button>
            <el-button @click="libraryFilters.musicTypeId = ''; libraryFilters.musicName = ''; libraryFilters.musicAuthor = ''; libraryPage = 1; loadLibrary()">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
      <el-table v-loading="libraryLoading" :data="libraryList" border>
        <el-table-column prop="musicLibraryId" label="编号" width="80" fixed="left" />
        <el-table-column prop="musicTypeName" label="分类名称" min-width="140" />
        <el-table-column prop="musicName" label="歌曲名称" min-width="180" fixed="left" />
        <el-table-column prop="musicUrl" label="歌曲地址" min-width="320">
          <template #default="{ row }">
            <audio v-if="row.musicUrl" :src="row.musicUrl" controls preload="metadata" class="audio-player"></audio>
            <span v-else>暂无语音</span>
          </template>
        </el-table-column>
        <el-table-column prop="musicAuthor" label="歌曲作者" min-width="140" />
        <el-table-column label="歌曲封面图" width="100">
          <template #default="{ row }">
            <el-image v-if="row.musicPicUrl" :src="row.musicPicUrl" class="thumb-image" :preview-src-list="[row.musicPicUrl]" preview-teleported />
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button size="small" type="primary" @click="chooseLibrary(row)">选择</el-button></template>
        </el-table-column>
      </el-table>
      <BasePagination :current-page="libraryPage" :page-size="librarySize" :page-sizes="[10, 20, 30, 50, 100]" :total="libraryTotal" @update:currentPage="libraryPage = $event; loadLibrary()" @update:pageSize="librarySize = $event; libraryPage = 1; loadLibrary()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.mplay {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.mplay-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mplay-hero__badge {
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

.mplay-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.mplay-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.mplay-filter.nf-filter-card {
  margin-bottom: 0;
}

.mplay-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.mplay-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.mplay-table.nf-table-card {
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
  margin-bottom: 12px;
}

.audio-player {
  width: 100%;
  max-width: 320px;
}

.thumb-image {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: block;
}

.large-thumb {
  width: 148px;
  height: 148px;
  border-radius: 10px;
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
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
}

.cover-box {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

@media (max-width: 768px) {
  .mplay-filter__form {
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

  .audio-player {
    max-width: 100%;
  }
}
</style>

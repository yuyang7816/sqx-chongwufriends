<script setup>
import { computed, ref, reactive } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import { getMusicTypePageApi } from '@/api/modules/merchant-center'
import {
  createMusicLibraryApi,
  createRandomMusicLibraryApi,
  deleteMusicLibraryApi,
  getMusicLibraryPageApi,
  updateMusicLibraryApi,
} from '@/api/modules/music-library'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const musicList = ref([])

const categoryOptions = ref([])

const filters = reactive({
  musicTypeId: '',
  musicName: '',
  musicAuthor: '',
})

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const dialogTitle = computed(() => (form.musicLibraryId ? '修改歌曲' : '添加歌曲'))
const uploadLoadingField = ref('')

const form = reactive({
  musicLibraryId: '',
  musicTypeId: '',
  musicName: '',
  musicUrl: '',
  musicPicUrl: '',
  musicAuthor: '',
})

const randomDialogVisible = ref(false)
const randomDialogLoading = ref(false)
const randomCount = ref('')

function resetForm() {
  Object.assign(form, {
    musicLibraryId: '',
    musicTypeId: '',
    musicName: '',
    musicUrl: '',
    musicPicUrl: '',
    musicAuthor: '',
  })
}

async function loadCategories() {
  const data = await getMusicTypePageApi({ page: 1, limit: 50 })
  categoryOptions.value = data?.records || data?.list || []
}

async function loadMusicList() {
  loading.value = true
  try {
    const data = await getMusicLibraryPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      musicTypeId: filters.musicTypeId,
      musicName: filters.musicName.trim(),
      musicAuthor: filters.musicAuthor.trim(),
      tearoomName: '',
    })
    musicList.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadMusicList()
}

function handleReset() {
  filters.musicTypeId = ''
  filters.musicName = ''
  filters.musicAuthor = ''
  pageNum.value = 1
  loadMusicList()
  loadCategories()
}

async function handleAudioUpload(option) {
  uploadLoadingField.value = 'musicUrl'
  try {
    const data = await uploadMerchantImageApi(option.file)
    form.musicUrl = String(data?.url || data?.data || data?.src || data?.path || data || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    uploadLoadingField.value = ''
  }
}

async function handleImageUpload(option) {
  uploadLoadingField.value = 'musicPicUrl'
  try {
    const data = await uploadMerchantImageApi(option.file)
    form.musicPicUrl = String(data?.url || data?.data || data?.src || data?.path || data || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    uploadLoadingField.value = ''
  }
}

function beforeAudioUpload(file) {
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (ext !== 'mp3') {
    showError('上传文件只能是mp3格式')
    return false
  }
  return true
}

function openCreateDialog() {
  resetForm()
  editDialogVisible.value = true
}

function openEditDialog(row) {
  Object.assign(form, {
    musicLibraryId: row.musicLibraryId,
    musicTypeId: row.musicTypeId || '',
    musicName: row.musicName || '',
    musicUrl: row.musicUrl || '',
    musicPicUrl: row.musicPicUrl || '',
    musicAuthor: row.musicAuthor || '',
  })
  editDialogVisible.value = true
}

async function submitMusic() {
  if (!form.musicTypeId) return showError('请选择歌曲分类')
  if (!form.musicName.trim()) return showError('请输入歌曲名称')
  if (!form.musicUrl) return showError('请上传歌曲地址')
  if (!form.musicAuthor.trim()) return showError('请输入歌曲作者')

  editDialogLoading.value = true
  try {
    const payload = {
      musicLibraryId: form.musicLibraryId || undefined,
      musicTypeId: form.musicTypeId,
      musicName: form.musicName.trim(),
      musicUrl: form.musicUrl,
      musicPicUrl: form.musicPicUrl,
      musicAuthor: form.musicAuthor.trim(),
      tearoomId: '',
    }
    if (form.musicLibraryId) {
      await updateMusicLibraryApi(payload)
    } else {
      delete payload.musicLibraryId
      await createMusicLibraryApi(payload)
    }
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadMusicList()
  } finally {
    editDialogLoading.value = false
  }
}

async function removeMusic(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMusicLibraryApi(row.musicLibraryId)
  showSuccess('删除成功')
  await loadMusicList()
}

function openRandomDialog() {
  randomCount.value = ''
  randomDialogVisible.value = true
}

async function submitRandom() {
  if (!randomCount.value) return showError('请输入数量')
  randomDialogLoading.value = true
  try {
    await createRandomMusicLibraryApi({ num: randomCount.value, tearoomId: '' })
    showSuccess('操作成功')
    randomDialogVisible.value = false
    await loadMusicList()
  } finally {
    randomDialogLoading.value = false
  }
}

loadCategories()
loadMusicList()
</script>

<template>
  <div class="mlib">
    <header class="mlib-hero nf-page-hero">
      <span class="mlib-hero__badge">音乐管理</span>
      <h1 class="mlib-hero__title">音乐列表</h1>
      <p class="mlib-hero__desc">管理音乐库歌曲资源，支持按分类、名称与作者筛选，可添加、修改与随机批量导入</p>
    </header>

    <div class="mlib-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="mlib-filter__form" @submit.prevent>
        <el-form-item label="分类名称">
          <el-select v-model="filters.musicTypeId" placeholder="请选择分类" clearable @change="handleSearch">
            <el-option v-for="item in categoryOptions" :key="item.musicTypeId" :label="item.musicTypeName" :value="item.musicTypeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="歌曲名称">
          <el-input v-model="filters.musicName" placeholder="请输入歌曲名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="歌曲作者">
          <el-input v-model="filters.musicAuthor" placeholder="请输入歌曲作者" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="mlib-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :disabled="!isAuth('musicLibrary:add')" @click="openCreateDialog">添加</el-button>
            <el-button type="warning" :disabled="!isAuth('musicLibrary:add')" @click="openRandomDialog">随机添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="mlib-table nf-table-card">
      <el-table v-loading="loading" :data="musicList" border>
        <el-table-column prop="musicLibraryId" label="编号" width="80" />
        <el-table-column prop="musicTypeName" label="分类名称" min-width="140" />
        <el-table-column prop="musicName" label="歌曲名称" min-width="180" />
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" :disabled="!isAuth('musicLibrary:update')" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('musicLibrary:delete')" @click="removeMusic(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadMusicList()" @update:pageSize="pageSize = $event; pageNum = 1; loadMusicList()" />
    </div>

    <el-dialog v-model="editDialogVisible" :title="dialogTitle" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row">
          <span class="form-label">分类名称</span>
          <el-radio-group v-model="form.musicTypeId" class="dialog-field wrap-radio">
            <el-radio v-for="item in categoryOptions" :key="item.musicTypeId" :value="item.musicTypeId">{{ item.musicTypeName }}</el-radio>
          </el-radio-group>
        </div>
        <div class="form-row"><span class="form-label">歌曲名称</span><el-input v-model="form.musicName" class="dialog-field" placeholder="请输入歌曲名称" /></div>
        <div class="form-row"><span class="form-label">歌曲地址</span><div class="upload-field"><el-upload :show-file-list="false" :before-upload="beforeAudioUpload" :http-request="handleAudioUpload"><el-button size="small" type="primary" :loading="uploadLoadingField === 'musicUrl'">点击上传</el-button></el-upload><el-input v-if="form.musicUrl" v-model="form.musicUrl" class="dialog-field" /></div></div>
        <div class="form-row"><span class="form-label">歌曲作者</span><el-input v-model="form.musicAuthor" class="dialog-field" placeholder="请输入歌曲作者" /></div>
        <div class="form-row"><span class="form-label">歌曲封面图</span><div class="upload-row"><el-image v-if="form.musicPicUrl" :src="form.musicPicUrl" class="large-thumb" :preview-src-list="[form.musicPicUrl]" preview-teleported /><el-upload :show-file-list="false" :http-request="handleImageUpload"><el-button :loading="uploadLoadingField === 'musicPicUrl'">上传</el-button></el-upload></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitMusic">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="randomDialogVisible" title="随机添加" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-row">
        <span class="form-label">数量</span>
        <el-input v-model="randomCount" class="dialog-field" placeholder="请输入数量" />
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="randomDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="randomDialogLoading" @click="submitRandom">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.mlib {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.mlib-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mlib-hero__badge {
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

.mlib-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.mlib-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.mlib-filter.nf-filter-card {
  margin-bottom: 0;
}

.mlib-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.mlib-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.mlib-table.nf-table-card {
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

.upload-row,
.upload-field {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.wrap-radio {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

@media (max-width: 768px) {
  .mlib-filter__form {
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

  .upload-row,
  .upload-field {
    flex-wrap: wrap;
    width: 100%;
  }

  .audio-player {
    max-width: 100%;
  }
}
</style>

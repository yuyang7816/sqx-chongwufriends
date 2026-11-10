<script setup>
import { computed, ref, reactive } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import {
  createMusicCategoryApi,
  deleteMusicCategoryApi,
  getMusicCategoryPageApi,
  updateMusicCategoryApi,
} from '@/api/modules/music-category'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const categoryList = ref([])

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const uploadLoading = ref(false)
const progress = ref(0)

const form = reactive({
  musicTypeId: '',
  musicTypeName: '',
  musicTypeImg: '',
})

const dialogTitle = computed(() => (form.musicTypeId ? '修改分类' : '添加分类'))

async function loadCategories() {
  loading.value = true
  try {
    const data = await getMusicCategoryPageApi({
      page: pageNum.value,
      limit: pageSize.value,
    })
    categoryList.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleRefresh() {
  pageNum.value = 1
  loadCategories()
}

function openCreateDialog() {
  form.musicTypeId = ''
  form.musicTypeName = ''
  form.musicTypeImg = ''
  progress.value = 0
  editDialogVisible.value = true
}

function openEditDialog(row) {
  form.musicTypeId = row.musicTypeId
  form.musicTypeName = row.musicTypeName || ''
  form.musicTypeImg = row.musicTypeImg || ''
  progress.value = form.musicTypeImg ? 100 : 0
  editDialogVisible.value = true
}

async function handleImageUpload(option) {
  uploadLoading.value = true
  progress.value = 0
  try {
    const data = await uploadMerchantImageApi(option.file)
    form.musicTypeImg = String(data?.url || data?.data || data?.src || data?.path || data || '')
    progress.value = 100
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    uploadLoading.value = false
  }
}

async function submitCategory() {
  if (!form.musicTypeName.trim()) return showError('请输入分类名称')
  if (!form.musicTypeImg) return showError('请上传分类图片')

  editDialogLoading.value = true
  try {
    const payload = {
      musicTypeName: form.musicTypeName.trim(),
      musicTypeImg: form.musicTypeImg,
      musicTypeId: form.musicTypeId || undefined,
    }
    if (form.musicTypeId) {
      await updateMusicCategoryApi(payload)
    } else {
      delete payload.musicTypeId
      await createMusicCategoryApi(payload)
    }
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadCategories()
  } finally {
    editDialogLoading.value = false
  }
}

async function removeCategory(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMusicCategoryApi(row.musicTypeId)
  showSuccess('删除成功')
  await loadCategories()
}

loadCategories()
</script>

<template>
  <div class="mcat">
    <header class="mcat-hero nf-page-hero">
      <span class="mcat-hero__badge">音乐管理</span>
      <h1 class="mcat-hero__title">音乐分类</h1>
      <p class="mcat-hero__desc">维护音乐分类名称与封面图，支持添加、修改与删除分类</p>
    </header>

    <div class="mcat-table nf-table-card">
      <div class="mcat-table__toolbar">
        <el-space>
          <el-button type="primary" plain @click="handleRefresh">刷新</el-button>
          <el-button type="primary" :disabled="!isAuth('musicActivity:add')" @click="openCreateDialog">添加</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="categoryList" border>
        <el-table-column prop="musicTypeId" label="编号" width="80" />
        <el-table-column prop="musicTypeName" label="分类名称" min-width="180" />
        <el-table-column label="分类图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.musicTypeImg" :src="row.musicTypeImg" class="thumb-image" :preview-src-list="[row.musicTypeImg]" preview-teleported />
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" :disabled="!isAuth('musicActivity:update')" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('musicActivity:delete')" @click="removeCategory(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadCategories()" @update:pageSize="pageSize = $event; pageNum = 1; loadCategories()" />
    </div>

    <el-dialog v-model="editDialogVisible" :title="dialogTitle" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row">
          <span class="form-label">分类名称</span>
          <el-input v-model="form.musicTypeName" class="dialog-field" placeholder="请输入分类名称" />
        </div>
        <div class="form-row">
          <span class="form-label">分类图片</span>
          <div class="upload-row">
            <el-image v-if="form.musicTypeImg && progress === 100" :src="form.musicTypeImg" class="large-thumb" :preview-src-list="[form.musicTypeImg]" preview-teleported />
            <el-upload :show-file-list="false" :http-request="handleImageUpload">
              <el-button :loading="uploadLoading">上传</el-button>
            </el-upload>
            <el-progress v-if="progress > 0 && progress < 100" type="circle" :percentage="progress" />
          </div>
        </div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitCategory">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.mcat {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.mcat-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mcat-hero__badge {
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

.mcat-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.mcat-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.mcat-table.nf-table-card {
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

.mcat-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
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

.upload-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }

  .form-label {
    width: auto;
    text-align: left;
  }

  .upload-row {
    flex-wrap: wrap;
  }
}
</style>

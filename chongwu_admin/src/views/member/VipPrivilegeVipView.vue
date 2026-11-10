<script setup>
import { computed, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import {
  createVipPrivilegeVipApi,
  deleteVipPrivilegeVipApi,
  getVipPrivilegeVipPageApi,
  updateVipPrivilegeVipApi,
} from '@/api/modules/vip-privilege-vip'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const privilegeList = ref([])

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const uploadLoading = ref(false)
const dialogTitle = computed(() => (form.id ? '修改' : '添加'))

const form = reactive({
  id: '',
  imageUrl: '',
  name: '',
})

function resetForm() {
  Object.assign(form, {
    id: '',
    imageUrl: '',
    name: '',
  })
}

function normalizeRow(item = {}) {
  return {
    ...item,
    id: item.id,
    imageUrl: item.imageUrl || '',
    name: item.name || '',
    createTime: item.createTime || item.createdAt || '',
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getVipPrivilegeVipPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      state: -1,
      classify: 50,
    })
    privilegeList.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function refreshData() {
  pageNum.value = 1
  loadData()
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  Object.assign(form, {
    id: row.id || '',
    imageUrl: row.imageUrl || '',
    name: row.name || '',
  })
  dialogVisible.value = true
}

async function handleImageUpload(option) {
  uploadLoading.value = true
  try {
    const data = await uploadMerchantImageApi(option.file)
    form.imageUrl = String(data?.url || data?.data || data?.src || data?.path || data || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    uploadLoading.value = false
  }
}

async function submitForm() {
  if (!form.imageUrl) return showError('请上传图片')
  if (!form.name.trim()) return showError('请输入特权名称')

  dialogLoading.value = true
  try {
    const payload = {
      imageUrl: form.imageUrl,
      id: form.id || undefined,
      name: form.name.trim(),
      classify: 50,
    }
    if (form.id) {
      await updateVipPrivilegeVipApi(payload)
    } else {
      delete payload.id
      await createVipPrivilegeVipApi(payload)
    }
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确定删除此条信息?')
  await deleteVipPrivilegeVipApi(row.id)
  if (privilegeList.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1
  }
  showSuccess('删除成功')
  await loadData()
}

loadData()
</script>

<template>
  <div class="vpv">
    <header class="vpv-hero nf-page-hero">
      <span class="vpv-hero__badge">会员管理</span>
      <h1 class="vpv-hero__title">会员特权</h1>
      <p class="vpv-hero__desc">管理会员特权图标与名称展示，支持上传图片与增删改操作</p>
    </header>

    <div class="vpv-table nf-table-card">
      <div class="vpv-table__toolbar">
        <el-space>
          <el-button type="primary" plain @click="refreshData">刷新</el-button>
          <el-button type="primary" :disabled="!isAuth('memberDetailsVip:add')" @click="openCreateDialog">添加</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="privilegeList" border>
        <el-table-column prop="id" label="编号" width="60" />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" class="thumb-image" :preview-src-list="[row.imageUrl]" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="特权名称" min-width="180" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" type="primary" :disabled="!isAuth('vipPrivilegeVip:update')" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('vipPrivilegeVip:delete')" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadData()" @update:pageSize="pageSize = $event; loadData()" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row">
          <span class="form-label">图片</span>
          <div class="upload-row">
            <el-image v-if="form.imageUrl" :src="form.imageUrl" class="large-thumb" :preview-src-list="[form.imageUrl]" preview-teleported />
            <el-upload :show-file-list="false" :http-request="handleImageUpload">
              <el-button :loading="uploadLoading">上传</el-button>
            </el-upload>
          </div>
        </div>
        <div class="form-row"><span class="form-label">特权名称</span><el-input v-model="form.name" class="dialog-field" placeholder="请输入特权名称" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.vpv {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.vpv-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.vpv-hero__badge {
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

.vpv-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.vpv-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.vpv-table.nf-table-card {
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

.vpv-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.thumb-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
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
  min-width: 0;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
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

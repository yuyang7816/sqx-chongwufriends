<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { deleteUpgradeApi, getUpgradePage, saveUpgradeApi } from '@/api/modules/upgrades'
import { validateForm } from '@/utils/form'
import { confirmAction, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const detailLoading = ref(false)
const editingId = ref(0)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const methodOptions = [
  { label: '普通升级', value: 'false' },
  { label: '强制升级', value: 'true' },
]

const initialForm = {
  id: 0,
  wgtUrl: '',
  androidWgtUrl: '',
  iosWgtUrl: '',
  version: '',
  iosVersion: '',
  method: 'false',
  des: '',
}

const form = reactive({ ...initialForm })

const rules = {
  wgtUrl: [{ required: true, message: '请输入统一地址', trigger: 'blur' }],
  androidWgtUrl: [{ required: true, message: '请输入安卓地址', trigger: 'blur' }],
  iosWgtUrl: [{ required: true, message: '请输入苹果地址', trigger: 'blur' }],
  version: [{ required: true, message: '请输入安卓版本号', trigger: 'blur' }],
  iosVersion: [{ required: true, message: '请输入 iOS 版本号', trigger: 'blur' }],
  method: [{ required: true, message: '请选择升级方式', trigger: 'change' }],
  des: [{ required: true, message: '请输入升级内容', trigger: 'blur' }],
}

function resetForm() {
  Object.assign(form, initialForm)
}

function normalizeUpgradeRow(item = {}) {
  return {
    ...item,
    id: item.id,
    wgtUrl: item.wgtUrl || '',
    androidWgtUrl: item.androidWgtUrl || '',
    iosWgtUrl: item.iosWgtUrl || '',
    version: item.version || '',
    iosVersion: item.iosVersion || '',
    method: `${item.method ?? 'false'}`,
    des: item.des || '',
    createAt: item.createAt || item.createTime || '',
  }
}

function buildSubmitPayload() {
  return {
    id: editingId.value || undefined,
    wgtUrl: form.wgtUrl.trim(),
    androidWgtUrl: form.androidWgtUrl.trim(),
    iosWgtUrl: form.iosWgtUrl.trim(),
    version: form.version.trim(),
    iosVersion: form.iosVersion.trim(),
    method: form.method,
    des: form.des.trim(),
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getUpgradePage({ page: pageNum.value, limit: pageSize.value })
    tableData.value = (data.list || []).map(normalizeUpgradeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function openCreate() {
  editingId.value = 0
  resetForm()
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEdit(row) {
  editingId.value = row.id
  detailLoading.value = true
  try {
    Object.assign(form, {
      id: row.id,
      wgtUrl: row.wgtUrl,
      androidWgtUrl: row.androidWgtUrl,
      iosWgtUrl: row.iosWgtUrl,
      version: row.version,
      iosVersion: row.iosVersion,
      method: `${row.method}`,
      des: row.des,
    })
    dialogVisible.value = true
    await nextTick()
    formRef.value?.clearValidate()
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    await saveUpgradeApi(buildSubmitPayload())
    dialogVisible.value = false
    showSuccess(editingId.value ? '升级配置已更新' : '升级配置已新增')
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确定删除此条升级配置吗？')
  await deleteUpgradeApi(row.id)
  if (tableData.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1
  }
  showSuccess('删除成功')
  await loadData()
}

function handleDialogVisible(value) {
  dialogVisible.value = value
  if (!value) {
    editingId.value = 0
    resetForm()
  }
}

onMounted(loadData)
</script>

<template>
  <div class="supg">
    <header class="supg-hero nf-page-hero">
      <span class="supg-hero__badge">系统管理</span>
      <h1 class="supg-hero__title">升级配置</h1>
      <p class="supg-hero__desc">维护移动端 App 版本升级包地址、版本号与升级方式，支持普通与强制升级策略</p>
    </header>

    <div class="supg-table nf-table-card">
      <div class="supg-table__toolbar">
        <el-button v-if="isAuth('app:add')" type="primary" @click="openCreate">添加升级</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="编号" width="80" align="center" />
      <el-table-column prop="wgtUrl" label="统一地址" min-width="220" show-overflow-tooltip />
      <el-table-column prop="androidWgtUrl" label="安卓" min-width="220" show-overflow-tooltip />
      <el-table-column prop="iosWgtUrl" label="苹果" min-width="220" show-overflow-tooltip />
      <el-table-column prop="version" label="安卓版本" min-width="110" align="center" />
      <el-table-column prop="iosVersion" label="iOS 版本" min-width="110" align="center" />
      <el-table-column label="升级方式" min-width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.method === 'true' ? 'danger' : 'info'">{{ row.method === 'true' ? '强制升级' : '普通升级' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="des" label="升级内容" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createAt" label="创建时间" min-width="180" align="center" />
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-space>
            <el-button v-if="isAuth('app:update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="isAuth('app:delete')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 30, 50, 100]"
      :total="total"
      @update:currentPage="pageNum = $event; loadData()"
      @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
    />
    </div>

    <AppDialog
      :model-value="dialogVisible"
      :title="editingId ? '修改升级' : '添加升级'"
      :loading="dialogLoading"
      width="720px"
      @update:modelValue="handleDialogVisible"
      @confirm="handleSubmit"
    >
      <div v-loading="detailLoading">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
          <el-form-item label="统一地址" prop="wgtUrl">
            <el-input v-model="form.wgtUrl" placeholder="请输入统一地址" />
          </el-form-item>
          <el-form-item label="安卓地址" prop="androidWgtUrl">
            <el-input v-model="form.androidWgtUrl" placeholder="请输入安卓地址" />
          </el-form-item>
          <el-form-item label="苹果地址" prop="iosWgtUrl">
            <el-input v-model="form.iosWgtUrl" placeholder="请输入苹果地址" />
          </el-form-item>
          <el-form-item label="安卓版本号" prop="version">
            <el-input v-model="form.version" placeholder="请输入安卓版本号" />
          </el-form-item>
          <el-form-item label="iOS 版本号" prop="iosVersion">
            <el-input v-model="form.iosVersion" placeholder="请输入 iOS 版本号" />
          </el-form-item>
          <el-form-item label="升级方式" prop="method">
            <el-select v-model="form.method" placeholder="请选择升级方式" class="form-select-full">
              <el-option v-for="item in methodOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="升级内容" prop="des">
            <el-input v-model="form.des" type="textarea" :rows="3" placeholder="请输入升级内容" />
          </el-form-item>
        </el-form>
      </div>
    </AppDialog>
  </div>
</template>

<style scoped lang="scss">
.supg {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.supg-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.supg-hero__badge {
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

.supg-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.supg-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.supg-table.nf-table-card {
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

.supg-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>

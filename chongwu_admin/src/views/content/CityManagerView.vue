<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { createCityApi, deleteCityApi, getCityPage, updateCityApi } from '@/api/modules/cities'
import { validateForm } from '@/utils/form'
import { confirmAction, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(0)

const initialForm = {
  cityId: 0,
  city: '',
  status: 1,
}

const form = reactive({ ...initialForm })

const rules = {
  city: [{ required: true, message: '请输入城市名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function resetForm() {
  Object.assign(form, initialForm)
}

function normalizeCityRow(item = {}) {
  return {
    ...item,
    cityId: item.cityId,
    city: item.city || '',
    status: Number(item.status ?? 1),
  }
}

function buildSubmitPayload() {
  return {
    cityId: editingId.value || undefined,
    city: form.city.trim(),
    status: Number(form.status),
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getCityPage({
      page: pageNum.value,
      limit: pageSize.value,
      city: '',
    })
    tableData.value = (data.list || []).map(normalizeCityRow)
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
  editingId.value = row.cityId
  Object.assign(form, {
    cityId: row.cityId,
    city: row.city,
    status: Number(row.status ?? 1),
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    if (editingId.value) {
      await updateCityApi(buildSubmitPayload())
      showSuccess('城市已更新')
    } else {
      await createCityApi(buildSubmitPayload())
      showSuccess('城市已新增')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确定删除城市“${row.city}”吗？`)
  await deleteCityApi(row.cityId)
  if (tableData.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1
  }
  showSuccess('删除成功')
  await loadData()
}

async function handleStatusChange(row) {
  try {
    await updateCityApi({
      cityId: row.cityId,
      city: row.city,
      status: Number(row.status),
    })
    showSuccess('状态已更新')
    await loadData()
  } catch (error) {
    row.status = row.status === 1 ? 2 : 1
  }
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
  <div class="city-mgr">
    <header class="city-mgr-hero nf-page-hero">
      <span class="city-mgr-hero__badge">内容管理</span>
      <h1 class="city-mgr-hero__title">城市管理</h1>
      <p class="city-mgr-hero__desc">维护平台开放城市列表，支持新增、编辑与启用状态切换</p>
    </header>

    <div class="city-mgr-table nf-table-card">
      <div class="city-mgr-table__toolbar">
        <el-button v-if="isAuth('citys:add')" type="primary" @click="openCreate">添加城市</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="cityId" label="编号" width="80" align="center" />
        <el-table-column prop="city" label="城市" min-width="220" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="2"
              :disabled="!isAuth('citys:update')"
              inline-prompt
              active-text="开"
              inactive-text="关"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-space>
              <el-button v-if="isAuth('citys:update')" link type="primary" @click="openEdit(row)">修改</el-button>
              <el-button v-if="isAuth('citys:delete')" link type="danger" @click="handleDelete(row)">删除</el-button>
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
    </div>

    <AppDialog
      :model-value="dialogVisible"
      :title="editingId ? '修改城市' : '添加城市'"
      :loading="dialogLoading"
      width="560px"
      @update:modelValue="handleDialogVisible"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="城市名称" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">开启</el-radio>
            <el-radio :value="2">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>

<style scoped lang="scss">
.city-mgr {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.city-mgr-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.city-mgr-hero__badge {
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

.city-mgr-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.city-mgr-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.city-mgr-table.nf-table-card {
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

.city-mgr-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>

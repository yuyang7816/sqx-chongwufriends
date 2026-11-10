<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { createCategoryApi, deleteCategoryApi, getCategoryPage, updateCategoryApi } from '@/api/modules/categories'
import { useDictStore } from '@/stores/dict'
import { buildPageParams, patchFormModel, resetFormModel, validateForm } from '@/utils/form'
import { confirmAction, showSuccess } from '@/utils/modal'

const dictStore = useDictStore()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const editingId = ref(null)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filters = reactive({ keyword: '', type: '', status: '' })
const initialForm = { name: '', code: '', type: 'activity', sort: 1, status: 'enabled', remark: '' }
const form = reactive({ ...initialForm })
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const data = await getCategoryPage(buildPageParams(filters, pageNum.value, pageSize.value))
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; loadData() }
function handleReset() { Object.assign(filters, { keyword: '', type: '', status: '' }); pageNum.value = 1; loadData() }
function openCreate() { editingId.value = null; resetFormModel(form, initialForm); dialogVisible.value = true }
function openEdit(row) { editingId.value = row.id; patchFormModel(form, row, initialForm); dialogVisible.value = true }

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    if (editingId.value) await updateCategoryApi(editingId.value, form)
    else await createCategoryApi(form)
    showSuccess(editingId.value ? '分类已更新' : '分类已新增')
    dialogVisible.value = false
    loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除分类“${row.name}”吗？`)
  await deleteCategoryApi(row.id)
  showSuccess('分类已删除')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="ctg">
    <header class="ctg-hero nf-page-hero">
      <span class="ctg-hero__badge">内容管理</span>
      <h1 class="ctg-hero__title">分类管理</h1>
      <p class="ctg-hero__desc">维护活动与内容分类，支持按关键词、类型与状态筛选查询</p>
    </header>

    <div class="ctg-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="ctg-filter__form" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" clearable placeholder="分类名称 / 编码" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.type" clearable placeholder="请选择类型">
            <el-option v-for="item in dictStore.categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="请选择状态">
            <el-option v-for="item in dictStore.statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="ctg-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="openCreate">新增分类</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="ctg-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="code" label="分类编码" min-width="160" />
        <el-table-column label="类型" min-width="140">
          <template #default="{ row }">{{ dictStore.categoryOptions.find((item) => item.value === row.type)?.label }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" min-width="80" />
        <el-table-column prop="remark" label="说明" min-width="180" />
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        @update:current-page="pageNum = $event; loadData()"
        @update:page-size="pageSize = $event; pageNum = 1; loadData()"
      />
    </div>

    <AppDialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" :loading="dialogLoading" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="分类名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类编码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="分类类型">
          <el-select v-model="form.type">
            <el-option v-for="item in dictStore.categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="enabled">启用</el-radio>
            <el-radio value="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="1" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
    </AppDialog>
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

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
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

@media (max-width: 768px) {
  .ctg-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }
}
</style>

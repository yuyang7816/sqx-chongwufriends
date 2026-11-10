<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  createStrategyCategoryApi,
  deleteStrategyCategoryApi,
  getStrategyCategoryPageApi,
  updateStrategyCategoryApi,
} from '@/api/modules/pet-strategy-category'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)

const filters = reactive({
  name: '',
  status: '',
})

const form = reactive({
  name: '',
  sort: 0,
  status: 1,
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

function resetForm() {
  Object.assign(form, {
    name: '',
    sort: 0,
    status: 1,
  })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getStrategyCategoryPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      name: filters.name.trim(),
      status: filters.status,
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载分类失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.name = ''
  filters.status = ''
  pageNum.value = 1
  loadData()
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name || '',
    sort: row.sort ?? 0,
    status: Number(row.status ?? 1),
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name.trim()) return showError('请输入分类名称')
  dialogLoading.value = true
  try {
    const payload = {
      name: form.name.trim(),
      sort: Number(form.sort || 0),
      status: Number(form.status),
    }
    if (editingId.value) {
      await updateStrategyCategoryApi({ ...payload, id: editingId.value })
      showSuccess('修改成功')
    } else {
      await createStrategyCategoryApi(payload)
      showSuccess('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确定删除分类「${row.name}」吗？分类下有攻略时无法删除。`)
  try {
    await deleteStrategyCategoryApi(row.id)
    showSuccess('删除成功')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

async function handleStatusChange(row, previousValue) {
  try {
    await updateStrategyCategoryApi({
      id: row.id,
      name: row.name,
      sort: row.sort ?? 0,
      status: Number(row.status),
    })
    showSuccess('状态已更新')
  } catch (error) {
    row.status = previousValue
    showError(error?.msg || error?.message || '状态更新失败')
  }
}

loadData()
</script>

<template>
  <div class="pstratcat">
    <header class="pstratcat-hero nf-page-hero">
      <span class="pstratcat-hero__badge">宠物业务</span>
      <h1 class="pstratcat-hero__title">攻略分类</h1>
      <p class="pstratcat-hero__desc">对应用户端养宠攻略筛选分类，排序越小越靠前；停用后用户端不可见</p>
    </header>

    <div class="pstratcat-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pstratcat-filter__form" @submit.prevent>
        <el-form-item label="名称">
          <el-input v-model="filters.name" clearable placeholder="分类名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部" style="width: 120px">
            <el-option
              v-for="item in statusOptions"
              :key="`status-${item.value}`"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="pstratcat-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="primary" plain @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pstratcat-table nf-table-card">
      <div class="pstratcat-table__toolbar">
        <el-button
          type="primary"
          :disabled="!isAuth('pet:strategy:category:add')"
          @click="openCreate"
        >新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="!isAuth('pet:strategy:category:update')"
              @change="(value) => handleStatusChange(row, value === 1 ? 0 : 1)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-space>
              <el-button
                size="small"
                type="primary"
                :disabled="!isAuth('pet:strategy:category:update')"
                @click="openEdit(row)"
              >编辑</el-button>
              <el-button
                size="small"
                type="danger"
                :disabled="!isAuth('pet:strategy:category:delete')"
                @click="handleDelete(row)"
              >删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination
        v-model:page="pageNum"
        v-model:limit="pageSize"
        :total="total"
        @change="loadData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="480px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form label-width="90px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.name" placeholder="如：汪星人" maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pstratcat {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pstratcat-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pstratcat-hero__badge {
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

.pstratcat-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pstratcat-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pstratcat-filter.nf-filter-card {
  margin-bottom: 0;
}

.pstratcat-filter__form {
  margin-bottom: 0;
}

.pstratcat-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pstratcat-table.nf-table-card {
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

.pstratcat-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>

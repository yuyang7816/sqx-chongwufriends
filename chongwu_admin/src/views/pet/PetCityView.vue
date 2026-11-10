<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  deletePetCityApi,
  getPetCityPageApi,
  savePetCityApi,
  updatePetCityApi,
} from '@/api/modules/pet-city'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)

const form = reactive({
  name: '',
  code: '',
  sort: 0,
  status: 1,
})

function resetForm() {
  Object.assign(form, { name: '', code: '', sort: 0, status: 1 })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetCityPageApi({ page: pageNum.value, limit: pageSize.value })
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载城市失败')
  } finally {
    loading.value = false
  }
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
    code: row.code || '',
    sort: row.sort ?? 0,
    status: Number(row.status ?? 1),
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.name.trim()) return showError('请输入城市名称')
  dialogLoading.value = true
  try {
    const payload = {
      name: form.name.trim(),
      code: form.code.trim(),
      sort: Number(form.sort || 0),
      status: Number(form.status || 1),
    }
    if (editingId.value) {
      await updatePetCityApi({ id: editingId.value, ...payload })
    } else {
      await savePetCityApi(payload)
    }
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction('确认删除该城市？')
  await deletePetCityApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

loadData()
</script>

<template>
  <div class="pcity">
    <header class="pcity-hero nf-page-hero">
      <span class="pcity-hero__badge">宠物业务</span>
      <h1 class="pcity-hero__title">宠物城市</h1>
      <p class="pcity-hero__desc">维护用户端探索首页城市数据，与「内容管理-城市管理」旧模块独立</p>
    </header>

    <div class="pcity-table nf-table-card">
      <el-alert
        type="info"
        show-icon
        :closable="false"
        class="pcity-table__alert"
        title="用户端探索首页读取 pet_city 数据；与「内容管理-城市管理」旧模块独立。"
      />

      <div class="pcity-table__toolbar">
        <el-space wrap>
          <el-button type="primary" @click="openCreate">新增城市</el-button>
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="城市名称" min-width="140" />
        <el-table-column prop="code" label="编码" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeRow(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑城市' : '新增城市'" width="480px" class="nf-pro-dialog" align-center>
<el-form label-width="90px">
        <el-form-item label="城市名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" placeholder="如 SH" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pcity {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pcity-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pcity-hero__badge {
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

.pcity-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pcity-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pcity-table.nf-table-card {
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

.pcity-table__alert {
  margin-bottom: 12px;
}

.pcity-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>

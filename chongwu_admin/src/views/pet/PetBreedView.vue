<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  deletePetBreedApi,
  getPetBreedPageApi,
  savePetBreedApi,
  updatePetBreedApi,
} from '@/api/modules/pet-breed'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const petTypeFilter = ref('')
const editingId = ref(null)

const form = reactive({
  petType: 'dog',
  name: '',
  sort: 0,
  status: 1,
})

function resetForm() {
  Object.assign(form, { petType: 'dog', name: '', sort: 0, status: 1 })
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: pageNum.value, limit: pageSize.value }
    if (petTypeFilter.value) params.petType = petTypeFilter.value
    const data = await getPetBreedPageApi(params)
    tableData.value = data.list || []
    total.value = data.total || 0
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
    petType: row.petType || 'dog',
    name: row.name || '',
    sort: Number(row.sort || 0),
    status: Number(row.status ?? 1),
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.name.trim()) return showError('请输入品种名称')
  dialogLoading.value = true
  try {
    const payload = {
      petType: form.petType,
      name: form.name.trim(),
      sort: Number(form.sort || 0),
      status: Number(form.status ?? 1),
    }
    if (editingId.value) await updatePetBreedApi({ id: editingId.value, ...payload })
    else await savePetBreedApi(payload)
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction('确认删除该品种？')
  await deletePetBreedApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

loadData()
</script>

<template>
  <div class="pbreed">
    <header class="pbreed-hero nf-page-hero">
      <span class="pbreed-hero__badge">宠物业务</span>
      <h1 class="pbreed-hero__title">品种字典</h1>
      <p class="pbreed-hero__desc">维护狗猫品种基础数据，供宠物档案选择与用户端展示使用</p>
    </header>

    <div class="pbreed-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pbreed-filter__form" @submit.prevent>
        <el-form-item label="类型">
          <el-select v-model="petTypeFilter" placeholder="宠物类型" clearable @change="loadData">
            <el-option label="狗" value="dog" />
            <el-option label="猫" value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item class="pbreed-filter__actions">
          <el-space wrap>
            <el-button type="primary" :disabled="!isAuth('pet:breed:add')" @click="openCreate">新增品种</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pbreed-table nf-table-card">
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="petType" label="类型" width="80">
        <template #default="{ row }">{{ row.petType === 'cat' ? '猫' : '狗' }}</template>
      </el-table-column>
      <el-table-column prop="name" label="品种名称" min-width="140" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-space wrap alignment="start">
            <el-button size="small" type="primary" :disabled="!isAuth('pet:breed:update')" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :disabled="!isAuth('pet:breed:delete')" @click="removeRow(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>
    <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑品种' : '新增品种'" width="480px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form v-loading="dialogLoading" label-width="88px">
        <el-form-item label="类型" required>
          <el-select v-model="form.petType" style="width: 100%">
            <el-option label="狗" value="dog" />
            <el-option label="猫" value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种名称" required>
          <el-input v-model="form.name" placeholder="如：柴犬" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
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
.pbreed {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pbreed-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pbreed-hero__badge {
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

.pbreed-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pbreed-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pbreed-filter.nf-filter-card {
  margin-bottom: 0;
}

.pbreed-filter__form {
  margin-bottom: 0;

  :deep(.el-select) {
    width: 140px;
  }
}

.pbreed-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pbreed-table.nf-table-card {
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
</style>

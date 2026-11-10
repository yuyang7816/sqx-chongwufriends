<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  deletePetSensitiveApi,
  getPetSensitivePageApi,
  savePetSensitiveApi,
  updatePetSensitiveApi,
} from '@/api/modules/pet-sensitive'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)

const form = reactive({
  id: '',
  word: '',
  status: 1,
})

function resetForm() {
  Object.assign(form, { id: '', word: '', status: 1 })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetSensitivePageApi({ page: pageNum.value, limit: pageSize.value })
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.word) return showError('请输入敏感词')
  dialogLoading.value = true
  try {
    if (form.id) await updatePetSensitiveApi({ ...form })
    else await savePetSensitiveApi({ ...form })
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction('确认删除该敏感词？')
  await deletePetSensitiveApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

loadData()
</script>

<template>
  <div class="psens">
    <header class="psens-hero nf-page-hero">
      <span class="psens-hero__badge">宠物业务</span>
      <h1 class="psens-hero__title">敏感词</h1>
      <p class="psens-hero__desc">维护内容审核敏感词库，启用后将在用户端发帖与评论中生效</p>
    </header>

    <div class="psens-table nf-table-card">
      <div class="psens-table__toolbar">
        <el-space wrap>
          <el-button type="primary" @click="openCreate">新增敏感词</el-button>
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="敏感词" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeRow(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />

      <el-dialog v-model="dialogVisible" title="敏感词" width="420px" class="nf-pro-dialog" align-center>
<el-form label-width="80px">
          <el-form-item label="词语"><el-input v-model="form.word" /></el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
  </div>
</template>

<style scoped lang="scss">
.psens {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.psens-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.psens-hero__badge {
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

.psens-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.psens-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.psens-table.nf-table-card {
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

.psens-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>

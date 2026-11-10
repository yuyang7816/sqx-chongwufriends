<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  deletePetTaskApi,
  getPetTaskPageApi,
  savePetTaskApi,
  updatePetTaskApi,
} from '@/api/modules/pet-task'
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
  title: '',
  description: '',
  rewardDesc: '',
  taskGroup: 'daily',
  actionType: 'none',
  rewardCoins: 0,
  rewardPoints: 0,
  requiredLevel: 1,
  sort: 0,
  status: 1,
})

const actionTypeOptions = [
  { label: '无（手动任务）', value: 'none' },
  { label: '投喂宠物', value: 'feed' },
  { label: '发布动态', value: 'post' },
  { label: '点赞动态', value: 'like' },
  { label: '评论动态', value: 'comment' },
  { label: '分享动态', value: 'share' },
  { label: '集市兑换', value: 'buy' },
  { label: '设置常驻宠物', value: 'resident' },
]

function resetForm() {
  Object.assign(form, {
    id: '',
    title: '',
    description: '',
    rewardDesc: '',
    taskGroup: '每日待办',
    actionType: 'none',
    rewardCoins: 0,
    rewardPoints: 0,
    requiredLevel: 1,
    sort: 0,
    status: 1,
  })
}

function actionTypeText(value) {
  const item = actionTypeOptions.find((o) => o.value === value)
  return item ? item.label : (value || '-')
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetTaskPageApi({ page: pageNum.value, limit: pageSize.value })
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
  if (!form.title) return showError('请输入任务标题')
  dialogLoading.value = true
  try {
    if (form.id) await updatePetTaskApi({ ...form })
    else await savePetTaskApi({ ...form })
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction('确认删除该任务？')
  await deletePetTaskApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

loadData()
</script>

<template>
  <div class="ptask">
    <header class="ptask-hero nf-page-hero">
      <span class="ptask-hero__badge">宠物业务</span>
      <h1 class="ptask-hero__title">岛宅任务</h1>
      <p class="ptask-hero__desc">配置岛宅每日待办、家主任务与社群任务，设置金币与积分奖励</p>
    </header>

    <div class="ptask-table nf-table-card">
      <div class="ptask-table__toolbar">
        <el-space wrap>
          <el-button type="primary" @click="openCreate">新增任务</el-button>
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="taskGroup" label="分组" width="100" />
        <el-table-column label="触发动作" width="110">
          <template #default="{ row }">{{ actionTypeText(row.actionType) }}</template>
        </el-table-column>
        <el-table-column prop="rewardCoins" label="金币" width="80" />
        <el-table-column prop="rewardPoints" label="积分" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
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

    <el-dialog v-model="dialogVisible" title="岛宅任务" width="560px" class="nf-pro-dialog" align-center>
<el-form label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="奖励说明"><el-input v-model="form.rewardDesc" /></el-form-item>
        <el-form-item label="分组">
          <el-select v-model="form.taskGroup">
            <el-option label="家主任务" value="家主任务" />
            <el-option label="社群任务" value="社群任务" />
            <el-option label="每日待办" value="每日待办" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发动作">
          <el-select v-model="form.actionType" placeholder="选择自动完成的触发动作">
            <el-option v-for="item in actionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级要求"><el-input-number v-model="form.requiredLevel" :min="1" /></el-form-item>
        <el-form-item label="金币奖励"><el-input-number v-model="form.rewardCoins" :min="0" /></el-form-item>
        <el-form-item label="积分奖励"><el-input-number v-model="form.rewardPoints" :min="0" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
</template>

<style scoped lang="scss">
.ptask {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.ptask-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ptask-hero__badge {
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

.ptask-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.ptask-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.ptask-table.nf-table-card {
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

.ptask-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>

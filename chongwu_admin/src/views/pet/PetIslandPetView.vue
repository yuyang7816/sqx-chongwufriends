<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import UserFilterFields from '@/components/common/UserFilterFields.vue'
import { getPetIslandPageApi, updatePetIslandApi } from '@/api/modules/pet-island'
import { useRouteUserFilter } from '@/utils/user-filter'
import { showError, showSuccess } from '@/utils/modal'

const { filters, appendUserFilter } = useRouteUserFilter()
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)

const form = reactive({
  id: '',
  userId: '',
  hunger: 50,
  clean: 50,
  mood: 50,
  vitality: 50,
  intimacy: 0,
  dailyProgress: 50,
  residentPetId: '',
  residentPetIds: '',
  feedCooldownUntil: '',
  foodBowlFilled: 0,
  waterBowlFilled: 0,
})

function resetForm() {
  Object.assign(form, {
    id: '',
    userId: '',
    hunger: 50,
    clean: 50,
    mood: 50,
    vitality: 50,
    intimacy: 0,
    dailyProgress: 50,
    residentPetId: '',
    residentPetIds: '',
    feedCooldownUntil: '',
    foodBowlFilled: 0,
    waterBowlFilled: 0,
  })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetIslandPageApi(appendUserFilter({ page: pageNum.value, limit: pageSize.value }))
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    userId: row.userId,
    hunger: row.hunger ?? 50,
    clean: row.clean ?? 50,
    mood: row.mood ?? 50,
    vitality: row.vitality ?? 50,
    intimacy: row.intimacy ?? 0,
    dailyProgress: row.dailyProgress ?? 50,
    residentPetId: row.residentPetId || '',
    residentPetIds: row.residentPetIds || '',
    feedCooldownUntil: row.feedCooldownUntil || '',
    foodBowlFilled: row.foodBowlFilled ? 1 : 0,
    waterBowlFilled: row.waterBowlFilled ? 1 : 0,
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.id) return showError('缺少岛宅ID')
  dialogLoading.value = true
  try {
    await updatePetIslandApi({ ...form })
    showSuccess('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

function petTypeLabel(type) {
  if (type === 'dog') return '狗'
  if (type === 'cat') return '猫'
  return type || '-'
}

loadData()
</script>

<template>
  <div class="pipet">
    <header class="pipet-hero nf-page-hero">
      <span class="pipet-hero__badge">宠物业务</span>
      <h1 class="pipet-hero__title">岛宅状态</h1>
      <p class="pipet-hero__desc">查看与管理用户岛宅宠物状态，支持饱食度、清洁度、心情等指标编辑</p>
    </header>

    <div class="pipet-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pipet-filter__form" @submit.prevent>
        <UserFilterFields v-model:phone="filters.phone" v-model:username="filters.username" @search="handleSearch" />
        <el-form-item class="pipet-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pipet-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="petName" label="宠物名称" min-width="110">
          <template #default="{ row }">{{ row.petName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="petType" label="类型" width="80">
          <template #default="{ row }">{{ petTypeLabel(row.petType) }}</template>
        </el-table-column>
        <el-table-column prop="breed" label="品种" min-width="100">
          <template #default="{ row }">{{ row.breed || '-' }}</template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">{{ row.gender || '-' }}</template>
        </el-table-column>
        <el-table-column prop="hunger" label="饱食度" width="80" />
        <el-table-column prop="clean" label="清洁度" width="80" />
        <el-table-column prop="mood" label="心情" width="80" />
        <el-table-column prop="vitality" label="活力" width="80" />
        <el-table-column prop="dailyProgress" label="今日进度" width="90" />
        <el-table-column prop="intimacy" label="亲密度" width="90" />
        <el-table-column prop="residentPetId" label="常驻宠物" width="100" />
        <el-table-column prop="residentPetIds" label="常驻列表" min-width="120" />
        <el-table-column prop="feedCooldownUntil" label="投喂冷却至" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" title="编辑岛宅状态" width="560px" class="nf-pro-dialog" align-center>
<el-form label-width="110px">
        <el-form-item label="用户ID"><el-input v-model="form.userId" disabled /></el-form-item>
        <el-form-item label="饱食度"><el-input-number v-model="form.hunger" :min="0" :max="100" /></el-form-item>
        <el-form-item label="清洁度"><el-input-number v-model="form.clean" :min="0" :max="100" /></el-form-item>
        <el-form-item label="心情值"><el-input-number v-model="form.mood" :min="0" :max="100" /></el-form-item>
        <el-form-item label="活力值"><el-input-number v-model="form.vitality" :min="0" :max="100" /></el-form-item>
        <el-form-item label="今日进度"><el-input-number v-model="form.dailyProgress" :min="0" :max="100" /></el-form-item>
        <el-form-item label="亲密度"><el-input-number v-model="form.intimacy" :min="0" :max="9999" /></el-form-item>
        <el-form-item label="常驻宠物ID"><el-input v-model="form.residentPetId" /></el-form-item>
        <el-form-item label="常驻列表"><el-input v-model="form.residentPetIds" placeholder="如 1,2" /></el-form-item>
        <el-form-item label="投喂冷却至"><el-input v-model="form.feedCooldownUntil" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        <el-form-item label="饭盆已喂"><el-switch v-model="form.foodBowlFilled" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="水盆已喂"><el-switch v-model="form.waterBowlFilled" :active-value="1" :inactive-value="0" /></el-form-item>
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
.pipet {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pipet-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pipet-hero__badge {
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

.pipet-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pipet-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pipet-filter.nf-filter-card {
  margin-bottom: 0;
}

.pipet-filter__form {
  margin-bottom: 0;
}

.pipet-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pipet-table.nf-table-card {
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

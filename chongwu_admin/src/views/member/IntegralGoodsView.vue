<script setup>
import { computed, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  createIntegralGoodsApi,
  deleteIntegralGoodsApi,
  getIntegralGoodsPageApi,
  updateIntegralGoodsApi,
} from '@/api/modules/integral-goods'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = computed(() => (form.payClassifyId ? '修改' : '添加'))

const form = reactive({
  payClassifyId: '',
  memberId: '',
  price: '',
  money: '',
  giveMoney: '',
  sort: '',
  payContent: '',
})

function resetForm() {
  Object.assign(form, {
    payClassifyId: '',
    memberId: '',
    price: '',
    money: '',
    giveMoney: '',
    sort: '',
    payContent: '',
  })
}

function normalizeRow(item = {}) {
  return {
    ...item,
    payClassifyId: item.payClassifyId,
    price: item.price ?? '',
    payContent: item.payContent || '',
    sort: item.sort ?? '',
    giveMoney: item.giveMoney ?? '',
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getIntegralGoodsPageApi({
      page: pageNum.value,
      limit: pageSize.value,
    })
    tableData.value = (data.list || []).map(normalizeRow)
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
    payClassifyId: row.payClassifyId || '',
    memberId: row.memberId || '',
    price: row.price ?? '',
    money: row.money ?? '',
    giveMoney: row.giveMoney ?? '',
    sort: row.sort ?? '',
    payContent: row.payContent || '',
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (form.price === '') return showError('请输入售价')
  if (form.payContent === '' && form.giveMoney === '') {
    return showError('赠送金额 和 充值描述 不能同时为空')
  }

  dialogLoading.value = true
  try {
    const payload = {
      money: form.money,
      memberId: form.memberId,
      price: form.price,
      sort: form.sort,
      giveMoney: form.giveMoney,
      payClassifyId: form.payClassifyId || undefined,
      payContent: form.payContent,
    }
    if (form.payClassifyId) {
      await updateIntegralGoodsApi(payload)
    } else {
      delete payload.payClassifyId
      await createIntegralGoodsApi(payload)
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
  await deleteIntegralGoodsApi(row.payClassifyId)
  if (tableData.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1
  }
  showSuccess('删除成功')
  await loadData()
}

loadData()
</script>

<template>
  <div class="ig">
    <header class="ig-hero nf-page-hero">
      <span class="ig-hero__badge">会员管理</span>
      <h1 class="ig-hero__title">充值配置</h1>
      <p class="ig-hero__desc">配置会员充值套餐售价、赠送金额与排序，支持新增、修改与删除</p>
    </header>

    <div class="ig-table nf-table-card">
      <div class="ig-table__toolbar">
        <el-space>
          <el-button type="primary" plain @click="refreshData">刷新</el-button>
          <el-button type="primary" :disabled="!isAuth('IntegralGoods:add')" @click="openCreateDialog">添加</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="payClassifyId" label="编号" width="60" />
        <el-table-column prop="price" label="售价" min-width="120" />
        <el-table-column prop="payContent" label="充值描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="giveMoney" label="赠送金额" min-width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" type="primary" :disabled="!isAuth('IntegralGoods:update')" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('IntegralGoods:delete')" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadData()" @update:pageSize="pageSize = $event; loadData()" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">售价</span><el-input v-model="form.price" class="dialog-field" placeholder="请输入售价" /></div>
        <div class="form-row"><span class="form-label">赠送金额</span><el-input v-model="form.giveMoney" class="dialog-field" placeholder="请输入金额数量" /></div>
        <div class="form-row"><span class="form-label">充值描述</span><el-input v-model="form.payContent" class="dialog-field" placeholder="请输入充值描述" /></div>
        <div class="form-row"><span class="form-label">排序</span><el-input v-model="form.sort" class="dialog-field" placeholder="请输入排序" /></div>
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
.ig {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.ig-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ig-hero__badge {
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

.ig-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.ig-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.ig-table.nf-table-card {
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

.ig-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
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

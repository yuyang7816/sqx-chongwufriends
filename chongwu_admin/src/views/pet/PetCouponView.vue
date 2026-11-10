<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  createPetCouponApi,
  deletePetCouponApi,
  getPetCouponPageApi,
  updatePetCouponApi,
} from '@/api/modules/pet-coupon'
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

const filters = reactive({ title: '' })

const form = reactive({
  title: '',
  amountDesc: '',
  minAmount: 0,
  couponType: 'store',
  validDays: 30,
  status: 1,
})

const couponTypeOptions = [
  { label: '门店优惠', value: 'store' },
  { label: '投喂优惠', value: 'feed' },
  { label: '积分兑换', value: 'points' },
]

function resetForm() {
  Object.assign(form, {
    title: '',
    amountDesc: '',
    minAmount: 0,
    couponType: 'store',
    validDays: 30,
    status: 1,
  })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetCouponPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      title: filters.title.trim(),
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载优惠券模板失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
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
    title: row.title || '',
    amountDesc: row.amountDesc || '',
    minAmount: row.minAmount ?? 0,
    couponType: row.couponType || 'store',
    validDays: row.validDays ?? 30,
    status: row.status ?? 1,
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.title.trim()) {
    showError('请输入券标题')
    return
  }
  dialogLoading.value = true
  try {
    const payload = { ...form, title: form.title.trim() }
    if (editingId.value) {
      await updatePetCouponApi({ ...payload, id: editingId.value })
      showSuccess('模板已更新')
    } else {
      await createPetCouponApi(payload)
      showSuccess('模板已创建')
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
  await confirmAction('确认删除该优惠券模板吗？')
  try {
    await deletePetCouponApi(row.id)
    showSuccess('已删除')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

loadData()
</script>

<template>
  <div class="pcoupon">
    <header class="pcoupon-hero nf-page-hero">
      <span class="pcoupon-hero__badge">宠物业务</span>
      <h1 class="pcoupon-hero__title">优惠券模板</h1>
      <p class="pcoupon-hero__desc">维护门店、投喂、积分兑换等优惠券模板，支持新增、编辑与删除</p>
    </header>

    <div class="pcoupon-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pcoupon-filter__form" @submit.prevent>
        <el-form-item label="券标题">
          <el-input v-model="filters.title" placeholder="搜索券标题" clearable style="width: 220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="pcoupon-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pcoupon-table nf-table-card">
      <div class="pcoupon-table__toolbar">
        <el-space wrap>
          <el-button v-if="isAuth('pet:coupon:add')" type="success" @click="openCreate">新增模板</el-button>
          <el-button v-if="isAuth('pet:coupon:grant')" @click="$router.push('/pet/coupon-grant')">发券管理</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="amountDesc" label="面额描述" min-width="120" />
        <el-table-column prop="couponType" label="类型" width="120" />
        <el-table-column prop="minAmount" label="门槛金额" width="100" />
        <el-table-column prop="validDays" label="有效天数" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isAuth('pet:coupon:update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="isAuth('pet:coupon:delete')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑优惠券模板' : '新增优惠券模板'" width="520px" class="nf-pro-dialog" align-center>
<el-form label-width="96px">
        <el-form-item label="标题"><el-input v-model="form.title" maxlength="50" /></el-form-item>
        <el-form-item label="面额描述"><el-input v-model="form.amountDesc" maxlength="50" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.couponType" style="width: 100%">
            <el-option v-for="item in couponTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="门槛金额"><el-input-number v-model="form.minAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="有效天数"><el-input-number v-model="form.validDays" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="2">禁用</el-radio>
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
.pcoupon {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pcoupon-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pcoupon-hero__badge {
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

.pcoupon-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pcoupon-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pcoupon-filter.nf-filter-card {
  margin-bottom: 0;
}

.pcoupon-filter__form {
  margin-bottom: 0;
}

.pcoupon-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pcoupon-table.nf-table-card {
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

.pcoupon-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>

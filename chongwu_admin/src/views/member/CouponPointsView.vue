<script setup>
import { computed, reactive, ref } from 'vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import {
  createCouponPointsApi,
  deleteCouponPointsApi,
  getCouponPointsPageApi,
  publishCouponPointsApi,
  updateCouponPointsApi,
} from '@/api/modules/coupon-points'
import BasePagination from '@/components/common/BasePagination.vue'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const activeTab = ref('first')

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const couponList = ref([])

const filters = reactive({
  status: '',
  couponName: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '开启', value: 1 },
  { label: '关闭', value: 2 },
]

const createDialogVisible = ref(false)
const createDialogLoading = ref(false)
const createUploadLoading = ref(false)

const createForm = reactive({
  couponName: '',
  integral: 0,
  sort: 0,
  status: 1,
  goodsImages: '',
})

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const editUploadLoading = ref(false)

const editForm = reactive({
  couponId: '',
  type: 2,
  couponName: '',
  integral: '',
  sort: '',
  status: 1,
  goodsImages: '',
  createTime: '',
})

const publishDialogVisible = ref(false)
const publishDialogLoading = ref(false)

const publishForm = reactive({
  couponId: '',
  couponName: '',
  issueNumber: '',
  status: 1,
  isLimit: 1,
  type: 2,
  goodsImages: '',
  integral: 0,
  validDay: 30,
  hour: '',
  couponType: 1,
  minMoney: 0,
  minHour: '',
})

function resetCreateForm() {
  Object.assign(createForm, {
    couponName: '',
    integral: 0,
    sort: 0,
    status: 1,
    goodsImages: '',
  })
}

function resetPublishForm() {
  Object.assign(publishForm, {
    couponId: '',
    couponName: '',
    issueNumber: '',
    status: 1,
    isLimit: 1,
    type: 2,
    goodsImages: '',
    integral: 0,
    validDay: 30,
    hour: '',
    couponType: 1,
    minMoney: 0,
    minHour: '',
  })
}

function normalizeRow(item = {}) {
  return {
    ...item,
    couponId: item.couponId,
    couponName: item.couponName || '',
    goodsImages: item.goodsImages || '',
    integral: item.integral ?? 0,
    sort: item.sort ?? 0,
    status: Number(item.status ?? 1),
    createTime: item.createTime || item.createdAt || '',
    type: Number(item.type || 2),
    couponType: Number(item.couponType || 1),
    lessMoney: item.lessMoney ?? 0,
    minMoney: item.minMoney ?? 0,
    validDay: item.validDay ?? 30,
    hour: item.hour || '',
    minHour: item.minHour || '',
  }
}

function statusText(value) {
  return Number(value) === 1 ? '开启' : '关闭'
}

async function loadData() {
  loading.value = true
  try {
    const data = await getCouponPointsPageApi({
      page: pageNum.value - 1,
      size: pageSize.value,
      type: 2,
      status: filters.status,
      couponName: filters.couponName.trim(),
    })
    couponList.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  pageNum.value = 1
  filters.status = ''
  filters.couponName = ''
  loadData()
}

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
}

async function handleCreateImageUpload(option) {
  createUploadLoading.value = true
  try {
    const data = await uploadMerchantImageApi(option.file)
    createForm.goodsImages = String(data?.url || data?.data || data?.src || data?.path || data || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    createUploadLoading.value = false
  }
}

async function submitCreate() {
  if (!createForm.couponName.trim()) return showError('请输入商品名称')
  if (!createForm.goodsImages) return showError('请上传图片')

  createDialogLoading.value = true
  try {
    await createCouponPointsApi({
      couponName: createForm.couponName.trim(),
      lessMoney: 0,
      minMoney: 0,
      integral: createForm.integral,
      sort: createForm.sort,
      status: createForm.status,
      type: 2,
      validDay: 30,
      hour: 4,
      goodsImages: createForm.goodsImages,
      couponType: 1,
      minHour: '',
      isLimitTime: 1,
      weeks: '',
      times: '',
    })
    showSuccess('操作成功')
    createDialogVisible.value = false
    await loadData()
  } finally {
    createDialogLoading.value = false
  }
}

function openEditDialog(row) {
  Object.assign(editForm, {
    couponId: row.couponId,
    type: row.type,
    couponName: row.couponName || '',
    integral: row.integral ?? '',
    sort: row.sort ?? '',
    status: row.status,
    goodsImages: row.goodsImages || '',
    createTime: row.createTime || '',
  })
  editDialogVisible.value = true
}

async function handleEditImageUpload(option) {
  editUploadLoading.value = true
  try {
    const data = await uploadMerchantImageApi(option.file)
    editForm.goodsImages = String(data?.url || data?.data || data?.src || data?.path || data || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    editUploadLoading.value = false
  }
}

async function submitEdit() {
  if (!editForm.goodsImages) return showError('请上传图片')

  editDialogLoading.value = true
  try {
    await updateCouponPointsApi({
      couponId: editForm.couponId,
      couponName: editForm.couponName,
      hour: '',
      lessMoney: '',
      minMoney: '',
      sort: editForm.sort,
      status: editForm.status,
      type: editForm.type,
      validDay: '',
      integral: editForm.integral,
      createTime: editForm.createTime,
      goodsImages: editForm.goodsImages,
      couponType: 1,
      minHour: '',
    })
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadData()
  } finally {
    editDialogLoading.value = false
  }
}

function openPublishDialog(row) {
  resetPublishForm()
  Object.assign(publishForm, {
    couponId: row.couponId,
    couponName: row.couponName || '',
    status: row.status,
    type: row.type,
    goodsImages: row.goodsImages || '',
    integral: row.integral ?? 0,
    validDay: row.validDay ?? 30,
    hour: row.hour || '',
    couponType: row.couponType || 1,
    minMoney: row.minMoney ?? 0,
    minHour: row.minHour || '',
  })
  publishDialogVisible.value = true
}

function handleIsLimitChange() {
  if (Number(publishForm.isLimit) === 2) {
    publishForm.issueNumber = 0
  }
}

async function submitPublish() {
  if (publishForm.issueNumber === '') return showError('请输入发布数量')

  publishDialogLoading.value = true
  try {
    await publishCouponPointsApi({
      couponId: publishForm.couponId,
      tearoomId: '',
      goodsIds: '',
      status: publishForm.status,
      type: publishForm.type,
      teamRule: '',
      buyRule: '',
      issueNumber: publishForm.issueNumber,
      remainNumber: publishForm.issueNumber,
      hour: publishForm.hour,
      couponName: publishForm.couponName,
      integral: publishForm.integral,
      goodsImages: publishForm.goodsImages,
      validDay: publishForm.validDay,
      isLimit: publishForm.isLimit,
      couponType: publishForm.couponType,
      minMoney: publishForm.minMoney,
      minHour: publishForm.minHour,
      isLimitTime: 2,
      weeks: '',
      times: '',
    })
    showSuccess('操作成功')
    publishDialogVisible.value = false
    await loadData()
  } finally {
    publishDialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确定删除此条信息?')
  await deleteCouponPointsApi(row.couponId)
  if (couponList.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1
  }
  showSuccess('删除成功')
  await loadData()
}

loadData()
</script>

<template>
  <div class="cp">
    <header class="cp-hero nf-page-hero">
      <span class="cp-hero__badge">会员管理</span>
      <h1 class="cp-hero__title">积分商品</h1>
      <p class="cp-hero__desc">管理积分兑换商品，支持按状态与名称筛选，可添加、修改、发布与删除</p>
    </header>

    <div class="cp-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="cp-filter__form" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="filters.couponName" clearable placeholder="请输入商品名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="cp-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :disabled="!isAuth('couponPoints:add')" @click="openCreateDialog">添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="cp-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="积分商品" name="first">
          <div class="cp-table nf-table-card">
            <el-table v-loading="loading" :data="couponList" border>
              <el-table-column prop="couponId" label="编号" width="80" fixed="left" />
              <el-table-column prop="couponName" label="商品名称" min-width="160" fixed="left" />
              <el-table-column label="图片" width="100">
                <template #default="{ row }">
                  <el-image v-if="row.goodsImages" :src="row.goodsImages" class="thumb-image" :preview-src-list="[row.goodsImages]" preview-teleported />
                </template>
              </el-table-column>
              <el-table-column prop="integral" label="兑换积分" width="100" />
              <el-table-column prop="sort" label="排序" width="100" />
              <el-table-column label="状态" width="80" fixed="right">
                <template #default="{ row }">
                  <span :class="row.status === 1 ? 'status-primary' : ''">{{ statusText(row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-space wrap>
                    <el-button size="small" type="primary" :disabled="!isAuth('coupon:issue')" @click="openPublishDialog(row)">发布</el-button>
                    <el-button size="small" type="primary" :disabled="!isAuth('couponPoints:update')" @click="openEditDialog(row)">修改</el-button>
                    <el-button size="small" type="danger" :disabled="!isAuth('couponPoints:delete')" @click="handleDelete(row)">删除</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>

            <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadData()" @update:pageSize="pageSize = $event; loadData()" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="createDialogVisible" title="添加商品" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">商品名称</span><el-input v-model="createForm.couponName" class="dialog-field" placeholder="请输入商品名称" /></div>
        <div class="form-row"><span class="form-label">兑换积分</span><el-input v-model="createForm.integral" class="dialog-field" type="number" placeholder="请输入兑换积分" /></div>
        <div class="form-row"><span class="form-label">排序</span><el-input v-model="createForm.sort" class="dialog-field" type="number" placeholder="请输入排序号" /></div>
        <div class="form-row"><span class="form-label">状态</span><el-radio-group v-model="createForm.status"><el-radio :value="1">开启</el-radio><el-radio :value="2">关闭</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">图片</span><div class="upload-row"><el-image v-if="createForm.goodsImages" :src="createForm.goodsImages" class="large-thumb" :preview-src-list="[createForm.goodsImages]" preview-teleported /><el-upload :show-file-list="false" :http-request="handleCreateImageUpload"><el-button :loading="createUploadLoading">上传</el-button></el-upload></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createDialogLoading" @click="submitCreate">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改商品" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">商品名称</span><el-input v-model="editForm.couponName" class="dialog-field" placeholder="请输入商品名称" /></div>
        <div class="form-row"><span class="form-label">兑换积分</span><el-input v-model="editForm.integral" class="dialog-field" placeholder="请输入兑换积分" /></div>
        <div class="form-row"><span class="form-label">排序</span><el-input v-model="editForm.sort" class="dialog-field" placeholder="请输入排序号" /></div>
        <div class="form-row"><span class="form-label">状态</span><el-radio-group v-model="editForm.status"><el-radio :value="1">开启</el-radio><el-radio :value="2">关闭</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">图片</span><div class="upload-row"><el-image v-if="editForm.goodsImages" :src="editForm.goodsImages" class="large-thumb" :preview-src-list="[editForm.goodsImages]" preview-teleported /><el-upload :show-file-list="false" :http-request="handleEditImageUpload"><el-button :loading="editUploadLoading">上传</el-button></el-upload></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitEdit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="publishDialogVisible" title="发布商品" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">商品ID</span><el-input :model-value="publishForm.couponId" class="dialog-field" disabled /></div>
        <div class="form-row"><span class="form-label">商品名称</span><el-input v-model="publishForm.couponName" class="dialog-field" /></div>
        <div class="form-row"><span class="form-label">是否限量</span><el-radio-group v-model="publishForm.isLimit" @change="handleIsLimitChange"><el-radio :value="1">限量</el-radio><el-radio :value="2">不限量</el-radio></el-radio-group></div>
        <div v-if="publishForm.isLimit === 1" class="form-row"><span class="form-label">发布数量</span><el-input v-model="publishForm.issueNumber" class="dialog-field" type="number" placeholder="请输入发布数量" /></div>
        <div class="form-row"><span class="form-label">状态</span><el-radio-group v-model="publishForm.status"><el-radio :value="1">开启</el-radio><el-radio :value="2">关闭</el-radio></el-radio-group></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="publishDialogLoading" @click="submitPublish">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.cp {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.cp-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cp-hero__badge {
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

.cp-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.cp-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.cp-filter.nf-filter-card {
  margin-bottom: 0;
}

.cp-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.cp-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.cp-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
    padding: 0 16px;
    background: #fff;
    border: 1px solid var(--nf-border);
    border-radius: 8px 8px 0 0;
    box-shadow: var(--nf-shadow-card);
  }

  :deep(.el-tabs__content) {
    overflow: visible;
  }

  :deep(.el-tab-pane) {
    padding-top: 0;
  }
}

.cp-table.nf-table-card {
  margin-bottom: 0;
  margin-top: 0;
  padding-bottom: 12px;
  border-radius: 0 0 8px 8px;
  border-top: none;
  box-shadow: var(--nf-shadow-card);

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

.thumb-image {
  width: 50px;
  height: 50px;
  border-radius: 8px;
}

.large-thumb {
  width: 148px;
  height: 148px;
  border-radius: 10px;
}

.status-primary {
  color: var(--nf-brand-deep);
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.form-label {
  width: 100px;
  line-height: 32px;
  text-align: right;
  color: var(--nf-text-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .cp-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }

  .form-row {
    flex-direction: column;
  }

  .form-label {
    width: auto;
    text-align: left;
    line-height: 1.5;
  }
}
</style>

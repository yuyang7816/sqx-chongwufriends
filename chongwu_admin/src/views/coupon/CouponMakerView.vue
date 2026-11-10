<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import {
  createCouponMakerApi,
  getCouponMakerGoodsOptionsApi,
  getCouponMakerPageApi,
  getCouponMakerTearoomOptionsApi,
  getCouponMakerVipCardOptionsApi,
  publishCouponMakerApi,
  updateCouponMakerApi,
} from '@/api/modules/coupon-maker'
import BasePagination from '@/components/common/BasePagination.vue'
import { showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const TAB_CONFIG = {
  first: { label: '积分兑换券', type: 2 },
  second: { label: '团购券', type: 1 },
  three: { label: '新人券', type: 3 },
  four: { label: '会员劵', type: 4 },
}

const activeTab = ref('first')
const currentType = computed(() => TAB_CONFIG[activeTab.value]?.type || 2)

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

const tearoomOptions = ref([])
const goodsOptions = ref([])
const vipCardOptions = ref([])

const createDialogVisible = ref(false)
const createDialogLoading = ref(false)
const createUploadLoading = ref(false)

const createForm = reactive({
  type: 2,
  couponName: '',
  couponType: 1,
  lessMoney: 0,
  minMoney: 0,
  integral: 0,
  validDay: 30,
  usedTime: 4,
  minHour: '',
  vipCardId: '',
  goodsImages: '',
  sort: 0,
  status: 1,
  isLimitTime: 1,
  weeks: [],
  times: [],
})

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const editUploadLoading = ref(false)

const editForm = reactive({
  couponId: '',
  type: 2,
  couponName: '',
  couponType: 1,
  lessMoney: '',
  minMoney: '',
  integral: '',
  validDay: '',
  usedTime: '',
  minHour: '',
  vipCardId: '',
  goodsImages: '',
  sort: '',
  status: 1,
  createTime: '',
})

const publishDialogVisible = ref(false)
const publishDialogLoading = ref(false)

const publishForm = reactive({
  couponId: '',
  couponName: '',
  storeId: '',
  goodsIds: [],
  issueNumber: '',
  isLimit: 1,
  isLimitTime: 1,
  status: 1,
  type: 2,
  couponType: 1,
  goodsImages: '',
  integral: 0,
  validDay: 30,
  hour: '',
  minMoney: 0,
  vipCardId: '',
  minHour: '',
  teamRule: '',
  buyRule: '',
  weeks: [],
  times: [],
  startTime: '',
  endTime: '',
})

function typeText(value) {
  if (Number(value) === 1) return '团购券'
  if (Number(value) === 2) return '积分兑换券'
  if (Number(value) === 3) return '新人券'
  if (Number(value) === 4) return '会员劵'
  return '-'
}

function couponTypeText(value) {
  if (Number(value) === 1) return '优惠券'
  if (Number(value) === 2) return '实物'
  if (Number(value) === 3) return '定时券'
  if (Number(value) === 4) return '周期卡'
  if (Number(value) === 5) return '充值卡'
  return '-'
}

function statusText(value) {
  return Number(value) === 1 ? '开启' : '关闭'
}

function resetCreateForm() {
  Object.assign(createForm, {
    type: currentType.value,
    couponName: '',
    couponType: 1,
    lessMoney: 0,
    minMoney: 0,
    integral: 0,
    validDay: 30,
    usedTime: 4,
    minHour: '',
    vipCardId: '',
    goodsImages: '',
    sort: 0,
    status: 1,
    isLimitTime: 1,
    weeks: [],
    times: [],
  })
}

function resetPublishForm() {
  Object.assign(publishForm, {
    couponId: '',
    couponName: '',
    storeId: '',
    goodsIds: [],
    issueNumber: '',
    isLimit: 1,
    isLimitTime: 1,
    status: 1,
    type: currentType.value,
    couponType: 1,
    goodsImages: '',
    integral: 0,
    validDay: 30,
    hour: '',
    minMoney: 0,
    vipCardId: '',
    minHour: '',
    teamRule: '',
    buyRule: '',
    weeks: [],
    times: [],
    startTime: '',
    endTime: '',
  })
  goodsOptions.value = []
}

function normalizeRow(item = {}) {
  return {
    ...item,
    couponId: item.couponId,
    type: Number(item.type || 0),
    couponType: Number(item.couponType || 1),
    couponName: item.couponName || '',
    goodsImages: item.goodsImages || '',
    integral: item.integral ?? 0,
    lessMoney: item.lessMoney ?? 0,
    minMoney: item.minMoney ?? 0,
    hour: item.hour || '',
    minHour: item.minHour || '',
    validDay: item.validDay ?? 0,
    sort: item.sort ?? 0,
    status: Number(item.status ?? 1),
    createTime: item.createTime || item.createdAt || '',
    vipCardId: item.vipCardId || '',
  }
}

async function loadCouponList() {
  loading.value = true
  try {
    const data = await getCouponMakerPageApi({
      page: pageNum.value - 1,
      size: pageSize.value,
      type: currentType.value,
      status: filters.status,
      couponName: filters.couponName.trim(),
    })
    couponList.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadTearoomOptions() {
  tearoomOptions.value = await getCouponMakerTearoomOptionsApi()
}

async function loadVipCardOptions() {
  vipCardOptions.value = await getCouponMakerVipCardOptionsApi()
}

async function loadGoodsOptions() {
  if (!publishForm.storeId) {
    goodsOptions.value = []
    return
  }
  goodsOptions.value = await getCouponMakerGoodsOptionsApi({
    page: 1,
    limit: 100,
    goodsName: '',
    status: '',
    tearoomId: publishForm.storeId,
  })
}

function handleSearch() {
  pageNum.value = 1
  loadCouponList()
}

function handleReset() {
  pageNum.value = 1
  filters.status = ''
  filters.couponName = ''
  loadCouponList()
}

watch(activeTab, () => {
  pageNum.value = 1
  pageSize.value = 10
  loadCouponList()
})

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
  loadVipCardOptions()
}

function handleCreateTypeChange() {
  createForm.couponType = 1
  createForm.vipCardId = ''
  createForm.usedTime = 4
  createForm.minHour = ''
}

function handleCreateCouponTypeChange() {
  createForm.usedTime = ''
  createForm.minHour = ''
  createForm.vipCardId = ''
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
  if (!createForm.couponName.trim()) return showError('请输入优惠券名称')
  if (!createForm.goodsImages) return showError('请上传图片')
  if (Number(createForm.couponType) === 1 && (createForm.usedTime === '' || createForm.usedTime == null)) {
    return showError('请输入使用时长')
  }

  createDialogLoading.value = true
  try {
    await createCouponMakerApi({
      couponName: createForm.couponName.trim(),
      lessMoney: createForm.lessMoney,
      minMoney: createForm.minMoney,
      integral: createForm.integral,
      sort: createForm.sort,
      status: createForm.status,
      type: createForm.type,
      validDay: createForm.validDay,
      hour: createForm.usedTime,
      goodsImages: createForm.goodsImages,
      couponType: createForm.couponType,
      minHour: createForm.minHour,
      isLimitTime: createForm.isLimitTime,
      weeks: createForm.weeks.toString(),
      times: createForm.times.toString(),
      vipCardId: createForm.vipCardId,
    })
    showSuccess('操作成功')
    createDialogVisible.value = false
    await loadCouponList()
  } finally {
    createDialogLoading.value = false
  }
}

function openEditDialog(row) {
  Object.assign(editForm, {
    couponId: row.couponId,
    type: row.type,
    couponName: row.couponName || '',
    couponType: row.couponType,
    lessMoney: row.lessMoney ?? '',
    minMoney: row.minMoney ?? '',
    integral: row.integral ?? '',
    validDay: row.validDay ?? '',
    usedTime: row.hour || '',
    minHour: row.minHour || '',
    vipCardId: row.vipCardId || '',
    goodsImages: row.goodsImages || '',
    sort: row.sort ?? '',
    status: row.status,
    createTime: row.createTime || '',
  })
  editDialogVisible.value = true
  loadVipCardOptions()
}

function handleEditCouponTypeChange() {
  editForm.usedTime = ''
  editForm.minHour = ''
  editForm.vipCardId = ''
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
  if (Number(editForm.couponType) === 1 && (editForm.usedTime === '' || editForm.usedTime == null)) {
    return showError('请输入使用时长')
  }

  editDialogLoading.value = true
  try {
    await updateCouponMakerApi({
      couponId: editForm.couponId,
      couponName: editForm.couponName,
      hour: editForm.usedTime,
      lessMoney: editForm.lessMoney,
      minMoney: editForm.minMoney,
      sort: editForm.sort,
      status: editForm.status,
      type: editForm.type,
      validDay: editForm.validDay,
      integral: editForm.integral,
      createTime: editForm.createTime,
      goodsImages: editForm.goodsImages,
      couponType: editForm.couponType,
      minHour: editForm.minHour,
      vipCardId: editForm.vipCardId,
    })
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadCouponList()
  } finally {
    editDialogLoading.value = false
  }
}

async function openPublishDialog(row) {
  resetPublishForm()
  Object.assign(publishForm, {
    couponId: row.couponId,
    couponName: row.couponName || '',
    status: row.status,
    type: row.type,
    couponType: row.couponType,
    goodsImages: row.goodsImages || '',
    integral: row.integral ?? 0,
    validDay: row.validDay ?? 30,
    hour: row.hour || '',
    minMoney: row.minMoney ?? 0,
    vipCardId: row.vipCardId || '',
    minHour: row.minHour || '',
    isLimitTime: row.couponType === 3 ? 1 : 1,
  })
  publishDialogVisible.value = true
  await loadTearoomOptions()
}

function handlePublishLimitChange() {
  if (Number(publishForm.isLimit) === 2) {
    publishForm.issueNumber = 0
  }
}

function addPublishTime() {
  if (!publishForm.startTime) return showError('请选择开始时间')
  if (!publishForm.endTime) return showError('请选择结束时间')
  publishForm.times.push(`${publishForm.startTime}-${publishForm.endTime}`)
  publishForm.startTime = ''
  publishForm.endTime = ''
}

function removePublishTime(index) {
  publishForm.times.splice(index, 1)
}

async function submitPublish() {
  if (publishForm.issueNumber === '') return showError('请输入发布数量')
  if (Number(publishForm.isLimitTime) === 1) {
    if (!publishForm.weeks.length) return showError('请选择限制周期')
    if (!publishForm.times.length) return showError('请添加使用时间')
  } else {
    publishForm.weeks = []
    publishForm.times = []
  }
  if (!publishForm.teamRule.trim()) return showError('请输入使用规则')
  if (!publishForm.buyRule.trim()) return showError('请输入购买须知')

  publishDialogLoading.value = true
  try {
    await publishCouponMakerApi({
      couponId: publishForm.couponId,
      tearoomId: publishForm.storeId,
      goodsIds: publishForm.goodsIds.toString(),
      status: publishForm.status,
      type: publishForm.type,
      teamRule: publishForm.teamRule,
      buyRule: publishForm.buyRule,
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
      vipCardId: publishForm.vipCardId,
      minHour: publishForm.minHour,
      isLimitTime: publishForm.isLimitTime,
      weeks: publishForm.weeks.toString(),
      times: publishForm.times.toString(),
    })
    showSuccess('操作成功')
    publishDialogVisible.value = false
    await loadCouponList()
  } finally {
    publishDialogLoading.value = false
  }
}

function showIntegralColumn(row) {
  return Number(row.type) === 2 ? row.integral : '-'
}

loadCouponList()
</script>

<template>
  <div class="cm">
    <header class="cm-hero nf-page-hero">
      <span class="cm-hero__badge">优惠券</span>
      <h1 class="cm-hero__title">优惠券制作</h1>
      <p class="cm-hero__desc">管理积分兑换券、团购券、新人券与会员券模板，支持筛选、添加、修改与发布</p>
    </header>

    <div class="cm-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="cm-filter__form" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠券名称">
          <el-input v-model="filters.couponName" clearable placeholder="请输入优惠券名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="cm-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :disabled="!isAuth('coupon:add')" @click="openCreateDialog">添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="cm-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane v-for="(tab, key) in TAB_CONFIG" :key="key" :label="tab.label" :name="key">
          <div class="cm-table nf-table-card">
            <el-table v-loading="loading" :data="couponList" border>
              <el-table-column prop="couponId" label="编号" width="80" fixed="left" />
              <el-table-column label="类型" width="100"><template #default="{ row }">{{ typeText(row.type) }}</template></el-table-column>
              <el-table-column label="券类型" width="100" fixed="left"><template #default="{ row }">{{ couponTypeText(row.couponType) }}</template></el-table-column>
              <el-table-column prop="couponName" label="名称" width="150" fixed="left" />
              <el-table-column label="图片" width="100"><template #default="{ row }"><el-image v-if="row.goodsImages" :src="row.goodsImages" class="thumb-image" :preview-src-list="[row.goodsImages]" preview-teleported /></template></el-table-column>
              <el-table-column prop="integral" label="兑换积分"><template #default="{ row }">{{ showIntegralColumn(row) }}</template></el-table-column>
              <el-table-column prop="lessMoney" label="现价" />
              <el-table-column prop="minMoney" label="原价" />
              <el-table-column prop="hour" label="使用时长(半小时)"><template #default="{ row }">{{ Number(row.couponType) === 1 ? row.hour : '-' }}</template></el-table-column>
              <el-table-column prop="validDay" label="有效期限(天)" />
              <el-table-column prop="sort" label="排序" />
              <el-table-column label="状态" width="80" fixed="right"><template #default="{ row }"><span :class="row.status === 1 ? 'status-primary' : ''">{{ statusText(row.status) }}</span></template></el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="150" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-space wrap>
                    <el-button size="small" type="primary" :disabled="!isAuth('coupon:issue')" @click="openPublishDialog(row)">发布</el-button>
                    <el-button size="small" type="primary" :disabled="!isAuth('coupon:update')" @click="openEditDialog(row)">修改</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>

            <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadCouponList()" @update:pageSize="pageSize = $event; loadCouponList()" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="createDialogVisible" title="添加优惠券" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">类型</span><el-radio-group v-model="createForm.type" @change="handleCreateTypeChange"><el-radio :value="1">团购券</el-radio><el-radio :value="2">积分兑换券</el-radio><el-radio :value="3">新人券</el-radio><el-radio :value="4">会员劵</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">券类型</span><el-radio-group v-model="createForm.couponType" @change="handleCreateCouponTypeChange"><el-radio :value="1">优惠券</el-radio><el-radio v-if="createForm.type !== 3" :value="2">实物</el-radio><el-radio :value="3">定时券</el-radio><el-radio v-if="createForm.type === 1" :value="4">周期卡</el-radio><el-radio v-if="createForm.type === 1" :value="5">充值卡</el-radio></el-radio-group></div>
        <div v-if="createForm.couponType === 4" class="form-row"><span class="form-label">选择周期卡</span><el-select v-model="createForm.vipCardId" class="dialog-field" placeholder="请选择周期卡"><el-option v-for="item in vipCardOptions" :key="item.vipCardId" :label="item.cardName" :value="item.vipCardId" /></el-select></div>
        <div class="form-row"><span class="form-label">名称</span><el-input v-model="createForm.couponName" class="dialog-field" placeholder="请输入名称" /></div>
        <div class="form-row"><span class="form-label">现价/面值</span><el-input v-model="createForm.lessMoney" class="dialog-field" type="number" placeholder="请输入现价/面值" /></div>
        <div v-if="createForm.couponType === 1" class="form-row"><span class="form-label">原价/最低消费金额</span><el-input v-model="createForm.minMoney" class="dialog-field" type="number" placeholder="请输入原价/最低消费金额" /></div>
        <div v-if="createForm.type === 2" class="form-row"><span class="form-label">兑换积分</span><el-input v-model="createForm.integral" class="dialog-field" type="number" placeholder="请输入兑换积分" /></div>
        <div v-if="createForm.couponType === 1" class="form-row"><span class="form-label">使用时长(半小时)</span><el-input v-model="createForm.usedTime" class="dialog-field" type="number" placeholder="请输入使用时长(半小时)" /></div>
        <div class="form-row"><span class="form-label">有效期限(天)</span><el-input v-model="createForm.validDay" class="dialog-field" type="number" placeholder="请输入有效期限(天)" /></div>
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

    <el-dialog v-model="editDialogVisible" title="修改优惠券" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">类型</span><el-radio-group v-model="editForm.type"><el-radio :value="1">团购券</el-radio><el-radio :value="2">积分兑换券</el-radio><el-radio :value="3">新人券</el-radio><el-radio :value="4">会员劵</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">券类型</span><el-radio-group v-model="editForm.couponType" @change="handleEditCouponTypeChange"><el-radio :value="1">优惠券</el-radio><el-radio v-if="editForm.type !== 3" :value="2">实物</el-radio><el-radio :value="3">定时券</el-radio><el-radio v-if="editForm.type === 1" :value="4">周期卡</el-radio><el-radio v-if="editForm.type === 1" :value="5">充值卡</el-radio></el-radio-group></div>
        <div v-if="editForm.couponType === 4" class="form-row"><span class="form-label">选择周期卡</span><el-select v-model="editForm.vipCardId" class="dialog-field" placeholder="请选择周期卡"><el-option v-for="item in vipCardOptions" :key="item.vipCardId" :label="item.cardName" :value="item.vipCardId" /></el-select></div>
        <div class="form-row"><span class="form-label">名称</span><el-input v-model="editForm.couponName" class="dialog-field" placeholder="请输入名称" /></div>
        <div class="form-row"><span class="form-label">现价/面值</span><el-input v-model="editForm.lessMoney" class="dialog-field" placeholder="请输入现价/面值" /></div>
        <div v-if="editForm.couponType === 1" class="form-row"><span class="form-label">原价/最低消费金额</span><el-input v-model="editForm.minMoney" class="dialog-field" placeholder="请输入原价/最低消费金额" /></div>
        <div v-if="editForm.type === 2" class="form-row"><span class="form-label">兑换积分</span><el-input v-model="editForm.integral" class="dialog-field" placeholder="请输入兑换积分" /></div>
        <div v-if="editForm.couponType === 1" class="form-row"><span class="form-label">使用时长(半小时)</span><el-input v-model="editForm.usedTime" class="dialog-field" placeholder="请输入使用时长(半小时)" /></div>
        <div class="form-row"><span class="form-label">有效期限(天)</span><el-input v-model="editForm.validDay" class="dialog-field" placeholder="请输入有效期限(天)" /></div>
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

    <el-dialog v-model="publishDialogVisible" title="发布优惠券" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">选择羽毛球馆</span><el-select v-model="publishForm.storeId" class="dialog-field" placeholder="请选择羽毛球馆" @change="loadGoodsOptions"><el-option v-for="item in tearoomOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" /></el-select></div>
        <div v-if="publishForm.couponType === 1 || publishForm.couponType === 3" class="form-row form-row-top"><span class="form-label">选择羽毛球</span><el-checkbox-group v-model="publishForm.goodsIds" class="checkbox-wrap"><el-checkbox v-for="item in goodsOptions" :key="item.goodsId" :label="item.goodsId">{{ item.goodsName }}</el-checkbox></el-checkbox-group></div>
        <div class="form-row"><span class="form-label">优惠券ID</span><el-input :model-value="publishForm.couponId" class="dialog-field" disabled /></div>
        <div class="form-row"><span class="form-label">优惠券名称</span><el-input v-model="publishForm.couponName" class="dialog-field" /></div>
        <div class="form-row"><span class="form-label">是否限量</span><el-radio-group v-model="publishForm.isLimit" @change="handlePublishLimitChange"><el-radio :value="1">限量</el-radio><el-radio :value="2">不限量</el-radio></el-radio-group></div>
        <div v-if="publishForm.isLimit === 1" class="form-row"><span class="form-label">发布数量</span><el-input v-model="publishForm.issueNumber" class="dialog-field" type="number" placeholder="请输入发布数量" /></div>
        <div v-if="publishForm.couponType !== 3" class="form-row"><span class="form-label">是否限制时间</span><el-radio-group v-model="publishForm.isLimitTime"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></div>
        <div v-if="publishForm.isLimitTime === 1" class="form-row form-row-top"><span class="form-label">限制周期(可使用)</span><el-checkbox-group v-model="publishForm.weeks" class="checkbox-wrap"><el-checkbox label="1">周一</el-checkbox><el-checkbox label="2">周二</el-checkbox><el-checkbox label="3">周三</el-checkbox><el-checkbox label="4">周四</el-checkbox><el-checkbox label="5">周五</el-checkbox><el-checkbox label="6">周六</el-checkbox><el-checkbox label="7">周日</el-checkbox></el-checkbox-group></div>
        <div v-if="publishForm.isLimitTime === 1" class="form-row form-row-top"><span class="form-label">使用时间</span><div class="dialog-field"><div class="time-tags"><span v-for="(item, index) in publishForm.times" :key="`${item}-${index}`" class="time-tag">{{ item }}<el-button link type="danger" @click="removePublishTime(index)">删</el-button></span></div><div v-if="publishForm.times.length === 0" class="time-picker-row"><el-time-picker v-model="publishForm.startTime" style="width: 150px" placeholder="起始时间" format="HH:mm" value-format="HH:mm" /><el-time-picker v-model="publishForm.endTime" style="width: 150px" placeholder="结束时间" format="HH:mm" value-format="HH:mm" /><el-button type="primary" size="small" circle @click="addPublishTime">+</el-button></div></div></div>
        <div class="form-row"><span class="form-label">状态</span><el-radio-group v-model="publishForm.status"><el-radio :value="1">开启</el-radio><el-radio :value="2">关闭</el-radio></el-radio-group></div>
        <div class="form-row form-row-top"><span class="form-label">使用规则</span><el-input v-model="publishForm.teamRule" class="dialog-field" type="textarea" :rows="3" placeholder="请输入使用规则" /></div>
        <div class="form-row form-row-top"><span class="form-label">购买须知</span><el-input v-model="publishForm.buyRule" class="dialog-field" type="textarea" :rows="3" placeholder="请输入购买须知" /></div>
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
.cm {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.cm-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cm-hero__badge {
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

.cm-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.cm-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.cm-filter.nf-filter-card {
  margin-bottom: 0;
}

.cm-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.cm-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.cm-tabs {
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

.cm-table.nf-table-card {
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
  align-items: center;
  gap: 12px;
}

.form-row-top {
  align-items: flex-start;
}

.form-label {
  width: 140px;
  text-align: right;
  color: var(--el-text-color-secondary);
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

.checkbox-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.time-picker-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.time-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.time-tag {
  padding: 4px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .cm-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }

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

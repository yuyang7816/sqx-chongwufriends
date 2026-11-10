<script setup>
import { computed, ref, reactive } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import BasePagination from '@/components/common/BasePagination.vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import {
  createCoachServiceApi,
  createCoachServiceSkuApi,
  deleteCoachServiceApi,
  deleteCoachServiceSkuApi,
  getCoachServicePageApi,
  getCoachServiceSkuPageApi,
  getCoachServiceUnitOptionsApi,
  getCoachServiceVenueOptionsApi,
  updateCoachServiceApi,
  updateCoachServiceSkuApi,
  updateCoachServiceStatusApi,
} from '@/api/modules/coach-service'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const serviceList = ref([])

const venueOptions = ref([])
const unitOptions = ref([])

const filters = reactive({
  tearoomId: '',
  serviceName: '',
  status: '',
})

const statusOptions = [
  { id: '', title: '全部' },
  { id: 1, title: '正常' },
  { id: 2, title: '关闭' },
]

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const editDialogTitle = computed(() => (serviceForm.helpServiceId ? '修改服务' : '添加服务'))
const uploadLoadingField = ref('')

const serviceForm = reactive({
  helpServiceId: '',
  tearoomId: '',
  coverImg: '',
  titleImg: [],
  detailsImg: [],
  serviceName: '',
  content: '',
  price: '',
  unit: '',
  labels: [],
  labelInput: '',
  status: 1,
  sort: 1,
  details: '',
  videoUrlArr: [],
  purchaseLimitation: 1,
  purchaseLimitationDay: '',
})

const videoUploading = ref(false)
const videoPreviewVisible = ref(false)
const previewVideos = ref([])

const skuDialogVisible = ref(false)
const skuLoading = ref(false)
const skuPage = ref(1)
const skuSize = ref(10)
const skuTotal = ref(0)
const skuList = ref([])
const currentServiceId = ref('')

const skuEditDialogVisible = ref(false)
const skuEditLoading = ref(false)
const skuEditTitle = computed(() => (skuForm.helpServiceSkuId ? '修改' : '添加'))
const skuForm = reactive({
  helpServiceSkuId: '',
  skuName: '',
  money: '',
  sort: '',
})

function splitImages(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function formatMoney(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toFixed(2) : '--'
}

function resetServiceForm() {
  Object.assign(serviceForm, {
    helpServiceId: '',
    tearoomId: '',
    coverImg: '',
    titleImg: [],
    detailsImg: [],
    serviceName: '',
    content: '',
    price: '',
    unit: '',
    labels: [],
    labelInput: '',
    status: 1,
    sort: 1,
    details: '',
    videoUrlArr: [],
    purchaseLimitation: 1,
    purchaseLimitationDay: '',
  })
}

async function loadServices() {
  loading.value = true
  try {
    const data = await getCoachServicePageApi({
      page: pageNum.value,
      limit: pageSize.value,
      status: filters.status,
      serviceName: filters.serviceName.trim(),
      tearoomId: filters.tearoomId,
    })
    serviceList.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadBaseOptions() {
  const [venues, units] = await Promise.all([
    getCoachServiceVenueOptionsApi(),
    getCoachServiceUnitOptionsApi(),
  ])
  venueOptions.value = venues
  unitOptions.value = units
}

function handleSearch() {
  pageNum.value = 1
  loadServices()
}

function handleReset() {
  filters.status = ''
  filters.serviceName = ''
  filters.tearoomId = ''
  pageNum.value = 1
  loadServices()
}

function addLabel() {
  const value = serviceForm.labelInput.trim()
  if (!value) return
  serviceForm.labels.push(value)
  serviceForm.labelInput = ''
}

async function handleImageUpload(field, option, target = 'single') {
  uploadLoadingField.value = field
  try {
    const data = await uploadMerchantImageApi(option.file)
    const url = String(data?.url || data?.data || data?.src || data?.path || data || '')
    if (target === 'single') {
      serviceForm[field] = url
    } else {
      serviceForm[field].push(url)
    }
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    uploadLoadingField.value = ''
  }
}

function beforeVideoUpload(file) {
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (ext !== 'mp4') {
    showError('上传文件只能是mp4格式')
    return false
  }
  videoUploading.value = true
  return true
}

async function handleVideoUpload(option) {
  videoUploading.value = true
  try {
    const data = await uploadMerchantImageApi(option.file)
    const url = String(data?.url || data?.data || data?.src || data?.path || data || '')
    serviceForm.videoUrlArr.push(url)
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    videoUploading.value = false
  }
}

function openCreateDialog() {
  resetServiceForm()
  editDialogVisible.value = true
}

function openEditDialog(row) {
  resetServiceForm()
  Object.assign(serviceForm, {
    helpServiceId: row.helpServiceId,
    tearoomId: row.tearoomId || '',
    coverImg: row.coverImg || '',
    titleImg: splitImages(row.titleImg),
    detailsImg: splitImages(row.detailsImg),
    serviceName: row.serviceName || '',
    content: row.content || '',
    price: row.price ?? '',
    unit: row.unit || '',
    labels: row.labels ? String(row.labels).split(',').filter(Boolean) : [],
    status: Number(row.status || 1),
    sort: row.sort ?? 1,
    details: row.details || '',
    videoUrlArr: splitImages(row.videoUrl),
    purchaseLimitation: Number(row.purchaseLimitation || 1),
    purchaseLimitationDay: row.purchaseLimitationDay || '',
  })
  editDialogVisible.value = true
}

async function submitService() {
  if (!serviceForm.tearoomId) return showError('请选择羽毛球馆')
  if (!serviceForm.serviceName.trim()) return showError('请输入教练名称')
  if (!serviceForm.coverImg) return showError('请上传封面图')
  if (!serviceForm.titleImg.length) return showError('请上传轮播图')
  if (!serviceForm.detailsImg.length) return showError('请上传详情图')
  if (serviceForm.price === '' || serviceForm.price === null) return showError('请输入价格')
  if (!serviceForm.content.trim()) return showError('请输入副标题')
  if (!serviceForm.unit) return showError('请选择单位')
  if (serviceForm.sort === '' || serviceForm.sort === null) return showError('请输入排序')
  if (Number(serviceForm.purchaseLimitation) === 1 && !serviceForm.purchaseLimitationDay) return showError('请输入限购周期')

  editDialogLoading.value = true
  try {
    const payload = {
      helpServiceId: serviceForm.helpServiceId || undefined,
      coverImg: serviceForm.coverImg,
      titleImg: serviceForm.titleImg.join(','),
      detailsImg: serviceForm.detailsImg.join(','),
      serviceName: serviceForm.serviceName.trim(),
      oldPrice: '',
      price: serviceForm.price,
      memberPrice: '',
      unit: serviceForm.unit,
      labels: serviceForm.labels.join(','),
      oneFixedClassifyId: '',
      twoFixedClassifyId: '',
      status: serviceForm.status,
      sort: serviceForm.sort,
      isRecommend: 1,
      city: '',
      content: serviceForm.content.trim(),
      purchaseLimitation: serviceForm.purchaseLimitation,
      promotionRate: 0,
      purchaseLimitationDay: serviceForm.purchaseLimitationDay,
      masterRate: 0,
      tearoomId: serviceForm.tearoomId,
      details: serviceForm.details,
      videoUrl: serviceForm.videoUrlArr.join(','),
    }
    if (serviceForm.helpServiceId) {
      await updateCoachServiceApi(payload)
    } else {
      delete payload.helpServiceId
      await createCoachServiceApi(payload)
    }
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadServices()
  } finally {
    editDialogLoading.value = false
  }
}

async function removeService(row) {
  await confirmAction('确定删除此条信息?')
  await deleteCoachServiceApi(row.helpServiceId)
  showSuccess('删除成功')
  await loadServices()
}

async function toggleService(row) {
  try {
    await updateCoachServiceStatusApi({
      helpServiceId: row.helpServiceId,
      coverImg: row.coverImg,
      titleImg: row.titleImg,
      detailsImg: row.detailsImg,
      serviceName: row.serviceName,
      oldPrice: row.oldPrice,
      price: row.price,
      memberPrice: row.memberPrice,
      unit: row.unit,
      labels: row.labels,
      oneFixedClassifyId: row.oneFixedClassifyId,
      twoFixedClassifyId: row.twoFixedClassifyId,
      status: row.status,
      sort: row.sort,
      isRecommend: row.isRecommend,
      city: row.city,
      content: row.content,
      purchaseLimitation: row.purchaseLimitation,
      promotionRate: row.promotionRate,
      purchaseLimitationDay: row.purchaseLimitationDay,
      masterRate: row.masterRate,
      details: row.details,
      videoUrl: row.videoUrl,
    })
    showSuccess('操作成功')
  } catch (error) {
    row.status = Number(row.status) === 1 ? 2 : 1
    throw error
  } finally {
    loadServices()
  }
}

async function togglePurchaseLimitation(row) {
  try {
    await updateCoachServiceStatusApi({
      helpServiceId: row.helpServiceId,
      coverImg: row.coverImg,
      titleImg: row.titleImg,
      detailsImg: row.detailsImg,
      serviceName: row.serviceName,
      oldPrice: row.oldPrice,
      price: row.price,
      memberPrice: row.memberPrice,
      unit: row.unit,
      labels: row.labels,
      oneFixedClassifyId: row.oneFixedClassifyId,
      twoFixedClassifyId: row.twoFixedClassifyId,
      status: row.status,
      sort: row.sort,
      isRecommend: row.isRecommend,
      city: row.city,
      content: row.content,
      purchaseLimitation: row.purchaseLimitation,
      promotionRate: row.promotionRate,
      purchaseLimitationDay: row.purchaseLimitationDay,
      masterRate: row.masterRate,
      details: row.details,
      videoUrl: row.videoUrl,
    })
    showSuccess('操作成功')
  } catch (error) {
    row.purchaseLimitation = Number(row.purchaseLimitation) === 1 ? 2 : 1
    throw error
  } finally {
    loadServices()
  }
}

function openVideoPreview(url) {
  previewVideos.value = splitImages(url)
  videoPreviewVisible.value = true
}

async function openSkuDialog(row) {
  currentServiceId.value = row.helpServiceId
  skuPage.value = 1
  skuDialogVisible.value = true
  await loadSkuList()
}

async function loadSkuList() {
  skuLoading.value = true
  try {
    const data = await getCoachServiceSkuPageApi({
      page: skuPage.value,
      limit: skuSize.value,
      helpServiceId: currentServiceId.value,
    })
    skuList.value = data.list || []
    skuTotal.value = data.total || 0
  } finally {
    skuLoading.value = false
  }
}

function openSkuEditDialog(row) {
  Object.assign(skuForm, {
    helpServiceSkuId: row?.helpServiceSkuId || '',
    skuName: row?.skuName || '',
    money: row?.money || '',
    sort: row?.sort || '',
  })
  skuEditDialogVisible.value = true
}

async function submitSku() {
  if (!skuForm.skuName.trim()) return showError('请输入规格名称')
  if (skuForm.money === '' || skuForm.money === null) return showError('请输入价格')
  skuEditLoading.value = true
  try {
    const payload = {
      helpServiceSkuId: skuForm.helpServiceSkuId || undefined,
      helpServiceId: currentServiceId.value,
      money: skuForm.money,
      skuName: skuForm.skuName.trim(),
      sort: skuForm.sort,
    }
    if (skuForm.helpServiceSkuId) {
      await updateCoachServiceSkuApi(payload)
    } else {
      delete payload.helpServiceSkuId
      await createCoachServiceSkuApi(payload)
    }
    showSuccess('操作成功')
    skuEditDialogVisible.value = false
    await loadSkuList()
  } finally {
    skuEditLoading.value = false
  }
}

async function removeSku(row) {
  await confirmAction('确定删除此条信息?')
  await deleteCoachServiceSkuApi(row.helpServiceSkuId)
  showSuccess('删除成功')
  await loadSkuList()
}

loadBaseOptions()
loadServices()
</script>

<template>
  <div class="coach-svc">
    <header class="coach-svc-hero nf-page-hero">
      <span class="coach-svc-hero__badge">教练管理</span>
      <h1 class="coach-svc-hero__title">教练服务</h1>
      <p class="coach-svc-hero__desc">管理教练服务信息，支持筛选查询、添加服务与规格维护</p>
    </header>

    <div class="coach-svc-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="coach-svc-filter__form" @submit.prevent>
        <el-form-item label="羽毛球馆">
          <el-select v-model="filters.tearoomId" placeholder="请选择羽毛球馆" clearable @change="handleSearch">
            <el-option v-for="item in venueOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练名称">
          <el-input v-model="filters.serviceName" placeholder="请输入教练名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item class="coach-svc-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :disabled="!isAuth('localityJlfw:add')" @click="openCreateDialog">添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="coach-svc-table nf-table-card">
      <el-table v-loading="loading" :data="serviceList" border height="600">
        <el-table-column prop="helpServiceId" label="编号" width="80" fixed="left" />
        <el-table-column prop="tearoomName" label="羽毛球馆名称" min-width="150" fixed="left" />
        <el-table-column prop="serviceName" label="教练名称" min-width="150" fixed="left" />
        <el-table-column prop="content" label="副标题" min-width="160" />
        <el-table-column label="封面图" width="100">
          <template #default="{ row }">
            <el-image v-if="row.coverImg" :src="row.coverImg" class="thumb-image" :preview-src-list="[row.coverImg]" preview-teleported />
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column label="轮播图" width="180">
          <template #default="{ row }">
            <div v-if="splitImages(row.titleImg).length" class="image-list-cell">
              <el-image v-for="item in splitImages(row.titleImg)" :key="item" :src="item" :preview-src-list="splitImages(row.titleImg)" fit="cover" class="thumb-image" preview-teleported />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="详情图" width="180">
          <template #default="{ row }">
            <div v-if="splitImages(row.detailsImg).length" class="image-list-cell">
              <el-image v-for="item in splitImages(row.detailsImg)" :key="item" :src="item" :preview-src-list="splitImages(row.detailsImg)" fit="cover" class="thumb-image" preview-teleported />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="details" label="详情介绍" min-width="280">
          <template #default="{ row }">
            <el-popover placement="top-start" trigger="hover" width="320">
              <template #reference>
                <div class="ellipsis-text html-preview" v-html="row.details || '-'" />
              </template>
              <div class="html-content" v-html="row.details || '-'" />
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="视频" width="100">
          <template #default="{ row }">
            <el-button v-if="row.videoUrl" size="small" type="primary" @click="openVideoPreview(row.videoUrl)">查看视频</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column label="是否限购" width="120">
          <template #default="{ row }">
            <div class="switch-stack">
              <span :class="Number(row.purchaseLimitation) === 1 ? 'text-success' : 'text-danger'">{{ Number(row.purchaseLimitation) === 1 ? '是' : '否' }}</span>
              <el-switch v-model="row.purchaseLimitation" :disabled="!isAuth('localityJlfw:update')" :active-value="1" :inactive-value="2" @change="togglePurchaseLimitation(row)" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="限购周期(单位：天)" width="150">
          <template #default="{ row }">{{ Number(row.purchaseLimitation) === 1 ? row.purchaseLimitationDay : '-' }}</template>
        </el-table-column>
        <el-table-column prop="labels" label="标签" min-width="120" />
        <el-table-column prop="unit" label="单位" width="90" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="状态" width="100" fixed="right">
          <template #default="{ row }">
            <el-switch v-model="row.status" :disabled="!isAuth('localityJlfw:update')" :active-value="1" :inactive-value="2" @change="toggleService(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" plain @click="openSkuDialog(row)">规格列表</el-button>
              <el-button size="small" type="primary" :disabled="!isAuth('localityJlfw:update')" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('localityJlfw:delete')" @click="removeService(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 40]" :total="total" @update:currentPage="pageNum = $event; loadServices()" @update:pageSize="pageSize = $event; pageNum = 1; loadServices()" />
    </div>

    <el-dialog v-model="editDialogVisible" :title="editDialogTitle" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row">
          <span class="form-label">羽毛球馆</span>
          <el-select v-model="serviceForm.tearoomId" class="dialog-field" placeholder="请选择羽毛球馆">
            <el-option v-for="item in venueOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" />
          </el-select>
        </div>
        <div class="form-row"><span class="form-label">教练名称</span><el-input v-model="serviceForm.serviceName" class="dialog-field" placeholder="请输入教练名称" /></div>
        <div class="form-row"><span class="form-label">副标题</span><el-input v-model="serviceForm.content" class="dialog-field" placeholder="请输入副标题" /></div>
        <div class="form-row"><span class="form-label">封面图</span><div class="upload-row"><el-image v-if="serviceForm.coverImg" :src="serviceForm.coverImg" class="large-thumb" :preview-src-list="[serviceForm.coverImg]" preview-teleported /><el-upload :show-file-list="false" :http-request="(option) => handleImageUpload('coverImg', option)"><el-button :loading="uploadLoadingField === 'coverImg'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">轮播图</span><div class="upload-list"><div v-for="(item, index) in serviceForm.titleImg" :key="`${item}-${index}`" class="image-item"><el-image :src="item" class="large-thumb" :preview-src-list="serviceForm.titleImg" preview-teleported /><el-button class="remove-btn" circle size="small" @click="serviceForm.titleImg.splice(index, 1)">x</el-button></div><el-upload :show-file-list="false" :http-request="(option) => handleImageUpload('titleImg', option, 'array')"><el-button :loading="uploadLoadingField === 'titleImg'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">详情图</span><div class="upload-list"><div v-for="(item, index) in serviceForm.detailsImg" :key="`${item}-${index}`" class="image-item"><el-image :src="item" class="large-thumb" :preview-src-list="serviceForm.detailsImg" preview-teleported /><el-button class="remove-btn" circle size="small" @click="serviceForm.detailsImg.splice(index, 1)">x</el-button></div><el-upload :show-file-list="false" :http-request="(option) => handleImageUpload('detailsImg', option, 'array')"><el-button :loading="uploadLoadingField === 'detailsImg'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">价格</span><el-input v-model="serviceForm.price" type="number" min="0" class="dialog-field" placeholder="请输入价格" /></div>
        <div class="form-row"><span class="form-label">标签</span><div class="label-row"><el-tag v-for="(item, index) in serviceForm.labels" :key="`${item}-${index}`" closable @close="serviceForm.labels.splice(index, 1)">{{ item }}</el-tag><el-input v-model="serviceForm.labelInput" class="small-input" placeholder="请输入标签" @keyup.enter="addLabel" /><el-button size="small" type="primary" @click="addLabel">添加</el-button></div></div>
        <div class="form-row"><span class="form-label">单位</span><el-radio-group v-model="serviceForm.unit"><el-radio v-for="item in unitOptions" :key="item" :value="item">{{ item }}</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">视频</span><div class="video-upload-box"><div v-if="serviceForm.videoUrlArr.length" class="video-link-list"><div v-for="(item, index) in serviceForm.videoUrlArr" :key="`${item}-${index}`" class="video-link-item"><span class="break-all">{{ item }}</span><el-button link type="danger" @click="serviceForm.videoUrlArr.splice(index, 1)">删除</el-button></div></div><el-upload :show-file-list="false" :before-upload="beforeVideoUpload" :http-request="handleVideoUpload"><el-button size="small" type="primary" :loading="videoUploading">点击上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">是否限购</span><el-radio-group v-model="serviceForm.purchaseLimitation"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></div>
        <div v-if="Number(serviceForm.purchaseLimitation) === 1" class="form-row"><span class="form-label">限购周期(单位：天)</span><el-input v-model="serviceForm.purchaseLimitationDay" type="number" min="0" class="dialog-field" placeholder="请输入限购周期" /></div>
        <div class="form-row"><span class="form-label">状态</span><el-radio-group v-model="serviceForm.status"><el-radio :value="1">正常</el-radio><el-radio :value="2">关闭</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">排序</span><el-input v-model="serviceForm.sort" type="number" min="0" class="dialog-field" placeholder="请输入排序" /></div>
        <div class="form-row editor-row"><span class="form-label">详情介绍</span><div class="editor-wrap"><QuillEditor v-model:content="serviceForm.details" content-type="html" theme="snow" class="editor" /></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitService">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="skuDialogVisible" title="规格列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="dialog-toolbar align-right">
        <el-space>
          <el-button size="small" type="primary" plain @click="skuPage = 1; loadSkuList()">刷新</el-button>
          <el-button size="small" type="primary" :disabled="!isAuth('localityJlfw:add')" @click="openSkuEditDialog()">添加</el-button>
        </el-space>
      </div>
      <el-table v-loading="skuLoading" :data="skuList" border>
        <el-table-column prop="helpServiceSkuId" label="编号" width="80" />
        <el-table-column prop="skuName" label="规格名称" min-width="160" />
        <el-table-column prop="money" label="价格" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" :disabled="!isAuth('localityJlfw:update')" @click="openSkuEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('localityJlfw:delete')" @click="removeSku(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination :current-page="skuPage" :page-size="skuSize" :page-sizes="[10, 20, 30, 40]" :total="skuTotal" @update:currentPage="skuPage = $event; loadSkuList()" @update:pageSize="skuSize = $event; skuPage = 1; loadSkuList()" />
    </el-dialog>

    <el-dialog v-model="skuEditDialogVisible" :title="skuEditTitle" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">规格名称</span><el-input v-model="skuForm.skuName" class="dialog-field" placeholder="请输入规格名称" /></div>
        <div class="form-row"><span class="form-label">价格</span><el-input v-model="skuForm.money" class="dialog-field" placeholder="请输入价格" /></div>
        <div class="form-row"><span class="form-label">排序</span><el-input v-model="skuForm.sort" type="number" min="0" class="dialog-field" placeholder="请输入排序" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="skuEditDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="skuEditLoading" @click="submitSku">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="videoPreviewVisible" title="视频" width="600px" destroy-on-close class="nf-pro-dialog" align-center>
<div v-if="previewVideos.length" class="video-preview-list">
        <video v-for="(item, index) in previewVideos" :key="`${item}-${index}`" :src="item" controls class="video-player"></video>
      </div>
      <span v-else>暂无视频</span>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.coach-svc {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.coach-svc-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.coach-svc-hero__badge {
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

.coach-svc-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.coach-svc-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.coach-svc-filter.nf-filter-card {
  margin-bottom: 0;
}

.coach-svc-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.coach-svc-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.coach-svc-table.nf-table-card {
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

.thumb-image {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: block;
}

.large-thumb {
  width: 148px;
  height: 148px;
  border-radius: 10px;
}

.image-list-cell,
.upload-list,
.label-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
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
  width: 120px;
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
  line-height: 32px;
  text-align: right;
}

.dialog-field,
.editor,
.editor-wrap {
  flex: 1;
}

.editor-wrap {
  width: 100%;
}

.small-input {
  width: 180px;
}

.image-item {
  position: relative;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
}

.video-upload-box,
.video-link-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.video-link-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.break-all,
.html-content :deep(p),
.html-preview :deep(p) {
  word-break: break-all;
}

.ellipsis-text {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.html-content :deep(img),
.html-preview :deep(img) {
  max-width: 100%;
}

.switch-stack {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.text-success {
  color: var(--el-color-success);
}

.text-danger {
  color: var(--el-color-danger);
}

.align-right {
  text-align: right;
}

.dialog-toolbar {
  margin-bottom: 12px;
}

.video-preview-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.video-player {
  width: 100%;
}

:deep(.editor-wrap .ql-toolbar.ql-snow) {
  border-radius: 8px 8px 0 0;
}

:deep(.editor-wrap .ql-container.ql-snow) {
  min-height: 300px;
  border-radius: 0 0 8px 8px;
}

:deep(.editor-wrap .ql-editor) {
  min-height: 300px;
  font-size: 14px;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .coach-svc-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }

  .form-row,
  .video-link-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-label {
    width: auto;
    text-align: left;
  }

  .small-input {
    width: 100%;
  }
}
</style>

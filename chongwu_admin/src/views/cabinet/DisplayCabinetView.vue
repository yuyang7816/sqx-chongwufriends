<script setup>
import { computed, ref, reactive } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getApiBaseUrl, isAuth } from '@/utils/tool'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import {
  createDisplayCabinetApi,
  deleteDisplayCabinetApi,
  getDisplayCabinetPageApi,
  getSmallGoodsPageApi,
  getVenueSimplePageApi,
  updateDisplayCabinetApi,
} from '@/api/modules/display-cabinet'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const cabinetList = ref([])

const filters = reactive({
  tearoomName: '',
  name: '',
  isEmpty: '',
  title: '',
})

const isEmptyOptions = [
  { value: '', label: '全部' },
  { value: 1, label: '有' },
  { value: 2, label: '无' },
]

const goodsOptions = ref([])

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const dialogTitle = computed(() => (form.id ? '修改' : '添加'))
const form = reactive({
  id: '',
  tearoomId: '',
  tearoomName: '',
  name: '',
  serveCode: '',
  deviceId: '',
  roomSelect: '',
  isEmpty: '',
  smallGoodsId: '',
  instructCode: '',
})

const qrDialogVisible = ref(false)
const qrImageUrl = ref('')

const venueDialogVisible = ref(false)
const venueLoading = ref(false)
const venuePage = ref(1)
const venueSize = ref(10)
const venueTotal = ref(0)
const venueList = ref([])
const venueKeyword = ref('')

function buildAddress(row) {
  return [row.tearoomProvince, row.tearoomCity, row.tearoomDistrict, row.tearoomAddress].filter(Boolean).join('')
}

function splitImages(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

async function loadCabinets() {
  loading.value = true
  try {
    const data = await getDisplayCabinetPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      isEmpty: filters.isEmpty,
      name: filters.name.trim(),
      title: filters.title.trim(),
      tearoomName: filters.tearoomName.trim(),
    })
    cabinetList.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadGoodsOptions() {
  const data = await getSmallGoodsPageApi({ page: -1, limit: 1000 })
  goodsOptions.value = data.list || []
}

function handleSearch() {
  pageNum.value = 1
  loadCabinets()
}

function handleReset() {
  filters.tearoomName = ''
  filters.name = ''
  filters.isEmpty = ''
  filters.title = ''
  pageNum.value = 1
  loadCabinets()
}

function openCreateDialog() {
  Object.assign(form, {
    id: '',
    tearoomId: '',
    tearoomName: '',
    name: '',
    serveCode: '',
    deviceId: '',
    roomSelect: '',
    isEmpty: '',
    smallGoodsId: '',
    instructCode: '',
  })
  loadGoodsOptions()
  editDialogVisible.value = true
}

function openEditDialog(row) {
  Object.assign(form, {
    id: row.id,
    tearoomId: row.tearoomId || '',
    tearoomName: row.tearoomName || '',
    name: row.name || '',
    serveCode: row.serveCode || '',
    deviceId: row.deviceId || '',
    roomSelect: row.roomSelect || '',
    isEmpty: row.isEmpty || '',
    smallGoodsId: row.smallGoodsId || '',
    instructCode: row.instructCode || '',
  })
  loadGoodsOptions()
  editDialogVisible.value = true
}

async function submitCabinet() {
  if (!form.tearoomId) return showError('请选择羽毛球馆')
  if (!form.name.trim()) return showError('请输入名称')
  if (form.isEmpty === '') return showError('请选择是否有货物')
  if (!form.smallGoodsId) return showError('请选择商品')

  editDialogLoading.value = true
  try {
    const payload = {
      id: form.id || undefined,
      name: form.name.trim(),
      deviceId: form.deviceId,
      roomSelect: form.roomSelect,
      serveCode: form.serveCode,
      isEmpty: form.isEmpty,
      smallGoodsId: form.smallGoodsId,
      tearoomId: form.tearoomId,
      instructCode: form.instructCode,
    }
    if (form.id) {
      await updateDisplayCabinetApi(payload)
    } else {
      delete payload.id
      await createDisplayCabinetApi(payload)
    }
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadCabinets()
  } finally {
    editDialogLoading.value = false
  }
}

async function removeCabinet(row) {
  await confirmAction('确定删除此数据?')
  await deleteDisplayCabinetApi(row.id)
  showSuccess('删除成功')
  await loadCabinets()
}

function openQrDialog(row) {
  qrImageUrl.value = `${getApiBaseUrl()}invite/mpCreateQr?relation=${row.id}&page=box/cabinet/cabinet`
  qrDialogVisible.value = true
}

function openQrWindow() {
  if (!qrImageUrl.value) return
  window.open(qrImageUrl.value, '_blank')
}

async function loadVenues() {
  venueLoading.value = true
  try {
    const data = await getVenueSimplePageApi({
      page: venuePage.value,
      limit: venueSize.value,
      tearoomName: venueKeyword.value.trim(),
      city: '',
      status: '',
    })
    venueList.value = data.list || []
    venueTotal.value = data.total || 0
  } finally {
    venueLoading.value = false
  }
}

function openVenueDialog() {
  venuePage.value = 1
  venueKeyword.value = ''
  venueDialogVisible.value = true
  loadVenues()
}

function chooseVenue(row) {
  form.tearoomId = row.tearoomId
  form.tearoomName = row.tearoomName || ''
  venueDialogVisible.value = false
}

loadCabinets()
loadGoodsOptions()
</script>

<template>
  <div class="cab">
    <header class="cab-hero nf-page-hero">
      <span class="cab-hero__badge">展示柜</span>
      <h1 class="cab-hero__title">展示柜管理</h1>
      <p class="cab-hero__desc">管理各羽毛球馆展示柜设备，支持筛选查询、添加修改与二维码查看</p>
    </header>

    <div class="cab-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="cab-filter__form" @submit.prevent>
        <el-form-item label="羽毛球馆名称">
          <el-input v-model="filters.tearoomName" clearable placeholder="请输入羽毛球馆名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="filters.name" clearable placeholder="请输入名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="是否有货物">
          <el-select v-model="filters.isEmpty" clearable placeholder="请选择是否有货物" @change="handleSearch">
            <el-option v-for="item in isEmptyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="filters.title" clearable placeholder="请输入商品名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="cab-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :disabled="!isAuth('displayCabinet:add')" @click="openCreateDialog">添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="cab-table nf-table-card">
      <el-table v-loading="loading" :data="cabinetList" border>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="tearoomName" label="羽毛球馆名称" min-width="160" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="title" label="商品名称" min-width="160">
          <template #default="{ row }">{{ row.title || '-' }}</template>
        </el-table-column>
        <el-table-column prop="serveCode" label="服务编号" min-width="140" />
        <el-table-column prop="deviceId" label="设备编号" min-width="140" />
        <el-table-column prop="roomSelect" label="箱门编号" min-width="120" />
        <el-table-column label="是否有货物" width="100">
          <template #default="{ row }">{{ row.isEmpty === 1 ? '有' : '无' }}</template>
        </el-table-column>
        <el-table-column label="二维码" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openQrDialog(row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" :disabled="!isAuth('displayCabinet:update')" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('displayCabinet:delete')" @click="removeCabinet(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadCabinets()" @update:pageSize="pageSize = $event; pageNum = 1; loadCabinets()" />
    </div>

    <el-dialog v-model="editDialogVisible" :title="dialogTitle" width="680px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">羽毛球馆</span><el-input v-model="form.tearoomName" class="dialog-field" placeholder="请选择羽毛球馆" readonly @click="openVenueDialog" /></div>
        <div class="form-row"><span class="form-label">名称</span><el-input v-model="form.name" class="dialog-field" placeholder="请输入名称" /></div>
        <div class="form-row"><span class="form-label">服务编号</span><el-input v-model="form.serveCode" class="dialog-field" placeholder="请输入服务编号" /></div>
        <div class="form-row"><span class="form-label">设备编号</span><el-input v-model="form.deviceId" class="dialog-field" placeholder="请输入设备编号" /></div>
        <div class="form-row"><span class="form-label">箱门编号</span><el-input v-model="form.roomSelect" class="dialog-field" placeholder="请输入箱门编号" /></div>
        <div class="form-row"><span class="form-label">是否有货物</span><el-radio-group v-model="form.isEmpty"><el-radio :value="1">有</el-radio><el-radio :value="2">无</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">商品</span><el-select v-model="form.smallGoodsId" filterable class="dialog-field"><el-option v-for="item in goodsOptions" :key="item.id" :label="item.title" :value="item.id" /></el-select></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitCabinet">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="qrDialogVisible" title="二维码" width="520px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="qr-box">
        <img :src="qrImageUrl" class="qr-image" />
        <el-button type="primary" @click="openQrWindow">打开窗口查看</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="venueDialogVisible" title="羽毛球馆列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="羽毛球馆名称">
          <el-input v-model="venueKeyword" clearable placeholder="请输入羽毛球馆名称" @keyup.enter="venuePage = 1; loadVenues()" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="venuePage = 1; loadVenues()">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="venueLoading" :data="venueList" border>
        <el-table-column prop="tearoomId" label="编号" width="80" fixed="left" />
        <el-table-column prop="tearoomName" label="羽毛球馆名称" min-width="140" fixed="left" />
        <el-table-column label="羽毛球馆logo" width="100">
          <template #default="{ row }">
            <el-image v-if="row.tearoomTitleImage" :src="row.tearoomTitleImage" class="thumb-image" :preview-src-list="splitImages(row.tearoomTitleImage)" preview-teleported />
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column label="羽毛球馆轮播图" width="180">
          <template #default="{ row }">
            <div v-if="splitImages(row.tearoomCoverImage).length" class="image-list-cell">
              <el-image v-for="item in splitImages(row.tearoomCoverImage)" :key="item" :src="item" :preview-src-list="splitImages(row.tearoomCoverImage)" fit="cover" class="thumb-image" preview-teleported />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="羽毛球馆地址" min-width="240">
          <template #default="{ row }">
            <div>{{ buildAddress(row) }}</div>
            <div>经度：{{ row.tearoomLongitude || '未绑定' }}</div>
            <div>纬度：{{ row.tearoomLatitude || '未绑定' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="营业时间" width="120">
          <template #default="{ row }">{{ row.startTime }}--{{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="score" label="羽毛球馆评分" width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button size="small" type="primary" @click="chooseVenue(row)">选择</el-button></template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="venuePage" :page-size="venueSize" :page-sizes="[10, 20, 30, 50, 100]" :total="venueTotal" @update:currentPage="venuePage = $event; loadVenues()" @update:pageSize="venueSize = $event; venuePage = 1; loadVenues()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.cab {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.cab-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cab-hero__badge {
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

.cab-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.cab-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.cab-filter.nf-filter-card {
  margin-bottom: 0;
}

.cab-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.cab-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.cab-table.nf-table-card {
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
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
}

.thumb-image {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: block;
}

.image-list-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.qr-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.qr-image {
  width: 50%;
}

@media (max-width: 768px) {
  .cab-filter__form {
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
  }

  .qr-image {
    width: 100%;
  }
}
</style>

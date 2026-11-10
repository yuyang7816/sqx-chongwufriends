<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  bindVenueGateDeviceApi,
  createBoundRoomApi,
  deleteBoundRoomApi,
  getBoundRoomPageApi,
  getRoomGroupListApi,
  getVenueDevicePageApi,
  getVenueMonitorListApi,
  toggleVenueRoomsApi,
  updateBoundRoomApi,
} from '@/api/modules/merchant-device'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const filters = reactive({
  tearoomName: '',
  userName: '',
})

const viewDialogVisible = ref(false)
const currentVenue = ref({})

const bindGateDialogVisible = ref(false)
const bindGateLoading = ref(false)
const bindGateForm = reactive({
  tearoomId: 0,
  innerCode: '',
  roomNo: '',
  roomName: '',
})
const groupOptions = ref([])
const groupDeviceOptions = ref([])

const monitorDialogVisible = ref(false)
const monitorUrl = ref('')

const toggleDialogVisible = ref(false)
const toggleLoading = ref(false)
const toggleForm = reactive({
  tearoomId: 0,
  type: 1,
})

const roomListDialogVisible = ref(false)
const roomListLoading = ref(false)
const roomList = ref([])
const currentRoomVenue = ref({ tearoomId: 0, tearoomName: '' })

const roomEditDialogVisible = ref(false)
const roomEditLoading = ref(false)
const roomEditMode = ref('create')
const roomForm = reactive({
  tearoomId: 0,
  roomId: 0,
  roomType: 1,
  roomName: '',
  roomNo: '',
  deviceId: '',
  serveCode: '',
  isOpen: 2,
})

const roomTypeOptions = [
  { label: '设备', value: 1 },
  { label: '温度设备', value: 8 },
]

const roomTypeTextMap = {
  1: '设备',
  8: '温度设备',
}

const currentVenueSummary = computed(() => ({
  name: currentVenue.value.tearoomName || '-',
  logo: currentVenue.value.tearoomTitleImage || '',
  address: [currentVenue.value.tearoomProvince, currentVenue.value.tearoomCity, currentVenue.value.tearoomDistrict, currentVenue.value.tearoomAddress].filter(Boolean).join('') || '-',
  owner: currentVenue.value.userName || '-',
  phone: currentVenue.value.phone || currentVenue.value.tearoomPhone || '-',
  businessHours: currentVenue.value.startTime && currentVenue.value.endTime ? `${currentVenue.value.startTime}-${currentVenue.value.endTime}` : '-',
}))

function splitImages(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function buildAddress(row) {
  return [row.tearoomProvince, row.tearoomCity, row.tearoomDistrict, row.tearoomAddress].filter(Boolean).join('')
}

function filterGroupDevices(devices = []) {
  return devices.filter((item) => item.vmsDeviceTypeCh !== 'DTU' && item.vmsDeviceTypeCh !== '核心控制器')
}

async function loadData() {
  loading.value = true
  try {
    const data = await getVenueDevicePageApi({
      page: pageNum.value,
      limit: pageSize.value,
      tearoomName: filters.tearoomName.trim(),
      userName: filters.userName.trim(),
      city: '',
      status: '',
    })
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

function handleReset() {
  filters.tearoomName = ''
  filters.userName = ''
  pageNum.value = 1
  loadData()
}

function openVenueView(row) {
  currentVenue.value = { ...row }
  viewDialogVisible.value = true
}

async function loadAllGroups() {
  groupOptions.value = await getRoomGroupListApi({ innerCode: '' })
}

async function loadGroupDevices(innerCode) {
  groupDeviceOptions.value = []
  if (!innerCode) return
  const result = await getRoomGroupListApi({ innerCode })
  const first = result[0]
  groupDeviceOptions.value = filterGroupDevices(first?.devices || [])
}

async function openBindGateDialog(row) {
  bindGateForm.tearoomId = Number(row.tearoomId || 0)
  bindGateForm.innerCode = row.innerCode || ''
  bindGateForm.roomNo = row.roomNo || ''
  bindGateForm.roomName = row.roomName || ''
  await loadAllGroups()
  await loadGroupDevices(bindGateForm.innerCode)
  bindGateDialogVisible.value = true
}

async function handleBindGroupChange(value) {
  bindGateForm.roomNo = ''
  bindGateForm.roomName = ''
  await loadGroupDevices(value)
}

function handleBindDeviceChange(value) {
  const target = groupDeviceOptions.value.find((item) => item.vmsDeviceNo === value)
  bindGateForm.roomName = target?.vmsDeviceTypeCh || ''
}

async function submitBindGate() {
  if (!bindGateForm.innerCode) return showError('请选择设备组')
  if (!bindGateForm.roomNo) return showError('请选择设备')
  bindGateLoading.value = true
  try {
    await bindVenueGateDeviceApi({
      tearoomId: bindGateForm.tearoomId,
      innerCode: bindGateForm.innerCode,
      roomNo: bindGateForm.roomNo,
      roomName: bindGateForm.roomName,
    })
    showSuccess('操作成功')
    bindGateDialogVisible.value = false
    await loadData()
  } finally {
    bindGateLoading.value = false
  }
}

async function openMonitorDialog(row) {
  monitorUrl.value = ''
  const list = await getVenueMonitorListApi({ innerCode: row.equipment_sn || row.innerCode || '' }).catch(() => [])
  monitorUrl.value = list?.[0]?.vmscameras?.[0]?.pcAddress || ''
  monitorDialogVisible.value = true
}

function openToggleDialog(row) {
  toggleForm.tearoomId = Number(row.tearoomId || 0)
  toggleForm.type = 1
  toggleDialogVisible.value = true
}

async function submitToggle() {
  toggleLoading.value = true
  try {
    await toggleVenueRoomsApi({ tearoomId: toggleForm.tearoomId, type: toggleForm.type })
    showSuccess('操作成功')
    toggleDialogVisible.value = false
    await loadData()
  } finally {
    toggleLoading.value = false
  }
}

function openDeviceItemsPage(row) {
  router.push({
    path: `/merchants/${row.tearoomId}/device-items`,
    query: {
      storeName: row.tearoomName || '',
      equipmentSn: row.innerCode || '',
    },
  })
}

async function openRoomListDialog(row) {
  currentRoomVenue.value = { tearoomId: Number(row.tearoomId || 0), tearoomName: row.tearoomName || '' }
  roomListDialogVisible.value = true
  await loadRoomList()
}

async function loadRoomList() {
  roomListLoading.value = true
  try {
    const data = await getBoundRoomPageApi({
      page: 1,
      limit: 20,
      roomName: '',
      tearoomId: currentRoomVenue.value.tearoomId,
    })
    roomList.value = data.list || []
  } finally {
    roomListLoading.value = false
  }
}

function openCreateRoomDialog() {
  Object.assign(roomForm, {
    tearoomId: currentRoomVenue.value.tearoomId,
    roomId: 0,
    roomType: 1,
    roomName: '',
    roomNo: '',
    deviceId: '',
    serveCode: '',
    isOpen: 2,
  })
  roomEditMode.value = 'create'
  roomEditDialogVisible.value = true
}

function openEditRoomDialog(row) {
  Object.assign(roomForm, {
    tearoomId: row.tearoomId,
    roomId: row.roomId,
    roomType: Number(row.roomType || 1),
    roomName: row.roomName || '',
    roomNo: row.roomNo || '',
    deviceId: row.deviceId || '',
    serveCode: row.serveCode || '',
    isOpen: Number(row.isOpen || 2),
  })
  roomEditMode.value = 'edit'
  roomEditDialogVisible.value = true
}

async function submitRoom() {
  if (Number(roomForm.roomType) === 8) {
    if (!roomForm.deviceId) return showError('请输入设备ID')
    if (!roomForm.serveCode) return showError('请输入服务编号')
  } else {
    if (!roomForm.roomName) return showError('请输入设备名称')
    if (!roomForm.roomNo) return showError('请输入设备编号')
  }
  roomEditLoading.value = true
  try {
    const payload = {
      tearoomId: roomForm.tearoomId,
      roomId: roomEditMode.value === 'edit' ? roomForm.roomId : undefined,
      roomType: roomForm.roomType,
      roomName: roomForm.roomName,
      roomNo: roomForm.roomNo,
      isOpen: roomForm.isOpen,
      deviceId: roomForm.deviceId,
      serveCode: roomForm.serveCode,
    }
    if (roomEditMode.value === 'edit') {
      await updateBoundRoomApi(payload)
    } else {
      delete payload.roomId
      await createBoundRoomApi(payload)
    }
    showSuccess('操作成功')
    roomEditDialogVisible.value = false
    await loadRoomList()
  } finally {
    roomEditLoading.value = false
  }
}

async function removeRoom(row) {
  await confirmAction('确定要删除该设备?')
  await deleteBoundRoomApi(row.roomId)
  showSuccess('操作成功')
  await loadRoomList()
}

async function toggleRoomOpen(row) {
  try {
    await updateBoundRoomApi({
      tearoomId: row.tearoomId,
      roomId: row.roomId,
      roomType: row.roomType,
      roomName: row.roomName,
      roomNo: row.roomNo,
      isOpen: row.isOpen,
      deviceId: row.deviceId,
      serveCode: row.serveCode,
    })
    showSuccess('操作成功')
  } catch (error) {
    row.isOpen = Number(row.isOpen) === 1 ? 2 : 1
    throw error
  } finally {
    loadRoomList()
  }
}

loadData()
</script>

<template>
  <div class="mdv">
    <header class="mdv-hero nf-page-hero">
      <span class="mdv-hero__badge">羽毛球馆</span>
      <h1 class="mdv-hero__title">设备列表</h1>
      <p class="mdv-hero__desc">管理场馆设备绑定、监控与开关操作，支持按店铺名称与商户昵称筛选</p>
    </header>

    <div class="mdv-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="mdv-filter__form" @submit.prevent>
        <el-form-item label="店铺名称">
          <el-input v-model="filters.tearoomName" clearable placeholder="请输入店铺名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="商户昵称">
          <el-input v-model="filters.userName" clearable placeholder="请输入商户昵称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="mdv-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="mdv-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="tearoomId" label="编号" width="80" fixed="left" />
        <el-table-column prop="tearoomName" label="场馆名称" width="140" fixed="left" />
        <el-table-column label="场馆 logo" width="100">
          <template #default="{ row }">
            <span v-if="!row.tearoomTitleImage">暂无图片</span>
            <el-image v-else :src="row.tearoomTitleImage" :preview-src-list="splitImages(row.tearoomTitleImage)" fit="cover" class="thumb-image" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column label="场馆轮播图" width="180">
          <template #default="{ row }">
            <div v-if="splitImages(row.tearoomCoverImage).length" class="image-list-cell">
              <el-image v-for="item in splitImages(row.tearoomCoverImage)" :key="item" :src="item" :preview-src-list="splitImages(row.tearoomCoverImage)" fit="cover" class="thumb-image mini-thumb" preview-teleported />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="场馆地址" min-width="240">
          <template #default="{ row }">
            <div>{{ buildAddress(row) }}</div>
            <div>经度：{{ row.tearoomLongitude || '未绑定' }}</div>
            <div>纬度：{{ row.tearoomLatitude || '未绑定' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="营业时间" width="120">
          <template #default="{ row }">{{ row.startTime }}--{{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="innerCode" label="设备组编号" width="120" />
        <el-table-column prop="roomNo" label="设备编号" width="120" />
        <el-table-column prop="roomName" label="设备" width="120" />
        <el-table-column label="绑定设备" width="130">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openBindGateDialog(row)">绑定大门设备</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" @click="openVenueView(row)">查看场馆</el-button>
              <el-button size="small" type="primary" @click="openDeviceItemsPage(row)">场馆设备列表</el-button>
              <el-button size="small" type="primary" @click="openMonitorDialog(row)">场馆监控</el-button>
              <el-button size="small" type="warning" plain @click="openToggleDialog(row)">一键开关</el-button>
              <el-button size="small" type="primary" plain :disabled="!isAuth('merchantList:update')" @click="openRoomListDialog(row)">设备列表</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadData()" @update:pageSize="pageSize = $event; pageNum = 1; loadData()" />
    </div>

    <el-dialog v-model="viewDialogVisible" title="查看场馆" width="720px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="detail-grid">
        <div class="detail-item"><span class="detail-label">场馆名称</span><span class="detail-value">{{ currentVenueSummary.name }}</span></div>
        <div class="detail-item detail-item-wide"><span class="detail-label">场馆图片</span><el-image v-if="currentVenueSummary.logo" :src="currentVenueSummary.logo" class="large-thumb" :preview-src-list="[currentVenueSummary.logo]" preview-teleported /><span v-else class="detail-value">暂无图片</span></div>
        <div class="detail-item detail-item-wide"><span class="detail-label">场馆地址</span><span class="detail-value">{{ currentVenueSummary.address }}</span></div>
        <div class="detail-item"><span class="detail-label">场馆主办人</span><span class="detail-value">{{ currentVenueSummary.owner }}</span></div>
        <div class="detail-item"><span class="detail-label">场馆手机号</span><span class="detail-value">{{ currentVenueSummary.phone }}</span></div>
        <div class="detail-item"><span class="detail-label">开店时间</span><span class="detail-value">{{ currentVenueSummary.businessHours }}</span></div>
      </div>
    </el-dialog>

    <el-dialog v-model="bindGateDialogVisible" title="修改设备" width="640px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row">
          <span class="form-label">设备组</span>
          <el-select v-model="bindGateForm.innerCode" placeholder="请选择设备组" class="dialog-field" @change="handleBindGroupChange">
            <el-option v-for="item in groupOptions" :key="item.innerCode" :label="item.vmsGroupName" :value="item.innerCode" />
          </el-select>
        </div>
        <div class="form-row">
          <span class="form-label">设备</span>
          <el-select v-model="bindGateForm.roomNo" placeholder="请选择设备" class="dialog-field" @change="handleBindDeviceChange">
            <el-option v-for="item in groupDeviceOptions" :key="item.vmsDeviceNo" :label="item.vmsDeviceTypeCh" :value="item.vmsDeviceNo" />
          </el-select>
        </div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="bindGateDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="bindGateLoading" @click="submitBindGate">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="monitorDialogVisible" title="场馆监控" width="1000px" destroy-on-close class="nf-pro-dialog" align-center>
<iframe v-if="monitorUrl" :src="monitorUrl" class="monitor-frame"></iframe>
      <div v-else class="empty-monitor">暂无数据</div>
      <template #footer>
        <el-button @click="monitorDialogVisible = false">关闭弹框</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="toggleDialogVisible" title="一键开关设备" width="640px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-row">
        <span class="form-label">修改状态</span>
        <el-radio-group v-model="toggleForm.type">
          <el-radio :value="1">开启</el-radio>
          <el-radio :value="2">关闭</el-radio>
        </el-radio-group>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="toggleDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="toggleLoading" @click="submitToggle">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="roomListDialogVisible" title="设备清单" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="dialog-toolbar">
        <el-space>
          <el-button type="primary" size="small" plain @click="loadRoomList">刷新</el-button>
          <el-button type="primary" size="small" :disabled="!isAuth('devices:add')" @click="openCreateRoomDialog">添加</el-button>
        </el-space>
      </div>
      <el-table v-loading="roomListLoading" :data="roomList" border>
        <el-table-column prop="roomId" label="编号" width="80" />
        <el-table-column label="设备类型" width="120">
          <template #default="{ row }">{{ roomTypeTextMap[row.roomType] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="roomName" label="设备名称" min-width="160" />
        <el-table-column prop="roomNo" label="设备编号" min-width="160" />
        <el-table-column prop="deviceId" label="设备ID" min-width="140" />
        <el-table-column prop="serveCode" label="服务编号" min-width="140" />
        <el-table-column label="是否开启" width="120">
          <template #default="{ row }">
            <el-switch v-model="row.isOpen" :active-value="1" :inactive-value="2" :disabled="!isAuth('merchantList:update')" @change="toggleRoomOpen(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" :disabled="!isAuth('devices:update')" @click="openEditRoomDialog(row)">修改</el-button>
              <el-button size="small" type="warning" :disabled="!isAuth('devices:update')" @click="removeRoom(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="roomEditDialogVisible" :title="roomEditMode === 'edit' ? '修改设备' : '添加设备'" width="640px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row">
          <span class="form-label">设备类型</span>
          <el-radio-group v-model="roomForm.roomType">
            <el-radio v-for="item in roomTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </div>
        <div v-if="roomForm.roomType !== 8" class="form-row">
          <span class="form-label">设备名称</span>
          <el-input v-model="roomForm.roomName" class="dialog-field" placeholder="请输入设备名称" />
        </div>
        <div v-if="roomForm.roomType !== 8" class="form-row">
          <span class="form-label">设备编号</span>
          <el-input v-model="roomForm.roomNo" class="dialog-field" placeholder="请输入设备编号" />
        </div>
        <div v-if="roomForm.roomType === 8" class="form-row">
          <span class="form-label">设备ID</span>
          <el-input v-model="roomForm.deviceId" class="dialog-field" placeholder="请输入设备ID" />
        </div>
        <div v-if="roomForm.roomType === 8" class="form-row">
          <span class="form-label">服务编号</span>
          <el-input v-model="roomForm.serveCode" class="dialog-field" placeholder="请输入服务编号" />
        </div>
        <div class="form-row">
          <span class="form-label">是否开启</span>
          <el-radio-group v-model="roomForm.isOpen">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="2">否</el-radio>
          </el-radio-group>
        </div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="roomEditDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="roomEditLoading" @click="submitRoom">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.mdv {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.mdv-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mdv-hero__badge {
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

.mdv-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.mdv-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.mdv-filter.nf-filter-card {
  margin-bottom: 0;
}

.mdv-filter__form {
  margin-bottom: 0;

  :deep(.el-input) {
    width: 180px;
  }
}

.mdv-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.mdv-table.nf-table-card {
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

.mini-thumb {
  width: 48px;
  height: 48px;
}

.large-thumb {
  width: 148px;
  height: 148px;
  border-radius: 10px;
}

.image-list-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.monitor-frame {
  width: 860px;
  height: 520px;
  margin: 0 auto;
  display: block;
  border: 0;
}

.empty-monitor {
  text-align: center;
  color: var(--el-text-color-secondary);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 24px;
}

.detail-item {
  display: flex;
  gap: 8px;
}

.detail-item-wide {
  grid-column: 1 / -1;
}

.detail-label,
.form-label {
  width: 110px;
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
}

.detail-value {
  color: var(--el-text-color-regular);
  word-break: break-all;
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

.dialog-field {
  flex: 1;
}

.dialog-toolbar {
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .mdv-filter__form {
    :deep(.el-input) {
      width: 100%;
    }
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-item,
  .form-row {
    flex-direction: column;
  }

  .detail-label,
  .form-label {
    width: auto;
  }

  .monitor-frame {
    width: 100%;
    height: 360px;
  }
}
</style>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import PageTopbar from '@/components/common/PageTopbar.vue'
import {
  bindGoodsInnerCodeApi,
  changeGoodsDeviceStatusApi,
  createBoundRoomApi,
  deleteBoundRoomApi,
  getBoundRoomPageApi,
  getGoodsDevicePageApi,
  getRoomGroupListApi,
  updateBoundRoomApi,
} from '@/api/modules/merchant-device'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const route = useRoute()
const router = useRouter()

const tearoomId = Number(route.params.id || 0)
const storeName = String(route.query.storeName || '')

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const goodsList = ref([])

const filters = reactive({
  goodsName: '',
  status: 0,
})

const statusOptions = [
  { label: '全部', value: 0 },
  { label: '上架', value: 1 },
  { label: '下架', value: 2 },
]

const roomListDialogVisible = ref(false)
const specialRoomListDialogVisible = ref(false)
const roomList = ref([])
const currentGoods = ref({ goodsId: 0, goodsCategory: 0, innerCode: '', goodsName: '' })

const bindDialogVisible = ref(false)
const bindLoading = ref(false)
const bindInnerCode = ref('')
const bindGroupOptions = ref([])

const roomType = ref(1)
const roomName = ref('')
const roomNo = ref('')
const serveCode = ref('')
const equipmentCode = ref('')
const roomSelect = ref('')
const musicSelectStart = ref('')
const musicSelectEnd = ref('')
const deviceId = ref('')
const roomWeight = ref('')
const selectedDeviceNo = ref('')
const groupDeviceOptions = ref([])
const roomEditLoading = ref(false)
const roomEditDialogVisible = ref(false)
const roomEditMode = ref('create')

const roomEditForm = reactive({
  roomId: 0,
  roomType: 1,
  roomNo: '',
  roomName: '',
  serveCode: '',
  equipmentCode: '',
  roomSelect: '',
  musicSelectStart: '',
  musicSelectEnd: '',
  deviceId: '',
  roomWeight: '',
})

function goBack() {
  router.back()
}

function formatMoney(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toFixed(2) : '--'
}

function splitImages(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function roomTypeText(type) {
  if (Number(type) === 1) return '设备'
  if (Number(type) === 2) return '插座'
  if (Number(type) === 3) return '闪开闪关'
  if (Number(type) === 4) return '语音'
  if (Number(type) === 5) return '称重'
  return '-'
}

function goodsStatusText(status) {
  if (Number(status) === 1) return '上架'
  if (Number(status) === 2) return '下架'
  if (Number(status) === 3) return '清洁中'
  if (Number(status) === 4) return '维护中'
  if (Number(status) === 5) return '已打烊'
  return '-'
}

function filterSelectableDevices(devices = []) {
  return devices.filter((item) => item.vmsDeviceTypeCh !== 'DTU' && item.vmsDeviceTypeCh !== '核心控制器')
}

async function loadGoods() {
  loading.value = true
  try {
    const data = await getGoodsDevicePageApi({
      page: pageNum.value,
      limit: pageSize.value,
      goodsName: filters.goodsName.trim(),
      status: filters.status,
      tearoomId,
      goodsCategory: '',
    })
    goodsList.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadGoods()
}

function handleReset() {
  filters.goodsName = ''
  filters.status = 0
  pageNum.value = 1
  loadGoods()
}

function refresh() {
  loadGoods()
}

async function changeGoodsStatus(row) {
  try {
    await changeGoodsDeviceStatusApi({ goods_id: row.goodsId, type: row.status })
    showSuccess('操作成功')
  } catch (error) {
    row.status = Number(row.status) === 1 ? 2 : 1
    throw error
  } finally {
    loadGoods()
  }
}

async function loadRoomList() {
  const data = await getBoundRoomPageApi({ page: 1, limit: 20, roomName: '', goodsId: currentGoods.value.goodsId })
  roomList.value = data.list || []
}

async function openRoomListDialog(row, variant) {
  currentGoods.value = {
    goodsId: Number(row.goodsId || 0),
    goodsCategory: Number(row.goodsCategory || 0),
    innerCode: row.innerCode || '',
    goodsName: row.goodsName || '',
  }
  await loadRoomList()
  if (variant === 1) {
    roomListDialogVisible.value = true
  } else {
    specialRoomListDialogVisible.value = true
  }
}

async function loadSelectableDevices() {
  const result = await getRoomGroupListApi({ innerCode: currentGoods.value.innerCode || '' })
  const first = result[0]
  groupDeviceOptions.value = filterSelectableDevices(first?.devices || [])
}

function resetRoomCreateFields() {
  selectedDeviceNo.value = ''
  roomType.value = currentGoods.value.goodsCategory === 4 ? 3 : 1
  roomName.value = ''
  roomNo.value = ''
  serveCode.value = ''
  equipmentCode.value = ''
  roomSelect.value = ''
  musicSelectStart.value = ''
  musicSelectEnd.value = ''
  deviceId.value = ''
  roomWeight.value = ''
}

async function openCreateRoomDialog() {
  resetRoomCreateFields()
  roomEditMode.value = 'create'
  if (currentGoods.value.goodsCategory !== 4) {
    await loadSelectableDevices()
  }
  roomEditDialogVisible.value = true
}

function handleSelectableDeviceChange(value) {
  const target = groupDeviceOptions.value.find((item) => item.vmsDeviceNo === value)
  roomName.value = target?.vmsDeviceTypeCh || ''
}

async function openEditRoomDialog(row) {
  roomEditMode.value = 'edit'
  Object.assign(roomEditForm, {
    roomId: row.roomId,
    roomType: Number(row.roomType || 1),
    roomNo: row.roomNo || '',
    roomName: row.roomName || '',
    serveCode: row.serveCode || '',
    equipmentCode: row.equipmentCode || '',
    roomSelect: row.roomSelect || '',
    musicSelectStart: row.musicSelectStart || '',
    musicSelectEnd: row.musicSelectEnd || '',
    deviceId: row.deviceId || '',
    roomWeight: row.roomWeight || '',
  })
  if (currentGoods.value.goodsCategory !== 4) {
    await loadSelectableDevices()
  }
  roomEditDialogVisible.value = true
}

async function submitCreateRoom() {
  if (Number(roomType.value) === 1 && !selectedDeviceNo.value) return showError('请选择设备')
  if (Number(roomType.value) === 2) {
    if (!selectedDeviceNo.value) return showError('请输入智能插座mac码')
    if (!roomName.value) return showError('请输入智能插座名称')
  }
  roomEditLoading.value = true
  try {
    await createBoundRoomApi({
      goodsId: currentGoods.value.goodsId,
      roomNo: selectedDeviceNo.value,
      roomType: roomType.value,
      roomName: roomName.value,
      serveCode: serveCode.value,
      equipmentCode: equipmentCode.value,
      roomSelect: roomSelect.value,
      musicSelectStart: musicSelectStart.value,
      musicSelectEnd: musicSelectEnd.value,
      deviceId: deviceId.value,
      roomWeight: roomWeight.value,
    })
    showSuccess('操作成功')
    roomEditDialogVisible.value = false
    await loadRoomList()
  } finally {
    roomEditLoading.value = false
  }
}

async function submitEditRoom() {
  roomEditLoading.value = true
  try {
    await updateBoundRoomApi({
      roomId: roomEditForm.roomId,
      roomNo: roomEditForm.roomNo,
      roomType: roomEditForm.roomType,
      roomName: roomEditForm.roomName,
      goodsId: currentGoods.value.goodsId,
      serveCode: roomEditForm.serveCode,
      equipmentCode: roomEditForm.equipmentCode,
      roomSelect: roomEditForm.roomSelect,
      musicSelectStart: roomEditForm.musicSelectStart,
      musicSelectEnd: roomEditForm.musicSelectEnd,
      deviceId: roomEditForm.deviceId,
      roomWeight: roomEditForm.roomWeight,
    })
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

async function openBindDialog(row) {
  currentGoods.value = {
    goodsId: Number(row.goodsId || 0),
    goodsCategory: Number(row.goodsCategory || 0),
    innerCode: row.innerCode || '',
    goodsName: row.goodsName || '',
  }
  bindInnerCode.value = row.innerCode || ''
  bindGroupOptions.value = await getRoomGroupListApi({ innerCode: route.query.equipmentSn || '' }).catch(() => [])
  bindDialogVisible.value = true
}

async function submitBind() {
  if (!bindInnerCode.value) return showError('请选择设备')
  bindLoading.value = true
  try {
    await bindGoodsInnerCodeApi({ goodsId: currentGoods.value.goodsId, innerCode: bindInnerCode.value })
    showSuccess('操作成功')
    bindDialogVisible.value = false
    await loadGoods()
  } finally {
    bindLoading.value = false
  }
}

loadGoods()
</script>

<template>
  <div class="mdi">
    <PageTopbar :title="storeName ? `${storeName} / 场馆设备列表` : '场馆设备列表'" @back="goBack" />

    <header class="mdi-hero nf-page-hero">
      <span class="mdi-hero__badge">羽毛球馆</span>
      <h1 class="mdi-hero__title">场馆设备列表</h1>
      <p class="mdi-hero__desc">{{ storeName ? `${storeName} · ` : '' }}管理场馆羽毛球场设备绑定、上下架与设备详情，支持按名称与状态筛选</p>
    </header>

    <div class="mdi-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="mdi-filter__form" @submit.prevent>
        <el-form-item label="羽毛球场名称">
          <el-input v-model="filters.goodsName" clearable placeholder="请输入羽毛球场" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="羽毛球场状态">
          <el-select v-model="filters.status" placeholder="请选择羽毛球场状态" @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="mdi-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" plain @click="refresh">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="mdi-table nf-table-card">
      <el-table v-loading="loading" :data="goodsList" border>
        <el-table-column prop="goodsId" label="编号" width="80" fixed="left" />
        <el-table-column label="羽毛球场图片" width="100" fixed="left">
          <template #default="{ row }">
            <el-image v-if="row.goodsCoverImage" :src="row.goodsCoverImage" class="thumb-image" :preview-src-list="splitImages(row.goodsCoverImage)" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="羽毛球场名称" min-width="180" />
        <el-table-column prop="counts" label="绑定设备数量" width="110" />
        <el-table-column prop="goodsType" label="羽毛球场信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="hourPrice" label="小时价" width="100"><template #default="{ row }">{{ formatMoney(row.hourPrice) }}</template></el-table-column>
        <el-table-column prop="minutePrice" label="分钟价" width="100"><template #default="{ row }">{{ formatMoney(row.minutePrice) }}</template></el-table-column>
        <el-table-column prop="cashPledge" label="押金" width="100"><template #default="{ row }">{{ formatMoney(row.cashPledge) }}</template></el-table-column>
        <el-table-column prop="subscription" label="预约价格" width="100"><template #default="{ row }">{{ formatMoney(row.subscription) }}</template></el-table-column>
        <el-table-column prop="yyToken" label="语音播报token" min-width="180" />
        <el-table-column prop="yyId" label="语音播报ID" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="上下架" width="120">
          <template #default="{ row }">
            <el-switch v-if="Number(row.status) === 1 || Number(row.status) === 2" v-model="row.status" :active-value="1" :inactive-value="2" @change="changeGoodsStatus(row)" />
            <span v-else>{{ goodsStatusText(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button v-if="Number(row.goodsCategory) !== 4" size="small" type="primary" @click="openBindDialog(row)">修改设备</el-button>
              <el-button size="small" type="primary" plain :disabled="!isAuth('merchantList:update')" @click="openRoomListDialog(row, Number(row.goodsCategory) === 4 ? 2 : 1)">设备详情</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadGoods()" @update:pageSize="pageSize = $event; pageNum = 1; loadGoods()" />
    </div>

    <el-dialog v-model="roomListDialogVisible" title="设备清单" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="dialog-toolbar">
        <el-space>
          <el-button type="primary" size="small" plain @click="loadRoomList">刷新</el-button>
          <el-button type="primary" size="small" :disabled="!isAuth('devices:add')" @click="openCreateRoomDialog">添加</el-button>
        </el-space>
      </div>
      <el-table :data="roomList" border>
        <el-table-column prop="roomId" label="编号" width="80" />
        <el-table-column label="设备类型" width="120"><template #default="{ row }">{{ roomTypeText(row.roomType) }}</template></el-table-column>
        <el-table-column prop="roomName" label="设备名称/智能插座名称" min-width="160" />
        <el-table-column prop="roomNo" label="设备编号/智能插座mac码" min-width="160" />
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

    <el-dialog v-model="specialRoomListDialogVisible" title="设备清单" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="dialog-toolbar">
        <el-space>
          <el-button type="primary" size="small" plain @click="loadRoomList">刷新</el-button>
          <el-button type="primary" size="small" :disabled="!isAuth('devices:add')" @click="openCreateRoomDialog">添加</el-button>
        </el-space>
      </div>
      <el-table :data="roomList" border>
        <el-table-column prop="roomId" label="编号" width="80" />
        <el-table-column label="设备类型" width="120"><template #default="{ row }">{{ roomTypeText(row.roomType) }}</template></el-table-column>
        <el-table-column prop="serveCode" label="服务编码" min-width="120" />
        <el-table-column prop="equipmentCode" label="设备模型编码" min-width="120" />
        <el-table-column prop="roomSelect" label="继电器序号" min-width="120" />
        <el-table-column prop="musicSelectStart" label="开启语音" min-width="120" />
        <el-table-column prop="musicSelectEnd" label="关闭语音" min-width="120" />
        <el-table-column prop="deviceId" label="设备ID" min-width="120" />
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
<template v-if="roomEditMode === 'create'">
        <el-form label-width="120px">
          <el-form-item label="设备类型">
            <el-radio-group v-model="roomType">
              <el-radio v-if="currentGoods.goodsCategory !== 4" :value="1">设备</el-radio>
              <el-radio v-if="currentGoods.goodsCategory !== 4" :value="2">插座</el-radio>
              <el-radio v-if="currentGoods.goodsCategory === 4" :value="3">闪开闪关</el-radio>
              <el-radio v-if="currentGoods.goodsCategory === 4" :value="4">语音</el-radio>
              <el-radio v-if="currentGoods.goodsCategory === 4" :value="5">称重</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="roomType === 1" label="设备">
            <el-select v-model="selectedDeviceNo" placeholder="请选择设备" class="dialog-field" @change="handleSelectableDeviceChange">
              <el-option v-for="item in groupDeviceOptions" :key="item.vmsDeviceNo" :label="item.vmsDeviceNote" :value="item.vmsDeviceNo" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="roomType === 2" label="智能插座mac码">
            <el-input v-model="selectedDeviceNo" class="dialog-field" placeholder="请输入智能插座mac码" />
          </el-form-item>
          <el-form-item v-if="roomType === 2" label="智能插座名称">
            <el-input v-model="roomName" class="dialog-field" placeholder="请输入智能插座名称" />
          </el-form-item>
          <el-form-item v-if="currentGoods.goodsCategory === 4" label="服务编码">
            <el-input v-model="serveCode" class="dialog-field" placeholder="请输入服务编码" />
          </el-form-item>
          <el-form-item v-if="currentGoods.goodsCategory === 4" label="设备模型编码">
            <el-input v-model="equipmentCode" class="dialog-field" placeholder="请输入设备模型编码" />
          </el-form-item>
          <el-form-item v-if="roomType === 3" label="继电器序号">
            <el-input v-model="roomSelect" class="dialog-field" placeholder="请输入继电器序号" />
          </el-form-item>
          <el-form-item v-if="roomType === 4" label="开启语音">
            <el-input v-model="musicSelectStart" class="dialog-field" placeholder="请输入开启语音" />
          </el-form-item>
          <el-form-item v-if="roomType === 4" label="关闭语音">
            <el-input v-model="musicSelectEnd" class="dialog-field" placeholder="请输入关闭语音" />
          </el-form-item>
          <el-form-item v-if="roomType === 5" label="设备ID">
            <el-input v-model="deviceId" class="dialog-field" placeholder="请输入设备ID" />
          </el-form-item>
        </el-form>
      </template>

      <template v-else>
        <el-form :model="roomEditForm" label-width="120px">
          <el-form-item label="设备类型">
            <el-radio-group v-model="roomEditForm.roomType">
              <el-radio v-if="currentGoods.goodsCategory !== 4" :value="1">设备</el-radio>
              <el-radio v-if="currentGoods.goodsCategory !== 4" :value="2">插座</el-radio>
              <el-radio v-if="currentGoods.goodsCategory === 4" :value="3">闪开闪关</el-radio>
              <el-radio v-if="currentGoods.goodsCategory === 4" :value="4">语音</el-radio>
              <el-radio v-if="currentGoods.goodsCategory === 4" :value="5">称重</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="roomEditForm.roomType === 1" label="设备">
            <el-select v-model="roomEditForm.roomNo" placeholder="请选择设备" class="dialog-field">
              <el-option v-for="item in groupDeviceOptions" :key="item.vmsDeviceNo" :label="item.vmsDeviceNote" :value="item.vmsDeviceNo" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="roomEditForm.roomType === 2" label="智能插座mac码">
            <el-input v-model="roomEditForm.roomNo" class="dialog-field" placeholder="请输入智能插座mac码" />
          </el-form-item>
          <el-form-item v-if="roomEditForm.roomType === 2" label="智能插座名称">
            <el-input v-model="roomEditForm.roomName" class="dialog-field" placeholder="请输入智能插座名称" />
          </el-form-item>
          <el-form-item v-if="currentGoods.goodsCategory === 4" label="服务编码">
            <el-input v-model="roomEditForm.serveCode" class="dialog-field" placeholder="请输入服务编码" />
          </el-form-item>
          <el-form-item v-if="currentGoods.goodsCategory === 4" label="设备模型编码">
            <el-input v-model="roomEditForm.equipmentCode" class="dialog-field" placeholder="请输入设备模型编码" />
          </el-form-item>
          <el-form-item v-if="roomEditForm.roomType === 3" label="继电器序号">
            <el-input v-model="roomEditForm.roomSelect" class="dialog-field" placeholder="请输入继电器序号" />
          </el-form-item>
          <el-form-item v-if="roomEditForm.roomType === 4" label="开启语音">
            <el-input v-model="roomEditForm.musicSelectStart" class="dialog-field" placeholder="请输入开启语音" />
          </el-form-item>
          <el-form-item v-if="roomEditForm.roomType === 4" label="关闭语音">
            <el-input v-model="roomEditForm.musicSelectEnd" class="dialog-field" placeholder="请输入关闭语音" />
          </el-form-item>
          <el-form-item v-if="roomEditForm.roomType === 5" label="设备ID">
            <el-input v-model="roomEditForm.deviceId" class="dialog-field" placeholder="请输入设备ID" />
          </el-form-item>
        </el-form>
      </template>

            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="roomEditDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="roomEditLoading" @click="roomEditMode === 'edit' ? submitEditRoom() : submitCreateRoom()">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="bindDialogVisible" title="修改设备" width="640px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-row">
        <span class="form-label">设备</span>
        <el-select v-model="bindInnerCode" placeholder="请选择设备" class="dialog-field">
          <el-option v-for="item in bindGroupOptions" :key="item.innerCode" :label="item.vmsGroupName" :value="item.innerCode" />
        </el-select>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="bindDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="bindLoading" @click="submitBind">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.mdi {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.mdi-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mdi-hero__badge {
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

.mdi-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.mdi-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.mdi-filter.nf-filter-card {
  margin-bottom: 0;
}

.mdi-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.mdi-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.mdi-table.nf-table-card {
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

.dialog-toolbar {
  margin-bottom: 12px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.form-label {
  width: 100px;
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
  line-height: 32px;
}

.dialog-field {
  flex: 1;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }

  .form-label {
    width: auto;
  }
}
</style>

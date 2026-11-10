<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import BasePagination from '@/components/common/BasePagination.vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import {
  createMemberPrivilegeApi,
  createMemberVipApi,
  deleteMemberPrivilegeApi,
  deleteMemberVipApi,
  getMemberCouponIssuePageApi,
  getMemberPrivilegePageApi,
  getMemberVipPageApi,
  updateMemberPrivilegeApi,
  updateMemberVipApi,
} from '@/api/modules/member-details'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const vipList = ref([])

const vipDialogVisible = ref(false)
const vipDialogLoading = ref(false)
const vipUploadLoadingField = ref('')
const vipDialogTitle = computed(() => (vipForm.vipId ? '修改' : '添加'))

const vipForm = reactive({
  vipId: '',
  vipName: '',
  vipImage: '',
  vipIndex: 1,
  money: '',
  userImg: '',
  vipIcon: '',
  fontColor: '#666666',
})

const privilegeDialogVisible = ref(false)
const privilegeLoading = ref(false)
const privilegePage = ref(1)
const privilegeSize = ref(10)
const privilegeTotal = ref(0)
const privilegeList = ref([])
const currentVipId = ref('')

const privilegeEditDialogVisible = ref(false)
const privilegeEditLoading = ref(false)
const privilegeUploadLoadingField = ref('')
const privilegeDialogTitle = computed(() => (privilegeForm.vipPrivilegeId ? '修改' : '添加'))

const privilegeForm = reactive({
  vipPrivilegeId: '',
  privilegeName: '',
  privilegeContent: '',
  privilegeImage: '',
  privilegeImage2: '',
  privilegeImage3: '',
  privilegeClassify: 1,
  privilegeType: 1,
  weekNum: '',
  parent: '',
  sort: 1,
  details: '',
  isEveryDay: 2,
  parent1: '',
  parent2: '',
  parent3: '',
  parent4: '',
  parent5: '',
  parent6: '',
  parent7: '',
})

const editorRef = ref()
const editorUploading = ref(false)
let quillInstance = null

const couponDialogVisible = ref(false)
const couponLoading = ref(false)
const couponPage = ref(0)
const couponSize = ref(10)
const couponTotal = ref(0)
const couponList = ref([])
const selectedCouponRows = ref([])
const selectedCouponIds = ref([])
const selectedCouponObjects = ref([])

const editorToolbar = [
  [{ header: [1, 2, 3, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ color: [] }, { background: [] }],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ align: [] }],
  ['blockquote', 'link', 'image'],
  ['clean'],
]

function privilegeClassifyText(value) {
  const map = {
    1: '赠送优惠券',
    2: '开台折扣',
    3: '开台问候',
    4: '专属图标',
    5: '专属赛事',
    6: '专属活动',
    7: '短信服务',
    8: '分销',
    9: '其他',
    10: '免押金',
    11: '预约',
    12: '点餐折扣',
    13: '自动开台',
    14: '可提前预约天数',
  }
  return map[Number(value)] || '-'
}

function privilegeTypeText(row) {
  if (![1, 7].includes(Number(row.privilegeClassify))) {
    return '-'
  }
  const type = Number(row.privilegeType)
  if (type === 1) return '每天'
  if (type === 2) {
    const weekMap = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
    return `每周${row.weekNum ? `(${weekMap[Number(row.weekNum)] || ''})` : ''}`
  }
  if (type === 3) return '每月'
  if (type === 4) return '每季度'
  if (type === 5) return '每年'
  if (type === 6) return '生日'
  return '-'
}

function stripHtml(value = '') {
  return String(value).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}

function resetVipForm() {
  Object.assign(vipForm, {
    vipId: '',
    vipName: '',
    vipImage: '',
    vipIndex: 1,
    money: '',
    userImg: '',
    vipIcon: '',
    fontColor: '#666666',
  })
}

function resetPrivilegeForm() {
  Object.assign(privilegeForm, {
    vipPrivilegeId: '',
    privilegeName: '',
    privilegeContent: '',
    privilegeImage: '',
    privilegeImage2: '',
    privilegeImage3: '',
    privilegeClassify: 1,
    privilegeType: 1,
    weekNum: '',
    parent: '',
    sort: 1,
    details: '',
    isEveryDay: 2,
    parent1: '',
    parent2: '',
    parent3: '',
    parent4: '',
    parent5: '',
    parent6: '',
    parent7: '',
  })
  selectedCouponIds.value = []
  selectedCouponObjects.value = []
}

function normalizeVipRow(item = {}) {
  return {
    ...item,
    vipId: item.vipId,
    vipName: item.vipName || '',
    vipImage: item.vipImage || '',
    userImg: item.userImg || '',
    vipIcon: item.vipIcon || '',
    fontColor: item.fontColor || '',
    money: item.money ?? '',
    vipIndex: item.vipIndex ?? '',
    createTime: item.createTime || item.createdAt || '',
  }
}

function normalizePrivilegeRow(item = {}) {
  return {
    ...item,
    vipPrivilegeId: item.vipPrivilegeId,
    privilegeName: item.privilegeName || '',
    privilegeContent: item.privilegeContent || '',
    privilegeImage: item.privilegeImage || '',
    privilegeImage2: item.privilegeImage2 || '',
    privilegeImage3: item.privilegeImage3 || '',
    privilegeClassify: Number(item.privilegeClassify || 0),
    privilegeType: Number(item.privilegeType || 0),
    weekNum: item.weekNum || '',
    parent: item.parent || '',
    sort: item.sort ?? '',
    details: item.details || '',
    createTime: item.createTime || item.createdAt || '',
    status: Number(item.status ?? 1),
    isEveryDay: Number(item.isEveryDay ?? 2),
    selfCouponIssueList: item.selfCouponIssueList || [],
  }
}

function normalizeCouponRow(item = {}) {
  return {
    ...item,
    couponIssueId: item.couponIssueId,
    couponName: item.couponName || '',
    issueNumber: item.issueNumber ?? '',
    remainNumber: item.remainNumber ?? '',
    hour: item.hour ?? '',
    validDay: item.validDay ?? '',
    teamRule: item.teamRule || '',
    buyRule: item.buyRule || '',
    type: Number(item.type || 0),
  }
}

function privilegeParentSummary(row) {
  const classify = Number(row.privilegeClassify)
  if (classify === 1) {
    return row.selfCouponIssueList?.length ? row.selfCouponIssueList.map((item) => item?.couponName || '-').join(';') : '-'
  }
  if (classify === 2 || classify === 12) return `折扣比例:${row.parent || '-'}`
  if (classify === 3) {
    if (Number(row.isEveryDay) !== 1) return `问候语:${row.parent || '-'}`
    const parts = String(row.parent || '').split(';').filter(Boolean)
    const weeks = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return parts.length ? parts.map((item, index) => `${weeks[index]}问候语：${item}`).join(';') : '-'
  }
  if (classify === 4) return row.parent || '-'
  if (classify === 7) return Number(row.parent) === 1 ? '营销短信' : Number(row.parent) === 2 ? '生日短信' : '-'
  if (classify === 8) return `每笔抽成比例:${row.parent || '-'}`
  if (classify === 14) return `可提前预约天数:${row.parent || '-'}`
  return '-'
}

async function loadVipList() {
  loading.value = true
  try {
    const data = await getMemberVipPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      vipName: '',
      isEnable: '',
    })
    vipList.value = (data.list || []).map(normalizeVipRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function refreshVipList() {
  pageNum.value = 1
  loadVipList()
}

async function openVipCreateDialog(row) {
  if (row) {
    Object.assign(vipForm, {
      vipId: row.vipId,
      vipName: row.vipName || '',
      vipImage: row.vipImage || '',
      vipIndex: row.vipIndex ?? 1,
      money: row.money ?? '',
      userImg: row.userImg || '',
      vipIcon: row.vipIcon || '',
      fontColor: row.fontColor || '#666666',
    })
  } else {
    resetVipForm()
  }
  vipDialogVisible.value = true
}

async function uploadVipImage(field, option) {
  vipUploadLoadingField.value = field
  try {
    const data = await uploadMerchantImageApi(option.file)
    vipForm[field] = String(data?.url || data?.data || data?.src || data?.path || data || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    vipUploadLoadingField.value = ''
  }
}

async function submitVip() {
  if (!vipForm.vipName.trim()) return showError('请输入会员名称')
  if (vipForm.money === '') return showError('请输入消费值')
  if (vipForm.vipIndex === '') return showError('请输入会员等级')
  if (!vipForm.vipImage) return showError('请上传图片')

  vipDialogLoading.value = true
  try {
    const payload = {
      vipName: vipForm.vipName.trim(),
      vipImage: vipForm.vipImage,
      vipIndex: vipForm.vipIndex,
      money: vipForm.money,
      vipId: vipForm.vipId || undefined,
      userImg: vipForm.userImg,
      vipIcon: vipForm.vipIcon,
      fontColor: vipForm.fontColor,
    }
    if (vipForm.vipId) {
      await updateMemberVipApi(payload)
    } else {
      delete payload.vipId
      await createMemberVipApi(payload)
    }
    showSuccess('操作成功')
    vipDialogVisible.value = false
    await loadVipList()
  } finally {
    vipDialogLoading.value = false
  }
}

async function deleteVip(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMemberVipApi(row.vipId)
  showSuccess('删除成功')
  await loadVipList()
}

async function openPrivilegeDialog(row) {
  currentVipId.value = row.vipId
  privilegePage.value = 1
  privilegeDialogVisible.value = true
  await loadPrivilegeList()
}

async function loadPrivilegeList() {
  privilegeLoading.value = true
  try {
    const data = await getMemberPrivilegePageApi({
      page: privilegePage.value,
      limit: privilegeSize.value,
      vipId: currentVipId.value,
      privilegeClassify: '',
    })
    privilegeList.value = (data.list || []).map(normalizePrivilegeRow)
    privilegeTotal.value = data.total || 0
  } finally {
    privilegeLoading.value = false
  }
}

function refreshPrivilegeList() {
  privilegePage.value = 1
  loadPrivilegeList()
}

async function openPrivilegeEditDialog(row) {
  if (row) {
    const rowsValue = JSON.parse(JSON.stringify(row))
    Object.assign(privilegeForm, {
      vipPrivilegeId: rowsValue.vipPrivilegeId,
      privilegeName: rowsValue.privilegeName || '',
      privilegeContent: rowsValue.privilegeContent || '',
      privilegeImage: rowsValue.privilegeImage || '',
      privilegeImage2: rowsValue.privilegeImage2 || '',
      privilegeImage3: rowsValue.privilegeImage3 || '',
      privilegeClassify: Number(rowsValue.privilegeClassify || 1),
      privilegeType: Number(rowsValue.privilegeType || 1),
      weekNum: rowsValue.weekNum || '',
      parent: rowsValue.parent || '',
      sort: rowsValue.sort ?? '',
      details: rowsValue.details || '',
      isEveryDay: Number(rowsValue.isEveryDay || 2),
      parent1: '',
      parent2: '',
      parent3: '',
      parent4: '',
      parent5: '',
      parent6: '',
      parent7: '',
    })
    if (Number(rowsValue.privilegeClassify) === 1) {
      selectedCouponIds.value = String(rowsValue.parent || '').split(',').filter(Boolean)
      selectedCouponObjects.value = rowsValue.selfCouponIssueList || []
    } else {
      selectedCouponIds.value = []
      selectedCouponObjects.value = []
    }
    if (Number(rowsValue.isEveryDay) === 1) {
      const parents = String(rowsValue.parent || '').split(';')
      privilegeForm.parent1 = parents[0] || ''
      privilegeForm.parent2 = parents[1] || ''
      privilegeForm.parent3 = parents[2] || ''
      privilegeForm.parent4 = parents[3] || ''
      privilegeForm.parent5 = parents[4] || ''
      privilegeForm.parent6 = parents[5] || ''
      privilegeForm.parent7 = parents[6] || ''
    }
  } else {
    resetPrivilegeForm()
  }
  privilegeEditDialogVisible.value = true
  await nextTick()
}

async function uploadPrivilegeImage(field, option) {
  privilegeUploadLoadingField.value = field
  try {
    const data = await uploadMerchantImageApi(option.file)
    privilegeForm[field] = String(data?.url || data?.data || data?.src || data?.path || data || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    privilegeUploadLoadingField.value = ''
  }
}

function removeCoupon(index) {
  selectedCouponIds.value.splice(index, 1)
  selectedCouponObjects.value.splice(index, 1)
}

async function openCouponDialog() {
  couponPage.value = 0
  selectedCouponRows.value = []
  couponDialogVisible.value = true
  await loadCouponList()
}

async function loadCouponList() {
  couponLoading.value = true
  try {
    const data = await getMemberCouponIssuePageApi({
      page: couponPage.value,
      size: couponSize.value,
      type: 4,
      tearoomName: '',
      couponName: '',
    })
    couponList.value = (data.list || []).map(normalizeCouponRow)
    couponTotal.value = data.total || 0
  } finally {
    couponLoading.value = false
  }
}

function handleCouponSelectionChange(rows) {
  selectedCouponRows.value = rows
}

function confirmCouponSelection() {
  selectedCouponRows.value.forEach((item) => {
    if (!selectedCouponIds.value.includes(String(item.couponIssueId))) {
      selectedCouponIds.value.push(String(item.couponIssueId))
      selectedCouponObjects.value.push(item)
    }
  })
  couponDialogVisible.value = false
}

function createImageInput() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  return input
}

function handleEditorReady(quill) {
  quillInstance = quill
  quill.getModule('toolbar')?.addHandler('image', handleEditorImageUpload)
}

function insertEditorImage(url) {
  const quill = quillInstance || editorRef.value?.getQuill?.()
  if (!quill) {
    privilegeForm.details = `${privilegeForm.details || ''}<p><img src="${url}" /></p>`
    return
  }
  const range = quill.getSelection(true)
  const index = range ? range.index : quill.getLength()
  quill.insertEmbed(index, 'image', url, 'user')
  quill.setSelection(index + 1, 0, 'silent')
  quill.focus()
  privilegeForm.details = quill.root.innerHTML
}

function handleEditorImageUpload() {
  const input = createImageInput()
  input.click()
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    editorUploading.value = true
    try {
      const data = await uploadMerchantImageApi(file)
      const imageUrl = String(data?.url || data?.data || data?.src || data?.path || data || '')
      if (!imageUrl) throw new Error('未获取到图片地址')
      insertEditorImage(imageUrl)
    } catch (error) {
      showError(error?.message || '图片上传失败')
    } finally {
      editorUploading.value = false
      input.value = ''
    }
  }
}

async function submitPrivilege() {
  if (Number(privilegeForm.isEveryDay) === 1) {
    if (!privilegeForm.parent1) return showError('周一问候语不能为空')
    if (!privilegeForm.parent2) return showError('周二问候语不能为空')
    if (!privilegeForm.parent3) return showError('周三问候语不能为空')
    if (!privilegeForm.parent4) return showError('周四问候语不能为空')
    if (!privilegeForm.parent5) return showError('周五问候语不能为空')
    if (!privilegeForm.parent6) return showError('周六问候语不能为空')
    if (!privilegeForm.parent7) return showError('周日问候语不能为空')
    privilegeForm.parent = `${privilegeForm.parent1};${privilegeForm.parent2};${privilegeForm.parent3};${privilegeForm.parent4};${privilegeForm.parent5};${privilegeForm.parent6};${privilegeForm.parent7};`
  }
  if (Number(privilegeForm.privilegeType) === 3 && !privilegeForm.weekNum) {
    return showError('周期不能为空')
  }
  if (Number(privilegeForm.privilegeClassify) === 1) {
    privilegeForm.parent = selectedCouponIds.value.toString()
  }

  privilegeEditLoading.value = true
  try {
    const payload = {
      vipPrivilegeId: privilegeForm.vipPrivilegeId || undefined,
      privilegeName: privilegeForm.privilegeName,
      privilegeContent: privilegeForm.privilegeContent,
      privilegeImage: privilegeForm.privilegeImage,
      privilegeClassify: privilegeForm.privilegeClassify,
      privilegeType: privilegeForm.privilegeType,
      parent: privilegeForm.parent,
      sort: privilegeForm.sort,
      vipId: currentVipId.value,
      details: privilegeForm.details,
      privilegeImage3: privilegeForm.privilegeImage3,
      privilegeImage2: privilegeForm.privilegeImage2,
      isEveryDay: privilegeForm.isEveryDay,
      weekNum: privilegeForm.weekNum,
    }
    if (privilegeForm.vipPrivilegeId) {
      await updateMemberPrivilegeApi(payload)
    } else {
      delete payload.vipPrivilegeId
      await createMemberPrivilegeApi(payload)
    }
    showSuccess('操作成功')
    privilegeEditDialogVisible.value = false
    await loadPrivilegeList()
  } finally {
    privilegeEditLoading.value = false
  }
}

async function deletePrivilege(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMemberPrivilegeApi(row.vipPrivilegeId)
  showSuccess('删除成功')
  await loadPrivilegeList()
}

async function togglePrivilegeStatus(row) {
  try {
    await updateMemberPrivilegeApi({ vipPrivilegeId: row.vipPrivilegeId, status: row.status })
    showSuccess('操作成功')
    await loadPrivilegeList()
  } catch (error) {
    row.status = row.status === 1 ? 2 : 1
  }
}

function clearVipIcon() {
  vipForm.vipIcon = ''
}

loadVipList()
</script>

<template>
  <div class="md">
    <header class="md-hero nf-page-hero">
      <span class="md-hero__badge">会员管理</span>
      <h1 class="md-hero__title">会员卡配置</h1>
      <p class="md-hero__desc">管理会员等级、图片与消费值，支持特权配置与增删改操作</p>
    </header>

    <div class="md-table nf-table-card">
      <div class="md-table__toolbar">
        <el-space>
          <el-button type="primary" plain @click="refreshVipList">刷新</el-button>
          <el-button type="primary" :disabled="!isAuth('memberDetails:add')" @click="openVipCreateDialog()">添加</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="vipList" border>
        <el-table-column prop="vipId" label="编号" width="80" />
        <el-table-column prop="vipName" label="会员名称" min-width="140" />
        <el-table-column label="会员图片" width="100"><template #default="{ row }"><el-image :src="row.vipImage" class="thumb-image" :preview-src-list="[row.vipImage]" preview-teleported /></template></el-table-column>
        <el-table-column label="我的会员图片" width="100"><template #default="{ row }"><el-image :src="row.userImg" class="thumb-image" :preview-src-list="[row.userImg]" preview-teleported /></template></el-table-column>
        <el-table-column label="会员图标" width="100"><template #default="{ row }"><el-image v-if="row.vipIcon" :src="row.vipIcon" class="thumb-image" :preview-src-list="[row.vipIcon]" preview-teleported /></template></el-table-column>
        <el-table-column label="字体颜色" width="100">
          <template #default="{ row }">
            <div v-if="row.fontColor" class="color-box"><div class="color-box__inner" :style="{ background: row.fontColor }"></div></div>
            <div v-else>-</div>
          </template>
        </el-table-column>
        <el-table-column prop="money" label="消费值" width="100" />
        <el-table-column prop="vipIndex" label="会员等级" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" type="primary" @click="openPrivilegeDialog(row)">特权</el-button>
              <el-button size="small" type="primary" :disabled="!isAuth('memberDetails:update')" @click="openVipCreateDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('memberDetails:delete')" @click="deleteVip(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[5, 10, 15, 20]" :total="total" @update:currentPage="pageNum = $event; loadVipList()" @update:pageSize="pageSize = $event; loadVipList()" />
    </div>

    <el-dialog v-model="vipDialogVisible" :title="vipDialogTitle" width="760px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">会员名称</span><el-input v-model="vipForm.vipName" class="dialog-field" placeholder="请输入会员名称" /></div>
        <div class="form-row"><span class="form-label">消费值</span><el-input v-model="vipForm.money" class="dialog-field" type="number" placeholder="请输入消费值" /></div>
        <div class="form-row"><span class="form-label">会员等级</span><el-input v-model="vipForm.vipIndex" class="dialog-field" type="number" placeholder="请输入会员等级" /></div>
        <div class="form-row"><span class="form-label">字体颜色</span><el-color-picker v-model="vipForm.fontColor" /></div>
        <div class="form-row"><span class="form-label">会员图片</span><div class="upload-row"><el-image v-if="vipForm.vipImage" :src="vipForm.vipImage" class="large-thumb" :preview-src-list="[vipForm.vipImage]" preview-teleported /><el-upload :show-file-list="false" :http-request="(option) => uploadVipImage('vipImage', option)"><el-button :loading="vipUploadLoadingField === 'vipImage'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">我的会员图片</span><div class="upload-row"><el-image v-if="vipForm.userImg" :src="vipForm.userImg" class="large-thumb" :preview-src-list="[vipForm.userImg]" preview-teleported /><el-upload :show-file-list="false" :http-request="(option) => uploadVipImage('userImg', option)"><el-button :loading="vipUploadLoadingField === 'userImg'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">会员图标</span><div class="upload-row"><div class="icon-upload-box"><el-image v-if="vipForm.vipIcon" :src="vipForm.vipIcon" class="large-thumb" :preview-src-list="[vipForm.vipIcon]" preview-teleported /><el-upload :show-file-list="false" :http-request="(option) => uploadVipImage('vipIcon', option)"><el-button :loading="vipUploadLoadingField === 'vipIcon'">上传</el-button></el-upload><el-button v-if="vipForm.vipIcon" type="danger" link class="delete-icon-btn" @click="clearVipIcon">删除</el-button></div></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="vipDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="vipDialogLoading" @click="submitVip">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="privilegeDialogVisible" title="会员特权" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="dialog-toolbar">
        <el-space>
          <el-button type="primary" plain @click="refreshPrivilegeList">刷新</el-button>
          <el-button type="primary" :disabled="!isAuth('memberDetails:add')" @click="openPrivilegeEditDialog()">添加</el-button>
        </el-space>
      </div>
      <el-table v-loading="privilegeLoading" :data="privilegeList" border>
        <el-table-column prop="vipPrivilegeId" label="编号" width="80" />
        <el-table-column prop="privilegeName" label="特权名称" min-width="140" />
        <el-table-column label="特权图片" width="100"><template #default="{ row }"><el-image :src="row.privilegeImage" class="thumb-image" :preview-src-list="[row.privilegeImage]" preview-teleported /></template></el-table-column>
        <el-table-column label="图标2" width="100"><template #default="{ row }"><el-image v-if="row.privilegeImage2" :src="row.privilegeImage2" class="thumb-image" :preview-src-list="[row.privilegeImage2]" preview-teleported /></template></el-table-column>
        <el-table-column label="图标3" width="100"><template #default="{ row }"><el-image v-if="row.privilegeImage3" :src="row.privilegeImage3" class="thumb-image" :preview-src-list="[row.privilegeImage3]" preview-teleported /></template></el-table-column>
        <el-table-column label="特权类型" width="130"><template #default="{ row }">{{ privilegeClassifyText(row.privilegeClassify) }}</template></el-table-column>
        <el-table-column label="触发频率" width="140"><template #default="{ row }">{{ privilegeTypeText(row) }}</template></el-table-column>
        <el-table-column label="对应上级" width="220">
          <template #default="{ row }">
            <el-popover placement="top-start" trigger="hover" :width="400">
              <template #reference><div class="clamp-text">{{ privilegeParentSummary(row) }}</div></template>
              <div>{{ privilegeParentSummary(row) }}</div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="privilegeContent" label="特权描述" width="220" show-overflow-tooltip />
        <el-table-column label="具体权益" width="250">
          <template #default="{ row }">
            <el-popover placement="top-start" trigger="hover" :width="320">
              <template #reference><div class="clamp-text">{{ stripHtml(row.details) || '-' }}</div></template>
              <div v-html="row.details"></div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="是否启用" width="120" fixed="right">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="2" :disabled="!isAuth('memberDetails:update')" @change="togglePrivilegeStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" type="primary" :disabled="!isAuth('memberDetails:update')" @click="openPrivilegeEditDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('memberDetails:delete')" @click="deletePrivilege(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination :current-page="privilegePage" :page-size="privilegeSize" :page-sizes="[5, 10, 15, 20]" :total="privilegeTotal" @update:currentPage="privilegePage = $event; loadPrivilegeList()" @update:pageSize="privilegeSize = $event; loadPrivilegeList()" />
    </el-dialog>

    <el-dialog v-model="privilegeEditDialogVisible" :title="privilegeDialogTitle" width="75%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">特权名称</span><el-input v-model="privilegeForm.privilegeName" class="dialog-field" placeholder="请输入特权名称" /></div>
        <div class="form-row"><span class="form-label">特权描述</span><el-input v-model="privilegeForm.privilegeContent" class="dialog-field" placeholder="请输入特权描述" /></div>
        <div class="form-row form-row-top"><span class="form-label">特权类型</span><el-radio-group v-model="privilegeForm.privilegeClassify" class="radio-wrap"><el-radio v-for="item in [1,2,3,4,5,6,7,8,9,10,11,12,13,14]" :key="item" :value="item">{{ privilegeClassifyText(item) }}</el-radio></el-radio-group></div>
        <div v-if="[1,7].includes(Number(privilegeForm.privilegeClassify))" class="form-row form-row-top"><span class="form-label">触发频率</span><el-radio-group v-model="privilegeForm.privilegeType" class="radio-wrap"><el-radio :value="1">每天</el-radio><el-radio :value="2">每周</el-radio><el-radio :value="3">每月</el-radio><el-radio :value="4">每季度</el-radio><el-radio :value="5">每年</el-radio><el-radio :value="6">生日</el-radio></el-radio-group></div>
        <div v-if="Number(privilegeForm.privilegeType) === 2" class="form-row form-row-top"><span class="form-label">周期</span><el-radio-group v-model="privilegeForm.weekNum" class="radio-wrap"><el-radio :value="1">周一</el-radio><el-radio :value="2">周二</el-radio><el-radio :value="3">周三</el-radio><el-radio :value="4">周四</el-radio><el-radio :value="5">周五</el-radio><el-radio :value="6">周六</el-radio><el-radio :value="7">周日</el-radio></el-radio-group></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3" class="form-row"><span class="form-label">是否单独设置每一天</span><el-radio-group v-model="privilegeForm.isEveryDay"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 1" class="form-row form-row-top"><span class="form-label">优惠券</span><div class="dialog-field"><el-space wrap><el-tag v-for="(tag, index) in selectedCouponObjects" :key="`${tag.couponIssueId}-${index}`" closable @close="removeCoupon(index)">{{ tag.couponName || '-' }}</el-tag><el-button size="small" @click="openCouponDialog">添加</el-button></el-space></div></div>
        <div v-if="[2,12].includes(Number(privilegeForm.privilegeClassify))" class="form-row"><span class="form-label">折扣比例</span><el-input v-model="privilegeForm.parent" class="dialog-field" type="number" placeholder="请输入折扣比例" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3 && Number(privilegeForm.isEveryDay) !== 1" class="form-row"><span class="form-label">问候语</span><el-input v-model="privilegeForm.parent" class="dialog-field" placeholder="请输入问候语" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3 && Number(privilegeForm.isEveryDay) === 1" class="form-row"><span class="form-label">周一问候语</span><el-input v-model="privilegeForm.parent1" class="dialog-field" placeholder="请输入周一问候语" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3 && Number(privilegeForm.isEveryDay) === 1" class="form-row"><span class="form-label">周二问候语</span><el-input v-model="privilegeForm.parent2" class="dialog-field" placeholder="请输入周二问候语" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3 && Number(privilegeForm.isEveryDay) === 1" class="form-row"><span class="form-label">周三问候语</span><el-input v-model="privilegeForm.parent3" class="dialog-field" placeholder="请输入周三问候语" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3 && Number(privilegeForm.isEveryDay) === 1" class="form-row"><span class="form-label">周四问候语</span><el-input v-model="privilegeForm.parent4" class="dialog-field" placeholder="请输入周四问候语" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3 && Number(privilegeForm.isEveryDay) === 1" class="form-row"><span class="form-label">周五问候语</span><el-input v-model="privilegeForm.parent5" class="dialog-field" placeholder="请输入周五问候语" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3 && Number(privilegeForm.isEveryDay) === 1" class="form-row"><span class="form-label">周六问候语</span><el-input v-model="privilegeForm.parent6" class="dialog-field" placeholder="请输入周六问候语" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 3 && Number(privilegeForm.isEveryDay) === 1" class="form-row"><span class="form-label">周日问候语</span><el-input v-model="privilegeForm.parent7" class="dialog-field" placeholder="请输入周日问候语" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 4" class="form-row"><span class="form-label">图片</span><div class="upload-row"><el-image v-if="privilegeForm.parent" :src="privilegeForm.parent" class="large-thumb" :preview-src-list="[privilegeForm.parent]" preview-teleported /><el-upload :show-file-list="false" :http-request="(option) => uploadPrivilegeImage('parent', option)"><el-button :loading="privilegeUploadLoadingField === 'parent'">上传</el-button></el-upload></div></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 7" class="form-row"><span class="form-label">短信类型</span><el-radio-group v-model="privilegeForm.parent"><el-radio :value="1">营销短信</el-radio><el-radio :value="2">生日短信</el-radio></el-radio-group></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 8" class="form-row"><span class="form-label">每笔抽成比例</span><el-input v-model="privilegeForm.parent" class="dialog-field" type="number" placeholder="请输入每笔抽成比例" /></div>
        <div v-if="Number(privilegeForm.privilegeClassify) === 14" class="form-row"><span class="form-label">可提前预约天数</span><el-input v-model="privilegeForm.parent" class="dialog-field" type="number" placeholder="请输入可提前预约天数" /></div>
        <div class="form-row"><span class="form-label">排序</span><el-input v-model="privilegeForm.sort" class="dialog-field" type="number" placeholder="请输入排序" /></div>
        <div class="form-row"><span class="form-label">特权图片</span><div class="upload-row"><el-image v-if="privilegeForm.privilegeImage" :src="privilegeForm.privilegeImage" class="large-thumb" :preview-src-list="[privilegeForm.privilegeImage]" preview-teleported /><el-upload :show-file-list="false" :http-request="(option) => uploadPrivilegeImage('privilegeImage', option)"><el-button :loading="privilegeUploadLoadingField === 'privilegeImage'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">图标2</span><div class="upload-row"><el-image v-if="privilegeForm.privilegeImage2" :src="privilegeForm.privilegeImage2" class="large-thumb" :preview-src-list="[privilegeForm.privilegeImage2]" preview-teleported /><el-upload :show-file-list="false" :http-request="(option) => uploadPrivilegeImage('privilegeImage2', option)"><el-button :loading="privilegeUploadLoadingField === 'privilegeImage2'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">图标3</span><div class="upload-row"><el-image v-if="privilegeForm.privilegeImage3" :src="privilegeForm.privilegeImage3" class="large-thumb" :preview-src-list="[privilegeForm.privilegeImage3]" preview-teleported /><el-upload :show-file-list="false" :http-request="(option) => uploadPrivilegeImage('privilegeImage3', option)"><el-button :loading="privilegeUploadLoadingField === 'privilegeImage3'">上传</el-button></el-upload></div></div>
        <div class="form-row form-row-top"><span class="form-label">具体权益</span><div class="dialog-field"><QuillEditor ref="editorRef" v-model:content="privilegeForm.details" content-type="html" theme="snow" :toolbar="editorToolbar" @ready="handleEditorReady" /><div v-if="editorUploading" class="editor-uploading">图片上传中...</div></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="privilegeEditDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="privilegeEditLoading" @click="submitPrivilege">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="couponDialogVisible" title="优惠券列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="dialog-toolbar">
        <el-space>
          <el-button type="primary" @click="couponPage = 1; loadCouponList()">刷新</el-button>
          <el-button type="primary" @click="confirmCouponSelection">确定</el-button>
        </el-space>
      </div>
      <el-table v-loading="couponLoading" :data="couponList" border @selection-change="handleCouponSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="couponIssueId" label="编号" width="80" />
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ Number(row.type) === 1 ? '团购券' : Number(row.type) === 2 ? '积分兑换券' : Number(row.type) === 3 ? '新人券' : Number(row.type) === 4 ? '会员劵' : '-' }}</template></el-table-column>
        <el-table-column prop="couponName" label="优惠券名称" min-width="160" />
        <el-table-column prop="issueNumber" label="发布数量" width="100" />
        <el-table-column prop="remainNumber" label="剩余数量" width="100" />
        <el-table-column prop="hour" label="使用时长(半小时)" width="140" />
        <el-table-column prop="validDay" label="有效期限(天)" width="120" />
        <el-table-column label="使用规则" min-width="220"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.teamRule }}</div></template><div>{{ row.teamRule }}</div></el-popover></template></el-table-column>
        <el-table-column label="购买须知" min-width="220"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.buyRule }}</div></template><div>{{ row.buyRule }}</div></el-popover></template></el-table-column>
      </el-table>
      <BasePagination :current-page="couponPage + 1" :page-size="couponSize" :page-sizes="[10, 20, 30, 50, 100]" :total="couponTotal" @update:currentPage="couponPage = $event - 1; loadCouponList()" @update:pageSize="couponSize = $event; couponPage = 0; loadCouponList()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.md {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.md-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.md-hero__badge {
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

.md-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.md-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.md-table.nf-table-card {
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

.md-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.dialog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
  width: 120px;
  text-align: right;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
  min-width: 0;
}

.thumb-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
}

.large-thumb {
  width: 148px;
  height: 148px;
  border-radius: 10px;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.icon-upload-box {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.delete-icon-btn {
  align-self: flex-start;
}

.color-box {
  border: 1px solid #efefef;
  width: 36px;
  height: 36px;
  padding: 3px;
  border-radius: 5px;
}

.color-box__inner {
  width: 100%;
  height: 100%;
}

.clamp-text {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  max-height: 80px;
}

.radio-wrap {
  display: flex;
  gap: 8px 16px;
  flex-wrap: wrap;
}

.editor-uploading {
  margin-top: 8px;
  color: var(--el-text-color-secondary);
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

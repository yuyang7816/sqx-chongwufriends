<script setup>
import { reactive, ref, watch } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  deleteCouponIssueApi,
  getCouponIssueGoodsOptionsApi,
  getCouponIssuePageApi,
  getCouponIssueTearoomOptionsApi,
  getCouponIssueUserPageApi,
  getCouponIssueVipOptionsApi,
  sendCouponToInactiveUsersApi,
  sendCouponToUsersApi,
  updateCouponIssueApi,
} from '@/api/modules/coupon-issue'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const TAB_CONFIG = {
  first: { label: '团购券', type: 1 },
  second: { label: '积分兑换券', type: 2 },
  three: { label: '新人券', type: 3 },
  four: { label: '会员券', type: 4 },
}

const activeTab = ref('first')
const currentType = ref(1)

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const issueList = ref([])

const filters = reactive({
  tearoomName: '',
  couponName: '',
  isHoliday: '',
  weeks: [],
})

const holidayOptions = [
  { label: '全部', value: '' },
  { label: '是', value: 1 },
  { label: '否', value: 2 },
]

const weeksOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
]

const giftDialogVisible = ref(false)
const giftDialogLoading = ref(false)
const giftType = ref('')
const selectedGiftCouponIds = ref([])
const selectedGiftCouponType = ref(1)
const selectedUserIds = ref([])
const selectedUserNames = ref([])
const inactiveDays = ref('')

const userDialogVisible = ref(false)
const userLoading = ref(false)
const userPage = ref(1)
const userSize = ref(10)
const userTotal = ref(0)
const userList = ref([])
const selectedUserRows = ref([])
const vipOptions = ref([])

const userFilters = reactive({
  inviterCode: '',
  phone: '',
  nickName: '',
  vipIndex: '',
  startTime: '',
  endTime: '',
  ordersStartTime: '',
  ordersEndTime: '',
})

const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const tearoomOptions = ref([])
const goodsOptions = ref([])
const editForm = reactive({})
const editLimitTime = ref(1)
const editHoliday = ref(2)
const editWeeks = ref([])
const editTimes = ref([])
const editStartTime = ref('')
const editEndTime = ref('')

function couponTypeText(value) {
  if (Number(value) === 1) return '优惠券'
  if (Number(value) === 2) return '实物'
  if (Number(value) === 3) return '定时券'
  if (Number(value) === 4) return '周期卡'
  if (Number(value) === 5) return '充值卡'
  return '-'
}

function weekText(value) {
  const map = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
  return map[Number(value)] || ''
}

function holidayText(value) {
  return Number(value) === 1 ? '是' : '否'
}

function normalizeIssueRow(item = {}) {
  return {
    ...item,
    couponIssueId: item.couponIssueId,
    couponType: Number(item.couponType || 1),
    tearoomId: Number(item.tearoomId || 0),
    tearoom: item.tearoom || {},
    goodsIds: item.goodsIds || '',
    goodsList: item.goodsList || [],
    couponName: item.couponName || '',
    issueNumber: item.issueNumber ?? 0,
    remainNumber: item.remainNumber ?? 0,
    integral: item.integral ?? 0,
    hour: item.hour || '',
    validDay: item.validDay ?? 0,
    isLimitTime: Number(item.isLimitTime ?? 1),
    weeks: item.weeks || '',
    times: item.times || '',
    teamRule: item.teamRule || '',
    buyRule: item.buyRule || '',
    isHoliday: Number(item.isHoliday ?? 2),
    status: Number(item.status ?? 1),
    createTime: item.createTime || item.createdAt || '',
  }
}

function normalizeUserRow(item = {}) {
  return {
    ...item,
    userId: item.userId,
    userName: item.userName || '未设置',
    phone: item.phone || '未绑定',
    avatar: item.avatar || '',
    vipIndex: item.vipIndex ?? '',
    vipName: item.vipName || '',
    inviterCode: item.inviterCode || '-',
    createTime: item.createTime || item.createdAt || '',
  }
}

function avatarText(row) {
  return String(row.userName || '未').slice(0, 1)
}

function rowWeeksText(row) {
  return String(row.weeks || '')
    .split(',')
    .filter(Boolean)
    .map(weekText)
    .join('；')
}

async function loadIssueList() {
  loading.value = true
  try {
    const data = await getCouponIssuePageApi({
      page: pageNum.value - 1,
      size: pageSize.value,
      couponName: filters.couponName.trim(),
      tearoomName: filters.tearoomName.trim(),
      type: currentType.value,
      weeks: filters.weeks.toString(),
      isHoliday: filters.isHoliday,
    })
    issueList.value = (data.list || []).map(normalizeIssueRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadIssueList()
}

function handleReset() {
  pageNum.value = 1
  filters.tearoomName = ''
  filters.couponName = ''
  filters.isHoliday = ''
  filters.weeks = []
  loadIssueList()
}

watch(activeTab, (value) => {
  currentType.value = TAB_CONFIG[value]?.type || 1
  pageNum.value = 1
  pageSize.value = 10
  loadIssueList()
})

async function handleRowChange(row) {
  try {
    await updateCouponIssueApi({
      ...row,
      goodsIds: Array.isArray(row.goodsIds) ? row.goodsIds.toString() : row.goodsIds,
    })
    showSuccess('操作成功')
    await loadIssueList()
  } catch (error) {
    await loadIssueList()
  }
}

async function handleDelete(row) {
  await confirmAction('确定删除此条信息?')
  await deleteCouponIssueApi(row.couponIssueId)
  showSuccess('删除成功')
  await loadIssueList()
}

function openGiftDialog(row) {
  selectedGiftCouponIds.value = [row.couponIssueId]
  selectedGiftCouponType.value = Number(row.couponType || 1)
  selectedUserIds.value = []
  selectedUserNames.value = []
  inactiveDays.value = ''
  giftType.value = ''
  giftDialogVisible.value = true
}

async function loadVipOptions() {
  vipOptions.value = await getCouponIssueVipOptionsApi()
}

async function loadUsers() {
  userLoading.value = true
  try {
    const data = await getCouponIssueUserPageApi({
      page: userPage.value,
      limit: userSize.value,
      member: -1,
      userName: userFilters.nickName.trim(),
      inviterCode: userFilters.inviterCode.trim(),
      startTime: userFilters.startTime,
      endTime: userFilters.endTime,
      isAuthentication: '',
      phone: userFilters.phone.trim(),
      ordersStartTime: userFilters.ordersStartTime,
      ordersEndTime: userFilters.ordersEndTime,
      isRate: '',
      isVip: '',
      moneyUser: '',
      vipIndex: userFilters.vipIndex,
      vipNo: '',
      isBirthday: '',
      birthdayMonth: '',
      integralNum: '',
    })
    const vipMap = new Map(vipOptions.value.map((item) => [String(item.vipIndex), item.vipName]))
    userList.value = (data.list || []).map((item) => normalizeUserRow({ ...item, vipName: vipMap.get(String(item.vipIndex)) || '' }))
    userTotal.value = data.total || 0
  } finally {
    userLoading.value = false
  }
}

async function openUserDialog() {
  userPage.value = 1
  Object.assign(userFilters, {
    inviterCode: '',
    phone: '',
    nickName: '',
    vipIndex: '',
    startTime: '',
    endTime: '',
    ordersStartTime: '',
    ordersEndTime: '',
  })
  await loadVipOptions()
  userDialogVisible.value = true
  await loadUsers()
}

function handleUserSelectionChange(rows) {
  selectedUserRows.value = rows
}

function confirmUsers() {
  selectedUserRows.value.forEach((item) => {
    if (!selectedUserIds.value.includes(item.userId)) {
      selectedUserIds.value.push(item.userId)
      selectedUserNames.value.push(item.userName)
    }
  })
  userDialogVisible.value = false
}

function removeSelectedUser(_name, index) {
  if (selectedUserIds.value.length < 2) {
    return showError('最少需要选中一个用户')
  }
  selectedUserIds.value.splice(index, 1)
  selectedUserNames.value.splice(index, 1)
}

async function submitGift() {
  if (!giftType.value) return showError('请选择赠送类型')
  if ((giftType.value === 1 || giftType.value === 3) && !selectedUserIds.value.length) {
    return showError('请选择用户')
  }
  if (giftType.value === 4 && !inactiveDays.value) return showError('请输入指定天数')

  giftDialogLoading.value = true
  try {
    if (giftType.value === 4) {
      await sendCouponToInactiveUsersApi({
        day: inactiveDays.value,
        couponIds: selectedGiftCouponIds.value.toString(),
      })
    } else {
      await sendCouponToUsersApi({
        userIds: selectedUserIds.value.toString(),
        couponIds: selectedGiftCouponIds.value.toString(),
        type: giftType.value === 2 ? 2 : 1,
        couponType: selectedGiftCouponType.value,
      })
    }
    showSuccess('操作成功')
    giftDialogVisible.value = false
    await loadIssueList()
  } finally {
    giftDialogLoading.value = false
  }
}

async function loadTearoomOptions() {
  tearoomOptions.value = await getCouponIssueTearoomOptionsApi()
}

async function loadGoodsOptions() {
  if (!editForm.tearoomId) {
    goodsOptions.value = []
    return
  }
  goodsOptions.value = await getCouponIssueGoodsOptionsApi({
    page: 1,
    limit: 100,
    goodsName: '',
    status: '',
    tearoomId: editForm.tearoomId,
  })
}

function openEditIssueDialog(row) {
  Object.assign(editForm, JSON.parse(JSON.stringify(row)))
  editForm.goodsIds = row.goodsIds ? String(row.goodsIds).split(',').filter(Boolean).map((item) => Number(item)) : []
  editForm.tearoomId = Number(row.tearoomId || 0)
  editLimitTime.value = row.isLimitTime || 1
  editHoliday.value = row.isHoliday || 2
  editWeeks.value = row.weeks ? String(row.weeks).split(',').filter(Boolean) : []
  editTimes.value = row.times ? String(row.times).split(',').filter(Boolean) : []
  editStartTime.value = ''
  editEndTime.value = ''
  editDialogVisible.value = true
  loadTearoomOptions()
  loadGoodsOptions()
}

function addEditTime() {
  if (!editStartTime.value) return showError('请选择开始时间')
  if (!editEndTime.value) return showError('请选择结束时间')
  editTimes.value.push(`${editStartTime.value}-${editEndTime.value}`)
  editStartTime.value = ''
  editEndTime.value = ''
}

function removeEditTime(index) {
  editTimes.value.splice(index, 1)
}

async function submitEditIssue() {
  if (editLimitTime.value === 1) {
    if (!editWeeks.value.length) return showError('请选择限制周期')
    if (!editTimes.value.length) return showError('请添加使用时间')
  } else {
    editWeeks.value = []
    editTimes.value = []
  }
  if (editForm.issueNumber === '') return showError('请输入发布数量')
  if (!editForm.teamRule) return showError('请输入使用规则')
  if (!editForm.buyRule) return showError('请输入购买须知')

  editDialogLoading.value = true
  try {
    await updateCouponIssueApi({
      ...editForm,
      goodsIds: Array.isArray(editForm.goodsIds) ? editForm.goodsIds.toString() : editForm.goodsIds,
      isLimitTime: editLimitTime.value,
      isHoliday: editHoliday.value,
      weeks: editWeeks.value.toString(),
      times: editTimes.value.toString(),
    })
    showSuccess('操作成功')
    editDialogVisible.value = false
    await loadIssueList()
  } finally {
    editDialogLoading.value = false
  }
}

loadIssueList()
</script>

<template>
  <div class="ci">
    <header class="ci-hero nf-page-hero">
      <span class="ci-hero__badge">优惠券</span>
      <h1 class="ci-hero__title">已发布优惠券</h1>
      <p class="ci-hero__desc">查看与管理已发布的团购券、积分兑换券、新人券与会员券，支持筛选、赠送、修改与删除</p>
    </header>

    <div class="ci-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="ci-filter__form" @submit.prevent>
        <el-form-item label="羽毛球馆名称"><el-input v-model="filters.tearoomName" clearable placeholder="请输入羽毛球馆名称" @keyup.enter="handleSearch" /></el-form-item>
        <el-form-item label="优惠券名称"><el-input v-model="filters.couponName" clearable placeholder="请输入优惠券名称" @keyup.enter="handleSearch" /></el-form-item>
        <el-form-item label="是否限制节假日使用"><el-select v-model="filters.isHoliday" clearable placeholder="请选择是否限制节假日使用" @change="handleSearch"><el-option v-for="item in holidayOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="周期"><el-select v-model="filters.weeks" multiple clearable class="ci-filter__weeks" placeholder="请选择周期" @change="handleSearch"><el-option v-for="item in weeksOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item class="ci-filter__actions"><el-space wrap><el-button type="primary" @click="handleSearch">刷新</el-button><el-button @click="handleReset">重置</el-button></el-space></el-form-item>
      </el-form>
    </div>

    <div class="ci-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane v-for="(tab, key) in TAB_CONFIG" :key="key" :label="tab.label" :name="key">
          <div class="ci-table nf-table-card">
          <el-table v-loading="loading" :data="issueList" border>
            <el-table-column prop="couponIssueId" label="编号" width="80" fixed="left" />
            <el-table-column label="券类型" fixed="left" width="100"><template #default="{ row }">{{ couponTypeText(row.couponType) }}</template></el-table-column>
            <el-table-column prop="tearoom.tearoomName" label="羽毛球馆名称" width="160" fixed="left" />
            <el-table-column label="羽毛球场名称" width="250"><template #default="{ row }"><span v-for="(item, index) in row.goodsList" :key="index">{{ item?.goodsName || '' }}<span v-if="index < row.goodsList.length - 1">;</span></span></template></el-table-column>
            <el-table-column prop="couponName" label="优惠券名称" width="150" fixed="left" />
            <el-table-column prop="issueNumber" label="发布数量" />
            <el-table-column prop="remainNumber" label="剩余数量" />
            <el-table-column v-if="currentType === 2" prop="integral" label="兑换积分" />
            <el-table-column prop="hour" label="使用时长(半小时)" />
            <el-table-column prop="validDay" label="有效期限(天)" />
            <el-table-column label="是否限制时间" width="120"><template #default="{ row }">{{ row.isLimitTime === 1 ? '是' : '否' }}</template></el-table-column>
            <el-table-column label="限制周期(可使用)" width="160"><template #default="{ row }">{{ rowWeeksText(row) }}</template></el-table-column>
            <el-table-column prop="times" label="使用时间" width="160" />
            <el-table-column label="使用规则" width="250"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.teamRule }}</div></template><div>{{ row.teamRule }}</div></el-popover></template></el-table-column>
            <el-table-column label="购买须知" width="250"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.buyRule }}</div></template><div>{{ row.buyRule }}</div></el-popover></template></el-table-column>
            <el-table-column label="是否限制节假日使用" fixed="right" width="150"><template #default="{ row }"><el-switch v-model="row.isHoliday" :active-value="1" :inactive-value="2" @change="handleRowChange(row)" /></template></el-table-column>
            <el-table-column label="状态" fixed="right" width="110"><template #default="{ row }"><el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleRowChange(row)" /></template></el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="150" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-space wrap>
                  <el-button size="small" type="primary" :disabled="!isAuth('coupon:add')" @click="openGiftDialog(row)">赠送优惠券</el-button>
                  <el-button size="small" type="primary" plain :disabled="!isAuth('coupon:update')" @click="openEditIssueDialog(row)">修改</el-button>
                  <el-button size="small" type="danger" :disabled="!isAuth('couponissue:delete')" @click="handleDelete(row)">删除</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
          <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadIssueList()" @update:pageSize="pageSize = $event; loadIssueList()" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="giftDialogVisible" title="赠送优惠券" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">赠送类型</span><el-radio-group v-model="giftType"><el-radio :value="1">指定用户</el-radio><el-radio :value="2">所有用户</el-radio><el-radio :value="3">订单用户</el-radio><el-radio :value="4">超过指定天数没来的用户</el-radio></el-radio-group></div>
        <div v-if="giftType === 1 || giftType === 3" class="form-row form-row-top"><span class="form-label">选择用户</span><div class="dialog-field"><el-tag v-for="(tag, index) in selectedUserNames" :key="`${tag}-${index}`" closable @close="removeSelectedUser(tag, index)">{{ tag }}</el-tag><el-button size="small" @click="openUserDialog">添加</el-button></div></div>
        <div v-if="giftType === 4" class="form-row"><span class="form-label">超过天数</span><el-input v-model="inactiveDays" class="dialog-field" placeholder="请输入超过天数" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="giftDialogVisible = false">取消</el-button><el-button type="primary" :loading="giftDialogLoading" @click="submitGift">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="userDialogVisible" title="用户列表" width="75%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="邀请人邀请码"><el-input v-model="userFilters.inviterCode" clearable placeholder="请输入邀请人邀请码" @keyup.enter="userPage = 1; loadUsers()" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="userFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="userPage = 1; loadUsers()" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="userFilters.nickName" clearable placeholder="请输入昵称" @keyup.enter="userPage = 1; loadUsers()" /></el-form-item>
        <el-form-item label="会员等级"><el-select v-model="userFilters.vipIndex" clearable style="width: 150px" @change="userPage = 1; loadUsers()"><el-option v-for="item in vipOptions" :key="item.vipIndex" :label="item.vipName" :value="item.vipIndex" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="userFilters.startTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="截止时间"><el-date-picker v-model="userFilters.endTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item v-if="giftType === 3" label="下单开始时间"><el-date-picker v-model="userFilters.ordersStartTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item v-if="giftType === 3" label="下单截止时间"><el-date-picker v-model="userFilters.ordersEndTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item><el-space><el-button type="primary" @click="userPage = 1; loadUsers()">查询</el-button><el-button @click="Object.assign(userFilters, { inviterCode: '', phone: '', nickName: '', vipIndex: '', startTime: '', endTime: '', ordersStartTime: '', ordersEndTime: '' }); userPage = 1; loadUsers()">重置</el-button><el-button type="primary" :disabled="selectedUserRows.length <= 0" @click="confirmUsers">确定</el-button></el-space></el-form-item>
      </el-form>
      <el-table v-loading="userLoading" :data="userList" @selection-change="handleUserSelectionChange" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userId" label="id" width="80" fixed="left" />
        <el-table-column prop="userName" label="昵称" width="120" fixed="left" />
        <el-table-column label="图像" width="90"><template #default="{ row }"><el-avatar v-if="row.avatar" :src="row.avatar" :size="40" /><el-avatar v-else :size="40">{{ avatarText(row) }}</el-avatar></template></el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="会员等级" width="120"><template #default="{ row }">{{ row.vipIndex ? row.vipName : '普通用户' }}</template></el-table-column>
        <el-table-column prop="inviterCode" label="邀请人邀请码" min-width="140" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
      </el-table>
      <BasePagination :current-page="userPage" :page-size="userSize" :page-sizes="[10, 20, 30, 50, 100]" :total="userTotal" @update:currentPage="userPage = $event; loadUsers()" @update:pageSize="userSize = $event; loadUsers()" />
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改优惠券" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">选择羽毛球馆</span><el-select v-model="editForm.tearoomId" class="dialog-field" placeholder="请选择羽毛球馆" @change="loadGoodsOptions"><el-option v-for="item in tearoomOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" /></el-select></div>
        <div v-if="editForm.couponType === 1" class="form-row form-row-top"><span class="form-label">选择羽毛球场</span><el-checkbox-group v-model="editForm.goodsIds" class="checkbox-wrap"><el-checkbox v-for="item in goodsOptions" :key="item.goodsId" :label="item.goodsId">{{ item.goodsName }}</el-checkbox></el-checkbox-group></div>
        <div class="form-row"><span class="form-label">优惠券ID</span><el-input :model-value="editForm.couponId" class="dialog-field" disabled /></div>
        <div class="form-row"><span class="form-label">优惠券名称</span><el-input v-model="editForm.couponName" class="dialog-field" /></div>
        <div class="form-row"><span class="form-label">是否限量</span><el-radio-group v-model="editForm.isLimit"><el-radio :value="1">限量</el-radio><el-radio :value="2">不限量</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">发布数量</span><el-input v-model="editForm.issueNumber" class="dialog-field" type="number" placeholder="请输入发布数量" /></div>
        <div class="form-row"><span class="form-label">是否限制时间</span><el-radio-group v-model="editLimitTime"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></div>
        <div v-if="editLimitTime === 1" class="form-row form-row-top"><span class="form-label">限制周期(可使用)</span><el-checkbox-group v-model="editWeeks" class="checkbox-wrap"><el-checkbox label="1">周一</el-checkbox><el-checkbox label="2">周二</el-checkbox><el-checkbox label="3">周三</el-checkbox><el-checkbox label="4">周四</el-checkbox><el-checkbox label="5">周五</el-checkbox><el-checkbox label="6">周六</el-checkbox><el-checkbox label="7">周日</el-checkbox></el-checkbox-group></div>
        <div class="form-row"><span class="form-label">是否限制节假日使用</span><el-radio-group v-model="editHoliday"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></div>
        <div v-if="editLimitTime === 1" class="form-row form-row-top"><span class="form-label">使用时间</span><div class="dialog-field"><div class="time-tags"><span v-for="(item, index) in editTimes" :key="`${item}-${index}`" class="time-tag">{{ item }}<el-button link type="danger" @click="removeEditTime(index)">删</el-button></span></div><div class="time-picker-row"><el-time-picker v-model="editStartTime" style="width: 150px" placeholder="起始时间" format="HH:mm" value-format="HH:mm" /><el-time-picker v-model="editEndTime" style="width: 150px" placeholder="结束时间" format="HH:mm" value-format="HH:mm" /><el-button type="primary" size="small" circle @click="addEditTime">+</el-button></div></div></div>
        <div class="form-row"><span class="form-label">状态</span><el-radio-group v-model="editForm.status"><el-radio :value="1">开启</el-radio><el-radio :value="2">关闭</el-radio></el-radio-group></div>
        <div class="form-row form-row-top"><span class="form-label">使用规则</span><el-input v-model="editForm.teamRule" class="dialog-field" type="textarea" :rows="3" placeholder="请输入使用规则" /></div>
        <div class="form-row form-row-top"><span class="form-label">购买须知</span><el-input v-model="editForm.buyRule" class="dialog-field" type="textarea" :rows="3" placeholder="请输入购买须知" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取消</el-button><el-button type="primary" :loading="editDialogLoading" @click="submitEditIssue">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.ci {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.ci-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ci-hero__badge {
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

.ci-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.ci-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.ci-filter.nf-filter-card {
  margin-bottom: 0;
}

.ci-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.ci-filter__weeks {
  width: 260px;
}

.ci-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.ci-tabs {
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

.ci-table.nf-table-card {
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
  width: 160px;
  text-align: right;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
}

.checkbox-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.clamp-text {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  max-height: 80px;
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
  margin-bottom: 8px;
}

.time-tag {
  padding: 4px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .ci-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }

  .ci-filter__weeks {
    width: 100%;
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

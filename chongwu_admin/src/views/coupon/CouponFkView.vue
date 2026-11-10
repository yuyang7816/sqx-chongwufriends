<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  deleteCouponFkCardsApi,
  deleteCouponFkTypeApi,
  exportCouponFkCardsApi,
  generateCouponFkCardApi,
  getCouponFkCardPageApi,
  getCouponFkIssuePageApi,
  getCouponFkTypePageApi,
  saveCouponFkTypeApi,
} from '@/api/modules/coupon-fk'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const router = useRouter()

const activeTab = ref('first')

const typeLoading = ref(false)
const typePage = ref(1)
const typeSize = ref(10)
const typeTotal = ref(0)
const typeList = ref([])

const cardLoading = ref(false)
const cardPage = ref(1)
const cardSize = ref(10)
const cardTotal = ref(0)
const cardList = ref([])
const selectedCardRows = ref([])

const cardFilters = reactive({
  sdkContent: '',
  sdkRemarks: '',
  nickName: '',
  status: '',
  startTime: '',
  endTime: '',
})

const cardStatusOptions = [
  { label: '全部', value: '' },
  { label: '待使用', value: 0 },
  { label: '已使用', value: 1 },
  { label: '已过期', value: 2 },
]

const typeDialogVisible = ref(false)
const typeDialogLoading = ref(false)
const typeDialogTitle = ref('添加')
const typeForm = reactive({
  typeId: '',
  money: '',
  remarks: '',
  validDay: '',
  sdkClassify: 1,
})
const selectedCouponIds = ref([])
const selectedCoupons = ref([])

const generateDialogVisible = ref(false)
const generateDialogLoading = ref(false)
const generateNum = ref('')
const currentTypeId = ref('')

const issuePickerVisible = ref(false)
const issuePickerLoading = ref(false)
const issuePickerPage = ref(1)
const issuePickerSize = ref(10)
const issuePickerTotal = ref(0)
const issuePickerList = ref([])
const issuePickerType = ref(1)
const issuePickerName = ref('')
const selectedIssueRows = ref([])

const issueTypeOptions = [
  { value: 1, label: '团购券' },
  { value: 2, label: '积分兑换券' },
  { value: 3, label: '新人券' },
]

const cardListDialogVisible = ref(false)
const cardDialogTypeId = ref('')

function sdkClassifyText(value) {
  return Number(value) === 1 ? '金额' : Number(value) === 2 ? '优惠券' : '-'
}

function cardStatusText(value) {
  if (Number(value) === 0) return '待使用'
  if (Number(value) === 1) return '已使用'
  if (Number(value) === 2) return '已过期'
  return '-'
}

function normalizeTypeRow(item = {}) {
  return {
    ...item,
    typeId: item.typeId,
    sdkClassify: Number(item.sdkClassify || 1),
    couponIds: item.couponIds || '',
    selfCouponIssueList: (item.selfCouponIssueList || []).filter(Boolean),
    remarks: item.remarks || '',
    money: item.money ?? '',
    validDay: item.validDay ?? '',
    createTime: item.createTime || item.createdAt || '',
  }
}

function normalizeCardRow(item = {}) {
  return {
    ...item,
    sdkId: item.sdkId,
    sdkRemarks: item.sdkRemarks || '',
    sdkContent: item.sdkContent || '',
    money: item.money ?? '',
    overdueTime: item.overdueTime || '',
    nickName: item.nickName || '',
    userId: item.userId || 0,
    status: Number(item.status ?? 0),
    createTime: item.createTime || item.createdAt || '',
  }
}

function normalizeIssueRow(item = {}) {
  return {
    ...item,
    couponIssueId: item.couponIssueId,
    couponName: item.couponName || '',
    issueNumber: item.issueNumber ?? 0,
    remainNumber: item.remainNumber ?? 0,
    hour: item.hour || '',
    validDay: item.validDay ?? 0,
    teamRule: item.teamRule || '',
    type: Number(item.type || 0),
  }
}

async function loadTypeList() {
  typeLoading.value = true
  try {
    const data = await getCouponFkTypePageApi({
      page: typePage.value,
      limit: typeSize.value,
      remarks: '',
    })
    typeList.value = (data.list || []).map(normalizeTypeRow)
    typeTotal.value = data.total || 0
    if (typePage.value > 1 && !typeList.value.length) {
      typePage.value -= 1
      await loadTypeList()
    }
  } finally {
    typeLoading.value = false
  }
}

function buildCardParams(typeId = '') {
  return {
    page: cardPage.value,
    limit: cardSize.value,
    sdkRemarks: cardFilters.sdkRemarks.trim(),
    status: cardFilters.status,
    typeId: typeId || cardDialogTypeId.value,
    nickName: cardFilters.nickName.trim(),
    sdkContent: cardFilters.sdkContent.trim(),
    endTime: cardFilters.endTime,
    startTime: cardFilters.startTime,
  }
}

async function loadCardList(typeId = '') {
  cardLoading.value = true
  try {
    const data = await getCouponFkCardPageApi(buildCardParams(typeId))
    cardList.value = (data.list || []).map(normalizeCardRow)
    cardTotal.value = data.total || 0
  } finally {
    cardLoading.value = false
  }
}

watch(activeTab, (value) => {
  if (value === 'first') {
    typePage.value = 1
    typeSize.value = 10
    loadTypeList()
    return
  }
  cardDialogTypeId.value = ''
  cardPage.value = 1
  cardSize.value = 10
  loadCardList('')
})

function resetTypeForm() {
  Object.assign(typeForm, {
    typeId: '',
    money: '',
    remarks: '',
    validDay: '',
    sdkClassify: 1,
  })
  selectedCouponIds.value = []
  selectedCoupons.value = []
}

function openTypeDialog(row) {
  typeDialogVisible.value = true
  if (row) {
    typeDialogTitle.value = '修改'
    Object.assign(typeForm, {
      typeId: row.typeId,
      money: row.money ?? '',
      remarks: row.remarks || '',
      validDay: row.validDay ?? '',
      sdkClassify: row.sdkClassify,
    })
    selectedCouponIds.value = row.couponIds ? String(row.couponIds).split(',').filter(Boolean) : []
    selectedCoupons.value = (row.selfCouponIssueList || []).filter(Boolean)
    return
  }
  typeDialogTitle.value = '添加'
  resetTypeForm()
}

async function submitType() {
  if (!typeForm.remarks.trim()) return showError('请输入名称')
  if (Number(typeForm.sdkClassify) === 1) {
    if (typeForm.money === '' || typeForm.money == null) return showError('请输入金额')
    if (typeForm.validDay === '' || typeForm.validDay == null) return showError('请输入有效天数')
  } else if (!selectedCoupons.value.length) {
    return showError('请选择优惠券')
  }

  typeDialogLoading.value = true
  try {
    await saveCouponFkTypeApi({
      typeId: typeForm.typeId,
      money: typeForm.money,
      remarks: typeForm.remarks.trim(),
      validDay: typeForm.validDay,
      couponIds: selectedCouponIds.value.toString(),
      sdkClassify: typeForm.sdkClassify,
    })
    showSuccess('操作成功')
    typeDialogVisible.value = false
    await loadTypeList()
  } finally {
    typeDialogLoading.value = false
  }
}

async function deleteType(row) {
  await confirmAction('确定删除此条信息?')
  await deleteCouponFkTypeApi(row.typeId)
  showSuccess('删除成功')
  await loadTypeList()
}

function openGenerateDialog(row) {
  currentTypeId.value = row.typeId
  generateNum.value = ''
  generateDialogVisible.value = true
}

async function submitGenerate() {
  if (!generateNum.value) return showError('请输入生成数量')
  if (Number(generateNum.value) > 100) return showError('单次生成数量数量最高为100')

  generateDialogLoading.value = true
  try {
    await generateCouponFkCardApi({
      typeId: currentTypeId.value,
      num: generateNum.value,
    })
    showSuccess('操作成功')
    generateDialogVisible.value = false
    await loadTypeList()
  } finally {
    generateDialogLoading.value = false
  }
}

async function openIssuePicker() {
  issuePickerPage.value = 1
  issuePickerType.value = 1
  issuePickerName.value = ''
  selectedIssueRows.value = []
  issuePickerVisible.value = true
  await loadIssuePickerList()
}

async function loadIssuePickerList() {
  issuePickerLoading.value = true
  try {
    const data = await getCouponFkIssuePageApi({
      page: issuePickerPage.value - 1,
      size: issuePickerSize.value,
      type: issuePickerType.value,
      tearoomName: '',
      couponName: issuePickerName.value.trim(),
    })
    issuePickerList.value = (data.list || []).map(normalizeIssueRow)
    issuePickerTotal.value = data.total || 0
  } finally {
    issuePickerLoading.value = false
  }
}

function handleIssueSelectionChange(rows) {
  selectedIssueRows.value = rows
}

function confirmIssues() {
  selectedIssueRows.value.forEach((item) => {
    const id = String(item.couponIssueId)
    if (!selectedCouponIds.value.includes(id)) {
      selectedCouponIds.value.push(id)
      selectedCoupons.value.push(item)
    }
  })
  issuePickerVisible.value = false
}

function removeCoupon(_item, index) {
  if (selectedCouponIds.value.length < 2) return showError('最少需要选中一张优惠券')
  selectedCouponIds.value.splice(index, 1)
  selectedCoupons.value.splice(index, 1)
}

async function openCardDialog(row) {
  cardDialogTypeId.value = row.typeId
  cardFilters.sdkRemarks = ''
  cardFilters.sdkContent = ''
  cardFilters.status = ''
  cardFilters.nickName = ''
  cardFilters.startTime = ''
  cardFilters.endTime = ''
  cardPage.value = 1
  await loadCardList(row.typeId)
  cardListDialogVisible.value = true
}

function handleCardSelectionChange(rows) {
  selectedCardRows.value = rows
}

async function deleteCard(row) {
  await confirmAction('确定删除此条信息?')
  await deleteCouponFkCardsApi(row.sdkId)
  showSuccess('删除成功')
  await loadCardList()
}

async function deleteCardsBulk() {
  if (!selectedCardRows.value.length) return
  await confirmAction('确定删除选中的卡密吗？')
  await deleteCouponFkCardsApi(selectedCardRows.value.map((item) => item.sdkId).toString())
  showSuccess('删除成功')
  await loadCardList()
}

function downloadBlob(blob, filename) {
  if (window.navigator.msSaveOrOpenBlob) {
    window.navigator.msSaveBlob(blob, filename)
    return
  }
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.download = filename
  link.style.display = 'none'
  link.href = url
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

async function exportCards() {
  const blob = await exportCouponFkCardsApi(buildCardParams())
  downloadBlob(blob, '卡密列表.xlsx')
}

function goUserDetail(row) {
  if (!row.userId) return
  router.push(`/users/${row.userId}`)
}

loadTypeList()
</script>

<template>
  <div class="cfk">
    <header class="cfk-hero nf-page-hero">
      <span class="cfk-hero__badge">优惠券</span>
      <h1 class="cfk-hero__title">发卡管理</h1>
      <p class="cfk-hero__desc">管理发卡分类与卡密列表，支持分类维护、卡密生成、筛选查询与批量操作</p>
    </header>

    <div v-if="activeTab === 'first'" class="cfk-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>操作</span>
      </div>
      <el-form :inline="true" class="cfk-filter__form" @submit.prevent>
        <el-form-item class="cfk-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="typePage = 1; loadTypeList()">刷新</el-button>
            <el-button type="primary" :disabled="!isAuth('couponFk:add')" @click="openTypeDialog()">添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div v-else class="cfk-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="cfk-filter__form" @submit.prevent>
        <el-form-item label="卡密"><el-input v-model="cardFilters.sdkContent" clearable placeholder="请输入卡密" @keyup.enter="cardPage = 1; loadCardList('')" /></el-form-item>
        <el-form-item label="卡密名称"><el-input v-model="cardFilters.sdkRemarks" clearable placeholder="请输入卡密名称" @keyup.enter="cardPage = 1; loadCardList('')" /></el-form-item>
        <el-form-item label="领取用户"><el-input v-model="cardFilters.nickName" clearable placeholder="请输入领取用户" @keyup.enter="cardPage = 1; loadCardList('')" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="cardFilters.status" clearable style="width: 150px" @change="cardPage = 1; loadCardList('')"><el-option v-for="item in cardStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="cardFilters.startTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="截止时间"><el-date-picker v-model="cardFilters.endTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item class="cfk-filter__actions"><el-space wrap><el-button type="primary" @click="cardPage = 1; loadCardList('')">查询</el-button><el-button @click="Object.assign(cardFilters, { sdkContent: '', sdkRemarks: '', nickName: '', status: '', startTime: '', endTime: '' }); cardPage = 1; loadCardList('')">重置</el-button><el-button type="primary" :disabled="selectedCardRows.length <= 0 || !isAuth('financeList:transfer')" @click="deleteCardsBulk">批量删除</el-button></el-space></el-form-item>
      </el-form>
    </div>

    <div class="cfk-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="发卡分类" name="first">
          <div class="cfk-table nf-table-card">
            <el-table v-loading="typeLoading" :data="typeList" border>
              <el-table-column prop="typeId" label="编号" width="80" />
              <el-table-column label="卡密类型" width="100"><template #default="{ row }">{{ sdkClassifyText(row.sdkClassify) }}</template></el-table-column>
              <el-table-column label="优惠券" width="220"><template #default="{ row }"><div v-if="row.selfCouponIssueList?.length"><div v-for="(item, index) in row.selfCouponIssueList" :key="index">{{ item?.couponName || '-' }}</div></div><div v-else>-</div></template></el-table-column>
              <el-table-column prop="remarks" label="名称" min-width="140" />
              <el-table-column prop="money" label="金额" width="100" />
              <el-table-column prop="validDay" label="有效天数" width="100" />
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="操作" width="240" fixed="right">
                <template #default="{ row }">
                  <el-space wrap>
                    <el-button size="small" type="primary" :disabled="!isAuth('couponFk:update')" @click="openTypeDialog(row)">编辑</el-button>
                    <el-button size="small" type="warning" @click="openCardDialog(row)">卡密列表</el-button>
                    <el-button size="small" type="danger" :disabled="!isAuth('couponFk:delete')" @click="deleteType(row)">删除</el-button>
                    <el-button size="small" type="primary" :disabled="!isAuth('couponFk:update')" @click="openGenerateDialog(row)">生成卡密</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>
            <BasePagination :current-page="typePage" :page-size="typeSize" :page-sizes="[10, 20, 30, 40]" :total="typeTotal" @update:currentPage="typePage = $event; loadTypeList()" @update:pageSize="typeSize = $event; loadTypeList()" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="发卡列表" name="putong">
          <div class="cfk-table nf-table-card">
            <el-table v-loading="cardLoading" :data="cardList" @selection-change="handleCardSelectionChange" border>
              <el-table-column type="selection" width="55" />
              <el-table-column prop="sdkId" label="编号" width="80" fixed="left" />
              <el-table-column prop="sdkRemarks" label="卡密名称" min-width="140" />
              <el-table-column prop="sdkContent" label="卡密" min-width="180" />
              <el-table-column prop="money" label="金额" width="100" />
              <el-table-column prop="overdueTime" label="到期时间" width="160" />
              <el-table-column label="领取用户" min-width="120"><template #default="{ row }"><span v-if="row.nickName" class="link-text" @click="goUserDetail(row)">{{ row.nickName }}</span><span v-else>未绑定</span></template></el-table-column>
              <el-table-column label="状态" width="100"><template #default="{ row }"><span :class="row.status === 0 ? 'text-danger' : row.status === 1 ? 'text-primary' : 'text-muted'">{{ cardStatusText(row.status) }}</span></template></el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="150" />
              <el-table-column label="操作" width="80"><template #default="{ row }"><el-button size="small" type="danger" :disabled="!isAuth('couponFk:delete')" @click="deleteCard(row)">删除</el-button></template></el-table-column>
            </el-table>
            <BasePagination :current-page="cardPage" :page-size="cardSize" :page-sizes="[10, 20, 30, 50, 100]" :total="cardTotal" @update:currentPage="cardPage = $event; loadCardList('')" @update:pageSize="cardSize = $event; loadCardList('')" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="680px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">类型</span><el-radio-group v-model="typeForm.sdkClassify"><el-radio :value="1">金额</el-radio><el-radio :value="2">优惠券</el-radio></el-radio-group></div>
        <div v-if="typeForm.sdkClassify === 2" class="form-row form-row-top"><span class="form-label">优惠券</span><div class="dialog-field"><el-tag v-for="(tag, index) in selectedCoupons" :key="`${tag?.couponIssueId || index}-${index}`" closable @close="removeCoupon(tag, index)">{{ tag?.couponName || '-' }}</el-tag><el-button size="small" @click="openIssuePicker">添加</el-button></div></div>
        <div class="form-row"><span class="form-label">名称</span><el-input v-model="typeForm.remarks" class="dialog-field" placeholder="请输入名称" /></div>
        <div v-if="typeForm.sdkClassify === 1" class="form-row"><span class="form-label">金额</span><el-input v-model="typeForm.money" class="dialog-field" placeholder="请输入金额" /></div>
        <div v-if="typeForm.sdkClassify === 1" class="form-row"><span class="form-label">有效天数</span><el-input v-model="typeForm.validDay" class="dialog-field" placeholder="请输入有效天数" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="typeDialogVisible = false">取消</el-button><el-button type="primary" :loading="typeDialogLoading" @click="submitType">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="generateDialogVisible" title="生成卡密" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack"><div class="form-row"><span class="form-label">生成数量</span><el-input v-model="generateNum" class="dialog-field" type="number" placeholder="请输入生成数量" /></div></div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="generateDialogVisible = false">取消</el-button><el-button type="primary" :loading="generateDialogLoading" @click="submitGenerate">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="cardListDialogVisible" title="卡密列表" width="70%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="领取用户"><el-input v-model="cardFilters.nickName" clearable placeholder="请输入领取用户" @keyup.enter="cardPage = 1; loadCardList()" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="cardFilters.status" clearable style="width: 150px" @change="cardPage = 1; loadCardList()"><el-option v-for="item in cardStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="cardFilters.startTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="截止时间"><el-date-picker v-model="cardFilters.endTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item><el-space><el-button type="primary" @click="cardPage = 1; loadCardList()">查询</el-button><el-button @click="Object.assign(cardFilters, { sdkRemarks: '', sdkContent: '', status: '', nickName: '', startTime: '', endTime: '' }); cardPage = 1; loadCardList()">重置</el-button><el-button type="warning" :disabled="!isAuth('couponFk:delete')" @click="exportCards">导出excel</el-button></el-space></el-form-item>
      </el-form>
      <el-table v-loading="cardLoading" :data="cardList" border>
        <el-table-column prop="sdkId" label="编号" width="80" fixed="left" />
        <el-table-column prop="sdkRemarks" label="卡密名称" min-width="140" />
        <el-table-column prop="sdkContent" label="卡密" min-width="180" />
        <el-table-column prop="money" label="金额" width="80" />
        <el-table-column prop="overdueTime" label="到期时间" width="160" />
        <el-table-column label="领取用户" min-width="120"><template #default="{ row }"><span v-if="row.nickName" class="link-text" @click="goUserDetail(row)">{{ row.nickName }}</span><span v-else>未绑定</span></template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><span :class="row.status === 0 ? 'text-danger' : row.status === 1 ? 'text-primary' : 'text-muted'">{{ cardStatusText(row.status) }}</span></template></el-table-column>
        <el-table-column prop="createTime" label="领取时间" width="150" />
        <el-table-column label="操作" width="80"><template #default="{ row }"><el-button size="small" type="danger" :disabled="!isAuth('couponFk:delete')" @click="deleteCard(row)">删除</el-button></template></el-table-column>
      </el-table>
      <BasePagination :current-page="cardPage" :page-size="cardSize" :page-sizes="[10, 20, 30, 50, 100]" :total="cardTotal" @update:currentPage="cardPage = $event; loadCardList()" @update:pageSize="cardSize = $event; loadCardList()" />
    </el-dialog>

    <el-dialog v-model="issuePickerVisible" title="优惠券列表" width="70%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="类型"><el-select v-model="issuePickerType" style="width: 150px" @change="issuePickerPage = 1; loadIssuePickerList()"><el-option v-for="item in issueTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="优惠券名称"><el-input v-model="issuePickerName" clearable placeholder="请输入优惠券名称" @keyup.enter="issuePickerPage = 1; loadIssuePickerList()" /></el-form-item>
        <el-form-item><el-space><el-button type="primary" @click="issuePickerPage = 1; loadIssuePickerList()">查询</el-button><el-button @click="issuePickerType = 1; issuePickerName = ''; issuePickerPage = 1; loadIssuePickerList()">重置</el-button><el-button type="primary" :disabled="selectedIssueRows.length <= 0" @click="confirmIssues">确定</el-button></el-space></el-form-item>
      </el-form>
      <el-table v-loading="issuePickerLoading" :data="issuePickerList" @selection-change="handleIssueSelectionChange" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="couponIssueId" label="编号" width="80" fixed="left" />
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ row.type === 1 ? '团购券' : row.type === 2 ? '积分兑换券' : '新人券' }}</template></el-table-column>
        <el-table-column prop="couponName" label="优惠券名称" width="150" fixed="left" />
        <el-table-column prop="issueNumber" label="发布数量" />
        <el-table-column prop="remainNumber" label="剩余数量" />
        <el-table-column prop="hour" label="使用时长(半小时)" />
        <el-table-column prop="validDay" label="有效期限(天)" />
        <el-table-column label="使用规则" width="250"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.teamRule }}</div></template><div>{{ row.teamRule }}</div></el-popover></template></el-table-column>
      </el-table>
      <BasePagination :current-page="issuePickerPage" :page-size="issuePickerSize" :page-sizes="[10, 20, 30, 50, 100]" :total="issuePickerTotal" @update:currentPage="issuePickerPage = $event; loadIssuePickerList()" @update:pageSize="issuePickerSize = $event; loadIssuePickerList()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.cfk {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.cfk-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cfk-hero__badge {
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

.cfk-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.cfk-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.cfk-filter.nf-filter-card {
  margin-bottom: 0;
}

.cfk-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.cfk-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.cfk-tabs {
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

.cfk-table.nf-table-card {
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

.search-grid {
  margin-bottom: 16px;
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
}

.link-text {
  color: var(--nf-brand-deep);
  cursor: pointer;
}

.text-primary {
  color: var(--nf-brand-deep);
}

.text-danger {
  color: #f56c6c;
}

.text-muted {
  color: #999;
}

.clamp-text {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  max-height: 80px;
}

@media (max-width: 768px) {
  .cfk-filter__form {
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

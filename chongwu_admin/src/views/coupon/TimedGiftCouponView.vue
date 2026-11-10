<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  createTimedGiftCouponApi,
  deleteTimedGiftCouponApi,
  getTimedGiftCouponIssuePageApi,
  getTimedGiftCouponPageApi,
  updateTimedGiftCouponApi,
} from '@/api/modules/timed-gift-coupon'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const activeName = ref('first')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activityList = ref([])
const activityNameFilter = ref('')

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加')
const form = reactive({
  couponActivityId: '',
  activityName: '',
  activityTime: '',
  endActivityTime: '',
  privilegeType: '',
})
const selectedCouponIds = ref([])
const selectedCoupons = ref([])

const issueDialogVisible = ref(false)
const issueLoading = ref(false)
const issuePage = ref(1)
const issueSize = ref(10)
const issueTotal = ref(0)
const issueList = ref([])
const issueType = ref(1)
const issueStatus = ref('')
const issueName = ref('')
const selectedIssueRows = ref([])

const issueTypeOptions = [
  { value: '', title: '全部' },
  { value: 1, title: '团购券' },
  { value: 2, title: '积分兑换券' },
  { value: 3, title: '新人券' },
]

const issueStatusOptions = [
  { value: '', title: '全部' },
  { value: 1, title: '开启' },
  { value: 2, title: '关闭' },
]

const privilegeTypeOptions = [
  { value: 1, label: '每天' },
  { value: 2, label: '每周' },
  { value: 3, label: '每月' },
  { value: 4, label: '每季度' },
  { value: 5, label: '每年' },
]

function privilegeTypeText(value) {
  return privilegeTypeOptions.find((item) => item.value === Number(value))?.label || '-'
}

function normalizeActivityRow(item = {}) {
  return {
    ...item,
    couponActivityId: item.couponActivityId,
    activityName: item.activityName || '',
    activityTime: item.activityTime || '',
    endActivityTime: item.endActivityTime || '',
    privilegeType: Number(item.privilegeType || 0),
    selfCouponIssueList: (item.selfCouponIssueList || []).filter(Boolean),
    couponIds: item.couponIds || '',
    createTime: item.createTime || item.createdAt || '',
  }
}

function normalizeIssueRow(item = {}) {
  return {
    ...item,
    couponIssueId: item.couponIssueId,
    type: Number(item.type || 0),
    couponName: item.couponName || '',
    issueNumber: item.issueNumber ?? 0,
    remainNumber: item.remainNumber ?? 0,
    hour: item.hour || '',
    validDay: item.validDay ?? 0,
    teamRule: item.teamRule || '',
  }
}

async function loadActivityList() {
  loading.value = true
  try {
    const data = await getTimedGiftCouponPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      activityName: activityNameFilter.value.trim(),
    })
    activityList.value = (data.list || []).map(normalizeActivityRow)
    total.value = data.total || 0
    if (pageNum.value > 1 && !activityList.value.length) {
      pageNum.value -= 1
      await loadActivityList()
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadActivityList()
}

function resetForm() {
  Object.assign(form, {
    couponActivityId: '',
    activityName: '',
    activityTime: '',
    endActivityTime: '',
    privilegeType: '',
  })
  selectedCouponIds.value = []
  selectedCoupons.value = []
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改'
    Object.assign(form, {
      couponActivityId: row.couponActivityId,
      activityName: row.activityName || '',
      activityTime: row.activityTime || '',
      endActivityTime: row.endActivityTime || '',
      privilegeType: row.privilegeType || '',
    })
    selectedCouponIds.value = row.couponIds ? String(row.couponIds).split(',').filter(Boolean) : []
    selectedCoupons.value = (row.selfCouponIssueList || []).filter(Boolean)
    return
  }
  dialogTitle.value = '添加'
  resetForm()
}

async function submitDialog() {
  if (!selectedCouponIds.value.length) return showError('请选择优惠券')
  if (!form.activityName.trim()) return showError('请输入活动名称')
  if (!form.activityTime) return showError('请选择活动开始时间')
  if (!form.endActivityTime) return showError('请选择活动结束时间')
  if (!form.privilegeType) return showError('请选择触发频率')

  dialogLoading.value = true
  try {
    const payload = {
      couponActivityId: form.couponActivityId || undefined,
      activityName: form.activityName.trim(),
      activityTime: form.activityTime,
      couponIds: selectedCouponIds.value.toString(),
      endActivityTime: form.endActivityTime,
      privilegeType: form.privilegeType,
    }
    if (form.couponActivityId) {
      await updateTimedGiftCouponApi(payload)
    } else {
      delete payload.couponActivityId
      await createTimedGiftCouponApi(payload)
    }
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadActivityList()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确定删除此条信息?')
  await deleteTimedGiftCouponApi(row.couponActivityId)
  showSuccess('删除成功')
  await loadActivityList()
}

async function openIssueDialog() {
  issuePage.value = 1
  issueType.value = 1
  issueStatus.value = ''
  issueName.value = ''
  selectedIssueRows.value = []
  issueDialogVisible.value = true
  await loadIssueList()
}

async function loadIssueList() {
  issueLoading.value = true
  try {
    const data = await getTimedGiftCouponIssuePageApi({
      page: issuePage.value - 1,
      size: issueSize.value,
      type: issueType.value,
      tearoomName: '',
      couponName: issueName.value.trim(),
      status: issueStatus.value,
    })
    issueList.value = (data.list || []).map(normalizeIssueRow)
    issueTotal.value = data.total || 0
  } finally {
    issueLoading.value = false
  }
}

function handleIssueSelectionChange(rows) {
  selectedIssueRows.value = rows
}

function removeCoupon(_item, index) {
  if (selectedCouponIds.value.length < 2) return showError('最少需要选中一张优惠券')
  selectedCouponIds.value.splice(index, 1)
  selectedCoupons.value.splice(index, 1)
}

function confirmIssues() {
  selectedIssueRows.value.forEach((item) => {
    const id = String(item.couponIssueId)
    if (!selectedCouponIds.value.includes(id)) {
      selectedCouponIds.value.push(id)
      selectedCoupons.value.push(item)
    }
  })
  issueDialogVisible.value = false
}

loadActivityList()
</script>

<template>
  <div class="tgc">
    <header class="tgc-hero nf-page-hero">
      <span class="tgc-hero__badge">优惠券</span>
      <h1 class="tgc-hero__title">优惠券活动</h1>
      <p class="tgc-hero__desc">配置定时赠送优惠券活动，支持按活动名称筛选、添加、编辑与删除</p>
    </header>

    <div class="tgc-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="tgc-filter__form" @submit.prevent>
        <el-form-item label="活动名称">
          <el-input v-model="activityNameFilter" clearable placeholder="请输入活动名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="tgc-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">刷新</el-button>
            <el-button type="primary" :disabled="!isAuth('timedGiftCoupon:add')" @click="openDialog()">添加</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="tgc-tabs">
      <el-tabs v-model="activeName">
        <el-tab-pane label="优惠券活动列表" name="first">
          <div class="tgc-table nf-table-card">
            <el-table v-loading="loading" :data="activityList" border>
              <el-table-column prop="couponActivityId" label="编号" width="80" />
              <el-table-column label="优惠券" width="220"><template #default="{ row }"><div v-if="row.selfCouponIssueList?.length"><div v-for="(item, index) in row.selfCouponIssueList" :key="index">{{ item?.couponName || '-' }}</div></div><div v-else>-</div></template></el-table-column>
              <el-table-column prop="activityName" label="活动名称" min-width="160" />
              <el-table-column prop="activityTime" label="活动开始时间" width="180" />
              <el-table-column prop="endActivityTime" label="活动结束时间" width="180" />
              <el-table-column label="触发频率" width="100"><template #default="{ row }">{{ privilegeTypeText(row.privilegeType) }}</template></el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="操作" width="160" fixed="right"><template #default="{ row }"><el-space wrap><el-button size="small" type="primary" :disabled="!isAuth('timedGiftCoupon:update')" @click="openDialog(row)">编辑</el-button><el-button size="small" type="danger" :disabled="!isAuth('timedGiftCoupon:delete')" @click="handleDelete(row)">删除</el-button></el-space></template></el-table-column>
            </el-table>

            <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 40]" :total="total" @update:currentPage="pageNum = $event; loadActivityList()" @update:pageSize="pageSize = $event; loadActivityList()" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row form-row-top"><span class="form-label">优惠券</span><div class="dialog-field"><el-tag v-for="(tag, index) in selectedCoupons" :key="`${tag?.couponIssueId || index}-${index}`" closable @close="removeCoupon(tag, index)">{{ tag?.couponName || '-' }}</el-tag><el-button size="small" @click="openIssueDialog">添加</el-button></div></div>
        <div class="form-row"><span class="form-label">活动名称</span><el-input v-model="form.activityName" class="dialog-field" placeholder="请输入活动名称" /></div>
        <div class="form-row"><span class="form-label">活动开始时间</span><el-date-picker v-model="form.activityTime" class="dialog-field" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择活动开始时间" /></div>
        <div class="form-row"><span class="form-label">活动结束时间</span><el-date-picker v-model="form.endActivityTime" class="dialog-field" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择活动结束时间" /></div>
        <div class="form-row"><span class="form-label">触发频率</span><el-radio-group v-model="form.privilegeType"><el-radio v-for="item in privilegeTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio></el-radio-group></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="dialogLoading" @click="submitDialog">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="issueDialogVisible" title="优惠券列表" width="70%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="类型"><el-select v-model="issueType" style="width: 150px" @change="issuePage = 1; loadIssueList()"><el-option v-for="item in issueTypeOptions" :key="item.value" :label="item.title" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="issueStatus" style="width: 150px" @change="issuePage = 1; loadIssueList()"><el-option v-for="item in issueStatusOptions" :key="item.value" :label="item.title" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="优惠券名称"><el-input v-model="issueName" placeholder="请输入优惠券名称" @keyup.enter="issuePage = 1; loadIssueList()" /></el-form-item>
        <el-form-item><el-space><el-button type="primary" @click="issuePage = 1; loadIssueList()">查询</el-button><el-button @click="issueType = 1; issueStatus = ''; issueName = ''; issuePage = 1; loadIssueList()">重置</el-button><el-button type="primary" :disabled="selectedIssueRows.length <= 0" @click="confirmIssues">确定</el-button></el-space></el-form-item>
      </el-form>
      <el-table v-loading="issueLoading" :data="issueList" @selection-change="handleIssueSelectionChange" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="couponIssueId" label="编号" width="80" fixed="left" />
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ row.type === 1 ? '团购券' : row.type === 2 ? '积分兑换券' : row.type === 3 ? '新人券' : '-' }}</template></el-table-column>
        <el-table-column prop="couponName" label="优惠券名称" width="150" fixed="left" />
        <el-table-column prop="issueNumber" label="发布数量" />
        <el-table-column prop="remainNumber" label="剩余数量" />
        <el-table-column prop="hour" label="使用时长(半小时)" />
        <el-table-column prop="validDay" label="有效期限(天)" />
        <el-table-column label="使用规则" width="250"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.teamRule }}</div></template><div>{{ row.teamRule }}</div></el-popover></template></el-table-column>
      </el-table>
      <BasePagination :current-page="issuePage" :page-size="issueSize" :page-sizes="[10, 20, 30, 50, 100]" :total="issueTotal" @update:currentPage="issuePage = $event; loadIssueList()" @update:pageSize="issueSize = $event; loadIssueList()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.tgc {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.tgc-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tgc-hero__badge {
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

.tgc-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.tgc-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.tgc-filter.nf-filter-card {
  margin-bottom: 0;
}

.tgc-filter__form {
  margin-bottom: 0;

  :deep(.el-input) {
    width: 180px;
  }
}

.tgc-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.tgc-tabs {
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

.tgc-table.nf-table-card {
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

.clamp-text {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  max-height: 80px;
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

.search-grid {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .tgc-filter__form {
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

<script setup>
import { computed, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  createMemberVipCardApi,
  deleteMemberVipCardApi,
  getMemberVipCardCouponPageApi,
  getMemberVipCardPageApi,
  updateMemberVipCardApi,
} from '@/api/modules/member-details-vip'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const cardList = ref([])

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = computed(() => (form.vipCardId ? '修改' : '添加'))

const form = reactive({
  vipCardId: '',
  cardType: 1,
  cardName: '',
  num: '',
  oldPrice: '',
  price: '',
  sort: '',
  priceNum: '',
})

const couponIds = ref([])
const couponList = ref([])

const couponDialogVisible = ref(false)
const couponLoading = ref(false)
const couponPage = ref(0)
const couponSize = ref(10)
const couponTotal = ref(0)
const couponTableData = ref([])
const selectedCouponRows = ref([])

function cardTypeText(value) {
  if (Number(value) === 1) return '天'
  if (Number(value) === 2) return '月'
  if (Number(value) === 3) return '年'
  return '-'
}

function cardNumText(row) {
  if (Number(row.cardType) === 1) return `${row.num || 0}天`
  if (Number(row.cardType) === 2) return `${row.num || 0}月`
  if (Number(row.cardType) === 3) return `${row.num || 0}年`
  return row.num || '-'
}

function resetForm() {
  Object.assign(form, {
    vipCardId: '',
    cardType: 1,
    cardName: '',
    num: '',
    oldPrice: '',
    price: '',
    sort: '',
    priceNum: '',
  })
  couponIds.value = []
  couponList.value = []
}

function normalizeRow(item = {}) {
  return {
    ...item,
    vipCardId: item.vipCardId,
    cardType: Number(item.cardType || 0),
    cardName: item.cardName || '',
    num: item.num ?? '',
    oldPrice: item.oldPrice ?? '',
    price: item.price ?? '',
    priceNum: item.priceNum ?? 0,
    couponIds: item.couponIds || '',
    couponList: item.couponList || [],
    sort: item.sort ?? '',
    createTime: item.createTime || item.createdAt || '',
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

async function loadData() {
  loading.value = true
  try {
    const data = await getMemberVipCardPageApi({
      page: pageNum.value,
      limit: pageSize.value,
    })
    cardList.value = (data.list || []).map(normalizeRow)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function refreshData() {
  pageNum.value = 1
  loadData()
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  Object.assign(form, {
    vipCardId: row.vipCardId || '',
    cardType: row.cardType || 1,
    cardName: row.cardName || '',
    num: row.num ?? '',
    oldPrice: row.oldPrice ?? '',
    price: row.price ?? '',
    sort: row.sort ?? '',
    priceNum: row.priceNum ?? '',
  })
  couponIds.value = row.couponIds ? String(row.couponIds).split(',').filter(Boolean) : []
  couponList.value = row.couponList ? [...row.couponList] : []
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.cardName.trim()) return showError('请输入会员名称')
  if (form.price === '') return showError('请输入现价')
  if (form.priceNum === '' || form.priceNum === null) return showError('请输入购买次数')
  if (!couponIds.value.length) return showError('请选择优惠券')

  dialogLoading.value = true
  try {
    const payload = {
      vipCardId: form.vipCardId || undefined,
      cardType: form.cardType,
      cardName: form.cardName.trim(),
      num: form.num,
      oldPrice: form.oldPrice,
      priceNum: form.priceNum,
      price: form.price,
      sort: form.sort,
      couponIds: couponIds.value.toString(),
    }
    if (form.vipCardId) {
      await updateMemberVipCardApi(payload)
    } else {
      delete payload.vipCardId
      await createMemberVipCardApi(payload)
    }
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMemberVipCardApi(row.vipCardId)
  if (cardList.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1
  }
  showSuccess('删除成功')
  await loadData()
}

async function openCouponDialog() {
  couponPage.value = 0
  selectedCouponRows.value = []
  couponDialogVisible.value = true
  await loadCouponData()
}

async function loadCouponData() {
  couponLoading.value = true
  try {
    const data = await getMemberVipCardCouponPageApi({
      page: couponPage.value,
      size: couponSize.value,
      type: 4,
      tearoomName: '',
      couponName: '',
    })
    couponTableData.value = (data.list || []).map(normalizeCouponRow)
    couponTotal.value = data.total || 0
  } finally {
    couponLoading.value = false
  }
}

function handleCouponSelectionChange(rows) {
  selectedCouponRows.value = rows
}

function removeCoupon(_item, index) {
  couponIds.value.splice(index, 1)
  couponList.value.splice(index, 1)
}

function confirmCouponSelection() {
  selectedCouponRows.value.forEach((item) => {
    const couponId = String(item.couponIssueId)
    if (!couponIds.value.includes(couponId)) {
      couponIds.value.push(couponId)
      couponList.value.push(item)
    }
  })
  couponDialogVisible.value = false
}

loadData()
</script>

<template>
  <div class="mdv">
    <header class="mdv-hero nf-page-hero">
      <span class="mdv-hero__badge">会员管理</span>
      <h1 class="mdv-hero__title">周期卡配置</h1>
      <p class="mdv-hero__desc">管理天/月/年周期会员卡套餐，配置价格、购买次数与关联优惠券</p>
    </header>

    <div class="mdv-table nf-table-card">
      <div class="mdv-table__toolbar">
        <el-space>
          <el-button type="primary" plain @click="refreshData">刷新</el-button>
          <el-button type="primary" :disabled="!isAuth('memberDetailsVip:add')" @click="openCreateDialog">添加</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="cardList" border>
        <el-table-column prop="vipCardId" label="编号" width="80" />
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ cardTypeText(row.cardType) }}</template></el-table-column>
        <el-table-column prop="cardName" label="会员名称" min-width="160" />
        <el-table-column label="数量" width="120"><template #default="{ row }">{{ cardNumText(row) }}</template></el-table-column>
        <el-table-column prop="oldPrice" label="原价" width="120" />
        <el-table-column prop="price" label="现价" width="120" />
        <el-table-column prop="priceNum" label="购买次数" width="120" />
        <el-table-column label="优惠券" width="260">
          <template #default="{ row }">
            <div v-if="row.couponList && row.couponList.length" class="tag-wrap">
              <el-tag v-for="(item, index) in row.couponList" :key="`${item.couponIssueId || index}-${index}`" type="success">{{ item.couponName }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" type="primary" :disabled="!isAuth('memberDetailsVip:update')" @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" :disabled="!isAuth('memberDetailsVip:delete')" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[5, 10, 15, 20]" :total="total" @update:currentPage="pageNum = $event; loadData()" @update:pageSize="pageSize = $event; loadData()" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">类型</span><el-radio-group v-model="form.cardType"><el-radio :value="1">天</el-radio><el-radio :value="2">月</el-radio><el-radio :value="3">年</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">会员名称</span><el-input v-model="form.cardName" class="dialog-field" placeholder="请输入会员名称" /></div>
        <div class="form-row"><span class="form-label">数量</span><el-input v-model="form.num" class="dialog-field" type="number" placeholder="请输入数量" /></div>
        <div class="form-row"><span class="form-label">原价</span><el-input v-model="form.oldPrice" class="dialog-field" type="number" placeholder="请输入原价" /></div>
        <div class="form-row"><span class="form-label">现价</span><el-input v-model="form.price" class="dialog-field" type="number" placeholder="请输入现价" /></div>
        <div class="form-row"><span class="form-label">购买次数</span><el-input v-model="form.priceNum" class="dialog-field" type="number" placeholder="请输入购买次数" /></div>
        <div class="form-row"><span class="form-label">排序</span><el-input v-model="form.sort" class="dialog-field" type="number" placeholder="请输入排序" /></div>
        <div class="form-row form-row-top"><span class="form-label">优惠券</span><div class="dialog-field"><el-space wrap><el-tag v-for="(tag, index) in couponList" :key="`${tag.couponIssueId || index}-${index}`" closable style="margin: 3px;" @close="removeCoupon(tag, index)">{{ tag.couponName }}</el-tag><el-button size="small" @click="openCouponDialog">添加</el-button></el-space></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="couponDialogVisible" title="优惠券列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="dialog-toolbar">
        <el-space>
          <el-button type="primary" @click="couponPage = 0; loadCouponData()">刷新</el-button>
          <el-button type="primary" @click="confirmCouponSelection">确定</el-button>
        </el-space>
      </div>
      <el-table v-loading="couponLoading" :data="couponTableData" border @selection-change="handleCouponSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="couponIssueId" label="编号" width="80" />
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ Number(row.type) === 1 ? '团购券' : Number(row.type) === 2 ? '积分兑换券' : Number(row.type) === 3 ? '新人券' : Number(row.type) === 4 ? '会员劵' : '-' }}</template></el-table-column>
        <el-table-column prop="couponName" label="优惠券名称" width="150" />
        <el-table-column prop="issueNumber" label="发布数量" width="100" />
        <el-table-column prop="remainNumber" label="剩余数量" width="100" />
        <el-table-column prop="hour" label="使用时长(半小时)" width="140" />
        <el-table-column prop="validDay" label="有效期限(天)" width="120" />
        <el-table-column label="使用规则" min-width="220"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.teamRule }}</div></template><div>{{ row.teamRule }}</div></el-popover></template></el-table-column>
        <el-table-column label="购买须知" min-width="220"><template #default="{ row }"><el-popover placement="top-start" trigger="hover" :width="500"><template #reference><div class="clamp-text">{{ row.buyRule }}</div></template><div>{{ row.buyRule }}</div></el-popover></template></el-table-column>
      </el-table>
      <BasePagination :current-page="couponPage + 1" :page-size="couponSize" :page-sizes="[10, 20, 30, 50, 100]" :total="couponTotal" @update:currentPage="couponPage = $event - 1; loadCouponData()" @update:pageSize="couponSize = $event; couponPage = 0; loadCouponData()" />
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
  border-radius: var(--nf-radius-sm);
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

.mdv-table__toolbar {
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
  width: 100px;
  text-align: right;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
  min-width: 0;
}

.tag-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.clamp-text {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  max-height: 80px;
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

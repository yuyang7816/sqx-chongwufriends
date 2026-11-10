<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { getExchangeRecordPageApi, getExchangeStatsApi } from '@/api/modules/exchange-list'

const router = useRouter()

const activeTab = ref('first')

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const recordList = ref([])

const filters = reactive({
  state: -1,
  userName: '',
  phone: '',
  orderId: '',
  classify: '',
  startTime: getDefaultStartDate(),
  endTime: getDefaultEndDate(),
  type: 1,
})

const stateOptions = [
  { label: '全部', value: -1 },
  { label: '待支付', value: 0 },
  { label: '支付成功', value: 1 },
  { label: '支付失败', value: 2 },
]

const classifyOptions = [
  { label: '全部', value: '' },
  { label: 'app微信', value: 1 },
  { label: '微信公众号', value: 2 },
  { label: '微信小程序', value: 3 },
  { label: '支付宝', value: 4 },
  { label: '支付宝H5', value: 5 },
]

const statFlag = ref(1)
const statDate = ref(getDefaultStatDate())
const stats = ref({})

const statFlagOptions = [
  { value: 1, label: '按天查询' },
  { value: 2, label: '按月查询' },
  { value: 3, label: '按年查询' },
]

const statPickerType = computed(() => {
  if (statFlag.value === 2) return 'month'
  if (statFlag.value === 3) return 'year'
  return 'date'
})

const statValueFormat = computed(() => {
  if (statFlag.value === 2) return 'YYYY-MM'
  if (statFlag.value === 3) return 'YYYY'
  return 'YYYY-MM-DD'
})

function getDefaultEndDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getDefaultStartDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}-01`
}

function getDefaultStatDate(flag = 1) {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  if (flag === 2) return `${year}-${month}`
  if (flag === 3) return `${year}`
  return `${year}-${month}-${day}`
}

function paymentTypeText(value) {
  if (Number(value) === 1) return '会员充值'
  if (Number(value) === 2) return '订单'
  if (Number(value) === 3) return '预约'
  if (Number(value) === 4) return '优惠券'
  return '-'
}

function classifyText(value) {
  if (Number(value) === 1) return 'app微信'
  if (Number(value) === 2) return '微信公众号'
  if (Number(value) === 3) return '微信小程序'
  if (Number(value) === 4) return '支付宝'
  if (Number(value) === 5) return '支付宝H5'
  return '-'
}

function stateText(value) {
  if (Number(value) === 0) return '待支付'
  if (Number(value) === 1) return '支付成功'
  if (Number(value) === 2) return '支付失败'
  return '-'
}

function normalizeRecord(item = {}) {
  return {
    ...item,
    id: item.id,
    type: Number(item.type || 0),
    userId: item.userId || '',
    userName: item.userName || '',
    phone: item.phone || '',
    orderId: item.orderId || '',
    money: item.money ?? '',
    classify: Number(item.classify || 0),
    state: Number(item.state ?? -1),
    createTime: item.createTime || item.createdAt || '',
    payTime: item.payTime || '',
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const data = await getExchangeRecordPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      endTime: filters.endTime || getDefaultEndDate(),
      startTime: filters.startTime || getDefaultStartDate(),
      state: filters.state,
      userName: filters.userName.trim(),
      phone: filters.phone.trim(),
      orderId: filters.orderId.trim(),
      type: filters.type,
      classify: filters.classify,
    })
    recordList.value = (data.list || []).map(normalizeRecord)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadRecords()
}

function handleReset() {
  pageNum.value = 1
  filters.userName = ''
  filters.phone = ''
  filters.orderId = ''
  filters.endTime = ''
  filters.startTime = ''
  filters.type = 1
  filters.state = -1
  filters.classify = ''
  loadRecords()
}

function goUserDetail(row) {
  if (!row.userId) return
  router.push(`/users/${row.userId}`)
}

async function loadStats() {
  stats.value = await getExchangeStatsApi({ time: statDate.value, flag: statFlag.value })
}

function handleTabChange() {
  if (activeTab.value === 'first') {
    pageNum.value = 1
    pageSize.value = 10
    loadRecords()
    return
  }
  statFlag.value = 1
  statDate.value = getDefaultStatDate(1)
  loadStats()
}

function handleStatFlagChange(value) {
  statFlag.value = value
  statDate.value = getDefaultStatDate(value)
  loadStats()
}

loadRecords()
</script>

<template>
  <div class="exl">
    <header class="exl-hero nf-page-hero">
      <span class="exl-hero__badge">会员管理</span>
      <h1 class="exl-hero__title">充值记录</h1>
      <p class="exl-hero__desc">查询会员卡充值流水与支付统计，支持多条件筛选与用户详情跳转</p>
    </header>

    <div v-if="activeTab === 'first'" class="exl-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="exl-filter__form" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="filters.state" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option v-for="item in stateOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名称">
          <el-input v-model="filters.userName" clearable placeholder="请输入用户名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="支付订单号">
          <el-input v-model="filters.orderId" clearable placeholder="请输入支付订单号" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="filters.classify" placeholder="请选择支付方式" clearable @change="handleSearch">
            <el-option v-for="item in classifyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="filters.startTime" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="filters.endTime" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择截止时间" />
        </el-form-item>
        <el-form-item class="exl-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div v-else-if="activeTab === 'sixth'" class="exl-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>统计筛选</span>
      </div>
      <el-form :inline="true" class="exl-filter__form exl-filter__form--stat" @submit.prevent>
        <el-form-item label="查询维度">
          <el-select v-model="statFlag" style="width: 150px" @change="handleStatFlagChange">
            <el-option v-for="item in statFlagOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="statDate" :type="statPickerType" :value-format="statValueFormat" :format="statValueFormat" placeholder="选择时间" @change="loadStats" />
        </el-form-item>
      </el-form>
    </div>

    <div class="exl-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="会员卡充值记录" name="first">
          <div class="exl-table nf-table-card">
            <el-table v-loading="loading" :data="recordList" border>
              <el-table-column prop="id" label="编号" width="60" />
              <el-table-column label="支付类型" width="100"><template #default="{ row }">{{ paymentTypeText(row.type) }}</template></el-table-column>
              <el-table-column prop="userName" label="用户名" min-width="120">
                <template #default="{ row }">
                  <span class="user-link" @click="goUserDetail(row)">{{ row.userName || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" width="180" />
              <el-table-column prop="orderId" label="充值订单号" width="200" />
              <el-table-column prop="money" label="充值金额" width="100" />
              <el-table-column prop="userId" label="会员编号" width="100" />
              <el-table-column label="分类" width="120"><template #default="{ row }">{{ classifyText(row.classify) }}</template></el-table-column>
              <el-table-column label="状态" width="100"><template #default="{ row }">{{ stateText(row.state) }}</template></el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="170" />
              <el-table-column prop="payTime" label="支付时间" width="170" />
            </el-table>

            <BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total" @update:currentPage="pageNum = $event; loadRecords()" @update:pageSize="pageSize = $event; loadRecords()" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="会员卡充值统计" name="sixth">
          <div class="exl-stats nf-table-card">
            <el-row :gutter="12">
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">支付总金额</div><div class="stat-value">{{ stats.sumMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">微信小程序支付金额</div><div class="stat-value">{{ stats.weiXinXCXMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">微信APP支付金额</div><div class="stat-value">{{ stats.weiXinAppMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">微信公众号支付金额</div><div class="stat-value">{{ stats.weiXinGZHMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">支付宝APP支付金额</div><div class="stat-value">{{ stats.zhiFuBaoAppMoney || 0 }}</div><div class="stat-unit">元</div></div></el-col>
              <el-col :xs="24" :sm="12" :lg="6"><div class="stat-card"><div class="stat-title">支付宝H5支付金额</div><div class="stat-value">{{ stats.zhiFuBaoH5Money || 0 }}</div><div class="stat-unit">元</div></div></el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped lang="scss">
.exl {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.exl-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.exl-hero__badge {
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

.exl-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.exl-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.exl-filter.nf-filter-card {
  margin-bottom: 0;
}

.exl-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.exl-filter__form--stat {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.exl-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.exl-tabs {
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

.exl-table.nf-table-card,
.exl-stats.nf-table-card {
  margin-bottom: 0;
  margin-top: 0;
  padding-bottom: 12px;
  border-radius: 0 0 8px 8px;
  border-top: none;
  box-shadow: var(--nf-shadow-card);
}

.exl-table.nf-table-card {
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

.user-link {
  color: var(--nf-brand-deep);
  cursor: pointer;
}

.stat-card {
  height: 100%;
  padding: 16px;
  border: 1px solid var(--nf-border);
  border-radius: var(--nf-radius-md);
  background: #fff;
  margin-bottom: 12px;
  box-shadow: var(--nf-shadow-card);
}

.stat-title {
  color: var(--nf-text-secondary);
  font-size: 13px;
  margin-bottom: 10px;
  line-height: 1.4;
}

.stat-value {
  color: var(--nf-brand-deep);
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.stat-unit {
  color: var(--nf-text-secondary);
  font-size: 12px;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .exl-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }
}
</style>

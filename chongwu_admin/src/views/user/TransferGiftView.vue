<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { getTransferGiftPageApi, getTransferGiftStatsApi } from '@/api/modules/transfer-gift'
import { showError } from '@/utils/modal'

const router = useRouter()

const loading = ref(false)
const statsLoading = ref(false)
const activeTab = ref('give')
const granularity = ref(1)
const selectedDate = ref(getDefaultDate())
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const stats = ref({})

const granularityOptions = [
  { label: '按天查询', value: 1 },
  { label: '按月查询', value: 2 },
  { label: '按年查询', value: 3 },
]

const activeClassify = computed(() => (activeTab.value === 'give' ? 10 : 20))
const pickerType = computed(() => {
  if (granularity.value === 2) return 'month'
  if (granularity.value === 3) return 'year'
  return 'date'
})
const valueFormat = computed(() => {
  if (granularity.value === 2) return 'YYYY-MM'
  if (granularity.value === 3) return 'YYYY'
  return 'YYYY-MM-DD'
})
const displayFormat = computed(() => valueFormat.value)

function getDefaultDate(flag = 1) {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  if (flag === 2) return `${year}-${month}`
  if (flag === 3) return `${year}`
  return `${year}-${month}-${day}`
}

function buildListParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    classify: activeClassify.value,
    time: selectedDate.value,
    flag: granularity.value,
  }
}

function buildStatsParams() {
  return {
    time: selectedDate.value,
    flag: granularity.value,
  }
}

function formatMoneyValue(value) {
  const amount = Number(value ?? 0)
  return Number.isFinite(amount) ? amount : 0
}

function amountText(row) {
  const amount = row?.money ?? 0
  if (Number(row?.type) === 2) return `- ${amount}`
  if (Number(row?.type) === 1) return `+ ${amount}`
  return String(amount)
}

function amountClass(row) {
  if (Number(row?.type) === 2) return 'amount-negative'
  if (Number(row?.type) === 1) return 'amount-positive'
  return ''
}

async function loadData() {
  loading.value = true
  try {
    const data = await getTransferGiftPageApi(buildListParams())
    tableData.value = data.list || []
    total.value = Number(data.total || 0)
    if (pageNum.value > 1 && !tableData.value.length) {
      pageNum.value -= 1
      await loadData()
    }
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  statsLoading.value = true
  try {
    stats.value = await getTransferGiftStatsApi(buildStatsParams())
  } finally {
    statsLoading.value = false
  }
}

async function reloadPage() {
  await Promise.all([loadStats(), loadData()])
}

function handleConditionChange() {
  pageNum.value = 1
  if (!selectedDate.value) {
    selectedDate.value = getDefaultDate(granularity.value)
  }
  reloadPage()
}

function handleGranularityChange() {
  selectedDate.value = getDefaultDate(granularity.value)
  handleConditionChange()
}

function handleTabChange() {
  pageNum.value = 1
  pageSize.value = 10
  loadData()
}

function goToUserDetail(row) {
  if (!row?.userId) {
    showError('当前记录未绑定用户')
    return
  }
  router.push(`/users/${row.userId}`)
}

onMounted(() => {
  reloadPage()
})
</script>

<template>
  <el-card shadow="never" class="page-card transfer-page">
    <template #header>
      <div class="card-header">
        <strong>转赠记录</strong>
      </div>
    </template>

    <div class="toolbar-row">
      <el-select v-model="granularity" style="width: 150px" @change="handleGranularityChange">
        <el-option v-for="item in granularityOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-date-picker
        v-model="selectedDate"
        :type="pickerType"
        :value-format="valueFormat"
        :format="displayFormat"
        placeholder="选择时间"
        clearable
        @change="handleConditionChange"
      />
    </div>

    <el-row v-loading="statsLoading" :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-title">赠送金额</div>
          <div class="stat-value">{{ formatMoneyValue(stats.giveMoney) }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-title">接受金额</div>
          <div class="stat-value">{{ formatMoneyValue(stats.takeMoney) }}</div>
        </div>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="赠送记录" name="give" />
      <el-tab-pane label="接受记录" name="receive" />
    </el-tabs>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="编号" width="100" />
      <el-table-column label="用户昵称" min-width="160">
        <template #default="{ row }">
          <span class="user-link" @click="goToUserDetail(row)">{{ row.userName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="金额" width="120">
        <template #default="{ row }">
          <span :class="amountClass(row)">{{ amountText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="activeTab === 'give' ? [10, 20, 30, 40] : [10, 20, 30, 50, 100]"
      :total="total"
      @update:currentPage="pageNum = $event; loadData()"
      @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
    />
  </el-card>
</template>

<style scoped>
.transfer-page {
  min-height: 100%;
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 12px;
}

.stat-card {
  height: 100%;
  padding: 24px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f5f9ff 100%);
}

.stat-title {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 14px;
}

.stat-value {
  color: var(--nf-brand-deep);
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.user-link {
  color: var(--el-color-primary);
  cursor: pointer;
}

.amount-negative {
  color: #f56c6c;
}

.amount-positive {
  color: #006600;
}
</style>

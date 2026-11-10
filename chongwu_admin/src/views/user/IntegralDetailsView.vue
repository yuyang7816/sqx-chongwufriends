<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { getIntegralDetailsPageApi } from '@/api/modules/integral-details'
import { showError } from '@/utils/modal'

const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const filters = reactive({
  userName: '',
  phone: '',
  startTime: '',
  endTime: '',
})

function formatAmountType(row) {
  const amount = row.num ?? 0
  if (Number(row.type) === 2) return `- ${amount}`
  if (Number(row.type) === 1) return `+ ${amount}`
  return String(amount)
}

function amountClass(row) {
  if (Number(row.type) === 2) return 'amount-negative'
  if (Number(row.type) === 1) return 'amount-positive'
  return ''
}

function buildQueryParams() {
  const params = {
    page: pageNum.value,
    limit: pageSize.value,
    startTime: filters.startTime,
    endTime: filters.endTime,
    userName: filters.userName.trim(),
    phone: filters.phone.trim(),
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
      delete params[key]
    }
  })
  return params
}

async function loadData() {
  loading.value = true
  try {
    const data = await getIntegralDetailsPageApi(buildQueryParams())
    tableData.value = data.list || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.userName = ''
  filters.phone = ''
  filters.startTime = ''
  filters.endTime = ''
  pageNum.value = 1
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
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>积分明细</strong>
      </div>
    </template>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="用户昵称">
        <el-input v-model="filters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="filters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker
          v-model="filters.startTime"
          type="date"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          placeholder="选择开始时间"
          clearable
        />
      </el-form-item>
      <el-form-item label="截止时间">
        <el-date-picker
          v-model="filters.endTime"
          type="date"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          placeholder="选择截止时间"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column label="用户名" min-width="180">
        <template #default="{ row }">
          <span class="user-link" @click="goToUserDetail(row)">{{ row.userName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="160" />
      <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">
          <span :class="amountClass(row)">{{ formatAmountType(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 30, 50, 100]"
      :total="total"
      @update:currentPage="pageNum = $event; loadData()"
      @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
    />
  </el-card>
</template>

<style scoped>
.user-link {
  color: var(--el-color-danger);
  cursor: pointer;
}

.amount-negative {
  color: #f56c6c;
}

.amount-positive {
  color: #006600;
}
</style>

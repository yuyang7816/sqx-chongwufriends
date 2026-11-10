<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import { cancelUserByMessageApi, getMessageCenterListApi } from '@/api/modules/message-center'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const filters = reactive({
  status: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待注销', value: 0 },
  { label: '已注销', value: 1 },
]

function buildQueryParams() {
  const params = {
    page: pageNum.value,
    limit: pageSize.value,
    state: 4,
    status: filters.status,
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
    const data = await getMessageCenterListApi(buildQueryParams())
    tableData.value = data?.list || data?.records || []
    total.value = Number(data?.totalCount || data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.status = ''
  pageNum.value = 1
  loadData()
}

function goToUserDetail(row) {
  if (!row?.userEntity) {
    showError('当前用户已注销')
    return
  }
  router.push(`/users/${row.userId}`)
}

async function handleCancelUser(row) {
  if (!row?.userEntity) {
    showError('当前用户已注销')
    return
  }
  await confirmAction('确定要注销该用户吗？')
  await cancelUserByMessageApi(row.userId, row.id)
  showSuccess('操作成功')
  await loadData()
}

watch(
  () => route.query.status,
  (value) => {
    filters.status = value === undefined ? '' : Number(value)
    pageNum.value = 1
    loadData()
  },
)

onMounted(() => {
  filters.status = route.query.status === undefined ? '' : Number(route.query.status)
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>注销信息</strong>
      </div>
    </template>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="全部" @change="handleSearch">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
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
      <el-table-column label="用户名称" min-width="160">
        <template #default="{ row }">
          <span class="user-link" @click="goToUserDetail(row)">{{ row.userName || '未绑定' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="账号" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
      <el-table-column prop="createAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.userEntity" link type="danger" :disabled="!isAuth('messageZx:shenhe')" @click="handleCancelUser(row)">注销用户</el-button>
          <span v-else class="muted-text">用户已注销</span>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 30, 40]"
      :total="total"
      @update:currentPage="pageNum = $event; loadData()"
      @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
    />
  </el-card>
</template>

<style scoped>
.user-link {
  color: var(--el-color-primary);
  cursor: pointer;
}

.muted-text {
  color: var(--el-text-color-secondary);
}
</style>

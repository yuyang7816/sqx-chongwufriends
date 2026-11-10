<script setup>
import { ref, watch } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getPetFeedOrderPageApi } from '@/api/modules/pet-feed'

const props = defineProps({
  userId: { type: [Number, String], required: true },
})

const statusMap = { 1: '待反馈', 2: '预计反馈', 3: '已反馈' }
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

async function loadData() {
  if (!props.userId) return
  loading.value = true
  try {
    const data = await getPetFeedOrderPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      userId: Number(props.userId),
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

watch(() => props.userId, () => {
  pageNum.value = 1
  loadData()
}, { immediate: true })
</script>

<template>
  <div>
    <el-table v-loading="loading" :data="tableData" border size="small">
      <el-table-column prop="orderNo" label="订单号" min-width="140" />
      <el-table-column prop="productName" label="商品" min-width="140" show-overflow-tooltip />
      <el-table-column prop="amount" label="金额" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ row.statusText || statusMap[row.status] || row.status }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
    </el-table>
    <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
  </div>
</template>

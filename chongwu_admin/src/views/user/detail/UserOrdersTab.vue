<script setup>
import { ref, watch } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getPetOrderPageApi } from '@/api/modules/pet-order'

const props = defineProps({
  userId: { type: [Number, String], required: true },
})

const bizTypeMap = { 1: '商城', 2: '积分兑换', 3: '其他' }
const payStatusMap = { 0: '待支付', 1: '已支付', 2: '已取消' }
const statusMap = { 0: '待处理', 1: '已完成', 2: '已取消' }

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

async function loadData() {
  if (!props.userId) return
  loading.value = true
  try {
    const data = await getPetOrderPageApi({
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
      <el-table-column prop="orderNo" label="订单号" min-width="150" />
      <el-table-column prop="payAmount" label="金额" width="90" />
      <el-table-column label="业务" width="100">
        <template #default="{ row }">{{ bizTypeMap[row.bizType] || row.bizType }}</template>
      </el-table-column>
      <el-table-column label="支付" width="90">
        <template #default="{ row }">{{ payStatusMap[row.payStatus] ?? row.payStatus }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ statusMap[row.status] ?? row.status }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
    </el-table>
    <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
  </div>
</template>

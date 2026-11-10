<script setup>
import { ref, watch } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getPetUserCouponPageApi } from '@/api/modules/pet-coupon'

const props = defineProps({
  userId: { type: [Number, String], required: true },
})

const statusMap = { 1: '未使用', 2: '已使用', 3: '已过期' }
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

async function loadData() {
  if (!props.userId) return
  loading.value = true
  try {
    const data = await getPetUserCouponPageApi({
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
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="templateId" label="模板ID" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusMap[row.status] ?? row.status }}</template>
      </el-table-column>
      <el-table-column prop="expireTime" label="过期时间" width="170" />
      <el-table-column prop="createTime" label="领取时间" width="170" />
    </el-table>
    <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
  </div>
</template>

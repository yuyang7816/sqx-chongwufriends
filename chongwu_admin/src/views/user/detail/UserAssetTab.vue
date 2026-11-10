<script setup>
import { ref, watch } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getPetUserAssetPageApi, getPetUserLedgerPageApi } from '@/api/modules/pet-user'

const props = defineProps({
  userId: { type: [Number, String], required: true },
})

const activeTab = ref('asset')
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const directionMap = { 1: '收入', 2: '支出' }

async function loadData() {
  if (!props.userId) return
  loading.value = true
  try {
    const params = { page: pageNum.value, limit: pageSize.value, userId: Number(props.userId) }
    const data = activeTab.value === 'asset'
      ? await getPetUserAssetPageApi(params)
      : await getPetUserLedgerPageApi(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  pageNum.value = 1
  loadData()
}

watch(() => props.userId, () => {
  pageNum.value = 1
  loadData()
}, { immediate: true })
</script>

<template>
  <div>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="资产余额" name="asset" />
      <el-tab-pane label="资产流水" name="ledger" />
    </el-tabs>
    <el-table v-if="activeTab === 'asset'" v-loading="loading" :data="tableData" border size="small">
      <el-table-column prop="points" label="积分" width="90" />
      <el-table-column prop="coins" label="金币" width="90" />
      <el-table-column prop="flowers" label="鲜花" width="90" />
      <el-table-column prop="loveValue" label="爱心值" width="90" />
      <el-table-column prop="levelName" label="等级" width="100" />
    </el-table>
    <el-table v-else v-loading="loading" :data="tableData" border size="small">
      <el-table-column prop="assetType" label="类型" width="100" />
      <el-table-column prop="changeAmount" label="变动" width="90" />
      <el-table-column label="方向" width="80">
        <template #default="{ row }">{{ directionMap[row.direction] ?? row.direction }}</template>
      </el-table-column>
      <el-table-column prop="title" label="说明" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="170" />
    </el-table>
    <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
  </div>
</template>

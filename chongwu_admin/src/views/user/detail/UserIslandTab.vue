<script setup>
import { ref, watch } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getPetIslandPageApi } from '@/api/modules/pet-island'

const props = defineProps({
  userId: { type: [Number, String], required: true },
})

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

async function loadData() {
  if (!props.userId) return
  loading.value = true
  try {
    const data = await getPetIslandPageApi({
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

function petTypeLabel(type) {
  if (type === 'dog') return '狗'
  if (type === 'cat') return '猫'
  return type || '-'
}

watch(() => props.userId, () => {
  pageNum.value = 1
  loadData()
}, { immediate: true })
</script>

<template>
  <div>
    <el-table v-loading="loading" :data="tableData" border size="small">
      <el-table-column prop="petName" label="宠物名称" min-width="110">
        <template #default="{ row }">{{ row.petName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="petType" label="类型" width="80">
        <template #default="{ row }">{{ petTypeLabel(row.petType) }}</template>
      </el-table-column>
      <el-table-column prop="breed" label="品种" min-width="100">
        <template #default="{ row }">{{ row.breed || '-' }}</template>
      </el-table-column>
      <el-table-column prop="gender" label="性别" width="70">
        <template #default="{ row }">{{ row.gender || '-' }}</template>
      </el-table-column>
      <el-table-column prop="hunger" label="饥饿" width="80" />
      <el-table-column prop="clean" label="清洁" width="80" />
      <el-table-column prop="mood" label="心情" width="80" />
      <el-table-column prop="vitality" label="活力" width="80" />
      <el-table-column prop="intimacy" label="亲密度" width="90" />
      <el-table-column prop="feedCooldownUntil" label="投喂冷却至" width="170" />
    </el-table>
    <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
  </div>
</template>

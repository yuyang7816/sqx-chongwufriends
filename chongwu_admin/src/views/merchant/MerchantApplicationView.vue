<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getMerchantApplicationPageApi } from '@/api/modules/merchant-application'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const filters = reactive({
  applyName: '',
  applyPhone: '',
})

async function loadData() {
  loading.value = true
  try {
    const data = await getMerchantApplicationPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      applyName: filters.applyName.trim(),
      applyPhone: filters.applyPhone.trim(),
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.applyName = ''
  filters.applyPhone = ''
  pageNum.value = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="ma">
    <header class="ma-hero nf-page-hero">
      <span class="ma-hero__badge">羽毛球馆</span>
      <h1 class="ma-hero__title">入驻申请</h1>
      <p class="ma-hero__desc">查看商户入驻申请记录，支持按负责人姓名与手机号筛选查询</p>
    </header>

    <div class="ma-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="ma-filter__form" @submit.prevent>
        <el-form-item label="负责人手机号">
          <el-input v-model="filters.applyPhone" clearable placeholder="请输入负责人手机号" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="负责人姓名">
          <el-input v-model="filters.applyName" clearable placeholder="负责人姓名筛选" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="ma-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="ma-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="applyId" label="编号" width="80" fixed="left" />
        <el-table-column prop="applyName" label="负责人姓名" min-width="140" />
        <el-table-column prop="applyPhone" label="负责人手机号" min-width="160" />
        <el-table-column prop="shopAddressDetails" label="店铺地址" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
      </el-table>

      <BasePagination
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 30, 50, 100]"
        :total="total"
        @update:currentPage="pageNum = $event; loadData()"
        @update:pageSize="pageSize = $event; pageNum = 1; loadData()"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.ma {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.ma-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ma-hero__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--nf-text-primary);
  background: var(--nf-brand-lite);
  border: 1px solid var(--nf-brand);
  border-radius: var(--nf-radius-sm, 6px);
}

.ma-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.ma-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.ma-filter.nf-filter-card {
  margin-bottom: 0;
}

.ma-filter__form {
  margin-bottom: 0;

  :deep(.el-input) {
    width: 180px;
  }
}

.ma-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.ma-table.nf-table-card {
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

@media (max-width: 768px) {
  .ma-filter__form {
    :deep(.el-input) {
      width: 100%;
    }
  }
}
</style>

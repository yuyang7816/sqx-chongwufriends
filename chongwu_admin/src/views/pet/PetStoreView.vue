<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import CoverImageUpload from '@/components/common/CoverImageUpload.vue'
import { listPetCitiesApi } from '@/api/modules/pet-city'
import {
  deletePetStoreApi,
  getPetStorePageApi,
  savePetStoreApi,
  updatePetStoreApi,
} from '@/api/modules/pet-store'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { toPublicFileUrl } from '@/utils/tool'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const cityOptions = ref([])

const form = reactive({
  id: '',
  cityId: '',
  name: '',
  address: '',
  phone: '',
  tone: '#f6d57c',
  coverUrl: '',
  businessStatus: 1,
  featured: 0,
  sort: 0,
})

function coverSrc(url) {
  return toPublicFileUrl(url)
}

function resetForm() {
  Object.assign(form, {
    id: '',
    cityId: '',
    name: '',
    address: '',
    phone: '',
    tone: '#f6d57c',
    coverUrl: '',
    businessStatus: 1,
    featured: 0,
    sort: 0,
  })
}

async function loadCities() {
  try {
    cityOptions.value = await listPetCitiesApi()
  } catch (e) {
    cityOptions.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetStorePageApi({ page: pageNum.value, limit: pageSize.value })
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    ...row,
    featured: Number(row.featured || 0),
    businessStatus: Number(row.businessStatus ?? 1),
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.name) return showError('请输入门店名称')
  if (!form.cityId) return showError('请选择城市')
  dialogLoading.value = true
  try {
    const payload = {
      ...form,
      cityId: Number(form.cityId),
      featured: Number(form.featured || 0),
      businessStatus: Number(form.businessStatus ?? 1),
      sort: Number(form.sort || 0),
    }
    if (form.id) {
      await updatePetStoreApi(payload)
    } else {
      await savePetStoreApi(payload)
    }
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction('确认删除该门店？')
  await deletePetStoreApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

onMounted(async () => {
  await loadCities()
  await loadData()
})
</script>

<template>
  <div class="pstore">
    <header class="pstore-hero nf-page-hero">
      <span class="pstore-hero__badge">宠物业务</span>
      <h1 class="pstore-hero__title">门店管理</h1>
      <p class="pstore-hero__desc">维护线下门店基础信息、封面与营业状态，支持新增、编辑与删除</p>
    </header>

    <div class="pstore-table nf-table-card">
      <div class="pstore-table__toolbar">
        <el-space wrap>
          <el-button type="primary" @click="openCreate">新增门店</el-button>
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="门店名称" min-width="140" />
        <el-table-column prop="cityId" label="城市ID" width="100" />
        <el-table-column label="封面" width="90">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="coverSrc(row.coverUrl)"
              :preview-src-list="[coverSrc(row.coverUrl)]"
              class="thumb-image"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="featured" label="推荐" width="80">
          <template #default="{ row }">{{ row.featured === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="businessStatus" label="状态" width="90">
          <template #default="{ row }">{{ row.businessStatus === 1 ? '营业' : '休息' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeRow(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" title="门店信息" width="560px" class="nf-pro-dialog" align-center>
<el-form label-width="90px">
        <el-form-item label="城市">
          <el-select v-model="form.cityId" placeholder="选择城市" style="width: 100%">
            <el-option v-for="c in cityOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="门店名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="展示色"><el-input v-model="form.tone" /></el-form-item>
        <el-form-item label="封面图">
          <CoverImageUpload v-model="form.coverUrl" />
        </el-form-item>
        <el-form-item label="首页推荐">
          <el-switch v-model="form.featured" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="营业状态">
          <el-radio-group v-model="form.businessStatus">
            <el-radio :value="1">营业</el-radio>
            <el-radio :value="0">休息</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pstore {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pstore-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pstore-hero__badge {
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

.pstore-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pstore-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pstore-table.nf-table-card {
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

.pstore-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.thumb-image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: block;
}
</style>

<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import CoverImageUpload from '@/components/common/CoverImageUpload.vue'
import {
  deletePetMarketApi,
  getPetMarketPageApi,
  savePetMarketApi,
  updatePetMarketApi,
} from '@/api/modules/pet-market'
import { toPublicFileUrl } from '@/utils/tool'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)

const ITEM_TYPE_MAP = {
  toy: '玩具',
  decor: '装饰品',
  bed: '床垫',
  prop: '其他',
}

const PET_TYPE_MAP = {
  dog: '汪星人',
  cat: '喵星人',
  all: '全部',
}

const form = reactive({
  id: '',
  name: '',
  coverUrl: '',
  itemType: 'toy',
  priceCoins: 0,
  pricePoints: 0,
  effectDesc: '',
  durability: 80,
  favor: 5,
  petType: 'dog',
  sort: 0,
  status: 1,
})

function resetForm() {
  Object.assign(form, {
    id: '',
    name: '',
    coverUrl: '',
    itemType: 'toy',
    priceCoins: 0,
    pricePoints: 0,
    effectDesc: '',
    durability: 80,
    favor: 5,
    petType: 'dog',
    sort: 0,
    status: 1,
  })
}

function coverSrc(url) {
  return toPublicFileUrl(url)
}

function itemTypeLabel(type) {
  return ITEM_TYPE_MAP[type] || type || '-'
}

function petTypeLabel(type) {
  return PET_TYPE_MAP[type] || type || '-'
}

function durabilityLabel(row) {
  if (row.durability === -1 || row.itemType === 'decor') return '∞'
  return row.durability ?? '-'
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetMarketPageApi({ page: pageNum.value, limit: pageSize.value })
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
    id: row.id,
    name: row.name || '',
    coverUrl: row.coverUrl || '',
    itemType: row.itemType || 'toy',
    priceCoins: row.priceCoins ?? 0,
    pricePoints: row.pricePoints ?? 0,
    effectDesc: row.effectDesc || '',
    durability: row.durability ?? 80,
    favor: row.favor ?? 0,
    petType: row.petType || 'dog',
    sort: row.sort ?? 0,
    status: row.status ?? 1,
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.name) return showError('请输入道具名称')
  dialogLoading.value = true
  try {
    const payload = {
      ...form,
      priceCoins: Number(form.priceCoins) || 0,
      pricePoints: Number(form.pricePoints) || 0,
      durability: Number(form.durability),
      favor: Number(form.favor) || 0,
      sort: Number(form.sort) || 0,
      status: Number(form.status) === 0 ? 0 : 1,
    }
    if (!Number.isFinite(payload.durability)) payload.durability = 80
    if (form.id) await updatePetMarketApi(payload)
    else await savePetMarketApi(payload)
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction('确认删除该道具？')
  await deletePetMarketApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

loadData()
</script>

<template>
  <div class="pmarket">
    <header class="pmarket-hero nf-page-hero">
      <span class="pmarket-hero__badge">宠物业务</span>
      <h1 class="pmarket-hero__title">集市道具</h1>
      <p class="pmarket-hero__desc">管理岛宅集市玩具、装饰品与床垫，配置金币/积分价格与亲密度效果</p>
    </header>

    <div class="pmarket-table nf-table-card">
      <div class="pmarket-table__toolbar">
        <el-space wrap>
          <el-button type="primary" @click="openCreate">新增道具</el-button>
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="tableData" border table-layout="auto">
        <el-table-column prop="id" label="ID" width="72" fixed />
        <el-table-column label="封面" width="88" fixed>
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="coverSrc(row.coverUrl)"
              :preview-src-list="[coverSrc(row.coverUrl)]"
              preview-teleported
              fit="contain"
              class="cover-thumb"
            >
              <template #error>
                <div class="cover-fallback">无图</div>
              </template>
            </el-image>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="110" show-overflow-tooltip />
        <el-table-column label="宠物类型" width="96">
          <template #default="{ row }">{{ petTypeLabel(row.petType) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="88">
          <template #default="{ row }">{{ itemTypeLabel(row.itemType) }}</template>
        </el-table-column>
        <el-table-column prop="priceCoins" label="金币" width="72" />
        <el-table-column prop="pricePoints" label="积分" width="72" />
        <el-table-column prop="favor" label="亲密度" width="80" />
        <el-table-column label="耐久" width="72">
          <template #default="{ row }">{{ durabilityLabel(row) }}</template>
        </el-table-column>
        <el-table-column prop="effectDesc" label="效果说明" min-width="140" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="72" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
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

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑道具' : '添加道具'"
      width="720px"
      align-center
      destroy-on-close
      class="nf-pro-dialog"
      :close-on-click-modal="false"
    >
      <el-form class="nf-pro-form" label-width="100px" label-position="right">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入道具名称" maxlength="40" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.itemType" placeholder="请选择" style="width: 100%">
            <el-option label="玩具" value="toy" />
            <el-option label="装饰品" value="decor" />
            <el-option label="床垫" value="bed" />
            <el-option label="其他" value="prop" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物类型">
          <el-select v-model="form.petType" placeholder="请选择" style="width: 100%">
            <el-option label="汪星人" value="dog" />
            <el-option label="喵星人" value="cat" />
            <el-option label="全部" value="all" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <CoverImageUpload v-model="form.coverUrl" empty-text="" tip="建议正方形透明底图标" />
        </el-form-item>
        <el-form-item label="金币价格">
          <el-input-number v-model="form.priceCoins" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="积分价格">
          <el-input-number v-model="form.pricePoints" :min="0" :step="1" />
          <div class="form-tip">大于 0 表示支持该兑换方式，购买时金币/积分二选一</div>
        </el-form-item>
        <el-form-item label="效果说明">
          <el-input v-model="form.effectDesc" placeholder="请输入，如：+15 亲密度" clearable />
        </el-form-item>
        <el-form-item label="耐久">
          <el-input-number v-model="form.durability" :min="-1" :max="100" :step="1" />
          <div class="form-tip">填 -1 表示永久（装饰品显示 ∞）</div>
        </el-form-item>
        <el-form-item label="亲密度">
          <el-input-number v-model="form.favor" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button class="nf-pro-dialog__cancel" @click="dialogVisible = false">取消</el-button>
          <el-button
            class="nf-pro-dialog__confirm"
            type="primary"
            :loading="dialogLoading"
            @click="submitForm"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pmarket {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pmarket-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pmarket-hero__badge {
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

.pmarket-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pmarket-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pmarket-table.nf-table-card {
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

.pmarket-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.cover-thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #f7f1ea;
  display: block;
}

.cover-fallback {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #f0e6db;
  color: #b08a62;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.muted {
  color: #c0c4cc;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>


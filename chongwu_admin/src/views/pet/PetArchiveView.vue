<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import CoverImageUpload from '@/components/common/CoverImageUpload.vue'
import MultiImageUpload from '@/components/common/MultiImageUpload.vue'
import TagInput from '@/components/common/TagInput.vue'
import { getPetBreedListApi } from '@/api/modules/pet-breed'
import {
  auditPetArchiveApi,
  batchAuditPetArchiveApi,
  deletePetArchiveApi,
  getPetArchiveInfoApi,
  getPetArchivePageApi,
  savePetArchiveApi,
  updatePetArchiveApi,
} from '@/api/modules/pet-archive'
import { getPetStorePageApi } from '@/api/modules/pet-store'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'
import { isBreedDefaultAvatar, resolveArchiveCover, resolveBreedAvatar } from '@/utils/pet-breed-avatar'

const auditMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
const auditTagType = { 0: 'warning', 1: 'success', 2: 'danger' }

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const storeOptions = ref([])
const breedOptions = ref([])
const dailyAlbumUrls = ref([])
const selectedRows = ref([])
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)
const batchRejectVisible = ref(false)
const batchRejectReason = ref('')

const auditFilter = ref('')
const adoptableFilter = ref('')
const storeFilter = ref('')
const petTypeFilter = ref('')

const form = reactive({
  id: '',
  storeId: '',
  name: '',
  petType: 'dog',
  breed: '',
  gender: '男',
  ageDesc: '',
  weightDesc: '',
  petCode: '',
  levelDesc: '',
  statusLabel: '',
  description: '',
  tags: [],
  goodAt: [],
  avoidItems: [],
  avatarUrl: '',
  adoptable: 0,
  auditStatus: 0,
  rejectReason: '',
  sort: 0,
  likeCount: 0,
})

const pendingCount = computed(() => tableData.value.filter((row) => Number(row.auditStatus) === 0).length)

function parseJsonArray(val) {
  if (Array.isArray(val)) return val
  if (!val) return []
  try {
    return JSON.parse(val)
  } catch (e) {
    return String(val).split(/[,，、]/).map((item) => item.trim()).filter(Boolean)
  }
}

function arrayToText(val) {
  return parseJsonArray(val).join('、')
}

function tagsToString(list) {
  return (list || []).join(',')
}

function resetForm() {
  Object.assign(form, {
    id: '',
    storeId: '',
    name: '',
    petType: 'dog',
    breed: '',
    gender: '男',
    ageDesc: '',
    weightDesc: '',
    petCode: '',
    levelDesc: '',
    statusLabel: '优秀员工',
    description: '',
    tags: [],
    goodAt: [],
    avoidItems: [],
    avatarUrl: '',
    adoptable: 0,
    auditStatus: 0,
    rejectReason: '',
    sort: 0,
    likeCount: 0,
  })
  dailyAlbumUrls.value = []
}

function extraAlbumUrls() {
  const cover = form.avatarUrl.trim()
  return dailyAlbumUrls.value
    .map((item) => String(item || '').trim())
    .filter((url) => url && url !== cover)
}

async function loadBreeds(petType = form.petType) {
  try {
    breedOptions.value = await getPetBreedListApi(petType)
  } catch (e) {
    breedOptions.value = []
  }
}

async function loadStores() {
  try {
    const data = await getPetStorePageApi({ page: 1, limit: 200 })
    storeOptions.value = data.list || []
  } catch (e) {
    storeOptions.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: pageNum.value, limit: pageSize.value }
    if (adoptableFilter.value !== '') params.adoptable = Number(adoptableFilter.value)
    if (storeFilter.value) params.storeId = Number(storeFilter.value)
    if (petTypeFilter.value) params.petType = petTypeFilter.value
    if (auditFilter.value !== '') params.auditStatus = Number(auditFilter.value)
    const data = await getPetArchivePageApi(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  loadBreeds('dog')
  applyBreedCover({ force: true })
  dialogVisible.value = true
}

async function openEdit(row) {
  resetForm()
  dialogLoading.value = true
  dialogVisible.value = true
  try {
    const data = await getPetArchiveInfoApi(row.id)
    Object.assign(form, {
      id: data.id,
      storeId: data.storeId,
      name: data.name || '',
      petType: data.petType || 'dog',
      breed: data.breed || '',
      gender: data.gender || '男',
      ageDesc: data.ageDesc || '',
      weightDesc: data.weightDesc || '',
      petCode: data.petCode || '',
      levelDesc: data.levelDesc || '',
      statusLabel: data.statusLabel || '',
      description: data.description || '',
      tags: parseJsonArray(data.tags),
      goodAt: parseJsonArray(data.goodAt),
      avoidItems: parseJsonArray(data.avoidItems),
      avatarUrl: data.avatarUrl || '',
      adoptable: Number(data.adoptable || 0),
      auditStatus: Number(data.auditStatus ?? 0),
      rejectReason: data.rejectReason || '',
      sort: Number(data.sort || 0),
      likeCount: Number(data.likeCount || 0),
    })
    await loadBreeds(form.petType)
    const cover = (data.avatarUrl || '').trim()
    dailyAlbumUrls.value = parseJsonArray(data.dailyAlbum).filter((url) => url && url !== cover)
    if (!form.avatarUrl || isBreedDefaultAvatar(form.avatarUrl)) {
      applyBreedCover({ force: true })
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载档案失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

function applyBreedCover({ force = false } = {}) {
  if (!form.breed && !form.petType) return
  const next = resolveBreedAvatar(form.breed, form.petType)
  if (force || !form.avatarUrl || isBreedDefaultAvatar(form.avatarUrl)) {
    form.avatarUrl = next
  }
}

function displayCover(row) {
  return resolveArchiveCover(row)
}

function buildPayload() {
  return {
    id: form.id || undefined,
    storeId: Number(form.storeId),
    name: form.name.trim(),
    petType: form.petType,
    breed: form.breed.trim(),
    gender: form.gender,
    ageDesc: form.ageDesc.trim(),
    weightDesc: form.weightDesc.trim(),
    petCode: form.petCode.trim(),
    levelDesc: form.levelDesc.trim(),
    statusLabel: form.statusLabel.trim(),
    description: form.description.trim(),
    tags: tagsToString(form.tags),
    goodAt: tagsToString(form.goodAt),
    avoidItems: tagsToString(form.avoidItems),
    avatarUrl: form.avatarUrl.trim(),
    dailyAlbumUrls: extraAlbumUrls(),
    adoptable: Number(form.adoptable || 0),
    auditStatus: Number(form.auditStatus ?? 0),
    rejectReason: form.auditStatus === 2 ? form.rejectReason.trim() : '',
    sort: Number(form.sort || 0),
    likeCount: Number(form.likeCount || 0),
  }
}

async function submitForm() {
  if (!form.name.trim()) return showError('请输入宠物昵称')
  if (!form.storeId) return showError('请选择所属门店')
  if (!form.breed.trim()) return showError('请选择或输入品种')
  if (!form.ageDesc.trim()) return showError('请输入年龄描述')
  if (!form.avatarUrl.trim()) return showError('请上传封面图')
  if (form.auditStatus === 2 && !form.rejectReason.trim()) return showError('拒绝时请填写原因')
  dialogLoading.value = true
  try {
    const payload = buildPayload()
    if (form.id) await updatePetArchiveApi(payload)
    else await savePetArchiveApi(payload)
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction('确认删除该宠物档案？')
  await deletePetArchiveApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

function canAuditRow(row) {
  return Number(row?.auditStatus) === 0
}

function handleSelectionChange(rows) {
  selectedRows.value = rows || []
}

async function auditRow(row, auditStatus, reason = '') {
  const actionText = auditStatus === 1 ? '通过' : '拒绝'
  await confirmAction(`确认${actionText}档案「${row.name}」？`)
  await auditPetArchiveApi({
    id: row.id,
    auditStatus,
    rejectReason: reason || undefined,
  })
  showSuccess(`已${actionText}`)
  await loadData()
}

function openReject(row) {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectReason.value.trim()) return showError('请填写拒绝原因')
  await auditPetArchiveApi({
    id: rejectTarget.value.id,
    auditStatus: 2,
    rejectReason: rejectReason.value.trim(),
  })
  rejectVisible.value = false
  showSuccess('已拒绝')
  await loadData()
}

async function batchApprove() {
  const ids = selectedRows.value.filter(canAuditRow).map((row) => row.id)
  if (!ids.length) return showError('请选择待审核档案')
  await confirmAction(`确认批量通过 ${ids.length} 条档案？`)
  await batchAuditPetArchiveApi({ ids, auditStatus: 1 })
  showSuccess('批量通过成功')
  selectedRows.value = []
  await loadData()
}

function openBatchReject() {
  const ids = selectedRows.value.filter(canAuditRow).map((row) => row.id)
  if (!ids.length) return showError('请选择待审核档案')
  batchRejectReason.value = ''
  batchRejectVisible.value = true
}

async function submitBatchReject() {
  const ids = selectedRows.value.filter(canAuditRow).map((row) => row.id)
  if (!batchRejectReason.value.trim()) return showError('请填写拒绝原因')
  await batchAuditPetArchiveApi({
    ids,
    auditStatus: 2,
    rejectReason: batchRejectReason.value.trim(),
  })
  batchRejectVisible.value = false
  showSuccess('批量拒绝成功')
  selectedRows.value = []
  await loadData()
}

watch(
  () => form.petType,
  (val, oldVal) => {
    if (val !== oldVal && !dialogLoading.value) {
      loadBreeds(val)
      if (!breedOptions.value.some((item) => item.name === form.breed)) {
        form.breed = ''
      }
      applyBreedCover()
    }
  },
)

watch(
  () => form.breed,
  (val, oldVal) => {
    if (dialogLoading.value) return
    if (val === oldVal) return
    applyBreedCover({ force: true })
  },
)

onMounted(async () => {
  await loadStores()
  await loadBreeds('dog')
  await loadData()
})
</script>

<template>
  <div class="parch">
    <header class="parch-hero nf-page-hero">
      <span class="parch-hero__badge">宠物业务</span>
      <h1 class="parch-hero__title">宠物档案</h1>
      <p class="parch-hero__desc">维护用户端宠物列表与详情数据，支持档案审核、批量操作与随拍相册管理</p>
    </header>

    <div class="parch-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
        <el-tag v-if="pendingCount" type="warning" size="small">本页待审 {{ pendingCount }}</el-tag>
      </div>
      <el-form :inline="true" class="parch-filter__form" @submit.prevent>
        <el-form-item label="审核状态">
          <el-radio-group v-model="auditFilter" @change="loadData">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button :label="0">待审</el-radio-button>
            <el-radio-button :label="1">通过</el-radio-button>
            <el-radio-button :label="2">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="门店">
          <el-select v-model="storeFilter" placeholder="选择门店" clearable @change="loadData">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="petTypeFilter" placeholder="宠物类型" clearable @change="loadData">
            <el-option label="狗" value="dog" />
            <el-option label="猫" value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="可领养">
          <el-select v-model="adoptableFilter" placeholder="可领养" clearable @change="loadData">
            <el-option label="全部" value="" />
            <el-option label="可领养" value="1" />
            <el-option label="不可领养" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item class="parch-filter__actions">
          <el-space wrap>
            <el-button type="primary" :disabled="!isAuth('pet:archive:add')" @click="openCreate">新增宠物</el-button>
            <el-button type="success" :disabled="!isAuth('pet:archive:batchAudit')" @click="batchApprove">批量通过</el-button>
            <el-button type="danger" :disabled="!isAuth('pet:archive:batchAudit')" @click="openBatchReject">批量拒绝</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="parch-table nf-table-card">
    <el-table v-loading="loading" :data="tableData" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <el-image
            :src="displayCover(row)"
            :preview-src-list="[displayCover(row)]"
            style="width: 48px; height: 48px; border-radius: 8px"
            fit="cover"
            preview-teleported
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="昵称" min-width="100" />
      <el-table-column prop="storeName" label="门店" min-width="110" />
      <el-table-column prop="petType" label="类型" width="70">
        <template #default="{ row }">{{ row.petType === 'cat' ? '猫' : '狗' }}</template>
      </el-table-column>
      <el-table-column prop="breed" label="品种" min-width="90" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="ageDesc" label="年龄" width="90" />
      <el-table-column label="性格" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ arrayToText(row.tags) || '-' }}</template>
      </el-table-column>
      <el-table-column label="审核" width="90">
        <template #default="{ row }">
          <el-tag :type="auditTagType[row.auditStatus] || 'info'" size="small">{{ auditMap[row.auditStatus] || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="拒绝原因" min-width="120" show-overflow-tooltip />
      <el-table-column prop="adoptable" label="可领养" width="80">
        <template #default="{ row }">{{ row.adoptable === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-space wrap alignment="start">
            <el-button v-if="canAuditRow(row)" size="small" type="success" :disabled="!isAuth('pet:archive:audit')" @click="auditRow(row, 1)">通过</el-button>
            <el-button v-if="canAuditRow(row)" size="small" type="danger" :disabled="!isAuth('pet:archive:audit')" @click="openReject(row)">拒绝</el-button>
            <el-button size="small" type="primary" :disabled="!isAuth('pet:archive:update')" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :disabled="!isAuth('pet:archive:delete')" @click="removeRow(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>
    <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑宠物档案' : '新增宠物档案'" width="760px" destroy-on-close class="nf-pro-dialog" align-center>
<el-form v-loading="dialogLoading" label-width="96px" class="archive-form">
        <el-divider content-position="left">基础信息（对应用户端列表/详情）</el-divider>
        <el-form-item label="封面图" required>
          <CoverImageUpload
            v-model="form.avatarUrl"
            tip="选择品种后会自动替换为对应插画封面，也可手动上传自定义图片"
          >
            <template #extra>
              <el-button round @click="applyBreedCover({ force: true })">按品种填充</el-button>
            </template>
          </CoverImageUpload>
        </el-form-item>
        <el-form-item label="所属门店" required>
          <el-select v-model="form.storeId" placeholder="选择门店" style="width: 100%">
            <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="昵称" required><el-input v-model="form.name" placeholder="如：豆包" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" required>
              <el-select v-model="form.petType" style="width: 100%">
                <el-option label="狗" value="dog" />
                <el-option label="猫" value="cat" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品种" required>
              <el-select v-model="form.breed" filterable allow-create default-first-option placeholder="选择或输入品种" style="width: 100%">
                <el-option v-for="item in breedOptions" :key="item.id" :label="item.name" :value="item.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" required>
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年龄" required><el-input v-model="form.ageDesc" placeholder="如：2岁" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重"><el-input v-model="form.weightDesc" placeholder="如：9kg" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="性格标签">
          <TagInput v-model="form.tags" placeholder="输入性格标签后回车" />
        </el-form-item>

        <el-divider content-position="left">详情扩展</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="宠物编号"><el-input v-model="form.petCode" placeholder="如：SH-007" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级标签"><el-input v-model="form.levelDesc" placeholder="如：Lv.5 明星宠物" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态标签"><el-input v-model="form.statusLabel" placeholder="如：优秀员工" /></el-form-item>
        <el-form-item label="爱做的事">
          <TagInput v-model="form.goodAt" placeholder="输入后回车添加" />
        </el-form-item>
        <el-form-item label="讨厌的事">
          <TagInput v-model="form.avoidItems" placeholder="输入后回车添加" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="关于 Ta 的介绍" />
        </el-form-item>

        <el-divider content-position="left">随拍相册（用户端详情 Tab）</el-divider>
        <el-form-item label="相册图片">
          <MultiImageUpload
            v-model="dailyAlbumUrls"
            :limit="20"
            tip="封面图会自动作为相册首图；社群相册来自已通过审核的社群帖子。"
          />
        </el-form-item>

        <el-divider content-position="left">运营设置</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="可领养"><el-switch v-model="form.adoptable" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="审核状态">
              <el-select v-model="form.auditStatus" style="width: 100%">
                <el-option label="待审" :value="0" />
                <el-option label="通过" :value="1" />
                <el-option label="拒绝" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.auditStatus === 2" label="拒绝原因">
          <el-input v-model="form.rejectReason" placeholder="请填写拒绝原因" />
        </el-form-item>
        <el-form-item v-if="form.id" label="点赞数"><el-input-number v-model="form.likeCount" :min="0" style="width: 180px" /></el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" :disabled="!isAuth(form.id ? 'pet:archive:update' : 'pet:archive:add')" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="拒绝档案" width="420px" class="nf-pro-dialog" align-center>
<el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写拒绝原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchRejectVisible" title="批量拒绝档案" width="420px" class="nf-pro-dialog" align-center>
<el-input v-model="batchRejectReason" type="textarea" :rows="3" placeholder="请填写拒绝原因" />
      <template #footer>
        <el-button @click="batchRejectVisible = false">取消</el-button>
        <el-button type="danger" @click="submitBatchReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.parch {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.parch-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.parch-hero__badge {
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

.parch-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.parch-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.parch-filter.nf-filter-card {
  margin-bottom: 0;
}

.parch-filter__form {
  margin-bottom: 0;

  :deep(.el-select) {
    width: 140px;
  }
}

.parch-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.parch-table.nf-table-card {
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

.archive-form {
  max-height: 62vh;
  overflow-y: auto;
  padding-right: 8px;
}
</style>

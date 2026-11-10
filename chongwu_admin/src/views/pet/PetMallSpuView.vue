<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import CoverImageUpload from '@/components/common/CoverImageUpload.vue'
import { getMallCategoryListApi } from '@/api/modules/pet-mall-category'
import { getMallSkuSpecListApi } from '@/api/modules/pet-mall-sku-spec'
import {
  deletePetMallSpuApi,
  getPetMallSpuInfoApi,
  getPetMallSpuPageApi,
  savePetMallSpuApi,
  updatePetMallSpuApi,
} from '@/api/modules/pet-mall'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const bizTypeMap = { 1: '实物商品', 2: '投喂商品', 3: '次卡商品', 4: '积分兑换' }
const specModeMap = { 1: '单规格', 2: '多规格' }

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const categoryOptions = ref([])
const specOptions = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const detailLoading = ref(false)
const editingId = ref(null)

const filters = reactive({
  keyword: '',
  categoryId: '',
  bizType: 2,
  status: '',
})

const form = reactive({
  name: '',
  categoryId: '',
  coverUrl: '',
  description: '',
  price: 0,
  originalPrice: 0,
  pointPrice: 0,
  bizType: 2,
  status: 1,
  sort: 0,
  specMode: 1,
  specIds: [],
  singleSku: {
    id: null,
    skuName: '标准装',
    price: 0,
    pointPrice: 0,
    stock: 999,
  },
})

/** 笛卡尔积生成后的可编辑 SKU 列表 */
const generatedSkus = ref([])
/** 当前商品上可增删的规格维度（从模板拷贝，不回写规格库） */
const editableRules = ref([])

const statusOptions = [
  { label: '全部', value: '' },
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

function parseSpecRules(item) {
  let rules = item?.ruleValue ?? item?.rule_value ?? item?.ruleValueJson
  if (typeof rules === 'string') {
    try { rules = JSON.parse(rules) } catch (e) { rules = [] }
  }
  if (!Array.isArray(rules)) return []
  return rules
    .map((rule) => {
      const names = []
      if (Array.isArray(rule?.items)) {
        rule.items.forEach((v) => {
          const name = String(v?.name || '').trim()
          if (name) names.push(name)
        })
      }
      String(rule?.detail || '')
        .split(',')
        .map((v) => v.trim())
        .filter(Boolean)
        .forEach((name) => { if (!names.includes(name)) names.push(name) })
      return {
        value: String(rule?.value || '').trim(),
        names,
      }
    })
    .filter((rule) => rule.value && rule.names.length)
}

/** 多规格组笛卡尔积：份量×口味 => 100g-原味 */
function cartesianNames(dimensions) {
  if (!dimensions.length) return []
  return dimensions.reduce((acc, dim) => {
    if (!acc.length) return dim.map((name) => name)
    const next = []
    acc.forEach((prefix) => {
      dim.forEach((name) => next.push(`${prefix}-${name}`))
    })
    return next
  }, [])
}

function getActiveSpec() {
  const specId = form.specIds[0]
  if (!specId) return null
  return specOptions.value.find((item) => item.id === specId) || null
}

function cloneRulesFromTemplate(spec, skuList = []) {
  if (!spec) return []
  const templateRules = parseSpecRules(spec)
  if (!templateRules.length) {
    return [{ value: '规格', names: [spec.specName || '标准装'], draft: '' }]
  }
  const skus = (skuList || []).filter((item) => item?.skuName)
  if (!skus.length) {
    return templateRules.map((rule) => ({
      value: rule.value,
      names: [...rule.names],
      draft: '',
    }))
  }
  const dimCount = templateRules.length
  const canSplit = skus.every((sku) => String(sku.skuName).split('-').length === dimCount)
  if (!canSplit) {
    return templateRules.map((rule) => ({
      value: rule.value,
      names: [...rule.names],
      draft: '',
    }))
  }
  const usedByDim = templateRules.map(() => new Set())
  skus.forEach((sku) => {
    String(sku.skuName).split('-').forEach((part, idx) => {
      const name = part.trim()
      if (name) usedByDim[idx].add(name)
    })
  })
  return templateRules.map((rule, idx) => {
    const used = [...usedByDim[idx]]
    const ordered = rule.names.filter((name) => used.includes(name))
    used.forEach((name) => {
      if (!ordered.includes(name)) ordered.push(name)
    })
    return {
      value: rule.value,
      names: ordered.length ? ordered : [...rule.names],
      draft: '',
    }
  })
}

function buildSkusFromEditableRules(preserveMap = {}) {
  const spec = getActiveSpec()
  const specId = form.specIds[0]
  if (!specId) return []
  const dimensions = editableRules.value
    .map((rule) => (rule.names || []).map((name) => String(name).trim()).filter(Boolean))
    .filter((names) => names.length)
  const names = dimensions.length ? cartesianNames(dimensions) : []
  return names.map((skuName) => {
    const old = preserveMap[`${specId}#${skuName}`] || preserveMap[skuName] || {}
    return {
      specId,
      skuName,
      price: Number(old.price ?? spec?.price ?? form.price ?? 0),
      pointPrice: Number(old.pointPrice ?? spec?.pointPrice ?? form.pointPrice ?? 0),
      stock: Number(old.stock ?? spec?.defaultStock ?? 999),
    }
  })
}

function rebuildGeneratedSkus(preserveCurrent = true) {
  const preserveMap = {}
  if (preserveCurrent) {
    generatedSkus.value.forEach((item) => {
      preserveMap[`${item.specId}#${item.skuName}`] = item
      preserveMap[item.skuName] = item
    })
  }
  generatedSkus.value = buildSkusFromEditableRules(preserveMap)
}

function resetForm() {
  Object.assign(form, {
    name: '',
    categoryId: '',
    coverUrl: '',
    description: '',
    price: 0,
    originalPrice: 0,
    pointPrice: 0,
    bizType: 2,
    status: 1,
    sort: 0,
    specMode: 1,
    specIds: [],
    singleSku: {
      id: null,
      skuName: '标准装',
      price: 0,
      pointPrice: 0,
      stock: 999,
    },
  })
  generatedSkus.value = []
  editableRules.value = []
}

function formatPrice(value) {
  const num = Number(value)
  return Number.isFinite(num) ? num.toFixed(2) : '0.00'
}

/** 单选规格模板，并拷贝为可编辑规则 */
function toggleSpecId(id) {
  if (form.specIds.includes(id)) {
    form.specIds = []
    editableRules.value = []
    generatedSkus.value = []
    return
  }
  form.specIds = [id]
  editableRules.value = cloneRulesFromTemplate(getActiveSpec())
  rebuildGeneratedSkus(true)
}

function onSpecModeChange(mode) {
  if (Number(mode) === 2) {
    if (form.specIds.length && !editableRules.value.length) {
      editableRules.value = cloneRulesFromTemplate(getActiveSpec())
    }
    rebuildGeneratedSkus(true)
  } else {
    generatedSkus.value = []
    editableRules.value = []
  }
}

function removeRuleTag(rule, tag) {
  if ((rule.names || []).length <= 1) return showError('规格值至少要有一个')
  rule.names = rule.names.filter((item) => item !== tag)
  rebuildGeneratedSkus(true)
}

function addRuleTag(rule) {
  const name = String(rule.draft || '').trim()
  if (!name) return
  if (!rule.names.includes(name)) rule.names.push(name)
  rule.draft = ''
  rebuildGeneratedSkus(true)
}

async function loadCategories() {
  try {
    const list = await getMallCategoryListApi()
    categoryOptions.value = (list || []).filter((item) => item.name !== '全部商品')
  } catch (error) {
    categoryOptions.value = []
  }
}

async function loadSpecOptions() {
  try {
    specOptions.value = await getMallSkuSpecListApi()
  } catch (error) {
    specOptions.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetMallSpuPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      keyword: filters.keyword.trim(),
      categoryId: filters.categoryId || undefined,
      bizType: filters.bizType,
      status: filters.status,
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.keyword = ''
  filters.categoryId = ''
  filters.bizType = 2
  filters.status = ''
  pageNum.value = 1
  loadData()
}

function filterFeedOnly() {
  filters.bizType = 2
  pageNum.value = 1
  loadData()
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  resetForm()
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const detail = await getPetMallSpuInfoApi(row.id)
    const firstSku = detail.skuList?.[0] || {}
    Object.assign(form, {
      name: detail.name || '',
      categoryId: detail.categoryId || '',
      coverUrl: detail.coverUrl || '',
      description: detail.description || '',
      price: Number(detail.price || 0),
      originalPrice: Number(detail.originalPrice || 0),
      pointPrice: Number(detail.pointPrice || 0),
      bizType: Number(detail.bizType || 2),
      status: Number(detail.status ?? 1),
      sort: Number(detail.sort || 0),
      specMode: Number(detail.specMode || (detail.skuList?.length > 1 ? 2 : 1)),
      specIds: [...new Set((detail.specIds || detail.skuList?.map((item) => item.specId).filter(Boolean) || []))].slice(0, 1),
      singleSku: {
        id: firstSku.id || null,
        skuName: firstSku.skuName || '标准装',
        price: Number(firstSku.price ?? detail.price ?? 0),
        pointPrice: Number(firstSku.pointPrice ?? detail.pointPrice ?? 0),
        stock: Number(firstSku.stock ?? 999),
      },
    })
    if (form.specMode === 2) {
      const preserveMap = {}
      ;(detail.skuList || []).forEach((sku) => {
        if (!sku?.skuName) return
        const item = {
          specId: sku.specId,
          skuName: sku.skuName,
          price: Number(sku.price || 0),
          pointPrice: Number(sku.pointPrice || 0),
          stock: Number(sku.stock || 0),
        }
        if (sku.specId) preserveMap[`${sku.specId}#${sku.skuName}`] = item
        preserveMap[sku.skuName] = item
      })
      editableRules.value = cloneRulesFromTemplate(getActiveSpec(), detail.skuList || [])
      rebuildGeneratedSkus(false)
      generatedSkus.value.forEach((item) => {
        const old = preserveMap[`${item.specId}#${item.skuName}`] || preserveMap[item.skuName]
        if (!old) return
        item.price = Number(old.price || 0)
        item.pointPrice = Number(old.pointPrice || 0)
        item.stock = Number(old.stock || 0)
      })
      if (!generatedSkus.value.length && (detail.skuList || []).length) {
        generatedSkus.value = (detail.skuList || []).map((sku) => ({
          specId: sku.specId || form.specIds[0],
          skuName: sku.skuName,
          price: Number(sku.price || 0),
          pointPrice: Number(sku.pointPrice || 0),
          stock: Number(sku.stock || 0),
        }))
      }
    } else {
      generatedSkus.value = []
      editableRules.value = []
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载商品详情失败')
    dialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

function buildPayload() {
  const bizType = Number(form.bizType || 2)
  const payload = {
    name: form.name.trim(),
    category: bizType === 2 ? 'feed' : undefined,
    categoryId: Number(form.categoryId),
    coverUrl: form.coverUrl,
    description: form.description.trim(),
    price: Number(form.price || 0),
    originalPrice: Number(form.originalPrice || 0),
    pointPrice: Number(form.pointPrice || 0),
    bizType,
    status: Number(form.status ?? 1),
    sort: Number(form.sort || 0),
    specMode: Number(form.specMode || 1),
  }
  if (payload.specMode === 2) {
    payload.specIds = [...form.specIds]
    payload.skuList = generatedSkus.value.map((item) => ({
      specId: item.specId,
      skuName: item.skuName,
      price: Number(item.price || 0),
      pointPrice: Number(item.pointPrice || 0),
      stock: Number(item.stock || 0),
    }))
  } else {
    payload.singleSku = {
      id: form.singleSku.id || undefined,
      skuName: form.singleSku.skuName.trim() || '标准装',
      price: Number(form.singleSku.price ?? form.price ?? 0),
      pointPrice: Number(form.singleSku.pointPrice ?? form.pointPrice ?? 0),
      stock: Number(form.singleSku.stock ?? 0),
    }
  }
  return payload
}

async function submitForm() {
  if (!form.name.trim()) return showError('请输入商品名称')
  if (!form.categoryId) return showError('请选择商品分类')
  if (!form.coverUrl) return showError('请上传商品封面')
  if (form.specMode === 2 && !form.specIds.length) return showError('多规格商品请至少选择一个规格')
  if (form.specMode === 2 && !editableRules.value.length) return showError('请至少保留一种规格')
  if (form.specMode === 2 && !generatedSkus.value.length) return showError('请先完善规格以生成SKU')
  if (form.specMode === 1 && !form.singleSku.skuName.trim()) return showError('请输入规格名称')

  dialogLoading.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updatePetMallSpuApi({ ...payload, id: editingId.value })
      showSuccess('修改成功')
    } else {
      await savePetMallSpuApi(payload)
      showSuccess('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction(`确认删除商品「${row.name}」吗？`)
  try {
    await deletePetMallSpuApi(row.id)
    showSuccess('删除成功')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

async function handleStatusChange(row, previousValue) {
  try {
    await updatePetMallSpuApi({
      id: row.id,
      name: row.name,
      category: Number(row.bizType) === 2 ? 'feed' : row.category,
      categoryId: row.categoryId,
      coverUrl: row.coverUrl,
      description: row.description || '',
      price: row.price,
      originalPrice: row.originalPrice,
      pointPrice: row.pointPrice,
      bizType: row.bizType,
      status: Number(row.status),
      sort: row.sort ?? 0,
      specMode: row.specMode || 1,
    })
    showSuccess('状态已更新')
  } catch (error) {
    row.status = previousValue
    showError(error?.msg || error?.message || '状态更新失败')
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadSpecOptions(), loadData()])
})
</script>

<template>
  <div class="pmall">
    <header class="pmall-hero nf-page-hero">
      <span class="pmall-hero__badge">宠物业务</span>
      <h1 class="pmall-hero__title">商品管理</h1>
      <p class="pmall-hero__desc">规格类型支持单/多规格；多规格从规格库选模板后按笛卡尔积生成规格明细</p>
    </header>

    <div class="pmall-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pmall-filter__form" @submit.prevent>
        <el-form-item label="名称">
          <el-input v-model="filters.keyword" clearable placeholder="商品名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" clearable placeholder="全部">
            <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.bizType" clearable placeholder="全部">
            <el-option v-for="(label, key) in bizTypeMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option v-for="item in statusOptions" :key="`status-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="pmall-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button @click="filterFeedOnly">仅投喂商品</el-button>
            <el-button type="primary" :disabled="!isAuth('pet:mall:add')" @click="openCreate">新增商品</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pmall-table nf-table-card">
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <el-image
            v-if="row.coverUrl"
            :src="row.coverUrl"
            :preview-src-list="[row.coverUrl]"
            fit="cover"
            class="thumb-image"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="110" />
      <el-table-column label="规格类型" width="100" align="center">
        <template #default="{ row }">{{ specModeMap[row.specMode] || '单规格' }}</template>
      </el-table-column>
      <el-table-column label="售价" width="90" align="right">
        <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="pointPrice" label="积分" width="80" align="center" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ bizTypeMap[row.bizType] || row.bizType }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            :disabled="!isAuth('pet:mall:update')"
            @change="(value) => handleStatusChange(row, value === 1 ? 0 : 1)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-space>
            <el-button size="small" type="primary" :disabled="!isAuth('pet:mall:update')" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :disabled="!isAuth('pet:mall:delete')" @click="removeRow(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑商品' : '新增商品'"
      :width="form.specMode === 2 ? '920px' : '760px'"
      destroy-on-close
      align-center
      class="nf-pro-dialog"
    >
      <div v-loading="detailLoading">
        <el-form label-width="100px">
          <el-form-item label="商品封面" required>
            <CoverImageUpload v-model="form.coverUrl" tip="建议上传清晰正方形商品封面" />
          </el-form-item>
          <el-form-item label="商品名称" required>
            <el-input v-model="form.name" placeholder="如：鲜肉罐头套餐" maxlength="64" show-word-limit />
          </el-form-item>
          <el-form-item label="商品分类" required>
            <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
              <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="商品描述">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="门店同款 · 宠友热推" />
          </el-form-item>
          <el-form-item label="售价">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
          </el-form-item>
          <el-form-item label="原价">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="1" />
          </el-form-item>
          <el-form-item label="积分价">
            <el-input-number v-model="form.pointPrice" :min="0" :step="1" />
          </el-form-item>
          <el-form-item label="商品类型">
            <el-select v-model="form.bizType" style="width: 100%">
              <el-option v-for="(label, key) in bizTypeMap" :key="key" :label="label" :value="Number(key)" />
            </el-select>
          </el-form-item>
          <el-form-item label="规格类型" required>
            <el-radio-group v-model="form.specMode" @change="onSpecModeChange">
              <el-radio :value="1">单规格</el-radio>
              <el-radio :value="2">多规格</el-radio>
            </el-radio-group>
          </el-form-item>

          <template v-if="form.specMode === 1">
            <el-form-item label="规格明细" required>
              <el-table :data="[form.singleSku]" border size="small" class="sku-detail-table">
                <el-table-column label="规格名称" min-width="160">
                  <template #default="{ row }">
                    <el-input v-model="row.skuName" placeholder="如：标准装" maxlength="64" />
                  </template>
                </el-table-column>
                <el-table-column label="售价" width="150">
                  <template #default="{ row }">
                    <el-input-number v-model="row.price" size="small" :min="0" :precision="2" :step="1" :controls="false" class="sku-num" />
                  </template>
                </el-table-column>
                <el-table-column label="积分" width="130">
                  <template #default="{ row }">
                    <el-input-number v-model="row.pointPrice" size="small" :min="0" :step="1" :controls="false" class="sku-num" />
                  </template>
                </el-table-column>
                <el-table-column label="库存" width="130">
                  <template #default="{ row }">
                    <el-input-number v-model="row.stock" size="small" :min="0" :step="1" :controls="false" class="sku-num" />
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
          </template>

          <template v-else>
            <el-form-item label="商品规格" required>
              <div class="goods-spec">
                <div class="goods-spec__toolbar">
                  <span class="goods-spec__hint">从规格库选择模板后，可对本商品增删规格值</span>
                  <div class="spec-pick">
                    <button
                      v-for="item in specOptions"
                      :key="item.id"
                      type="button"
                      class="spec-pill"
                      :class="{ 'is-active': form.specIds.includes(item.id) }"
                      @click="toggleSpecId(item.id)"
                    >
                      {{ item.specName }}
                    </button>
                  </div>
                </div>
                <p v-if="!specOptions.length" class="spec-tip">暂无可用规格，请先在「规格管理」中添加</p>
                <div v-if="editableRules.length" class="goods-spec__groups">
                  <div v-for="(rule, index) in editableRules" :key="`edit-rule-${index}-${rule.value}`" class="goods-spec__group">
                    <div class="goods-spec__group-label">{{ rule.value }}</div>
                    <div class="goods-spec__tags">
                      <el-tag
                        v-for="name in rule.names"
                        :key="`${rule.value}-${name}`"
                        closable
                        effect="plain"
                        class="goods-spec__tag"
                        @close="removeRuleTag(rule, name)"
                      >
                        {{ name }}
                      </el-tag>
                      <div class="goods-spec__add">
                        <el-input
                          v-model="rule.draft"
                          size="small"
                          class="goods-spec__add-input"
                          placeholder="请输入规格值"
                          @keyup.enter="addRuleTag(rule)"
                        />
                        <button type="button" class="goods-spec__add-btn" @click="addRuleTag(rule)">添加</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-form-item>
            <el-form-item v-if="generatedSkus.length" label="规格明细">
              <div class="sku-detail">
                <el-table :data="generatedSkus" border size="small" class="sku-detail-table" max-height="360">
                  <el-table-column label="规格组合" prop="skuName" min-width="160" show-overflow-tooltip>
                    <template #default="{ row }">
                      <span class="sku-detail__name">{{ row.skuName }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="售价" width="150">
                    <template #default="{ row }">
                      <el-input-number v-model="row.price" size="small" :min="0" :precision="2" :step="1" :controls="false" class="sku-num" />
                    </template>
                  </el-table-column>
                  <el-table-column label="积分" width="130">
                    <template #default="{ row }">
                      <el-input-number v-model="row.pointPrice" size="small" :min="0" :step="1" :controls="false" class="sku-num" />
                    </template>
                  </el-table-column>
                  <el-table-column label="库存" width="130">
                    <template #default="{ row }">
                      <el-input-number v-model="row.stock" size="small" :min="0" :step="1" :controls="false" class="sku-num" />
                    </template>
                  </el-table-column>
                </el-table>
                <p class="spec-tip">增删规格值后会自动按笛卡尔积刷新规格明细（如 100g-原味）</p>
              </div>
            </el-form-item>
          </template>

          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" :max="9999" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">上架</el-radio>
              <el-radio :value="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
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
.pmall {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pmall-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pmall-hero__badge {
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

.pmall-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pmall-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pmall-filter.nf-filter-card {
  margin-bottom: 0;
}

.pmall-filter__form {
  margin-bottom: 0;

  :deep(.el-form-item) {
    margin-bottom: 12px;
    margin-right: 12px;
  }

  :deep(.el-select) {
    width: 140px;
  }

  :deep(.el-input) {
    width: 160px;
  }
}

.pmall-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pmall-table.nf-table-card {
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

.thumb-image {
  width: 72px;
  height: 72px;
  border-radius: 8px;
}

.goods-spec {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goods-spec__toolbar {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.goods-spec__hint {
  font-size: 12px;
  color: var(--nf-text-secondary);
}

.spec-pick {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  width: 100%;
}

.spec-pill {
  min-width: 72px;
  height: 34px;
  padding: 0 18px;
  border: none;
  border-radius: 999px;
  background: #f3ebe0;
  color: #b59a7f;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.spec-pill.is-active {
  background: #c79662;
  color: #fff;
}

.goods-spec__groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid #ebe4da;
  border-radius: 10px;
  background: #faf8f5;
}

.goods-spec__group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goods-spec__group-label {
  font-size: 13px;
  font-weight: 600;
  color: #5a4030;
  line-height: 1.2;
}

.goods-spec__tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.goods-spec__tag {
  margin: 0;
  height: 28px;
  padding: 0 10px;
  line-height: 26px;
  border-color: #e8d9c8;
  background: #fff;
  color: #8a6a4a;
  border-radius: 6px;
}

.goods-spec__add {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.goods-spec__add-input {
  width: 132px;

  :deep(.el-input__wrapper) {
    border-radius: 6px;
    box-shadow: 0 0 0 1px #e8d9c8 inset;
    background: #fff;
  }

  :deep(.el-input__wrapper:hover),
  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #c79662 inset;
  }
}

.goods-spec__add-btn {
  height: 28px;
  padding: 0 14px;
  border: none;
  border-radius: 6px;
  background: #c79662;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  line-height: 28px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: #b88752;
  }
}

.sku-detail {
  width: 100%;
}

.sku-detail-table {
  width: 100%;

  :deep(.el-table__header th) {
    background: #f7f8fa;
    color: var(--nf-text-secondary);
    font-weight: 600;
  }

  :deep(.el-input-number) {
    width: 100%;
  }

  :deep(.el-input-number .el-input__wrapper) {
    padding-left: 8px;
    padding-right: 8px;
  }
}

.sku-detail__name {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  font-weight: 600;
  color: #5a4030;
  vertical-align: middle;
}

.sku-num {
  width: 100%;
}

.spec-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}
</style>

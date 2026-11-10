<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  deleteMallSkuSpecApi,
  getMallSkuSpecPageApi,
  saveMallSkuSpecApi,
  updateMallSkuSpecApi,
} from '@/api/modules/pet-mall-sku-spec'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

function isSpecAuth(action) {
  const specPerm = `pet:mall:spec:${action}`
  const mallPerm = action === 'list' ? 'pet:mall:list' : `pet:mall:${action}`
  return isAuth(specPerm) || isAuth(mallPerm)
}

function splitDetail(detail) {
  return String(detail || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

/** 兼容后端返回数组 / JSON 字符串 / items 结构 */
function parseRuleValue(raw) {
  let list = raw
  if (!list) return []
  if (typeof list === 'string') {
    try {
      list = JSON.parse(list)
    } catch (e) {
      return []
    }
  }
  if (!Array.isArray(list)) return []
  return list
    .map((item) => {
      const names = []
      if (Array.isArray(item?.items)) {
        item.items.forEach((v) => {
          const name = String(v?.name || '').trim()
          if (name) names.push(name)
        })
      }
      splitDetail(item?.detail).forEach((name) => {
        if (!names.includes(name)) names.push(name)
      })
      return {
        value: String(item?.value || '').trim(),
        detail: names.join(','),
        draft: '',
      }
    })
    .filter((item) => item.value && item.detail)
}

function formatRulePreview(row) {
  const rules = parseRuleValue(row?.ruleValue ?? row?.rule_value ?? row?.ruleValueJson)
  if (!rules.length) return row?.specName || '-'
  return rules.map((item) => `${item.value}: ${item.detail}`).join('；')
}

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const addingRule = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)

const filters = reactive({
  keyword: '',
  status: '',
})

const form = reactive({
  specName: '',
  price: 0,
  pointPrice: 0,
  defaultStock: 999,
  sort: 0,
  status: 1,
})

const ruleList = ref([])
const draftRule = reactive({ value: '', detail: '' })
const inputValues = reactive({})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

function resetForm() {
  Object.assign(form, {
    specName: '',
    price: 0,
    pointPrice: 0,
    defaultStock: 999,
    sort: 0,
    status: 1,
  })
  ruleList.value = []
  addingRule.value = false
  draftRule.value = ''
  draftRule.detail = ''
  Object.keys(inputValues).forEach((key) => delete inputValues[key])
}

function formatPrice(value) {
  const num = Number(value)
  return Number.isFinite(num) ? num.toFixed(2) : '0.00'
}

async function loadData() {
  loading.value = true
  try {
    const data = await getMallSkuSpecPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      keyword: filters.keyword.trim(),
      status: filters.status,
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载规格失败')
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
  filters.status = ''
  pageNum.value = 1
  loadData()
}

function applyFormFromRow(row) {
  const r = row || {}
  Object.assign(form, {
    specName: r.specName || r.spec_name || '',
    price: Number(r.price || 0),
    pointPrice: Number(r.pointPrice ?? r.point_price ?? 0),
    defaultStock: Number(r.defaultStock ?? r.default_stock ?? 999),
    sort: Number(r.sort || 0),
    status: Number(r.status ?? 1),
  })
  const raw = r.ruleValue ?? r.rule_value ?? r.ruleValueJson ?? r.rule_value_json
  ruleList.value = parseRuleValue(raw).map((item) => ({
    value: item.value,
    detail: item.detail,
    draft: '',
  }))
  addingRule.value = false
  draftRule.value = ''
  draftRule.detail = ''
  Object.keys(inputValues).forEach((key) => delete inputValues[key])
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row?.id ?? null
  applyFormFromRow(row)
  dialogVisible.value = true
}

function onDialogOpened() {
  // destroy-on-close 重建完成后，确保表单已带上当前行数据
  if (!editingId.value) return
  const row = (tableData.value || []).find((item) => item.id === editingId.value)
  if (row) applyFormFromRow(row)
}

function startAddRule() {
  if (!form.specName.trim()) return showError('请输入标题名称')
  addingRule.value = true
  draftRule.value = ''
  draftRule.detail = ''
}

function cancelAddRule() {
  addingRule.value = false
  draftRule.value = ''
  draftRule.detail = ''
}

function confirmAddRule() {
  if (!form.specName.trim()) return showError('请输入标题名称')
  if (!draftRule.value.trim()) return showError('请输入规格')
  if (!draftRule.detail.trim()) return showError('请输入规格值')
  ruleList.value.push({
    value: draftRule.value.trim(),
    detail: draftRule.detail.trim(),
    draft: '',
  })
  cancelAddRule()
}

function removeRule(index) {
  ruleList.value.splice(index, 1)
  delete inputValues[index]
}

function removeRuleTag(rule, tag) {
  const values = splitDetail(rule.detail)
  const next = values.filter((item) => item !== tag)
  if (!next.length) return showError('规格值至少要有一个')
  rule.detail = next.join(',')
}

function addRuleTag(index, rule) {
  const name = String(inputValues[index] || rule.draft || '').trim()
  if (!name) return
  const values = splitDetail(rule.detail)
  if (!values.includes(name)) values.push(name)
  rule.detail = values.join(',')
  inputValues[index] = ''
  rule.draft = ''
}

async function submitForm() {
  if (!form.specName.trim()) return showError('请输入标题名称')
  if (!ruleList.value.length) return showError('请至少添加一种规格')
  for (const rule of ruleList.value) {
    if (!rule.value.trim() || !splitDetail(rule.detail).length) {
      return showError('请完善规格与规格值')
    }
  }
  dialogLoading.value = true
  try {
    const payload = {
      specName: form.specName.trim(),
      ruleValue: ruleList.value.map((item) => ({
        value: item.value.trim(),
        detail: splitDetail(item.detail).join(','),
      })),
      price: Number(form.price || 0),
      pointPrice: Number(form.pointPrice || 0),
      defaultStock: Number(form.defaultStock || 0),
      sort: Number(form.sort || 0),
      status: Number(form.status ?? 1),
    }
    if (editingId.value) {
      await updateMallSkuSpecApi({ ...payload, id: editingId.value })
      showSuccess('规格已更新')
    } else {
      await saveMallSkuSpecApi(payload)
      showSuccess('规格已新增')
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
  await confirmAction(`确认删除规格「${row.specName}」吗？`)
  try {
    await deleteMallSkuSpecApi(row.id)
    showSuccess('删除成功')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="pmallspec">
    <header class="pmallspec-hero nf-page-hero">
      <span class="pmallspec-hero__badge">宠物业务</span>
      <h1 class="pmallspec-hero__title">规格管理</h1>
      <p class="pmallspec-hero__desc">维护规格标题与多组属性；商品侧按多规格笛卡尔积生成 SKU 并填写价格</p>
    </header>

    <div class="pmallspec-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pmallspec-filter__form" @submit.prevent>
        <el-form-item label="名称">
          <el-input v-model="filters.keyword" clearable placeholder="规格标题" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option v-for="item in statusOptions" :key="`status-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="pmallspec-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" :disabled="!isSpecAuth('add')" @click="openCreate">添加规格</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pmallspec-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="规格ID" width="90" />
        <el-table-column prop="specName" label="规格标题" min-width="140" />
        <el-table-column label="规格值" min-width="280">
          <template #default="{ row }">
            <div class="rule-cell">
              <div
                v-for="(rule, idx) in parseRuleValue(row.ruleValue ?? row.rule_value ?? row.ruleValueJson)"
                :key="`${row.id}-${idx}`"
                class="rule-cell__line"
              >
                <span class="rule-cell__name">{{ rule.value }}:</span>
                <span>{{ rule.detail }}</span>
              </div>
              <span v-if="!parseRuleValue(row.ruleValue ?? row.rule_value ?? row.ruleValueJson).length">{{ formatRulePreview(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="默认售价" width="100" align="right">
          <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="pointPrice" label="默认积分" width="90" align="center" />
        <el-table-column prop="defaultStock" label="默认库存" width="90" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" type="primary" :disabled="!isSpecAuth('update')" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" :disabled="!isSpecAuth('delete')" @click="removeRow(row)">删除</el-button>
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
      :title="editingId ? '修改规格' : '添加规格'"
      width="720px"
      destroy-on-close
      class="nf-pro-dialog spec-dialog"
      align-center
      @opened="onDialogOpened"
    >
      <div class="spec-dialog__body">
        <div class="spec-dialog__title-row">
          <span class="spec-dialog__label">规格标题：</span>
          <el-input v-model="form.specName" placeholder="请输入标题名称 例:衣服" style="width: 240px" maxlength="64" />
        </div>

        <div class="spec-rule-list">
          <div v-for="(rule, index) in ruleList" :key="`rule-${index}`" class="spec-rule-item">
            <div class="spec-rule-item__head">
              <span class="spec-rule-item__name">{{ rule.value }}</span>
              <el-button type="danger" link @click="removeRule(index)">删除</el-button>
            </div>
            <div class="spec-rule-item__tags">
              <el-tag
                v-for="tag in splitDetail(rule.detail)"
                :key="`${rule.value}-${tag}`"
                closable
                class="spec-rule-tag"
                @close="removeRuleTag(rule, tag)"
              >
                {{ tag }}
              </el-tag>
              <div class="spec-rule-add">
                <el-input
                  v-model="inputValues[index]"
                  size="small"
                  class="spec-rule-add__input"
                  placeholder="请输入属性名"
                  @keyup.enter="addRuleTag(index, rule)"
                />
                <el-button size="small" class="spec-rule-add__btn" @click="addRuleTag(index, rule)">添加</el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="spec-dialog__actions">
          <el-button v-if="!addingRule" type="primary" @click="startAddRule">添加新规格</el-button>
          <div v-else class="spec-draft-row">
            <div class="spec-draft-field">
              <span class="spec-dialog__label">规格：</span>
              <el-input v-model="draftRule.value" placeholder="例:颜色" style="width: 160px" />
            </div>
            <div class="spec-draft-field">
              <span class="spec-dialog__label">规格值：</span>
              <el-input v-model="draftRule.detail" placeholder="例:黑色" style="width: 160px" />
            </div>
            <el-button type="primary" @click="confirmAddRule">确 定</el-button>
            <el-button @click="cancelAddRule">取 消</el-button>
          </div>
        </div>

        <el-divider content-position="left">默认售卖信息（生成 SKU 时预填）</el-divider>
        <el-form label-width="100px" class="spec-extra-form" inline>
          <el-form-item label="默认售价">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
          </el-form-item>
          <el-form-item label="默认积分">
            <el-input-number v-model="form.pointPrice" :min="0" :step="1" />
          </el-form-item>
          <el-form-item label="默认库存">
            <el-input-number v-model="form.defaultStock" :min="0" :step="1" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pmallspec {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pmallspec-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pmallspec-hero__badge {
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

.pmallspec-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pmallspec-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pmallspec-filter.nf-filter-card { margin-bottom: 0; }

.pmallspec-filter__form {
  margin-bottom: 0;
  :deep(.el-select) { width: 120px; }
  :deep(.el-input) { width: 160px; }
}

.pmallspec-filter__actions {
  :deep(.el-form-item__content) { flex-wrap: wrap; }
}

.pmallspec-table.nf-table-card {
  margin-bottom: 0;
  padding-bottom: 12px;
}

.rule-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.5;
}

.rule-cell__line {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.rule-cell__name {
  color: var(--nf-text-secondary);
  font-weight: 600;
}

.spec-dialog__body { padding: 4px 8px 0; }

.spec-dialog__title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
}

.spec-dialog__label {
  width: 80px;
  text-align: right;
  flex-shrink: 0;
}

.spec-rule-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-left: 72px;
}

.spec-rule-item__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.spec-rule-item__name {
  font-size: 14px;
  font-weight: 600;
}

.spec-rule-item__tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.spec-rule-tag { margin: 0; }

.spec-rule-add {
  display: inline-flex;
  align-items: center;
}

.spec-rule-add__input {
  width: 120px;
  :deep(.el-input__wrapper) {
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
  }
}

.spec-rule-add__btn {
  height: 32px;
  margin-left: -1px;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  background: #f5f7fa;
}

.spec-dialog__actions { margin: 24px 0 8px 72px; }

.spec-draft-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.spec-draft-field {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.spec-extra-form { margin-left: 8px; }
</style>

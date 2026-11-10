<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import AppDialog from '@/components/common/AppDialog.vue'
import {
  getSystemConfigListApi,
  updateSystemConfigApi,
  uploadSystemConfigImageApi,
} from '@/api/modules/system-config'
import { validateForm } from '@/utils/form'
import { showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const tabs = [
  { name: 'first', label: '全局配置', condition: 'xitong' },
  { name: 'fifth', label: '微信配置', condition: 'weixin' },
  { name: 'sixth', label: '支付宝配置', condition: 'zhifubao' },
  { name: 'seventh', label: '短信配置', condition: 'duanxin' },
  { name: 'xieyi', label: '协议配置', condition: 'xieyi' },
  { name: 'wenjian', label: '文件上传配置', condition: 'oss' },
  { name: 'fuwufei', label: '服务费配置', condition: 'fuwufei' },
  { name: 'kaiguan', label: '开关配置', condition: 'kaiguan' },
  { name: 'image', label: '图片配置', condition: 'image' },
]

const activeTab = ref('first')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const imageUploading = ref(false)
const formRef = ref()
const editorRef = ref()
const tableData = ref([])
let quillInstance = null

const editorToolbar = [
  [{ header: [1, 2, 3, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ color: [] }, { background: [] }],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ align: [] }],
  ['blockquote', 'link', 'image'],
  ['clean'],
]

const form = reactive({
  id: '',
  type: '',
  min: '',
  value: '',
  max: '',
  createAt: '',
  conditionFrom: '',
})

const rules = {
  value: [{ validator: validateConfigValue, trigger: 'blur' }],
}

function validateConfigValue(_rule, value, callback) {
  if (form.conditionFrom === 'xieyi') {
    if (!stripHtml(value).length) {
      callback(new Error('请输入内容'))
      return
    }
    callback()
    return
  }
  if (!String(value ?? '').trim()) {
    callback(new Error('请输入内容'))
    return
  }
  callback()
}

function stripHtml(value = '') {
  return String(value).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}

function getCurrentCondition() {
  return tabs.find((item) => item.name === activeTab.value)?.condition || 'xitong'
}

function canEditConfig() {
  return isAuth('allocationList:update') || isAuth('sys:config:update')
}

function normalizeSwitchValue(value) {
  const text = String(value ?? '').trim()
  if (text === '1' || text === '是') return '是'
  if (text === '0' || text === '否') return '否'
  return text
}

function serializeSwitchValue(value) {
  if (value === '是') return '1'
  if (value === '否') return '0'
  return value
}

function normalizeRow(item = {}) {
  const conditionFrom = item.conditionFrom || item.paramType || getCurrentCondition()
  const rawValue = item.value ?? item.paramValue ?? ''
  return {
    ...item,
    id: item.id,
    type: item.type || item.paramKey,
    min: item.min || item.remark || item.paramTypeDesc || '',
    value: conditionFrom === 'kaiguan' ? normalizeSwitchValue(rawValue) : rawValue,
    max: item.max || '',
    createAt: item.createAt || item.createTime || '',
    conditionFrom,
    paramKey: item.paramKey || item.type,
    remark: item.remark || '',
    paramType: item.paramType || conditionFrom,
    paramTypeDesc: item.paramTypeDesc || '',
  }
}

function resetForm() {
  Object.assign(form, {
    id: '',
    type: '',
    min: '',
    value: '',
    max: '',
    createAt: '',
    conditionFrom: '',
  })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getSystemConfigListApi(getCurrentCondition())
    tableData.value = (Array.isArray(data) ? data : []).map(normalizeRow)
  } catch (error) {
    tableData.value = []
    showError(error?.msg || error?.message || '加载系统配置失败')
  } finally {
    loading.value = false
  }
}

async function handleTabChange() {
  await loadData()
}

async function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    type: row.type,
    min: row.min,
    value: row.value,
    max: row.max,
    createAt: row.createAt,
    conditionFrom: row.conditionFrom || getCurrentCondition(),
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

function buildSubmitPayload() {
  const value = form.conditionFrom === 'kaiguan'
    ? serializeSwitchValue(form.value)
    : form.value
  return {
    id: form.id,
    type: form.type,
    paramKey: form.type,
    value,
    min: form.min,
    max: form.max,
    createAt: form.createAt,
    conditionFrom: form.conditionFrom,
    remark: form.min,
    paramTypeDesc: form.min,
  }
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    await updateSystemConfigApi(buildSubmitPayload())
    dialogVisible.value = false
    showSuccess('配置已更新')
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

function handleDialogVisible(value) {
  dialogVisible.value = value
  if (!value) {
    resetForm()
  }
}

function handleEditorReady(quill) {
  quillInstance = quill
  quill.getModule('toolbar')?.addHandler('image', handleEditorImageUpload)
}

function createImageInput() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  return input
}

function extractUploadUrl(data) {
  if (typeof data === 'string') {
    return data
  }
  return data?.url || data?.src || data?.path || ''
}

function insertEditorImage(url) {
  const quill = quillInstance || editorRef.value?.getQuill?.()
  if (!quill) {
    form.value = `${form.value || ''}<p><img src="${url}" /></p>`
    return
  }
  const range = quill.getSelection(true)
  const index = range ? range.index : quill.getLength()
  quill.insertEmbed(index, 'image', url, 'user')
  quill.setSelection(index + 1, 0, 'silent')
  quill.focus()
  form.value = quill.root.innerHTML
}

function handleEditorContentChange() {
  formRef.value?.validateField('value').catch(() => false)
}

function handleEditorImageUpload() {
  const input = createImageInput()
  input.click()
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) {
      return
    }
    imageUploading.value = true
    try {
      const data = await uploadSystemConfigImageApi(file)
      const imageUrl = extractUploadUrl(data)
      if (!imageUrl) {
        throw new Error('未获取到图片地址')
      }
      insertEditorImage(imageUrl)
      handleEditorContentChange()
    } catch (error) {
      showError(error?.message || '图片上传失败')
    } finally {
      imageUploading.value = false
      input.value = ''
    }
  }
}

async function handleConfigImageUpload(file) {
  imageUploading.value = true
  try {
    const data = await uploadSystemConfigImageApi(file)
    const imageUrl = extractUploadUrl(data)
    if (!imageUrl) {
      throw new Error('未获取到图片地址')
    }
    form.value = imageUrl
    formRef.value?.validateField('value').catch(() => false)
    showSuccess('图片上传成功')
  } catch (error) {
    showError(error?.message || '图片上传失败')
  } finally {
    imageUploading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="scfg">
    <header class="scfg-hero nf-page-hero">
      <span class="scfg-hero__badge">系统配置</span>
      <h1 class="scfg-hero__title">系统配置</h1>
      <p class="scfg-hero__desc">维护全局参数、打榜规则与次数、岛宅每日进度加成、微信/支付宝/短信、协议、文件上传、服务费与开关等配置项</p>
    </header>

    <div class="scfg-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="item in tabs" :key="item.name" :label="item.label" :name="item.name">
          <div class="scfg-table nf-table-card">
            <el-alert
              v-if="item.name === 'fifth'"
              class="scfg-table__alert"
              type="info"
              :closable="false"
              show-icon
              title="微信小程序授权登录配置"
              description="请填写用户端小程序 AppID 与 AppSecret（sys_user_ma_appid / sys_user_ma_secret）。小程序端使用微信授权登录，APP/H5 使用手机号验证码登录。"
            />
            <el-table v-loading="loading" :data="tableData" border>
              <el-table-column prop="id" label="ID" align="center" width="80" />
              <el-table-column prop="type" label="配置键" :min-width="item.name === 'kaiguan' || item.name === 'image' ? 200 : 180" show-overflow-tooltip />
              <el-table-column prop="min" label="类型" :min-width="item.name === 'kaiguan' || item.name === 'image' ? 250 : 180" />
              <el-table-column prop="value" label="内容" :min-width="item.name === 'kaiguan' || item.name === 'image' ? 220 : 500">
                <template #default="{ row }">
                  <div class="config-value-clamp">{{ row.value }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="createAt" label="创建时间" :min-width="item.name === 'kaiguan' || item.name === 'image' ? 180 : 160" align="center" />
              <el-table-column label="操作" width="120" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button v-if="canEditConfig()" link type="primary" @click="openEdit(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <AppDialog
      :model-value="dialogVisible"
      title="修改"
      :loading="dialogLoading"
      width="860px"
      @update:modelValue="handleDialogVisible"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="配置类型">
          <el-input v-model="form.min" readonly />
        </el-form-item>
        <el-form-item label="内容" prop="value">
          <div v-if="form.conditionFrom === 'xieyi'" v-loading="imageUploading" class="config-editor-wrap">
            <QuillEditor
              ref="editorRef"
              v-model:content="form.value"
              content-type="html"
              theme="snow"
              :toolbar="editorToolbar"
              @ready="handleEditorReady"
              @update:content="handleEditorContentChange"
            />
          </div>
          <el-radio-group v-else-if="form.conditionFrom === 'kaiguan'" v-model="form.value">
            <el-radio value="是">是</el-radio>
            <el-radio value="否">否</el-radio>
          </el-radio-group>
          <div v-else-if="form.conditionFrom === 'image'" class="config-image-box">
            <el-image v-if="form.value" :src="form.value" fit="cover" class="config-image-preview" />
            <div v-else class="config-image-placeholder">暂无图片</div>
            <el-upload
              :show-file-list="false"
              :http-request="({ file }) => handleConfigImageUpload(file)"
              accept="image/*"
            >
              <el-button type="primary" :loading="imageUploading">上传图片</el-button>
            </el-upload>
          </div>
          <el-input
            v-else
            v-model="form.value"
            type="textarea"
            :rows="4"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>

<style scoped lang="scss">
.scfg {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.scfg-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.scfg-hero__badge {
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

.scfg-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.scfg-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.scfg-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
    padding: 0 16px;
    background: #fff;
    border: 1px solid var(--nf-border);
    border-radius: 8px 8px 0 0;
    box-shadow: var(--nf-shadow-card);
  }

  :deep(.el-tabs__content) {
    overflow: visible;
  }

  :deep(.el-tab-pane) {
    padding-top: 0;
  }
}

.scfg-table.nf-table-card {
  margin-bottom: 0;
  margin-top: 0;
  padding-bottom: 12px;
  border-radius: 0 0 8px 8px;
  border-top: none;
  box-shadow: var(--nf-shadow-card);

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

.scfg-table__alert {
  margin-bottom: 12px;
}

.config-value-clamp {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 22px;
  max-height: 66px;
  word-break: break-all;
}

.config-editor-wrap {
  width: 100%;
}

.config-image-box {
  display: flex;
  align-items: center;
  gap: 16px;
}

.config-image-preview,
.config-image-placeholder {
  width: 140px;
  height: 140px;
  border: 1px dashed #d0d7de;
  border-radius: 8px;
}

.config-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #fafafa;
}

:deep(.config-editor-wrap .ql-toolbar.ql-snow) {
  border-radius: 8px 8px 0 0;
}

:deep(.config-editor-wrap .ql-container.ql-snow) {
  min-height: 280px;
  border-radius: 0 0 8px 8px;
}

:deep(.config-editor-wrap .ql-editor) {
  min-height: 280px;
  font-size: 14px;
  line-height: 1.7;
}
</style>

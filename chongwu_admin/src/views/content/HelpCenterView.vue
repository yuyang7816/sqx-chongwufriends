<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  createHelpClassifyApi,
  createHelpWordApi,
  deleteHelpClassifyApi,
  deleteHelpWordApi,
  getHelpClassifyPage,
  getHelpWordPage,
  uploadHelpEditorImageApi,
  updateHelpClassifyApi,
  updateHelpWordApi,
} from '@/api/modules/help-center'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const activeTab = ref('classify')
const tableLoading = ref(false)
const classifyDialogVisible = ref(false)
const articleDialogVisible = ref(false)
const classifyDialogLoading = ref(false)
const articleDialogLoading = ref(false)
const classifyFormRef = ref()
const articleFormRef = ref()
const articleEditorRef = ref()
const articleImageUploading = ref(false)
let articleQuillInstance = null

const classifyList = ref([])
const articleList = ref([])
const articleTotal = ref(0)
const articlePageNum = ref(1)
const articlePageSize = ref(10)
const selectedClassifyId = ref('')

const classifyFilters = reactive({
  helpClassifyName: '',
})

const articleFilters = reactive({
  helpWordTitle: '',
})

const classifyInitialForm = {
  helpClassifyId: 0,
  helpClassifyName: '',
  sort: 0,
}

const articleInitialForm = {
  helpWordId: 0,
  helpClassifyId: '',
  helpWordTitle: '',
  helpWordContent: '',
  sort: 0,
}

const classifyForm = reactive({ ...classifyInitialForm })
const articleForm = reactive({ ...articleInitialForm })

const editorToolbar = [
  [{ header: [1, 2, 3, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ color: [] }, { background: [] }],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ align: [] }],
  ['blockquote', 'link', 'image'],
  ['clean'],
]

const classifyRules = {
  helpClassifyName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const articleRules = {
  helpClassifyId: [{ required: true, message: '请选择文章分类', trigger: 'change' }],
  helpWordTitle: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  helpWordContent: [{ validator: validateArticleContent, trigger: 'blur' }],
}

function validateArticleContent(_rule, value, callback) {
  if (!stripHtml(value).length) {
    callback(new Error('请输入文章内容'))
    return
  }
  callback()
}

function getCurrentTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const date = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${date} ${hours}:${minutes}:${seconds}`
}

function stripHtml(value = '') {
  return String(value).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}

function insertEditorImage(url) {
  const quill = articleQuillInstance || articleEditorRef.value?.getQuill?.()
  if (!quill) {
    articleForm.helpWordContent = `${articleForm.helpWordContent || ''}<p><img src="${url}" /></p>`
    return
  }
  const range = quill.getSelection(true)
  const index = range ? range.index : quill.getLength()
  quill.insertEmbed(index, 'image', url, 'user')
  quill.setSelection(index + 1, 0, 'silent')
  quill.focus()
  articleForm.helpWordContent = quill.root.innerHTML
}

function handleEditorReady(quill) {
  articleQuillInstance = quill
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

function handleEditorImageUpload() {
  const input = createImageInput()
  input.click()
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) {
      return
    }
    articleImageUploading.value = true
    try {
      const data = await uploadHelpEditorImageApi(file)
      const imageUrl = extractUploadUrl(data)
      if (!imageUrl) {
        throw new Error('未获取到图片地址')
      }
      insertEditorImage(imageUrl)
      handleArticleContentChange()
    } catch (error) {
      showError(error?.message || '图片上传失败')
    } finally {
      articleImageUploading.value = false
      input.value = ''
    }
  }
}

function resetClassifyForm() {
  Object.assign(classifyForm, classifyInitialForm)
}

function resetArticleForm() {
  Object.assign(articleForm, articleInitialForm)
}

function normalizeClassifyRow(item = {}) {
  return {
    ...item,
    helpClassifyId: item.helpClassifyId,
    helpClassifyName: item.helpClassifyName || '',
    sort: Number(item.sort || 0),
    createTime: item.createTime || '',
    state: Number(item.state ?? 1),
    remark: item.remark || '',
  }
}

function normalizeArticleRow(item = {}) {
  return {
    ...item,
    helpWordId: item.helpWordId,
    helpClassifyId: item.helpClassifyId ?? '',
    helpWordTitle: item.helpWordTitle || '',
    helpWordContent: item.helpWordContent || '',
    helpWordContentText: stripHtml(item.helpWordContent || ''),
    sort: Number(item.sort || 0),
    createTime: item.createTime || '',
  }
}

function getClassifyNameById(id) {
  return classifyList.value.find((item) => String(item.helpClassifyId) === String(id))?.helpClassifyName || '-'
}

async function loadClassifyList() {
  tableLoading.value = true
  try {
    const data = await getHelpClassifyPage({
      page: 1,
      limit: 100,
      helpClassifyName: classifyFilters.helpClassifyName.trim(),
    })
    classifyList.value = (data.list || []).map(normalizeClassifyRow)
    if (!classifyList.value.length || !classifyList.value.some((item) => String(item.helpClassifyId) === String(selectedClassifyId.value))) {
      selectedClassifyId.value = ''
    }
  } finally {
    tableLoading.value = false
  }
}

async function loadArticleList() {
  tableLoading.value = true
  try {
    const params = {
      helpWordTitle: articleFilters.helpWordTitle.trim(),
      page: articlePageNum.value,
      limit: articlePageSize.value,
    }
    if (selectedClassifyId.value) {
      params.helpClassifyId = selectedClassifyId.value
    }
    const data = await getHelpWordPage(params)
    articleList.value = (data.list || []).map(normalizeArticleRow)
    articleTotal.value = data.total || 0
  } finally {
    tableLoading.value = false
  }
}

async function handleTabChange() {
  if (activeTab.value === 'article') {
    if (!classifyList.value.length) {
      await loadClassifyList()
    }
    await loadArticleList()
  }
}

async function handleClassifySearch() {
  await loadClassifyList()
  if (activeTab.value === 'article') {
    articlePageNum.value = 1
    await loadArticleList()
  }
}

async function handleClassifyReset() {
  classifyFilters.helpClassifyName = ''
  await loadClassifyList()
  if (activeTab.value === 'article') {
    articlePageNum.value = 1
    await loadArticleList()
  }
}

function handleArticleSearch() {
  articlePageNum.value = 1
  loadArticleList()
}

function handleArticleReset() {
  articleFilters.helpWordTitle = ''
  selectedClassifyId.value = ''
  articlePageNum.value = 1
  loadArticleList()
}

async function handleClassifyChange() {
  articlePageNum.value = 1
  await loadArticleList()
}

async function openCreateClassify() {
  resetClassifyForm()
  classifyDialogVisible.value = true
  await nextTick()
  classifyFormRef.value?.clearValidate()
}

async function openEditClassify(row) {
  Object.assign(classifyForm, {
    helpClassifyId: row.helpClassifyId,
    helpClassifyName: row.helpClassifyName,
    sort: row.sort,
  })
  classifyDialogVisible.value = true
  await nextTick()
  classifyFormRef.value?.clearValidate()
}

async function submitClassify() {
  const valid = await validateForm(classifyFormRef)
  if (!valid) return
  classifyDialogLoading.value = true
  try {
    const payload = {
      helpClassifyId: classifyForm.helpClassifyId || undefined,
      helpClassifyName: classifyForm.helpClassifyName.trim(),
      sort: Number(classifyForm.sort || 0),
      state: 1,
      remark: '',
      createTime: getCurrentTime(),
    }
    if (classifyForm.helpClassifyId) {
      await updateHelpClassifyApi(payload)
      showSuccess('分类已更新')
    } else {
      await createHelpClassifyApi(payload)
      showSuccess('分类已新增')
    }
    classifyDialogVisible.value = false
    await loadClassifyList()
    if (activeTab.value === 'article') {
      await loadArticleList()
    }
  } finally {
    classifyDialogLoading.value = false
  }
}

async function handleDeleteClassify(row) {
  await confirmAction(`确定删除分类“${row.helpClassifyName}”吗？`)
  await deleteHelpClassifyApi(row.helpClassifyId)
  showSuccess('删除成功')
  await loadClassifyList()
  if (activeTab.value === 'article') {
    articlePageNum.value = 1
    await loadArticleList()
  }
}

async function openCreateArticle() {
  if (!classifyList.value.length) {
    showError('请先添加文章分类')
    return
  }
  resetArticleForm()
  articleForm.helpClassifyId = selectedClassifyId.value || classifyList.value[0].helpClassifyId
  articleDialogVisible.value = true
  await nextTick()
  articleFormRef.value?.clearValidate()
}

async function openEditArticle(row) {
  Object.assign(articleForm, {
    helpWordId: row.helpWordId,
    helpClassifyId: row.helpClassifyId,
    helpWordTitle: row.helpWordTitle,
    helpWordContent: row.helpWordContent,
    sort: row.sort,
  })
  articleDialogVisible.value = true
  await nextTick()
  articleFormRef.value?.clearValidate()
}

function handleArticleContentChange() {
  articleFormRef.value?.validateField('helpWordContent').catch(() => false)
}

async function submitArticle() {
  const valid = await validateForm(articleFormRef)
  if (!valid) return
  articleDialogLoading.value = true
  try {
    const payload = {
      helpWordId: articleForm.helpWordId || undefined,
      helpClassifyId: articleForm.helpClassifyId,
      helpWordTitle: articleForm.helpWordTitle.trim(),
      helpWordContent: articleForm.helpWordContent,
      createTime: getCurrentTime(),
      sort: Number(articleForm.sort || 0),
    }
    if (articleForm.helpWordId) {
      await updateHelpWordApi(payload)
      showSuccess('文章已更新')
    } else {
      await createHelpWordApi(payload)
      showSuccess('文章已新增')
    }
    articleDialogVisible.value = false
    await loadArticleList()
  } finally {
    articleDialogLoading.value = false
  }
}

async function handleDeleteArticle(row) {
  await confirmAction(`确定删除文章“${row.helpWordTitle}”吗？`)
  await deleteHelpWordApi(row.helpWordId)
  if (articleList.value.length === 1 && articlePageNum.value > 1) {
    articlePageNum.value -= 1
  }
  showSuccess('删除成功')
  await loadArticleList()
}

function handleClassifyDialogVisible(value) {
  classifyDialogVisible.value = value
  if (!value) {
    resetClassifyForm()
  }
}

function handleArticleDialogVisible(value) {
  articleDialogVisible.value = value
  if (!value) {
    resetArticleForm()
  }
}

onMounted(async () => {
  await loadClassifyList()
})
</script>

<template>
  <div class="hc">
    <header class="hc-hero nf-page-hero">
      <span class="hc-hero__badge">内容管理</span>
      <h1 class="hc-hero__title">帮助中心</h1>
      <p class="hc-hero__desc">维护帮助分类与文章内容，支持分类筛选、富文本编辑与权限控制</p>
    </header>

    <div class="hc-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="分类管理" name="classify">
          <div class="hc-filter nf-filter-card">
            <div class="nf-filter-card__head">
              <span>筛选条件</span>
            </div>
            <el-form :inline="true" class="hc-filter__form" @submit.prevent>
              <el-form-item label="分类名称">
                <el-input
                  v-model="classifyFilters.helpClassifyName"
                  clearable
                  placeholder="请输入分类名称"
                  @keyup.enter="handleClassifySearch"
                />
              </el-form-item>
              <el-form-item class="hc-filter__actions">
                <el-space wrap>
                  <el-button type="primary" @click="handleClassifySearch">查询</el-button>
                  <el-button @click="handleClassifyReset">重置</el-button>
                  <el-button v-if="isAuth('materialsList:add')" type="primary" @click="openCreateClassify">添加分类</el-button>
                </el-space>
              </el-form-item>
            </el-form>
          </div>

          <div class="hc-table nf-table-card">
            <el-table v-loading="tableLoading" :data="classifyList" border>
              <el-table-column prop="helpClassifyId" label="编号" width="80" align="center" />
              <el-table-column prop="helpClassifyName" label="分类名称" min-width="220" />
              <el-table-column prop="sort" label="排序" width="100" align="center" />
              <el-table-column prop="createTime" label="创建时间" min-width="180" align="center" />
              <el-table-column label="操作" width="160" fixed="right" align="center">
                <template #default="{ row }">
                  <el-space>
                    <el-button v-if="isAuth('materialsList:update')" link type="primary" @click="openEditClassify(row)">编辑</el-button>
                    <el-button v-if="isAuth('materialsList:delete')" link type="danger" @click="handleDeleteClassify(row)">删除</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="文章管理" name="article">
          <div class="hc-filter nf-filter-card">
            <div class="nf-filter-card__head">
              <span>筛选条件</span>
            </div>
            <el-form :inline="true" class="hc-filter__form" @submit.prevent>
              <el-form-item label="文章分类">
                <el-select v-model="selectedClassifyId" clearable placeholder="请选择类型" class="article-filter-select" @change="handleClassifyChange">
                  <el-option
                    v-for="item in classifyList"
                    :key="item.helpClassifyId"
                    :label="item.helpClassifyName"
                    :value="item.helpClassifyId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="文章名称">
                <el-input
                  v-model="articleFilters.helpWordTitle"
                  clearable
                  placeholder="请输入文章名称"
                  @keyup.enter="handleArticleSearch"
                />
              </el-form-item>
              <el-form-item class="hc-filter__actions">
                <el-space wrap>
                  <el-button type="primary" @click="handleArticleSearch">查询</el-button>
                  <el-button @click="handleArticleReset">重置</el-button>
                  <el-button v-if="isAuth('materialsList:add')" type="primary" @click="openCreateArticle">添加文章</el-button>
                </el-space>
              </el-form-item>
            </el-form>
          </div>

          <div class="hc-table nf-table-card">
            <el-table v-loading="tableLoading" :data="articleList" border>
              <el-table-column prop="helpWordId" label="编号" width="80" align="center" />
              <el-table-column prop="helpWordTitle" label="标题" min-width="220">
                <template #default="{ row }">
                  <div class="line-clamp-3">{{ row.helpWordTitle }}</div>
                </template>
              </el-table-column>
              <el-table-column label="分类" min-width="140" align="center">
                <template #default="{ row }">{{ getClassifyNameById(row.helpClassifyId) }}</template>
              </el-table-column>
              <el-table-column label="内容" min-width="260">
                <template #default="{ row }">
                  <div class="line-clamp-3">{{ row.helpWordContentText }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="sort" label="排序" width="100" align="center" />
              <el-table-column prop="createTime" label="创建时间" min-width="180" align="center" />
              <el-table-column label="操作" width="160" fixed="right" align="center">
                <template #default="{ row }">
                  <el-space>
                    <el-button v-if="isAuth('materialsList:update')" link type="primary" @click="openEditArticle(row)">编辑</el-button>
                    <el-button v-if="isAuth('materialsList:delete')" link type="danger" @click="handleDeleteArticle(row)">删除</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>

            <BasePagination
              :current-page="articlePageNum"
              :page-size="articlePageSize"
              :page-sizes="[10, 20, 30, 50, 100]"
              :total="articleTotal"
              @update:currentPage="articlePageNum = $event; loadArticleList()"
              @update:pageSize="articlePageSize = $event; articlePageNum = 1; loadArticleList()"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <AppDialog
      :model-value="classifyDialogVisible"
      :title="classifyForm.helpClassifyId ? '修改分类' : '添加分类'"
      :loading="classifyDialogLoading"
      width="560px"
      @update:modelValue="handleClassifyDialogVisible"
      @confirm="submitClassify"
    >
      <el-form ref="classifyFormRef" :model="classifyForm" :rules="classifyRules" label-width="88px">
        <el-form-item label="分类名称" prop="helpClassifyName">
          <el-input v-model="classifyForm.helpClassifyName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="classifyForm.sort" :min="0" />
        </el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog
      :model-value="articleDialogVisible"
      :title="articleForm.helpWordId ? '修改文章' : '添加文章'"
      :loading="articleDialogLoading"
      width="860px"
      @update:modelValue="handleArticleDialogVisible"
      @confirm="submitArticle"
    >
      <el-form ref="articleFormRef" :model="articleForm" :rules="articleRules" label-width="88px">
        <el-form-item label="文章分类" prop="helpClassifyId">
          <el-select v-model="articleForm.helpClassifyId" placeholder="请选择类型" class="form-select-full">
            <el-option
              v-for="item in classifyList"
              :key="item.helpClassifyId"
              :label="item.helpClassifyName"
              :value="item.helpClassifyId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="articleForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="文章标题" prop="helpWordTitle">
          <el-input v-model="articleForm.helpWordTitle" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="文章内容" prop="helpWordContent">
          <div v-loading="articleImageUploading" class="editor-wrap">
            <QuillEditor
              ref="articleEditorRef"
              v-model:content="articleForm.helpWordContent"
              content-type="html"
              theme="snow"
              :toolbar="editorToolbar"
              @ready="handleEditorReady"
              @update:content="handleArticleContentChange"
            />
          </div>
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>

<style scoped lang="scss">
.hc {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.hc-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hc-hero__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--nf-text-primary);
  background: var(--nf-brand-lite);
  border: 1px solid var(--nf-brand);
  border-radius: var(--nf-radius-sm);
}

.hc-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.hc-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.hc-tabs {
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
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding-top: 12px;
  }
}

.hc-filter.nf-filter-card {
  margin-bottom: 0;
  border-radius: 0;
  border-top: none;
  box-shadow: none;
}

.hc-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.hc-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.hc-table.nf-table-card {
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

.editor-wrap {
  width: 100%;
}

.article-filter-select {
  width: 220px;
}

:deep(.editor-wrap .ql-toolbar.ql-snow) {
  border-radius: 8px 8px 0 0;
}

:deep(.editor-wrap .ql-container.ql-snow) {
  min-height: 360px;
  border-radius: 0 0 8px 8px;
}

:deep(.editor-wrap .ql-editor) {
  min-height: 360px;
  font-size: 14px;
  line-height: 1.7;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 22px;
  max-height: 66px;
}

@media (max-width: 768px) {
  .hc-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }
}
</style>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import BasePagination from '@/components/common/BasePagination.vue'
import CoverImageUpload from '@/components/common/CoverImageUpload.vue'
import {
  deletePetStrategyApi,
  getPetStrategyCategoriesApi,
  getPetStrategyInfoApi,
  getPetStrategyPageApi,
  savePetStrategyApi,
  updatePetStrategyApi,
  uploadStrategyEditorImageApi,
} from '@/api/modules/pet-strategy'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth, toPublicFileUrl } from '@/utils/tool'

const router = useRouter()
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const previewVisible = ref(false)
const previewHtml = ref('')
const editorRef = ref()
const editorUploading = ref(false)
let quillInstance = null

const filters = reactive({
  title: '',
  tag: '',
  categoryId: '',
  status: '',
})

const form = reactive({
  id: '',
  categoryId: '',
  title: '',
  subTitle: '',
  tag: '',
  coverUrl: '',
  content: '',
  sort: 0,
  status: 1,
})

const editorToolbar = [
  [{ header: [1, 2, 3, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ color: [] }, { background: [] }],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ align: [] }],
  ['blockquote', 'link', 'image'],
  ['clean'],
]

const categoryOptions = computed(() => categories.value.filter((item) => Number(item.status) === 1 || item.id === form.categoryId))

const categoryMap = computed(() => {
  const map = {}
  categories.value.forEach((item) => {
    map[item.id] = item.name
  })
  return map
})

function stripHtml(value = '') {
  return String(value).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}

function resetForm() {
  Object.assign(form, {
    id: '',
    categoryId: '',
    title: '',
    subTitle: '',
    tag: '',
    coverUrl: '',
    content: '',
    sort: 0,
    status: 1,
  })
  quillInstance = null
}

function categoryLabel(row) {
  return row.categoryName || categoryMap.value[row.categoryId] || (row.categoryId ? `分类#${row.categoryId}` : '未分类')
}

function parseTags(row) {
  const raw = String(row?.tag || '').trim()
  if (!raw) return []
  return raw.split(/[,，、|/]/).map((s) => s.trim()).filter(Boolean).slice(0, 4)
}

async function loadCategories() {
  categories.value = await getPetStrategyCategoriesApi()
}

async function loadData() {
  loading.value = true
  try {
    const data = await getPetStrategyPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      title: filters.title.trim() || undefined,
      tag: filters.tag.trim() || undefined,
      categoryId: filters.categoryId === '' ? undefined : filters.categoryId,
      status: filters.status === '' ? undefined : filters.status,
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
  filters.title = ''
  filters.tag = ''
  filters.categoryId = ''
  filters.status = ''
  handleSearch()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  resetForm()
  dialogLoading.value = true
  dialogVisible.value = true
  try {
    const detail = await getPetStrategyInfoApi(row.id)
    Object.assign(form, {
      id: detail.id,
      categoryId: detail.categoryId ?? '',
      title: detail.title || '',
      subTitle: detail.subTitle || '',
      tag: detail.tag || '',
      coverUrl: detail.coverUrl || '',
      content: detail.content || '',
      sort: Number(detail.sort || 0),
      status: Number(detail.status ?? 1),
    })
  } catch (error) {
    dialogVisible.value = false
    showError(error?.msg || error?.message || '加载攻略详情失败')
  } finally {
    dialogLoading.value = false
  }
}

function rewriteHtmlMedia(html = '') {
  return String(html).replace(/<img\b([^>]*)>/gi, (full, attrs) => {
    const next = String(attrs).replace(/src=(["'])(.*?)\1/i, (_, q, src) => {
      const resolved = toPublicFileUrl(src)
      return `src=${q}${resolved || src}${q}`
    })
    return `<img${next}>`
  })
}

function openPreview(row) {
  const cover = row.coverUrl ? toPublicFileUrl(row.coverUrl) : ''
  const tags = parseTags(row).map((tag) => `<span class="preview-tag">${tag}</span>`).join('')
  let body = rewriteHtmlMedia(row.content || '<p>暂无正文</p>')
  // 与用户端一致：封面已单独展示时，去掉正文开头重复首图
  if (cover) {
    body = body.replace(/^\s*<img[^>]+src=["'][^"']+["'][^>]*>\s*/i, '')
  }
  previewHtml.value = `
    <div class="preview-article">
      ${tags ? `<div class="preview-tags">${tags}</div>` : ''}
      <h2>${row.title || ''}</h2>
      ${row.subTitle ? `<p class="preview-sub">${row.subTitle}</p>` : ''}
      ${cover ? `<img class="preview-cover" src="${cover}" alt="cover" />` : ''}
      <div class="preview-body">${body}</div>
    </div>
  `
  previewVisible.value = true
}

async function submitForm() {
  if (!form.title.trim()) return showError('请输入标题')
  if (!stripHtml(form.content)) return showError('请输入正文内容')
  dialogLoading.value = true
  try {
    const payload = {
      categoryId: form.categoryId === '' ? null : form.categoryId,
      title: form.title.trim(),
      subTitle: form.subTitle.trim(),
      tag: form.tag.trim(),
      coverUrl: form.coverUrl || '',
      content: form.content,
      sort: Number(form.sort || 0),
      status: Number(form.status),
    }
    if (form.id) await updatePetStrategyApi({ id: form.id, ...payload })
    else await savePetStrategyApi(payload)
    showSuccess('操作成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction('确认删除该攻略？删除后用户端将不可见。')
  await deletePetStrategyApi(row.id)
  showSuccess('删除成功')
  await loadData()
}

function handleEditorReady(quill) {
  quillInstance = quill
  quill.getModule('toolbar')?.addHandler('image', handleEditorImageUpload)
}

function handleEditorContentChange() {
  if (quillInstance) {
    form.content = quillInstance.root.innerHTML
  }
}

function insertEditorImage(url) {
  const quill = quillInstance || editorRef.value?.getQuill?.()
  if (!quill) {
    form.content = `${form.content || ''}<p><img src="${url}" /></p>`
    return
  }
  const range = quill.getSelection(true)
  const index = range ? range.index : quill.getLength()
  quill.insertEmbed(index, 'image', url, 'user')
  quill.setSelection(index + 1, 0, 'silent')
  quill.focus()
  form.content = quill.root.innerHTML
}

function handleEditorImageUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.click()
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    editorUploading.value = true
    try {
      const data = await uploadStrategyEditorImageApi(file)
      const imageUrl = typeof data === 'string' ? data : (data?.url || data?.path || '')
      if (!imageUrl) throw new Error('未获取到图片地址')
      insertEditorImage(imageUrl)
      handleEditorContentChange()
    } catch (error) {
      showError(error?.msg || error?.message || '图片上传失败')
    } finally {
      editorUploading.value = false
      input.value = ''
    }
  }
}

onMounted(async () => {
  await loadCategories()
  await loadData()
})
</script>

<template>
  <div class="pstrat">
    <header class="pstrat-hero nf-page-hero">
      <span class="pstrat-hero__badge">宠物业务</span>
      <h1 class="pstrat-hero__title">养宠攻略</h1>
      <p class="pstrat-hero__desc">维护用户端养宠攻略内容，支持分类、标签、封面上传与富文本编辑</p>
    </header>

    <div class="pstrat-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pstrat-filter__form" @submit.prevent>
        <el-form-item label="标题">
          <el-input v-model="filters.title" clearable placeholder="标题关键词" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="filters.tag" clearable placeholder="如 汪星人/幼宠" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" clearable placeholder="全部分类" style="width: 140px">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 120px">
            <el-option label="发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item class="pstrat-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button type="primary" plain @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pstrat-table nf-table-card">
      <div class="pstrat-table__toolbar">
        <el-space wrap>
          <el-button type="primary" :disabled="!isAuth('pet:strategy:add')" @click="openCreate">新增攻略</el-button>
          <el-button
            type="primary"
            plain
            :disabled="!isAuth('pet:strategy:category:list')"
            @click="router.push('/pet/strategy/categories')"
          >分类管理</el-button>
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </el-space>
      </div>

      <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <el-image
            v-if="row.coverUrl"
            :src="toPublicFileUrl(row.coverUrl)"
            :preview-src-list="[toPublicFileUrl(row.coverUrl)]"
            style="width:48px;height:48px;border-radius:8px"
            fit="cover"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="标题 / 副标题" min-width="220">
        <template #default="{ row }">
          <div class="title-cell">
            <strong>{{ row.title }}</strong>
            <span v-if="row.subTitle" class="sub-title">{{ row.subTitle }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="100">
        <template #default="{ row }">{{ categoryLabel(row) }}</template>
      </el-table-column>
      <el-table-column label="标签" min-width="140">
        <template #default="{ row }">
          <el-space wrap>
            <el-tag v-for="tag in parseTags(row)" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
            <span v-if="!parseTags(row).length">-</span>
          </el-space>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-space wrap alignment="start">
            <el-button size="small" type="primary" @click="openPreview(row)">预览</el-button>
            <el-button size="small" type="primary" :disabled="!isAuth('pet:strategy:update')" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :disabled="!isAuth('pet:strategy:delete')" @click="removeRow(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑养宠攻略' : '新增养宠攻略'"
      width="860px"
      destroy-on-close
      top="4vh"
     class="nf-pro-dialog" align-center>
<el-form v-loading="dialogLoading" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" clearable placeholder="选择分类（对应用户端筛选，可空）" style="width: 100%">
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="form.tag" placeholder="多个用逗号分隔，如：汪星人,幼宠" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="80" show-word-limit placeholder="攻略标题" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subTitle" maxlength="120" show-word-limit placeholder="列表摘要，用户端卡片展示" />
        </el-form-item>
        <el-form-item label="封面图">
          <CoverImageUpload v-model="form.coverUrl" tip="建议 16:9，用于列表与详情顶部" />
        </el-form-item>
        <el-form-item label="正文" required>
          <div class="editor-wrap">
            <QuillEditor
              ref="editorRef"
              v-model:content="form.content"
              content-type="html"
              theme="snow"
              :toolbar="editorToolbar"
              class="strategy-editor"
              @ready="handleEditorReady"
              @update:content="handleEditorContentChange"
            />
            <div v-if="editorUploading" class="editor-uploading">图片上传中...</div>
          </div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" :max="9999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">发布</el-radio>
                <el-radio :value="0">草稿</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
          type="primary"
          :loading="dialogLoading"
          :disabled="!isAuth(form.id ? 'pet:strategy:update' : 'pet:strategy:add')"
          @click="submitForm"
          >保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="用户端预览" width="720px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="preview-frame" v-html="previewHtml" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pstrat {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pstrat-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pstrat-hero__badge {
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

.pstrat-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pstrat-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pstrat-filter.nf-filter-card {
  margin-bottom: 0;
}

.pstrat-filter__form {
  margin-bottom: 0;
}

.pstrat-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pstrat-table.nf-table-card {
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

.pstrat-table__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sub-title {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}

.editor-wrap {
  width: 100%;
  position: relative;
}

.strategy-editor {
  min-height: 280px;
  background: #fff;
}

.strategy-editor :deep(.ql-container) {
  min-height: 240px;
}

.strategy-editor :deep(img) {
  max-width: 100%;
  max-height: 220px;
  width: auto;
  height: auto;
  object-fit: contain;
  display: block;
  margin: 8px 0;
  border-radius: 8px;
}

.editor-uploading {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
}

.preview-frame {
  max-height: 70vh;
  overflow: auto;
  padding: 8px 4px;
}

.preview-frame :deep(.preview-article) {
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #2f2a26;
}

.preview-frame :deep(.preview-tags) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.preview-frame :deep(.preview-tag) {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  background: #fff4e8;
  color: #c79662;
  font-size: 12px;
}

.preview-frame :deep(h2) {
  margin: 0 0 8px;
  font-size: 22px;
  line-height: 1.4;
}

.preview-frame :deep(.preview-sub) {
  margin: 0 0 14px;
  color: #8a8178;
  font-size: 14px;
}

/* 对齐用户端：封面固定高度裁剪，避免竖图占满预览 */
.preview-frame :deep(.preview-cover) {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 12px;
  margin: 12px 0;
  display: block;
  background: #f4e8d8;
}

.preview-frame :deep(.preview-body img) {
  max-width: 100%;
  max-height: 280px;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 12px;
  margin: 12px auto;
  display: block;
}

.preview-frame :deep(.preview-body) {
  line-height: 1.8;
  font-size: 15px;
}
</style>

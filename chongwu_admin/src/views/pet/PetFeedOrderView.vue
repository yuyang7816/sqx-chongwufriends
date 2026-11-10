<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import UserFilterFields from '@/components/common/UserFilterFields.vue'
import { getPetFeedOrderPageApi, uploadPetFeedFeedbackApi, uploadFeedFileApi } from '@/api/modules/pet-feed'
import { useRouteUserFilter } from '@/utils/user-filter'
import { showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const statusMap = { 1: '待反馈', 2: '预计 3 天内反馈', 3: '已反馈' }
const { filters, appendUserFilter } = useRouteUserFilter()

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const orderNo = ref('')
const status = ref('')
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const uploadLoading = ref(false)
const currentRow = ref(null)
const form = reactive({ videoUrl: '' })

function formatPrice(value) {
  const num = Number(value)
  return Number.isFinite(num) ? num.toFixed(2) : '0.00'
}

function statusText(row) {
  return row.statusText || statusMap[row.status] || row.status || '-'
}

async function loadData() {
  loading.value = true
  try {
    const params = appendUserFilter({ page: pageNum.value, limit: pageSize.value })
    if (orderNo.value.trim()) params.orderNo = orderNo.value.trim()
    if (status.value !== '') params.status = status.value
    const data = await getPetFeedOrderPageApi(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载投喂订单失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.phone = ''
  filters.username = ''
  orderNo.value = ''
  status.value = ''
  handleSearch()
}

function openFeedback(row) {
  currentRow.value = row
  form.videoUrl = row.videoUrl || ''
  dialogVisible.value = true
}

async function handleVideoUpload(option) {
  const file = option.file
  if (file.size > 100 * 1024 * 1024) {
    showError('视频大小不能超过 100MB')
    option.onError?.(new Error('too large'))
    return
  }
  uploadLoading.value = true
  try {
    const url = await uploadFeedFileApi(file)
    if (!url) throw new Error('未获取到视频地址')
    form.videoUrl = url
    showSuccess('视频上传成功')
    option.onSuccess?.(url)
  } catch (error) {
    showError(error?.msg || error?.message || '视频上传失败')
    option.onError?.(error)
  } finally {
    uploadLoading.value = false
  }
}

function clearVideo() {
  form.videoUrl = ''
}

async function submitFeedback() {
  if (!form.videoUrl.trim()) {
    showError('请上传反馈视频')
    return
  }
  dialogLoading.value = true
  try {
    await uploadPetFeedFeedbackApi({ orderId: currentRow.value.id, videoUrl: form.videoUrl.trim() })
    showSuccess('反馈视频已上传，用户端可查看')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '上传失败')
  } finally {
    dialogLoading.value = false
  }
}

loadData()
</script>

<template>
  <div class="pfeedord">
    <header class="pfeedord-hero nf-page-hero">
      <span class="pfeedord-hero__badge">商城管理</span>
      <h1 class="pfeedord-hero__title">投喂订单</h1>
      <p class="pfeedord-hero__desc">查看用户投喂订单，支持按订单号、用户、状态筛选，并可上传反馈视频</p>
    </header>

    <div class="pfeedord-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pfeedord-filter__form" @submit.prevent>
        <el-form-item label="订单号">
          <el-input
            v-model="orderNo"
            clearable
            placeholder="订单号"
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <UserFilterFields v-model:phone="filters.phone" v-model:username="filters.username" @search="handleSearch" />
        <el-form-item label="状态">
          <el-select v-model="status" placeholder="全部" clearable style="width: 160px" @change="handleSearch">
            <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item class="pfeedord-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" plain @click="loadData">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pfeedord-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="orderNo" label="订单号" min-width="150" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="pet" label="宠物" width="100" />
        <el-table-column prop="productName" label="商品" min-width="160" show-overflow-tooltip />
        <el-table-column label="数量" width="80" align="center">
          <template #default="{ row }">{{ row.quantity ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">¥{{ formatPrice(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="message" label="留言" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">{{ statusText(row) }}</template>
        </el-table-column>
        <el-table-column label="反馈" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isAuth('pet:feed:feedback')" link type="primary" @click="openFeedback(row)">上传视频</el-button>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination v-model:page="pageNum" v-model:limit="pageSize" :total="total" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" title="上传反馈视频" width="560px" class="nf-pro-dialog" align-center>
<el-form label-width="96px">
        <el-form-item label="订单ID"><el-input :model-value="currentRow?.id" disabled /></el-form-item>
        <el-form-item label="赠送信息">
          <span class="pfeedord-feedback__info">{{ currentRow?.pet || '-' }} · {{ currentRow?.productName || '-' }} ×{{ currentRow?.quantity ?? 1 }}</span>
        </el-form-item>
        <el-form-item label="反馈视频">
          <div class="pfeedord-video">
            <video v-if="form.videoUrl" class="pfeedord-video__player" :src="form.videoUrl" controls></video>
            <el-upload
              v-else
              class="pfeedord-video__uploader"
              :show-file-list="false"
              :http-request="handleVideoUpload"
              accept="video/*"
              :disabled="uploadLoading"
            >
              <div class="pfeedord-video__adder" :class="{ 'is-loading': uploadLoading }">
                <span class="pfeedord-video__adder-icon">▶</span>
                <span class="pfeedord-video__adder-text">{{ uploadLoading ? '上传中...' : '点击上传视频' }}</span>
              </div>
            </el-upload>
            <div v-if="form.videoUrl" class="pfeedord-video__actions">
              <el-button size="small" @click="clearVideo">移除</el-button>
            </div>
          </div>
          <p class="pfeedord-video__tip">支持 mp4/mov 等格式，单个文件不超过 100MB。上传后用户端「我的赠送记录」可查看该视频反馈。</p>
        </el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitFeedback">提交反馈</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pfeedord {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pfeedord-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pfeedord-hero__badge {
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

.pfeedord-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pfeedord-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pfeedord-filter.nf-filter-card {
  margin-bottom: 0;
}

.pfeedord-filter__form {
  margin-bottom: 0;
}

.pfeedord-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pfeedord-table.nf-table-card {
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

.pfeedord-feedback__info {
  color: var(--nf-text-primary);
  font-weight: 600;
}

.pfeedord-video {
  width: 100%;
}

.pfeedord-video__player {
  width: 100%;
  max-height: 260px;
  border-radius: 8px;
  background: #000;
}

.pfeedord-video__uploader {
  width: 100%;
}

.pfeedord-video__adder {
  width: 100%;
  height: 160px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: #8c8c8c;
  transition: border-color 0.2s ease, color 0.2s ease;
}

.pfeedord-video__adder:hover,
.pfeedord-video__adder.is-loading {
  border-color: var(--nf-menu-active-soft, #bc926d);
  color: var(--nf-menu-active-soft, #bc926d);
  background: #fffaf4;
}

.pfeedord-video__adder-icon {
  font-size: 28px;
}

.pfeedord-video__adder-text {
  font-size: 13px;
}

.pfeedord-video__actions {
  margin-top: 8px;
}

.pfeedord-video__tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import {
  auditPetCommunityApi,
  batchAuditPetCommunityApi,
  deletePetCommunityApi,
  deletePetCommunityCommentApi,
  getPetCommunityCommentPageApi,
  getPetCommunityConfigApi,
  getPetCommunityLikePageApi,
  getPetCommunityInfoApi,
  getPetCommunityPageApi,
  getPetCommunityStatsApi,
  offlinePetCommunityApi,
} from '@/api/modules/pet-community'
import { confirmAction, showSuccess } from '@/utils/modal'

const router = useRouter()

const auditMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
const statusMap = { 1: '正常', 2: '已下架' }

const auditTagType = { 0: 'warning', 1: 'success', 2: 'danger' }
const statusTagType = { 1: 'success', 2: 'info' }

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref({})

const commentVisible = ref(false)
const commentLoading = ref(false)
const commentList = ref([])
const commentTotal = ref(0)
const commentPageNum = ref(1)
const commentPageSize = ref(20)
const currentCommentPost = ref(null)

const likeVisible = ref(false)
const likeLoading = ref(false)
const likeList = ref([])
const likeTotal = ref(0)
const likePageNum = ref(1)
const likePageSize = ref(20)
const currentLikePost = ref(null)

const filters = ref({
  keyword: '',
  userName: '',
  auditStatus: '',
  status: '',
  dateRange: [],
})

const globalStats = ref({
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  offlineCount: 0,
  totalCount: 0,
})
const auditSwitchOn = ref(true)
const selectedRows = ref([])
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)
const batchRejectVisible = ref(false)
const batchRejectReason = ref('')

const VIDEO_EXT = ['.mp4', '.mov', '.m4v', '.webm']

const summary = computed(() => ({
  pending: globalStats.value.pendingCount,
  approved: globalStats.value.approvedCount,
  rejected: globalStats.value.rejectedCount,
  offline: globalStats.value.offlineCount,
}))

function isVideo(url) {
  return VIDEO_EXT.some((ext) => String(url).toLowerCase().includes(ext))
}

function mediaList(row) {
  return String(row?.mediaUrls || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function mediaTip(row) {
  const list = mediaList(row)
  if (!list.length) return ''
  const videos = list.filter(isVideo).length
  const images = list.length - videos
  const parts = []
  if (images) parts.push(`${images} 张图片`)
  if (videos) parts.push(`${videos} 个视频`)
  return parts.join(' · ')
}

function showAuditActions(row) {
  return Number(row?.auditStatus) === 0
}

function showManageActions(row) {
  return Number(row?.status) === 1
}

function auditText(value) {
  return auditMap[Number(value)] ?? value ?? '-'
}

function statusText(value) {
  return statusMap[Number(value)] ?? value ?? '-'
}

async function loadStats() {
  try {
    const [stats, config] = await Promise.all([
      getPetCommunityStatsApi(),
      getPetCommunityConfigApi(),
    ])
    globalStats.value = {
      pendingCount: Number(stats?.pendingCount || 0),
      approvedCount: Number(stats?.approvedCount || 0),
      rejectedCount: Number(stats?.rejectedCount || 0),
      offlineCount: Number(stats?.offlineCount || 0),
      totalCount: Number(stats?.totalCount || 0),
    }
    auditSwitchOn.value = String(config?.auditSwitch ?? config?.communityAuditSwitch ?? '1') === '1'
  } catch (e) {
    globalStats.value = { pendingCount: 0, approvedCount: 0, rejectedCount: 0, offlineCount: 0, totalCount: 0 }
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pageNum.value,
      limit: pageSize.value,
      keyword: filters.value.keyword.trim(),
      userName: filters.value.userName.trim(),
    }
    if (filters.value.auditStatus !== '') params.auditStatus = Number(filters.value.auditStatus)
    if (filters.value.status !== '') params.status = Number(filters.value.status)
    if (filters.value.dateRange?.length === 2) {
      params.startTime = filters.value.dateRange[0]
      params.endTime = filters.value.dateRange[1]
    }
    const data = await getPetCommunityPageApi(params)
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
  filters.value = { keyword: '', userName: '', auditStatus: '', status: '', dateRange: [] }
  pageNum.value = 1
  loadData()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows || []
}

async function auditRow(row, auditStatus, reason = '') {
  const actionText = auditStatus === 1 ? '通过' : '拒绝'
  await confirmAction(`确认${actionText}该动态？`)
  await auditPetCommunityApi({ id: row.id, auditStatus, rejectReason: reason || undefined })
  showSuccess(`已${actionText}`)
  if (detailVisible.value && detailData.value?.id === row.id) {
    detailData.value = { ...detailData.value, auditStatus, rejectReason: reason || detailData.value.rejectReason }
  }
  await Promise.all([loadData(), loadStats()])
}

function openReject(row) {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectTarget.value) return
  if (!rejectReason.value.trim()) {
    return
  }
  rejectVisible.value = false
  await auditRow(rejectTarget.value, 2, rejectReason.value.trim())
  rejectTarget.value = null
}

async function batchAudit(auditStatus) {
  if (!selectedRows.value.length) return
  const ids = selectedRows.value.map((row) => row.id)
  if (auditStatus === 2) {
    batchRejectVisible.value = true
    batchRejectReason.value = ''
    return
  }
  await confirmAction(`确认批量通过 ${ids.length} 条动态？`)
  await batchAuditPetCommunityApi({ ids, auditStatus: 1 })
  showSuccess('批量审核完成')
  selectedRows.value = []
  await Promise.all([loadData(), loadStats()])
}

async function confirmBatchReject() {
  if (!selectedRows.value.length) return
  if (!batchRejectReason.value.trim()) return
  const ids = selectedRows.value.map((row) => row.id)
  await batchAuditPetCommunityApi({
    ids,
    auditStatus: 2,
    rejectReason: batchRejectReason.value.trim(),
  })
  batchRejectVisible.value = false
  showSuccess('批量拒绝完成')
  selectedRows.value = []
  await Promise.all([loadData(), loadStats()])
}

function handleRowCommand(command, row) {
  if (command === 'detail') openDetail(row)
  else if (command === 'comments') openComments(row)
  else if (command === 'likes') openLikes(row)
  else if (command === 'approve') auditRow(row, 1)
  else if (command === 'reject') openReject(row)
  else if (command === 'offline') offlineRow(row)
  else if (command === 'delete') removeRow(row)
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = row
  try {
    detailData.value = await getPetCommunityInfoApi(row.id) || row
  } finally {
    detailLoading.value = false
  }
}

async function offlineRow(row) {
  await confirmAction('确认下架该动态？')
  await offlinePetCommunityApi({ id: row.id })
  showSuccess('已下架')
  if (detailVisible.value && detailData.value?.id === row.id) {
    detailData.value = { ...detailData.value, status: 2 }
  }
  await Promise.all([loadData(), loadStats()])
}

async function removeRow(row) {
  await confirmAction('确认删除该动态？删除后不可恢复。')
  await deletePetCommunityApi(row.id)
  showSuccess('删除成功')
  if (detailVisible.value && detailData.value?.id === row.id) {
    detailVisible.value = false
  }
  await Promise.all([loadData(), loadStats()])
}

async function removeComment(item) {
  await confirmAction('确认删除该评论？')
  await deletePetCommunityCommentApi(item.id)
  showSuccess('评论已删除')
  await loadComments()
  await loadData()
}

function commentUserName(item) {
  return item?.userName || item?.nickname || item?.name || (item?.userId ? `用户${item.userId}` : '用户')
}

function goToUserDetail(userId) {
  if (!userId) return
  router.push(`/users/${userId}`)
}

async function loadComments() {
  if (!currentCommentPost.value?.id) return
  commentLoading.value = true
  try {
    const data = await getPetCommunityCommentPageApi({
      postId: currentCommentPost.value.id,
      page: commentPageNum.value,
      limit: commentPageSize.value,
    })
    commentList.value = data.list || []
    commentTotal.value = data.total || 0
  } finally {
    commentLoading.value = false
  }
}

async function openComments(row) {
  currentCommentPost.value = row
  commentVisible.value = true
  commentPageNum.value = 1
  commentList.value = []
  commentTotal.value = 0
  await loadComments()
}

function likeUserName(item) {
  return item?.userName || item?.nickname || item?.name || (item?.userId ? `用户${item.userId}` : '用户')
}

async function loadLikes() {
  if (!currentLikePost.value?.id) return
  likeLoading.value = true
  try {
    const data = await getPetCommunityLikePageApi({
      postId: currentLikePost.value.id,
      page: likePageNum.value,
      limit: likePageSize.value,
    })
    likeList.value = data.list || []
    likeTotal.value = data.total || 0
  } finally {
    likeLoading.value = false
  }
}

async function openLikes(row) {
  currentLikePost.value = row
  likeVisible.value = true
  likePageNum.value = 1
  likeList.value = []
  likeTotal.value = 0
  await loadLikes()
}

onMounted(async () => {
  await Promise.all([loadStats(), loadData()])
})
</script>

<template>
  <div class="pcomm">
    <header class="pcomm-hero nf-page-hero">
      <span class="pcomm-hero__badge">宠物业务</span>
      <h1 class="pcomm-hero__title">朋友圈动态</h1>
      <p class="pcomm-hero__desc">按审核与上架状态管理用户发布内容</p>
    </header>

    <div class="pcomm-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
        <el-tag size="small" :type="auditSwitchOn ? 'success' : 'info'">
          审核开关：{{ auditSwitchOn ? '开启' : '关闭' }}
        </el-tag>
      </div>

      <div class="pcomm-stats">
        <div class="pcomm-stats__item">
          <span class="pcomm-stats__label">全站待审核</span>
          <strong class="pcomm-stats__value pcomm-stats__value--warning">{{ summary.pending }}</strong>
        </div>
        <div class="pcomm-stats__item">
          <span class="pcomm-stats__label">全站已通过</span>
          <strong class="pcomm-stats__value pcomm-stats__value--success">{{ summary.approved }}</strong>
        </div>
        <div class="pcomm-stats__item">
          <span class="pcomm-stats__label">全站已拒绝</span>
          <strong class="pcomm-stats__value pcomm-stats__value--danger">{{ summary.rejected }}</strong>
        </div>
        <div class="pcomm-stats__item">
          <span class="pcomm-stats__label">全站已下架</span>
          <strong class="pcomm-stats__value pcomm-stats__value--muted">{{ summary.offline }}</strong>
        </div>
      </div>

      <el-form :inline="true" class="pcomm-filter__form" @submit.prevent>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="filters.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="filters.userName" clearable placeholder="用户昵称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="内容关键词">
          <el-input v-model="filters.keyword" clearable placeholder="动态内容" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="filters.auditStatus" clearable placeholder="全部">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option label="正常" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-form-item>
        <el-form-item class="pcomm-filter__actions">
          <el-space wrap>
            <el-button
              v-if="selectedRows.length"
              type="success"
              @click="batchAudit(1)"
            >
              批量通过 ({{ selectedRows.length }})
            </el-button>
            <el-button
              v-if="selectedRows.length"
              type="warning"
              @click="batchAudit(2)"
            >
              批量拒绝 ({{ selectedRows.length }})
            </el-button>
            <el-button type="primary" plain @click="Promise.all([loadStats(), loadData()])">刷新</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pcomm-table nf-table-card">
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      class="community-table"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" fixed />
      <el-table-column prop="id" label="ID" width="76" fixed />
      <el-table-column label="用户" width="110" show-overflow-tooltip>
        <template #default="{ row }">
          <span
            v-if="row.userId"
            class="user-link"
            @click="goToUserDetail(row.userId)"
          >{{ row.userName || `用户${row.userId}` }}</span>
          <span v-else>{{ row.userName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="内容" min-width="240">
        <template #default="{ row }">
          <div class="content-cell">
            <p class="content-text">{{ row.content || '-' }}</p>
            <div v-if="mediaList(row).length" class="content-media-tip">{{ mediaTip(row) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="封面" width="88" align="center">
        <template #default="{ row }">
          <el-image
            v-if="mediaList(row)[0]"
            :src="mediaList(row)[0]"
            :preview-src-list="mediaList(row)"
            fit="cover"
            class="thumb-image"
          />
          <span v-else class="empty-thumb">无图</span>
        </template>
      </el-table-column>
      <el-table-column label="互动" width="100" align="center">
        <template #default="{ row }">
          <div class="metric-cell">
            <span>赞 {{ row.likeCount ?? 0 }}</span>
            <span>评 {{ row.commentCount ?? 0 }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="审核" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="auditTagType[row.auditStatus] || 'info'" size="small" effect="light">
            {{ auditText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="96" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.status] || 'info'" size="small" effect="light">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="168" />
      <el-table-column label="操作" width="170" fixed="right" align="center" class-name="action-column">
        <template #default="{ row }">
          <div class="action-cell">
            <div class="action-group">
              <el-button type="success" plain size="small" @click="openDetail(row)">详情</el-button>
              <el-button type="success" plain size="small" @click="openComments(row)">评论</el-button>
              <el-button type="success" plain size="small" @click="openLikes(row)">点赞</el-button>
            </div>
            <div v-if="showAuditActions(row)" class="action-group">
              <el-button type="success" plain size="small" @click="auditRow(row, 1)">通过</el-button>
              <el-button type="warning" plain size="small" @click="openReject(row)">拒绝</el-button>
            </div>
            <div v-if="showManageActions(row)" class="action-group">
              <el-button type="warning" plain size="small" @click="offlineRow(row)">下架</el-button>
              <el-button type="danger" plain size="small" @click="removeRow(row)">删除</el-button>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />
    </div>

    <el-dialog v-model="detailVisible" title="动态详情" width="760px" class="detail-dialog nf-pro-dialog" align-center>
<div v-loading="detailLoading" class="detail-body">
        <div class="detail-head">
          <div>
            <div
              v-if="detailData.userId"
              class="detail-user user-link"
              @click="goToUserDetail(detailData.userId)"
            >{{ detailData.userName || `用户${detailData.userId}` }}</div>
            <div v-else class="detail-user">{{ detailData.userName || '未知用户' }}</div>
            <div class="detail-time">{{ detailData.createTime || '-' }}</div>
          </div>
          <div class="detail-tags">
            <el-tag :type="auditTagType[detailData.auditStatus] || 'info'" size="small">
              {{ auditText(detailData.auditStatus) }}
            </el-tag>
            <el-tag :type="statusTagType[detailData.status] || 'info'" size="small">
              {{ statusText(detailData.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="门店">{{ detailData.storeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="宠物">{{ detailData.petName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="点赞">{{ detailData.likeCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="评论">{{ detailData.commentCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="拒绝原因" :span="2">{{ detailData.rejectReason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-content-box">
          <div class="detail-section-title">动态内容</div>
          <p class="detail-content">{{ detailData.content || '-' }}</p>
        </div>

        <div v-if="mediaList(detailData).length" class="detail-media-wrap">
          <div class="detail-section-title">媒体素材</div>
          <div class="detail-media-list">
            <el-image
              v-for="(url, index) in mediaList(detailData)"
              :key="`${detailData.id}-${index}`"
              :src="url"
              :preview-src-list="mediaList(detailData)"
              fit="cover"
              class="detail-media"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="detail-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <div class="detail-action-group">
            <el-button type="info" @click="openComments(detailData)">查看评论</el-button>
            <el-button type="warning" @click="openLikes(detailData)">查看点赞</el-button>
            <el-button v-if="showAuditActions(detailData)" type="success" @click="auditRow(detailData, 1)">通过</el-button>
            <el-button v-if="showAuditActions(detailData)" type="warning" @click="openReject(detailData)">拒绝</el-button>
            <el-button v-if="showManageActions(detailData)" type="primary" @click="offlineRow(detailData)">下架</el-button>
            <el-button v-if="showManageActions(detailData)" type="danger" @click="removeRow(detailData)">删除</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="commentVisible"
      :title="`评论列表 - 动态#${currentCommentPost?.id || ''}`"
      width="640px"
      class="comment-dialog nf-pro-dialog"
      destroy-on-close
     align-center>
<div class="comment-dialog-head">
        <span
          v-if="currentCommentPost?.userId"
          class="comment-dialog-user user-link"
          @click="goToUserDetail(currentCommentPost.userId)"
        >{{ currentCommentPost?.userName || `用户${currentCommentPost.userId}` }}</span>
        <span v-else class="comment-dialog-user">{{ currentCommentPost?.userName || '未知用户' }}</span>
        <span class="comment-dialog-meta">共 {{ commentTotal }} 条评论</span>
      </div>
      <div v-loading="commentLoading" class="comment-list-wrap">
        <div v-for="item in commentList" :key="item.id" class="comment-item">
          <el-avatar class="comment-avatar" :size="40" :src="item.avatar">
            {{ commentUserName(item).slice(0, 1) }}
          </el-avatar>
          <div class="comment-main">
            <div class="comment-meta">
              <span
                v-if="item.userId"
                class="comment-name user-link"
                @click="goToUserDetail(item.userId)"
              >{{ commentUserName(item) }}</span>
              <span v-else class="comment-name">{{ commentUserName(item) }}</span>
              <span class="comment-time">{{ item.createTime || '-' }}</span>
              <el-button link type="danger" size="small" @click="removeComment(item)">删除</el-button>
            </div>
            <p class="comment-content">{{ item.content || '-' }}</p>
          </div>
        </div>
        <el-empty v-if="!commentLoading && !commentList.length" description="暂无评论" />
      </div>
      <BasePagination
        v-if="commentTotal > 0"
        :current-page="commentPageNum"
        :page-size="commentPageSize"
        :total="commentTotal"
        :page-sizes="[10, 20, 50]"
        @update:current-page="commentPageNum = $event; loadComments()"
        @update:page-size="commentPageSize = $event; commentPageNum = 1; loadComments()"
      />
    </el-dialog>

    <el-dialog
      v-model="likeVisible"
      :title="`点赞列表 - 动态#${currentLikePost?.id || ''}`"
      width="640px"
      class="comment-dialog nf-pro-dialog"
      destroy-on-close
     align-center>
<div class="comment-dialog-head">
        <span
          v-if="currentLikePost?.userId"
          class="comment-dialog-user user-link"
          @click="goToUserDetail(currentLikePost.userId)"
        >{{ currentLikePost?.userName || `用户${currentLikePost.userId}` }}</span>
        <span v-else class="comment-dialog-user">{{ currentLikePost?.userName || '未知用户' }}</span>
        <span class="comment-dialog-meta">共 {{ likeTotal }} 人点赞</span>
      </div>
      <div v-loading="likeLoading" class="comment-list-wrap">
        <div v-for="item in likeList" :key="item.id" class="comment-item">
          <el-avatar class="comment-avatar" :size="40" :src="item.avatar">
            {{ likeUserName(item).slice(0, 1) }}
          </el-avatar>
          <div class="comment-main">
            <div class="comment-meta">
              <span
                v-if="item.userId"
                class="comment-name user-link"
                @click="goToUserDetail(item.userId)"
              >{{ likeUserName(item) }}</span>
              <span v-else class="comment-name">{{ likeUserName(item) }}</span>
              <span class="comment-time">{{ item.createTime || '-' }}</span>
            </div>
            <p class="comment-content like-tip">已点赞该动态</p>
          </div>
        </div>
        <el-empty v-if="!likeLoading && !likeList.length" description="暂无点赞" />
      </div>
      <BasePagination
        v-if="likeTotal > 0"
        :current-page="likePageNum"
        :page-size="likePageSize"
        :total="likeTotal"
        :page-sizes="[10, 20, 50]"
        @update:current-page="likePageNum = $event; loadLikes()"
        @update:page-size="likePageSize = $event; likePageNum = 1; loadLikes()"
      />
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="填写拒绝原因" width="480px" class="nf-pro-dialog" align-center>
<el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        maxlength="255"
        show-word-limit
        placeholder="请填写审核拒绝原因"
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="warning" :disabled="!rejectReason.trim()" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchRejectVisible" title="批量拒绝原因" width="480px" class="nf-pro-dialog" align-center>
<el-input
        v-model="batchRejectReason"
        type="textarea"
        :rows="4"
        maxlength="255"
        show-word-limit
        placeholder="请填写批量拒绝原因"
      />
      <template #footer>
        <el-button @click="batchRejectVisible = false">取消</el-button>
        <el-button type="warning" :disabled="!batchRejectReason.trim()" @click="confirmBatchReject">确认批量拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pcomm {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pcomm-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pcomm-hero__badge {
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

.pcomm-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pcomm-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pcomm-filter.nf-filter-card {
  margin-bottom: 0;
}

.pcomm-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.pcomm-stats__item {
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--nf-sidebar-bg-alt, #f7f8fa);
  border: 1px solid var(--nf-border);
}

.pcomm-stats__label {
  display: block;
  font-size: 12px;
  color: var(--nf-text-secondary);
  margin-bottom: 4px;
}

.pcomm-stats__value {
  font-size: 20px;
  line-height: 1;
  color: var(--nf-text-primary);
}

.pcomm-stats__value--warning { color: #e6a23c; }
.pcomm-stats__value--success { color: #67c23a; }
.pcomm-stats__value--danger { color: #f56c6c; }
.pcomm-stats__value--muted { color: #909399; }

.pcomm-filter__form {
  margin-bottom: 0;

  :deep(.el-select) {
    width: 140px;
  }
}

.pcomm-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pcomm-table.nf-table-card {
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

.user-link {
  color: var(--el-color-primary);
  cursor: pointer;
}

.detail-user.user-link,
.comment-dialog-user.user-link,
.comment-name.user-link {
  color: var(--el-color-primary);
}

.community-table :deep(.el-table__cell) {
  vertical-align: top;
}

.community-table :deep(.action-column .cell) {
  padding: 8px 6px;
}

.action-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 100%;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px 6px;
  width: 100%;
}

.action-group :deep(.el-button) {
  margin: 0;
  padding: 0 10px;
  font-size: 12px;
  height: 24px;
  line-height: 22px;
  border-radius: 4px;
}

.action-group + .action-group {
  padding-top: 4px;
  border-top: 1px dashed var(--el-border-color-extra-light);
}

.content-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.content-text {
  margin: 0;
  line-height: 1.5;
  color: var(--el-text-color-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.content-media-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.metric-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.thumb-image {
  width: 56px;
  height: 56px;
  border-radius: 8px;
}

.empty-thumb {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.detail-body {
  min-height: 120px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.detail-user {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.detail-time {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.detail-tags {
  display: flex;
  gap: 8px;
}

.detail-desc {
  margin-bottom: 16px;
}

.detail-content-box {
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
}

.detail-section-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.detail-content {
  margin: 0;
  line-height: 1.7;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
}

.detail-media-wrap {
  margin-top: 16px;
}

.detail-media-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-media {
  width: 112px;
  height: 112px;
  border-radius: 10px;
}

.detail-footer {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

.detail-action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.comment-dialog-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.comment-dialog-user {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.comment-dialog-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.comment-list-wrap {
  min-height: 160px;
  max-height: 52vh;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  flex-shrink: 0;
  background: var(--el-fill-color);
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.comment-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.comment-content {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  word-break: break-word;
}

.like-tip {
  color: var(--el-text-color-secondary);
}

@media (max-width: 960px) {
  .pcomm-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

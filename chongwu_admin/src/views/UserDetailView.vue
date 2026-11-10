<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageTopbar from '@/components/common/PageTopbar.vue'
import UserOrdersTab from '@/views/user/detail/UserOrdersTab.vue'
import UserFeedTab from '@/views/user/detail/UserFeedTab.vue'
import UserAssetTab from '@/views/user/detail/UserAssetTab.vue'
import UserCouponTab from '@/views/user/detail/UserCouponTab.vue'
import UserIslandTab from '@/views/user/detail/UserIslandTab.vue'
import {
  deleteUserApi,
  getUserInfoApi,
  getUserOverviewApi,
  updateUserStatusApi,
  updateUserApi,
} from '@/api/modules/users'
import {
  getPetUserAssetByUserIdApi,
  adjustPetUserAssetApi,
} from '@/api/modules/pet-user'
import { buildUserJumpQuery } from '@/utils/user-filter'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { DEFAULT_USER_AVATAR, isAuth } from '@/utils/tool'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const userInfo = ref({})
const overview = ref({})
const activeTab = ref(String(route.query.tab || 'profile'))
const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const userAsset = ref({ points: 0, coins: 0, flowers: 0, loveValue: 0, level: 1, levelName: 'Lv.1' })
const assetDialogVisible = ref(false)
const assetDialogLoading = ref(false)
const assetForm = reactive({
  assetType: 'coins',
  opType: 'add',
  amount: 0,
  reason: '',
})

const editForm = reactive({
  userName: '',
  sex: '',
  birthday: '',
  wechat: '',
  userType: 1,
  status: 1,
})

const sexOptions = [
  { label: '未设置', value: '' },
  { label: '男', value: 1 },
  { label: '女', value: 2 },
]

const userTypeOptions = [
  { label: '普通用户', value: 1 },
  { label: 'VIP用户', value: 2 },
]

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 2 },
]

const currentUserId = computed(() => Number(route.params.id || 0))
const statusText = computed(() => Number(userInfo.value.status) === 1 ? '正常' : '禁用')
const statusTag = computed(() => Number(userInfo.value.status) === 1 ? 'success' : 'danger')

const metricCards = computed(() => {
  const o = overview.value || {}
  return [
    { label: '商城订单', value: o.mallOrderCount ?? 0 },
    { label: '投喂订单', value: o.feedOrderCount ?? 0 },
    { label: '可用券', value: o.couponAvailable ?? 0 },
    { label: '岛宅', value: o.islandPetCount ?? 0 },
    { label: '待反馈投喂', value: o.giftPendingFeedback ?? 0 },
    { label: '进行中兑换', value: o.exchangePending ?? 0 },
    { label: '投诉', value: o.complaintCount ?? 0 },
  ]
})

const assetCards = computed(() => {
  const a = userAsset.value || {}
  return [
    { key: 'points', label: '积分', value: a.points ?? 0, icon: '✦', color: '#7c5cff' },
    { key: 'coins', label: '金币', value: a.coins ?? 0, icon: '◉', color: '#e0a458' },
    { key: 'flowers', label: '花瓣', value: a.flowers ?? 0, icon: '✿', color: '#d38ca1' },
    { key: 'love', label: '爱心值', value: a.loveValue ?? 0, icon: '♥', color: '#f07a7a' },
  ]
})

const assetTypeOptions = [
  { label: '积分', value: 'points' },
  { label: '金币', value: 'coins' },
  { label: '花瓣', value: 'flowers' },
  { label: '爱心值', value: 'love' },
]

const opTypeOptions = [
  { label: '增加', value: 'add' },
  { label: '减少', value: 'reduce' },
  { label: '设为', value: 'set' },
]

function sexText(value) {
  if (Number(value) === 1) return '男'
  if (Number(value) === 2) return '女'
  return '未设置'
}

function userTypeText(value) {
  return Number(value) === 2 ? 'VIP用户' : '普通用户'
}

function vipText(value) {
  return Number(value) === 1 ? '是' : '否'
}

function formatValue(value, fallback = '-') {
  return value === 0 || value ? value : fallback
}

function goBack() {
  router.push('/users')
}

function jumpQuery() {
  return buildUserJumpQuery(userInfo.value)
}

function openFullList(path) {
  router.push({ path, query: jumpQuery() })
}

function copyText(text, label = '内容') {
  const value = String(text || '').trim()
  if (!value) {
    showError(`无可复制${label}`)
    return
  }
  if (navigator?.clipboard?.writeText) {
    navigator.clipboard.writeText(value).then(() => showSuccess(`已复制${label}`)).catch(() => showError('复制失败'))
    return
  }
  showError('当前环境不支持复制')
}

async function loadData() {
  if (!currentUserId.value) {
    showError('用户ID无效')
    return
  }
  loading.value = true
  try {
    const [info, ov, asset] = await Promise.all([
      getUserInfoApi(currentUserId.value),
      getUserOverviewApi(currentUserId.value).catch(() => ({})),
      getPetUserAssetByUserIdApi(currentUserId.value).catch(() => ({})),
    ])
    userInfo.value = info || {}
    overview.value = ov || {}
    userAsset.value = asset || userAsset.value
  } finally {
    loading.value = false
  }
}

async function reloadAsset() {
  try {
    const asset = await getPetUserAssetByUserIdApi(currentUserId.value)
    userAsset.value = asset || userAsset.value
  } catch (e) {
    // ignore
  }
}

function openEditDialog() {
  editForm.userName = userInfo.value.userName || ''
  editForm.sex = userInfo.value.sex ?? ''
  editForm.birthday = userInfo.value.birthday || ''
  editForm.wechat = userInfo.value.wechat || ''
  editForm.userType = Number(userInfo.value.userType || 1)
  editForm.status = Number(userInfo.value.status || 1)
  editDialogVisible.value = true
}

async function submitEditDialog() {
  if (!editForm.userName.trim()) {
    showError('请输入用户昵称')
    return
  }
  editDialogLoading.value = true
  try {
    await updateUserApi({
      userId: currentUserId.value,
      userName: editForm.userName.trim(),
      sex: editForm.sex === '' ? undefined : Number(editForm.sex),
      birthday: editForm.birthday || undefined,
      wechat: editForm.wechat.trim() || undefined,
      userType: Number(editForm.userType),
      status: Number(editForm.status),
    })
    editDialogVisible.value = false
    showSuccess('修改成功')
    await loadData()
  } finally {
    editDialogLoading.value = false
  }
}

async function handleToggleStatus() {
  const previousStatus = Number(userInfo.value.status || 1)
  const nextStatus = previousStatus === 1 ? 2 : 1
  try {
    await updateUserStatusApi(currentUserId.value, nextStatus)
    showSuccess('状态已更新')
    await loadData()
  } catch (error) {
    userInfo.value.status = previousStatus
  }
}

async function handleDelete() {
  await confirmAction('确定删除该用户吗？')
  await deleteUserApi(currentUserId.value)
  showSuccess('删除成功')
  router.push('/users')
}

function openAssetDialog(assetType) {
  assetForm.assetType = assetType || 'coins'
  assetForm.opType = 'add'
  assetForm.amount = 0
  assetForm.reason = ''
  assetDialogVisible.value = true
}

async function submitAssetDialog() {
  if (!assetForm.amount && assetForm.opType !== 'set') {
    showError('请输入变动数量')
    return
  }
  assetDialogLoading.value = true
  try {
    await adjustPetUserAssetApi({
      userId: currentUserId.value,
      assetType: assetForm.assetType,
      opType: assetForm.opType,
      amount: Number(assetForm.amount) || 0,
      reason: assetForm.reason.trim() || '管理员调整',
    })
    assetDialogVisible.value = false
    showSuccess('资产调整成功')
    await reloadAsset()
  } catch (error) {
    showError(error?.msg || error?.message || '调整失败')
  } finally {
    assetDialogLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div v-loading="loading" class="udetail">
    <PageTopbar title="用户详情" @back="goBack" />

    <header class="udetail-hero nf-page-hero">
      <span class="udetail-hero__badge">用户中心</span>
      <h1 class="udetail-hero__title">用户详情</h1>
      <p class="udetail-hero__desc">查看用户资料、资产概览与商城/投喂/券包/岛宅等业务记录，支持编辑与状态管理</p>
    </header>

    <div class="udetail-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户资料" name="profile">
          <div class="udetail-body nf-table-card">
            <div class="profile-hero">
              <img :src="userInfo.avatar || overview.avatar || DEFAULT_USER_AVATAR" alt="头像" class="profile-avatar" />
              <div class="profile-hero-main">
                <div class="profile-hero-name-row">
                  <strong class="profile-hero-name">{{ overview.userName || userInfo.userName || '未设置昵称' }}</strong>
                  <el-tag :type="statusTag" size="small">{{ statusText }}</el-tag>
                  <el-tag v-if="Number(userInfo.userType ?? overview.userType) === 2" type="warning" size="small">VIP</el-tag>
                </div>
                <div class="profile-hero-sub">
                  <span class="profile-hero-tag">{{ userTypeText(userInfo.userType ?? overview.userType) }}</span>
                  <span class="profile-hero-divider">·</span>
                  <span>手机号：{{ formatValue(userInfo.phone || overview.phone) }}</span>
                  <span class="profile-hero-divider">·</span>
                  <span>邀请码：{{ formatValue(userInfo.invitationCode || overview.invitationCode) }}</span>
                </div>
                <div class="profile-hero-actions">
                  <el-button size="small" @click="copyText(userInfo.phone, '手机号')">复制手机号</el-button>
                  <el-button size="small" @click="copyText(userInfo.userName, '用户名')">复制用户名</el-button>
                  <el-button size="small" @click="copyText(userInfo.userId, '用户ID')">复制ID</el-button>
                  <el-button size="small" type="primary" :disabled="!isAuth('userList:update')" @click="openEditDialog">编辑用户</el-button>
                  <el-button size="small" :disabled="!isAuth('userList:delete')" @click="handleToggleStatus">
                    {{ Number(userInfo.status) === 1 ? '禁用用户' : '启用用户' }}
                  </el-button>
                  <el-button size="small" type="danger" :disabled="!isAuth('userList:delete')" @click="handleDelete">删除用户</el-button>
                  <el-button v-if="isAuth('pet:message:send')" size="small" @click="openFullList('/pet/messages')">发站内信</el-button>
                  <el-button v-if="isAuth('pet:coupon:grant')" size="small" @click="openFullList('/pet/coupon-grant')">发券</el-button>
                </div>
              </div>
            </div>

            <div class="asset-section">
              <div class="asset-section-head">
                <strong>用户资产</strong>
                <el-button size="small" type="primary" plain :disabled="!isAuth('pet:asset:list')" @click="openAssetDialog('coins')">调整资产</el-button>
              </div>
              <div class="asset-grid">
                <div v-for="item in assetCards" :key="item.key" class="asset-card" :style="{ '--asset-color': item.color }">
                  <div class="asset-icon">{{ item.icon }}</div>
                  <div class="asset-value">{{ item.value }}</div>
                  <div class="asset-label">{{ item.label }}</div>
                  <el-button class="asset-adjust-btn" link size="small" :disabled="!isAuth('pet:asset:list')" @click="openAssetDialog(item.key)">调整</el-button>
                </div>
              </div>
              <div class="asset-level-row">
                <span class="asset-level-label">等级</span>
                <el-tag size="small" type="success">{{ userAsset.levelName || ('Lv.' + (userAsset.level || 1)) }}</el-tag>
              </div>
              <UserAssetTab :user-id="currentUserId" />
            </div>

            <div class="metric-grid">
              <div v-for="item in metricCards" :key="item.label" class="metric-card">
                <div class="metric-value">{{ item.value }}</div>
                <div class="metric-label">{{ item.label }}</div>
              </div>
            </div>

            <el-descriptions :column="2" border class="profile-descriptions" title="基础资料">
              <el-descriptions-item label="昵称">{{ formatValue(userInfo.userName) }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ sexText(userInfo.sex) }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ formatValue(userInfo.age) }}</el-descriptions-item>
              <el-descriptions-item label="生日">{{ formatValue(userInfo.birthday) }}</el-descriptions-item>
              <el-descriptions-item label="微信号">{{ formatValue(userInfo.wechat) }}</el-descriptions-item>
              <el-descriptions-item label="来源">{{ formatValue(userInfo.platform) }}</el-descriptions-item>
              <el-descriptions-item label="邀请码">{{ formatValue(userInfo.invitationCode) }}</el-descriptions-item>
              <el-descriptions-item label="上级邀请码">{{ formatValue(userInfo.superiorInvitationCode) }}</el-descriptions-item>
              <el-descriptions-item label="小程序OpenId">{{ formatValue(userInfo.maOpenId) }}</el-descriptions-item>
              <el-descriptions-item label="公众号OpenId">{{ formatValue(userInfo.mpOpenId) }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatValue(userInfo.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatValue(userInfo.updateTime) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="isAuth('pet:order:list') || isAuth('pet:mall:list')" label="商城订单" name="orders" lazy>
          <div class="udetail-body nf-table-card">
            <div class="tab-toolbar">
              <el-button link type="primary" @click="openFullList('/pet/orders')">在完整列表中打开</el-button>
            </div>
            <UserOrdersTab :user-id="currentUserId" />
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="isAuth('pet:feed:list')" label="投喂订单" name="feed" lazy>
          <div class="udetail-body nf-table-card">
            <div class="tab-toolbar">
              <el-button link type="primary" @click="openFullList('/pet/feed')">在完整列表中打开</el-button>
            </div>
            <UserFeedTab :user-id="currentUserId" />
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="isAuth('pet:coupon:list') || isAuth('pet:mall:list')" label="券包" name="coupon" lazy>
          <div class="udetail-body nf-table-card">
            <div class="tab-toolbar">
              <el-button link type="primary" @click="openFullList('/pet/coupon-grant')">去发券</el-button>
            </div>
            <UserCouponTab :user-id="currentUserId" />
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="isAuth('pet:island:list')" label="岛宅宠物" name="island" lazy>
          <div class="udetail-body nf-table-card">
            <div class="tab-toolbar">
              <el-button link type="primary" @click="openFullList('/pet/island-pets')">在完整列表中打开</el-button>
            </div>
            <UserIslandTab :user-id="currentUserId" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="editDialogVisible" title="编辑用户" width="520px" class="nf-pro-dialog" align-center>
      <el-form label-width="100px">
        <el-form-item label="用户昵称">
          <el-input v-model="editForm.userName" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.sex">
            <el-option v-for="item in sexOptions" :key="`sex-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="editForm.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择生日" />
        </el-form-item>
        <el-form-item label="微信号">
          <el-input v-model="editForm.wechat" placeholder="请输入微信号" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="editForm.userType">
            <el-option v-for="item in userTypeOptions" :key="`type-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status">
            <el-option v-for="item in statusOptions" :key="`status-${item.value}`" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitEditDialog">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="assetDialogVisible" title="调整用户资产" width="480px" class="nf-pro-dialog" align-center>
      <el-form label-width="100px">
        <el-form-item label="资产类型">
          <el-select v-model="assetForm.assetType">
            <el-option v-for="item in assetTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作方式">
          <el-radio-group v-model="assetForm.opType">
            <el-radio v-for="item in opTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="assetForm.opType === 'set' ? '目标值' : '数量'">
          <el-input-number v-model="assetForm.amount" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注/原因">
          <el-input v-model="assetForm.reason" type="textarea" :rows="2" placeholder="选填，默认「管理员调整」" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="assetDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="assetDialogLoading" @click="submitAssetDialog">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.udetail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.udetail-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.udetail-hero__badge {
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

.udetail-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.udetail-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.udetail-tabs {
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
    padding: 0;
  }

  :deep(.el-tab-pane) {
    padding: 0;
  }
}

.udetail-body.nf-table-card {
  margin-bottom: 0;
  margin-top: 0;
  padding: 16px;
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

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px;
  margin-bottom: 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff8ef 0%, #fdf3e6 100%);
  border: 1px solid #f0e0cb;
}

.profile-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 4px 14px rgba(199, 150, 98, 0.18);
  flex-shrink: 0;
}

.profile-hero-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.profile-hero-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.profile-hero-name {
  font-size: 20px;
  font-weight: 700;
  color: #6d4c38;
}

.profile-hero-sub {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #9f8a77;
}

.profile-hero-tag {
  color: #c79662;
  font-weight: 600;
}

.profile-hero-divider {
  color: #d8c4ad;
}

.profile-hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.asset-section {
  margin-bottom: 18px;
  padding: 16px;
  border-radius: 12px;
  background: #fffdf9;
  border: 1px solid #f0e0cb;
}

.asset-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.asset-section-head strong {
  font-size: 15px;
  color: #6d4c38;
}

.asset-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.asset-card {
  position: relative;
  padding: 16px 12px 28px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #f0e0cb;
  text-align: center;
  transition: transform 0.15s ease;
}

.asset-card:hover {
  transform: translateY(-2px);
}

.asset-icon {
  font-size: 22px;
  color: var(--asset-color, #c79662);
  line-height: 1;
  margin-bottom: 6px;
}

.asset-value {
  font-size: 24px;
  font-weight: 700;
  color: #6d4c38;
  line-height: 1.1;
}

.asset-label {
  margin-top: 4px;
  font-size: 12px;
  color: #9f8a77;
}

.asset-adjust-btn {
  position: absolute;
  right: 8px;
  top: 8px;
}

.asset-level-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #9f8a77;
}

.asset-level-label {
  font-weight: 600;
  color: #6d4c38;
}

.avatar-image,
.avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 8px;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.profile-meta {
  display: grid;
  gap: 8px;
  color: var(--el-text-color-regular);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric-card {
  padding: 12px;
  border-radius: 8px;
  background: var(--nf-sidebar-bg-alt, #f7f8fa);
  border: 1px solid var(--nf-border);
  text-align: center;
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--nf-text-primary);
}

.metric-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--nf-text-secondary);
}

.profile-descriptions {
  margin-top: 0;
}

.tab-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

@media (max-width: 900px) {
  .profile-hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

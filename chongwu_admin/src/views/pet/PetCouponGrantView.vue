<script setup>
import { nextTick, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import UserFilterFields from '@/components/common/UserFilterFields.vue'
import { getPetCouponPageApi, grantPetCouponBatchApi } from '@/api/modules/pet-coupon'
import { getUserPageApi } from '@/api/modules/users'
import { useRouteUserFilter } from '@/utils/user-filter'
import { showError, showSuccess } from '@/utils/modal'
import { isAuth, DEFAULT_USER_AVATAR } from '@/utils/tool'

const { filters } = useRouteUserFilter()
const templateLoading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const templateList = ref([])

const form = reactive({
  templateId: '',
  count: 1,
})

// 用户选择
const userLoading = ref(false)
const userTableData = ref([])
const userPageNum = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userFilters = reactive({ userName: '', phone: '' })
const selectedUsers = ref([])
const userTableRef = ref(null)

async function loadTemplates() {
  templateLoading.value = true
  try {
    const data = await getPetCouponPageApi({ page: 1, limit: 100 })
    templateList.value = (data.list || []).filter(item => item.status === 1)
  } catch (error) {
    templateList.value = []
    showError(error?.msg || error?.message || '加载券模板失败')
  } finally {
    templateLoading.value = false
  }
}

function openGrant() {
  Object.assign(form, { templateId: '', count: 1 })
  Object.assign(userFilters, { userName: filters.username || '', phone: filters.phone || '' })
  selectedUsers.value = []
  dialogVisible.value = true
  loadUsers()
}

async function loadUsers() {
  userLoading.value = true
  try {
    const data = await getUserPageApi({
      page: userPageNum.value,
      limit: userPageSize.value,
      userName: userFilters.userName.trim(),
      phone: userFilters.phone.trim(),
      status: 1,
    })
    userTableData.value = data.list || []
    userTotal.value = data.total || 0
    // 翻页后恢复已选行勾选
    nextTick(() => {
      if (userTableRef.value) {
        userTableData.value.forEach(row => {
          if (selectedUsers.value.some(u => u.userId === row.userId)) {
            userTableRef.value.toggleRowSelection(row, true)
          }
        })
      }
    })
  } catch (error) {
    userTableData.value = []
    userTotal.value = 0
    showError(error?.msg || error?.message || '加载用户失败')
  } finally {
    userLoading.value = false
  }
}

function handleUserSearch() {
  userPageNum.value = 1
  loadUsers()
}

function handleSelectionChange(selection) {
  // 合并跨页选择：先移除当前页所有行，再加入当前页新选中
  const currentPageIds = userTableData.value.map(r => r.userId)
  selectedUsers.value = selectedUsers.value.filter(u => !currentPageIds.includes(u.userId))
  selectedUsers.value = selectedUsers.value.concat(selection)
}

function handleUserPageChange(p) {
  userPageNum.value = p
  loadUsers()
}

function handleUserPageSizeChange(s) {
  userPageSize.value = s
  userPageNum.value = 1
  loadUsers()
}

function sexText(value) {
  if (Number(value) === 1) return '男'
  if (Number(value) === 2) return '女'
  return '未设置'
}

function userTypeText(value) {
  return Number(value) === 2 ? 'VIP用户' : '普通用户'
}

async function submitGrant() {
  const templateId = Number(form.templateId)
  if (!templateId) {
    showError('请选择券模板')
    return
  }
  if (!selectedUsers.value.length) {
    showError('请至少选择一个用户')
    return
  }
  dialogLoading.value = true
  try {
    await grantPetCouponBatchApi({
      userIds: selectedUsers.value.map(u => u.userId),
      templateId,
      count: Number(form.count) || 1,
    })
    showSuccess(`已向 ${selectedUsers.value.length} 位用户发券成功`)
    dialogVisible.value = false
  } catch (error) {
    showError(error?.msg || error?.message || '发券失败')
  } finally {
    dialogLoading.value = false
  }
}

loadTemplates()
</script>

<template>
  <div class="pcgrant">
    <header class="pcgrant-hero nf-page-hero">
      <span class="pcgrant-hero__badge">宠物业务</span>
      <h1 class="pcgrant-hero__title">优惠券发放</h1>
      <p class="pcgrant-hero__desc">选择平台用户批量发放已启用的优惠券模板，发券后用户可在小程序「我的券包」查看</p>
    </header>

    <div class="pcgrant-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pcgrant-filter__form" @submit.prevent>
        <UserFilterFields v-model:phone="filters.phone" v-model:username="filters.username" />
        <el-form-item class="pcgrant-filter__actions">
          <el-space wrap>
            <el-button v-if="isAuth('pet:coupon:grant')" type="success" @click="openGrant">向用户发券</el-button>
            <el-button type="primary" plain @click="loadTemplates">刷新模板</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pcgrant-table nf-table-card">
      <el-alert
        type="info"
        show-icon
        :closable="false"
        class="pcgrant-table__alert"
        title="发券后用户可在小程序「我的券包」查看；请先在「优惠券模板」创建并启用模板。点击「向用户发券」可勾选多个用户批量发放。"
      />

      <el-table v-loading="templateLoading" :data="templateList" border>
        <el-table-column prop="id" label="模板ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="amountDesc" label="面额描述" min-width="120" />
        <el-table-column prop="couponType" label="类型" width="120" />
        <el-table-column prop="validDays" label="有效天数" width="100" />
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" title="向用户发券" width="960px" class="nf-pro-dialog pcgrant-dialog" align-center>
      <div class="pcgrant-dialog__top">
        <el-form :inline="true" class="pcgrant-dialog__form" @submit.prevent>
          <el-form-item label="券模板">
            <el-select v-model="form.templateId" placeholder="请选择模板" style="width: 260px">
              <el-option v-for="item in templateList" :key="item.id" :label="`${item.title} (#${item.id})`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="每人发放数量">
            <el-input-number v-model="form.count" :min="1" :max="20" />
          </el-form-item>
          <el-form-item>
            <span class="pcgrant-dialog__selected">已选 <b>{{ selectedUsers.length }}</b> 位用户</span>
          </el-form-item>
        </el-form>
      </div>

      <div class="pcgrant-dialog__filter">
        <el-form :inline="true" @submit.prevent>
          <el-form-item label="用户昵称">
            <el-input v-model="userFilters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="handleUserSearch" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleUserSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleUserSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        ref="userTableRef"
        v-loading="userLoading"
        :data="userTableData"
        border
        max-height="380"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" fixed />
        <el-table-column prop="userId" label="编号" width="80" fixed />
        <el-table-column label="昵称" min-width="140">
          <template #default="{ row }">
            <span class="nick-name">{{ row.userName || '未设置' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="头像" width="82">
          <template #default="{ row }">
            <img :src="row.avatar || DEFAULT_USER_AVATAR" alt="头像" class="avatar-image" />
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="140">
          <template #default="{ row }">{{ row.phone || '未绑定' }}</template>
        </el-table-column>
        <el-table-column label="性别" width="90">
          <template #default="{ row }">{{ sexText(row.sex) }}</template>
        </el-table-column>
        <el-table-column label="用户类型" width="110">
          <template #default="{ row }">{{ userTypeText(row.userType) }}</template>
        </el-table-column>
        <el-table-column prop="invitationCode" label="邀请码" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
      </el-table>
      <BasePagination
        :current-page="userPageNum"
        :page-size="userPageSize"
        :page-sizes="[10, 20, 50]"
        :total="userTotal"
        @update:currentPage="handleUserPageChange"
        @update:pageSize="handleUserPageSizeChange"
      />

      <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitGrant">确认发券</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pcgrant {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pcgrant-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pcgrant-hero__badge {
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

.pcgrant-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pcgrant-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pcgrant-filter.nf-filter-card {
  margin-bottom: 0;
}

.pcgrant-filter__form {
  margin-bottom: 0;
}

.pcgrant-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pcgrant-table.nf-table-card {
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

.pcgrant-table__alert {
  margin-bottom: 12px;
}

.pcgrant-dialog__top {
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--nf-border);
  margin-bottom: 12px;
}

.pcgrant-dialog__form {
  margin-bottom: 0;
}

.pcgrant-dialog__selected {
  color: var(--nf-text-secondary);
  font-size: 13px;

  b {
    color: var(--nf-brand-deep);
    font-size: 15px;
    margin: 0 2px;
  }
}

.pcgrant-dialog__filter {
  margin-bottom: 12px;

  :deep(.el-input) {
    width: 200px;
  }
}

.nick-name {
  color: #f56c6c;
}

.avatar-image {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}
</style>

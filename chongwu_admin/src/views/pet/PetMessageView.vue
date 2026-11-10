<script setup>
import { reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import UserFilterFields from '@/components/common/UserFilterFields.vue'
import {
  broadcastPetMessageApi,
  deletePetMessageApi,
  getPetMessagePageApi,
} from '@/api/modules/pet-message'
import { getUserPageApi } from '@/api/modules/users'
import { useRouteUserFilter } from '@/utils/user-filter'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const { filters, appendUserFilter } = useRouteUserFilter()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const typeFilter = ref('')

const form = reactive({
  phone: '',
  title: '',
  content: '',
  url: '',
  dataId: '',
})

const typeOptions = [
  { label: '全部类型', value: '' },
  { label: '系统公告', value: 1 },
  { label: '互动消息', value: 2 },
]

function typeLabel(type) {
  if (type === 1) return '系统公告'
  if (type === 2) return '互动消息'
  return String(type ?? '-')
}

function readLabel(isRead) {
  return Number(isRead) === 1 ? '已读' : '未读'
}

async function loadData() {
  loading.value = true
  try {
    const params = appendUserFilter({ page: pageNum.value, limit: pageSize.value })
    if (params.username) {
      params.userName = params.username
      delete params.username
    }
    if (typeFilter.value !== '') params.type = Number(typeFilter.value)
    const data = await getPetMessagePageApi(params)
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    showError(error?.msg || error?.message || '加载消息失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function openBroadcast() {
  Object.assign(form, {
    phone: filters.phone || '',
    title: '',
    content: '',
    url: '',
    dataId: '',
  })
  dialogVisible.value = true
}

async function resolveUserIdByPhone(phone) {
  const data = await getUserPageApi({ phone: String(phone).trim(), page: 1, limit: 1 })
  return data.list?.[0]?.userId || null
}

async function submitBroadcast() {
  if (!form.phone.trim()) {
    showError('请输入手机号')
    return
  }
  if (!form.title.trim()) {
    showError('请输入标题')
    return
  }
  if (!form.content.trim()) {
    showError('请输入内容')
    return
  }
  dialogLoading.value = true
  try {
    const userId = await resolveUserIdByPhone(form.phone)
    if (!userId) {
      showError('未找到该手机号对应用户')
      return
    }
    await broadcastPetMessageApi({
      userId,
      title: form.title.trim(),
      content: form.content.trim(),
      url: form.url.trim() || undefined,
      dataId: form.dataId ? Number(form.dataId) : undefined,
    })
    showSuccess('公告已发送')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '发送失败')
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction('确认删除该消息？')
  try {
    await deletePetMessageApi(row.id)
    showSuccess('已删除')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

loadData()
</script>

<template>
  <div class="pmsg">
    <header class="pmsg-hero nf-page-hero">
      <span class="pmsg-hero__badge">宠物业务</span>
      <h1 class="pmsg-hero__title">消息管理</h1>
      <p class="pmsg-hero__desc">查看用户系统公告与互动消息，支持筛选、发送公告与删除</p>
    </header>

    <div class="pmsg-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="pmsg-filter__form" @submit.prevent>
        <UserFilterFields v-model:phone="filters.phone" v-model:username="filters.username" @search="handleSearch" />
        <el-form-item label="消息类型">
          <el-select v-model="typeFilter" placeholder="消息类型" style="width: 140px">
            <el-option v-for="item in typeOptions" :key="item.label" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="pmsg-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button v-if="isAuth('pet:message:send')" type="success" @click="openBroadcast">发送系统公告</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="pmsg-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="dataId" label="关联ID" width="90" />
        <el-table-column prop="isRead" label="状态" width="80">
          <template #default="{ row }">{{ readLabel(row.isRead) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="isAuth('pet:message:delete')"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination
        v-model:page="pageNum"
        v-model:limit="pageSize"
        :total="total"
        @change="loadData"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="发送系统公告" width="560px" class="nf-pro-dialog" align-center>
<el-form label-width="96px">
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="接收用户手机号" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" maxlength="50" show-word-limit /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" maxlength="200" show-word-limit /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.url" placeholder="可选，如 /packageA/post/detail?id=9001" /></el-form-item>
        <el-form-item label="关联ID"><el-input v-model="form.dataId" placeholder="可选 dataId" /></el-form-item>
      </el-form>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitBroadcast">发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pmsg {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.pmsg-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pmsg-hero__badge {
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

.pmsg-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.pmsg-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.pmsg-filter.nf-filter-card {
  margin-bottom: 0;
}

.pmsg-filter__form {
  margin-bottom: 0;

  :deep(.el-select) {
    width: 140px;
  }
}

.pmsg-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.pmsg-table.nf-table-card {
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
</style>

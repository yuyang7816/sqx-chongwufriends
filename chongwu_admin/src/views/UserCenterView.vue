<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getUsers, saveUsers } from '@/mock/users'

const router = useRouter()
const authStore = useAuthStore()
const filters = reactive({ keyword: '', status: '', department: '' })
const users = ref(getUsers())
const page = ref(1)
const pageSize = ref(5)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const form = reactive({
  name: '',
  phone: '',
  email: '',
  status: '启用',
  department: '',
  balance: 0,
  memberLevel: '普通会员',
  inviteCode: '',
  source: '自然注册',
  inviteCount: 0,
  totalSpend: 0,
  createdAt: '',
  openid: '',
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  department: [{ required: true, message: '请输入部门', trigger: 'blur' }],
}

const filteredUsers = computed(() => {
  return users.value.filter((item) => {
    const keyword = filters.keyword.trim()
    const matchKeyword = !keyword || [item.name, item.phone, item.email].some((field) => field.includes(keyword))
    const matchStatus = !filters.status || item.status === filters.status
    const matchDepartment = !filters.department || item.department.toLowerCase().includes(filters.department.toLowerCase())
    return matchKeyword && matchStatus && matchDepartment
  })
})

const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

function syncUsers(nextUsers) {
  users.value = nextUsers
  saveUsers(nextUsers)
}

function resetForm() {
  form.name = ''
  form.phone = ''
  form.email = ''
  form.status = '启用'
  form.department = ''
  form.balance = 0
  form.memberLevel = '普通会员'
  form.inviteCode = ''
  form.source = '自然注册'
  form.inviteCount = 0
  form.totalSpend = 0
  form.createdAt = ''
  form.openid = ''
}

function handleSearchReset() {
  filters.keyword = ''
  filters.status = ''
  filters.department = ''
  page.value = 1
}

function openCreateDialog() {
  editingId.value = null
  resetForm()
  form.inviteCode = `SQX${Date.now().toString().slice(-5)}`
  form.createdAt = new Date().toLocaleString('zh-CN', { hour12: false })
  form.openid = `oYxA1cZ-${Date.now()}`
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

function goToDetail(row) {
  router.push(`/user-center/${row.id}`)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)

  if (!valid) {
    return
  }

  if (editingId.value) {
    const nextUsers = users.value.map((item) => {
      if (item.id === editingId.value) {
        return { ...item, ...form }
      }
      return item
    })
    syncUsers(nextUsers)
    ElMessage.success('用户信息已更新')
  } else {
    const nextUsers = [{ id: Date.now(), ...form }, ...users.value]
    syncUsers(nextUsers)
    page.value = 1
    ElMessage.success('用户已新增')
  }

  dialogVisible.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除用户“${row.name}”吗？`, '提示', { type: 'warning' })
  const nextUsers = users.value.filter((item) => item.id !== row.id)
  syncUsers(nextUsers)
  if (page.value > 1 && pagedUsers.value.length === 1) {
    page.value -= 1
  }
  ElMessage.success('用户已删除')
}
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <el-row justify="space-between" align="middle">
        <el-space direction="vertical" alignment="flex-start" :size="2">
          <el-text tag="b">用户档案</el-text>
          <el-text type="info">集中维护用户资料、生命周期状态与归属部门，让运营动作更加清晰可控。</el-text>
        </el-space>
        <el-tag>共 {{ filteredUsers.length }} 条</el-tag>
      </el-row>
    </template>

    <el-form :inline="true" :model="filters" style="margin-bottom: 16px;">
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" clearable placeholder="姓名 / 手机号 / 邮箱" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="请选择状态">
          <el-option label="启用" value="启用" />
          <el-option label="禁用" value="禁用" />
        </el-select>
      </el-form-item>
      <el-form-item label="部门">
        <el-input v-model="filters.department" clearable placeholder="请输入部门" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary">筛选</el-button>
          <el-button @click="handleSearchReset">重置</el-button>
          <el-button v-if="authStore.hasPermission('user:create')" type="success" @click="openCreateDialog">新增用户</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table :data="pagedUsers" border>
      <el-table-column label="姓名" min-width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="goToDetail(row)">{{ row.name }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="150" />
      <el-table-column prop="email" label="邮箱" min-width="220" />
      <el-table-column prop="department" label="部门" min-width="140" />
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-space>
            <el-button v-if="authStore.hasPermission('user:update')" link type="primary" @click="openEditDialog(row)">修改</el-button>
            <el-button v-if="authStore.hasPermission('user:delete')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <el-row justify="end" style="margin-top: 16px;">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :background="true"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[5, 10, 20]"
        :total="filteredUsers.length"
      />
    </el-row>

    <el-dialog v-model="dialogVisible" :title="editingId ? '修改用户' : '新增用户'" width="560px" class="nf-pro-dialog" align-center>
<el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="部门" prop="department"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="会员等级"><el-input v-model="form.memberLevel" /></el-form-item>
        <el-form-item label="用户来源"><el-input v-model="form.source" /></el-form-item>
        <el-form-item label="账户余额"><el-input-number v-model="form.balance" :min="0" :precision="2" style="width: 100%;" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="启用">启用</el-radio>
            <el-radio label="禁用">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </el-space>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getMenuListApi } from '@/api/modules/menus'
import { createRoleApi, deleteRoleApi, getRoleInfoApi, getRolePage, updateRoleApi } from '@/api/modules/roles'
import { validateForm } from '@/utils/form'
import { confirmAction, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const detailLoading = ref(false)
const formRef = ref()
const menuTreeRef = ref()
const tableData = ref([])
const tableSelections = ref([])
const menuTree = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const editingId = ref(0)

const filters = reactive({
  roleName: '',
})

const form = reactive({
  id: 0,
  roleName: '',
  remark: '',
})

const rules = {
  roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }],
}

const treeProps = {
  label: 'name',
  children: 'children',
}

function collectMenuIds(nodes = []) {
  const ids = []
  const walk = (list = []) => {
    list.forEach((node) => {
      if (node?.menuId != null) {
        ids.push(node.menuId)
      }
      if (node?.children?.length) {
        walk(node.children)
      }
    })
  }
  walk(nodes)
  return ids
}

function selectAllMenus() {
  menuTreeRef.value?.setCheckedKeys(collectMenuIds(menuTree.value))
}

function clearAllMenus() {
  menuTreeRef.value?.setCheckedKeys([])
}

function buildMenuTree(list = []) {
  const map = new Map()
  const roots = []

  list.forEach((item) => {
    map.set(item.menuId, {
      ...item,
      list: undefined,
      children: [],
    })
  })

  map.forEach((node) => {
    const parent = map.get(node.parentId)
    if (parent) {
      parent.children.push(node)
      return
    }
    roots.push(node)
  })

  const sortNodes = (nodes) => {
    nodes.sort((a, b) => {
      const orderDiff = Number(a.orderNum || 0) - Number(b.orderNum || 0)
      if (orderDiff !== 0) return orderDiff
      return Number(a.menuId || 0) - Number(b.menuId || 0)
    })
    nodes.forEach((node) => sortNodes(node.children || []))
    return nodes
  }

  return sortNodes(roots)
}

function resetForm() {
  Object.assign(form, {
    id: 0,
    roleName: '',
    remark: '',
  })
}

function normalizeRoleRow(item = {}) {
  return {
    ...item,
    roleId: item.roleId,
    roleName: item.roleName || '',
    remark: item.remark || '',
    createTime: item.createTime || '',
  }
}

function normalizeMenuIds(menuIdList = []) {
  if (!Array.isArray(menuIdList)) {
    return []
  }
  return menuIdList.filter((id) => Number(id) > 0)
}

function buildSubmitPayload() {
  const checked = menuTreeRef.value?.getCheckedKeys() || []
  const halfChecked = menuTreeRef.value?.getHalfCheckedKeys() || []
  const menuIdList = [...new Set([...checked, ...halfChecked].filter((id) => Number(id) > 0))]
  return {
    roleId: editingId.value || undefined,
    roleName: form.roleName.trim(),
    remark: form.remark.trim(),
    menuIdList,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getRolePage({
      page: pageNum.value,
      limit: pageSize.value,
      roleName: filters.roleName,
    })
    tableData.value = (data.list || []).map(normalizeRoleRow)
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
  filters.roleName = ''
  pageNum.value = 1
  loadData()
}

function handleSelectionChange(value) {
  tableSelections.value = value
}

async function loadMenuTree() {
  const menuList = await getMenuListApi()
  menuTree.value = buildMenuTree(menuList)
}

async function openCreate() {
  editingId.value = 0
  resetForm()
  dialogVisible.value = true
  detailLoading.value = true
  try {
    await loadMenuTree()
    await nextTick()
    formRef.value?.clearValidate()
    menuTreeRef.value?.setCheckedKeys([])
  } finally {
    detailLoading.value = false
  }
}

async function openEdit(row) {
  editingId.value = row.roleId
  resetForm()
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const [menus, detail] = await Promise.all([getMenuListApi(), getRoleInfoApi(row.roleId)])
    menuTree.value = buildMenuTree(menus)
    Object.assign(form, {
      id: detail.roleId || 0,
      roleName: detail.roleName || '',
      remark: detail.remark || '',
    })
    await nextTick()
    formRef.value?.clearValidate()
    menuTreeRef.value?.setCheckedKeys(normalizeMenuIds(detail.menuIdList))
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    const payload = buildSubmitPayload()
    if (editingId.value) {
      await updateRoleApi(payload)
      showSuccess('角色已更新')
    } else {
      await createRoleApi(payload)
      showSuccess('角色已新增')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  const roleIds = row ? [row.roleId] : tableSelections.value.map((item) => item.roleId)
  if (!roleIds.length) {
    return
  }
  const roleNames = row ? [row.roleName] : tableSelections.value.map((item) => item.roleName).filter(Boolean)
  const message = row
    ? `确认删除角色 ${row.roleName} 吗？`
    : `确认批量删除这些角色：${roleNames.join('、')} 吗？`
  await confirmAction(message)
  await deleteRoleApi(roleIds)
  showSuccess('操作成功')
  await loadData()
}

function handleDialogVisible(value) {
  dialogVisible.value = value
  if (!value) {
    editingId.value = 0
    resetForm()
    menuTreeRef.value?.setCheckedKeys([])
  }
}

onMounted(loadData)
</script>

<template>
  <div class="srole">
    <header class="srole-hero nf-page-hero">
      <span class="srole-hero__badge">系统管理</span>
      <h1 class="srole-hero__title">角色权限</h1>
      <p class="srole-hero__desc">配置后台角色与菜单授权，支持按角色名称筛选、批量删除与权限树分配</p>
    </header>

    <div class="srole-filter nf-filter-card">
      <div class="nf-filter-card__head">
        <span>筛选条件</span>
      </div>
      <el-form :inline="true" class="srole-filter__form" @submit.prevent>
        <el-form-item label="角色名称">
          <el-input v-model="filters.roleName" clearable placeholder="请输入角色名称" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item class="srole-filter__actions">
          <el-space wrap>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button v-if="isAuth('sys:role:save')" type="primary" @click="openCreate">新增角色</el-button>
            <el-button
              v-if="isAuth('sys:role:delete')"
              type="danger"
              :disabled="!tableSelections.length"
              @click="handleDelete()"
            >
              批量删除
            </el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </div>

    <div class="srole-table nf-table-card">
      <el-table v-loading="loading" :data="tableData" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="roleId" label="ID" width="80" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" align="center" />
        <el-table-column prop="remark" label="备注" min-width="220" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" align="center" />
        <el-table-column label="操作" fixed="right" min-width="150" align="center">
          <template #default="{ row }">
            <el-space>
              <el-button v-if="isAuth('sys:role:update')" size="small" type="primary" @click="openEdit(row)">修改</el-button>
              <el-button v-if="isAuth('sys:role:delete')" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 30, 50, 100]"
        :total="total"
        @update:current-page="pageNum = $event; loadData()"
        @update:page-size="pageSize = $event; pageNum = 1; loadData()"
      />
    </div>

    <AppDialog
      :model-value="dialogVisible"
      :title="editingId ? '修改角色' : '新增角色'"
      :loading="dialogLoading"
      width="720px"
      @update:modelValue="handleDialogVisible"
      @confirm="handleSubmit"
    >
      <div v-loading="detailLoading">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="form.roleName" placeholder="角色名称" maxlength="40" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" placeholder="备注" maxlength="120" />
          </el-form-item>
          <el-form-item label="授权">
            <div class="role-tree-toolbar">
              <el-button link type="primary" @click="selectAllMenus">全选</el-button>
              <el-button link type="primary" @click="clearAllMenus">取消全选</el-button>
            </div>
            <el-tree
              ref="menuTreeRef"
              class="role-menu-tree"
              :data="menuTree"
              :props="treeProps"
              node-key="menuId"
              show-checkbox
              default-expand-all
            />
          </el-form-item>
        </el-form>
      </div>
    </AppDialog>
  </div>
</template>

<style scoped lang="scss">
.srole {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.srole-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.srole-hero__badge {
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

.srole-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.srole-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.srole-filter.nf-filter-card {
  margin-bottom: 0;
}

.srole-filter__form {
  margin-bottom: 0;

  :deep(.el-input) {
    width: 180px;
  }
}

.srole-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.srole-table.nf-table-card {
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

.role-tree-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.role-menu-tree {
  width: 100%;
  max-height: 420px;
  padding: 12px;
  overflow: auto;
  border: 1px solid var(--nf-border);
  border-radius: 12px;
}
</style>

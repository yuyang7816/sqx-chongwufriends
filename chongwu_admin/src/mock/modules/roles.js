import { createModuleStorage, createPageResult } from '@/mock/database'

const storage = createModuleStorage('admin-roles', [
  { id: 3001, name: '超级管理员', code: 'SUPER_ADMIN', dataScope: 'ALL', status: 'enabled', userCount: 2, permissions: ['*'], remark: '平台最高权限角色' },
  { id: 3002, name: '运营主管', code: 'OPS_ADMIN', dataScope: 'DEPT_AND_CHILD', status: 'enabled', userCount: 5, permissions: ['user:view', 'category:view', 'decoration:view'], remark: '负责用户与运营内容' },
  { id: 3003, name: '财务主管', code: 'FINANCE_ADMIN', dataScope: 'DEPT', status: 'disabled', userCount: 2, permissions: ['user:view'], remark: '负责结算与财务核对' },
])

export function pageRoles(params) {
  const keyword = (params.keyword || '').trim()
  const list = storage.getAll().filter((item) => {
    const matchKeyword = !keyword || [item.name, item.code, item.remark].some((field) => field.includes(keyword))
    const matchStatus = !params.status || item.status === params.status
    return matchKeyword && matchStatus
  })
  return createPageResult(list, params.pageNum, params.pageSize)
}

export function createRole(payload) {
  const list = storage.getAll()
  list.unshift({ id: Date.now(), userCount: payload.userCount || 0, ...payload })
  storage.setAll(list)
}

export function updateRole(id, payload) {
  storage.setAll(storage.getAll().map((item) => (item.id === Number(id) ? { ...item, ...payload } : item)))
}

export function deleteRole(id) {
  storage.setAll(storage.getAll().filter((item) => item.id !== Number(id)))
}

import { createModuleStorage, createPageResult } from '@/mock/database'

const storage = createModuleStorage('admin-admins', [
  { id: 2001, name: '林越', account: 'super-admin', phone: '13900000001', roleCode: 'SUPER_ADMIN', roleName: '超级管理员', status: 'enabled', createdAt: '2026-05-01 10:00:00' },
  { id: 2002, name: '周衡', account: 'ops-lead', phone: '13900000002', roleCode: 'OPS_ADMIN', roleName: '运营主管', status: 'enabled', createdAt: '2026-05-02 10:00:00' },
  { id: 2003, name: '顾承安', account: 'finance-check', phone: '13900000003', roleCode: 'FINANCE_ADMIN', roleName: '财务主管', status: 'disabled', createdAt: '2026-05-03 10:00:00' },
])

export function pageAdmins(params) {
  const keyword = (params.keyword || '').trim()
  const list = storage.getAll().filter((item) => {
    const matchKeyword = !keyword || [item.name, item.account, item.phone, item.roleName].some((field) => field.includes(keyword))
    const matchStatus = !params.status || item.status === params.status
    const matchRoleCode = !params.roleCode || item.roleCode === params.roleCode
    return matchKeyword && matchStatus && matchRoleCode
  })
  return createPageResult(list, params.pageNum, params.pageSize)
}

export function createAdmin(payload) {
  const list = storage.getAll()
  list.unshift({ id: Date.now(), createdAt: new Date().toLocaleString('zh-CN', { hour12: false }), ...payload })
  storage.setAll(list)
}

export function updateAdmin(id, payload) {
  storage.setAll(storage.getAll().map((item) => (item.id === Number(id) ? { ...item, ...payload } : item)))
}

export function deleteAdmin(id) {
  storage.setAll(storage.getAll().filter((item) => item.id !== Number(id)))
}

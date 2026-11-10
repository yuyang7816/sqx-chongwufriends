import { createModuleStorage, createPageResult } from '@/mock/database'

const storage = createModuleStorage('admin-users', [
  { id: 1001, name: '陈知遥', phone: '13800000001', email: 'chen@example.com', department: '增长中心', memberLevel: '铂金会员', source: '抖音投放', balance: 2680.5, totalSpend: 18320, inviteCode: 'SQX8A21', status: 'enabled', createdAt: '2026-04-12 10:26:18', openid: 'openid-1001' },
  { id: 1002, name: '吴景衡', phone: '13800000002', email: 'wu@example.com', department: '运营中心', memberLevel: '黄金会员', source: '公众号裂变', balance: 305.8, totalSpend: 6280, inviteCode: 'SQX7K10', status: 'disabled', createdAt: '2026-03-18 15:08:42', openid: 'openid-1002' },
  { id: 1003, name: '赵清岚', phone: '13800000003', email: 'zhao@example.com', department: '平台架构', memberLevel: '白银会员', source: '自然注册', balance: 928.36, totalSpend: 2140, inviteCode: 'SQX6P33', status: 'enabled', createdAt: '2026-04-06 09:42:03', openid: 'openid-1003' },
  { id: 1004, name: '李沐川', phone: '13800000004', email: 'li@example.com', department: '财务管理', memberLevel: '黑金会员', source: '好友邀请', balance: 12880, totalSpend: 53200, inviteCode: 'SQX9M62', status: 'enabled', createdAt: '2026-01-25 18:20:11', openid: 'openid-1004' },
  { id: 1005, name: '孙以宁', phone: '13800000005', email: 'sun@example.com', department: '品牌市场', memberLevel: '普通会员', source: '自然注册', balance: 0, totalSpend: 380, inviteCode: 'SQX2D41', status: 'disabled', createdAt: '2026-05-09 12:03:27', openid: 'openid-1005' },
  { id: 1006, name: '高叙白', phone: '13800000006', email: 'gao@example.com', department: '风险控制', memberLevel: '黄金会员', source: '抖音投放', balance: 1460.2, totalSpend: 9640, inviteCode: 'SQX4Q28', status: 'enabled', createdAt: '2026-02-15 08:31:59', openid: 'openid-1006' },
])

export function pageUsers(params) {
  const keyword = (params.keyword || '').trim()
  const list = storage.getAll().filter((item) => {
    const matchKeyword = !keyword || [item.name, item.phone, item.email].some((field) => field.includes(keyword))
    const matchStatus = !params.status || item.status === params.status
    const matchDepartment = !params.department || item.department.includes(params.department)
    return matchKeyword && matchStatus && matchDepartment
  })
  return createPageResult(list, params.pageNum, params.pageSize)
}

export function getUserDetail(id) {
  return storage.getAll().find((item) => item.id === Number(id)) || null
}

export function createUser(payload) {
  const list = storage.getAll()
  list.unshift({ id: Date.now(), ...payload })
  storage.setAll(list)
}

export function updateUser(id, payload) {
  const list = storage.getAll().map((item) => (item.id === Number(id) ? { ...item, ...payload } : item))
  storage.setAll(list)
}

export function deleteUser(id) {
  storage.setAll(storage.getAll().filter((item) => item.id !== Number(id)))
}

export function toggleUserStatus(id, status) {
  updateUser(id, { status })
}

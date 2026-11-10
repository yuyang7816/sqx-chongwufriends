import { createModuleStorage, createPageResult } from '@/mock/database'

const storage = createModuleStorage('admin-categories', [
  { id: 6001, name: '新人专区', code: 'NEW_USER', type: 'activity', sort: 1, status: 'enabled', remark: '新用户承接分类' },
  { id: 6002, name: '会员福利', code: 'MEMBER_BENEFIT', type: 'member', sort: 2, status: 'enabled', remark: '会员权益分类' },
  { id: 6003, name: '增长专题', code: 'GROWTH_TOPIC', type: 'marketing', sort: 3, status: 'disabled', remark: '推广专题分类' },
])

export function pageCategories(params) {
  const keyword = (params.keyword || '').trim()
  const list = storage.getAll().filter((item) => {
    const matchKeyword = !keyword || [item.name, item.code, item.remark].some((field) => field.includes(keyword))
    const matchStatus = !params.status || item.status === params.status
    const matchType = !params.type || item.type === params.type
    return matchKeyword && matchStatus && matchType
  })
  return createPageResult(list.sort((a, b) => a.sort - b.sort), params.pageNum, params.pageSize)
}

export function createCategory(payload) {
  const list = storage.getAll()
  list.unshift({ id: Date.now(), ...payload })
  storage.setAll(list)
}

export function updateCategory(id, payload) {
  storage.setAll(storage.getAll().map((item) => (item.id === Number(id) ? { ...item, ...payload } : item)))
}

export function deleteCategory(id) {
  storage.setAll(storage.getAll().filter((item) => item.id !== Number(id)))
}

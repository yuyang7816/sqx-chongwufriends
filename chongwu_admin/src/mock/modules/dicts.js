import { createModuleStorage, createPageResult } from '@/mock/database'

const storage = createModuleStorage('admin-dicts', [
  { id: 5001, name: '用户状态', type: 'user_status', value: 'enabled', label: '启用', sort: 1, status: 'enabled', remark: '用户状态字典' },
  { id: 5002, name: '用户状态', type: 'user_status', value: 'disabled', label: '禁用', sort: 2, status: 'enabled', remark: '用户状态字典' },
  { id: 5003, name: '分类类型', type: 'category_type', value: 'activity', label: '平台活动', sort: 1, status: 'enabled', remark: '内容分类字典' },
])

export function pageDicts(params) {
  const keyword = (params.keyword || '').trim()
  const list = storage.getAll().filter((item) => {
    const matchKeyword = !keyword || [item.name, item.type, item.label, item.value].some((field) => field.includes(keyword))
    const matchStatus = !params.status || item.status === params.status
    const matchType = !params.type || item.type === params.type
    return matchKeyword && matchStatus && matchType
  })
  return createPageResult(list.sort((a, b) => a.sort - b.sort), params.pageNum, params.pageSize)
}

export function createDict(payload) {
  const list = storage.getAll()
  list.unshift({ id: Date.now(), ...payload })
  storage.setAll(list)
}

export function updateDict(id, payload) {
  storage.setAll(storage.getAll().map((item) => (item.id === Number(id) ? { ...item, ...payload } : item)))
}

export function deleteDict(id) {
  storage.setAll(storage.getAll().filter((item) => item.id !== Number(id)))
}

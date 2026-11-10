import { createModuleStorage, createPageResult } from '@/mock/database'

const storage = createModuleStorage('admin-menus', [
  { id: 4001, parentId: 0, title: '工作台', path: '/dashboard', permission: 'dashboard:view', type: 'menu', sort: 1, status: 'enabled' },
  { id: 4002, parentId: 0, title: '用户管理', path: '/users', permission: 'user:view', type: 'menu', sort: 2, status: 'enabled' },
  { id: 4003, parentId: 0, title: '系统管理', path: '/system', permission: '', type: 'catalog', sort: 3, status: 'enabled' },
  { id: 4004, parentId: 4003, title: '角色权限', path: '/system/roles', permission: 'role:view', type: 'menu', sort: 1, status: 'enabled' },
  { id: 4005, parentId: 4003, title: '菜单权限', path: '/system/menus', permission: 'menu:view', type: 'menu', sort: 2, status: 'enabled' },
  { id: 4006, parentId: 4003, title: '新增角色', path: '', permission: 'role:create', type: 'button', sort: 3, status: 'enabled' },
  { id: 4007, parentId: 0, title: '宠物管理', path: '/pet', permission: 'pet:view', type: 'catalog', sort: 4, status: 'enabled' },
  { id: 4008, parentId: 4007, title: '朋友圈动态', path: '/pet/community', permission: 'pet:community:view', type: 'menu', sort: 1, status: 'enabled' },
  { id: 4009, parentId: 4007, title: '投诉管理', path: '/pet/community/complaints', permission: 'pet:community:complaint:list', type: 'menu', sort: 2, status: 'enabled' },
  { id: 4010, parentId: 4007, title: '投诉类型', path: '/pet/community/complaint-types', permission: 'pet:community:complaint:type', type: 'menu', sort: 3, status: 'enabled' },
])

export function pageMenus(params) {
  const keyword = (params.keyword || '').trim()
  const list = storage.getAll().filter((item) => {
    const matchKeyword = !keyword || [item.title, item.path, item.permission].some((field) => field.includes(keyword))
    const matchStatus = !params.status || item.status === params.status
    return matchKeyword && matchStatus
  })
  return createPageResult(list.sort((a, b) => a.sort - b.sort), params.pageNum, params.pageSize)
}

export function getMenuOptions() {
  return storage.getAll().filter((item) => item.type !== 'button')
}

export function createMenu(payload) {
  const list = storage.getAll()
  list.unshift({ id: Date.now(), ...payload })
  storage.setAll(list)
}

export function updateMenu(id, payload) {
  storage.setAll(storage.getAll().map((item) => (item.id === Number(id) ? { ...item, ...payload } : item)))
}

export function deleteMenu(id) {
  storage.setAll(storage.getAll().filter((item) => item.id !== Number(id) && item.parentId !== Number(id)))
}

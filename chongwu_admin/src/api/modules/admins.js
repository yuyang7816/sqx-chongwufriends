import { get, post, resolveBusinessResponse } from '@/api/http'

export const adminApi = {
  list: '/sys/user/list',
  info: '/sys/user/info',
  create: '/sys/user/save',
  update: '/sys/user/update',
  delete: '/sys/user/delete',
  roleSelect: '/sys/role/select',
}

function resolveAdminListResponse(result) {
  if (Array.isArray(result)) {
    return { list: result, totalCount: result.length }
  }
  const data = result?.code === 0 || result?.code === 200 ? result.data || result : result
  return data?.page || data || {}
}

export function getAdminPage(params = {}) {
  return get(adminApi.list, params).then(resolveAdminListResponse).then((page) => ({
    list: page?.list || [],
    total: page?.totalCount || page?.total || 0,
  }))
}

export function getAdminInfoApi(id) {
  return get(`${adminApi.info}/${id}`).then(resolveBusinessResponse).then((data) => data?.user || data || {})
}

export function getRoleSelectApi() {
  return get(adminApi.roleSelect).then(resolveBusinessResponse).then((data) => data?.list || data || [])
}

export function createAdminApi(data) {
  return post(adminApi.create, data).then(resolveBusinessResponse)
}

export function updateAdminApi(data) {
  return post(adminApi.update, data).then(resolveBusinessResponse)
}

export function deleteAdminApi(userIds) {
  return post(adminApi.delete, userIds).then(resolveBusinessResponse)
}

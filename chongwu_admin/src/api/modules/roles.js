import { get, post, resolveBusinessResponse } from '@/api/http'

export const roleApi = {
  list: '/sys/role/list',
  info: '/sys/role/info',
  create: '/sys/role/save',
  update: '/sys/role/update',
  delete: '/sys/role/delete',
}

function resolveRoleListResponse(result) {
  if (Array.isArray(result)) {
    return { list: result, totalCount: result.length }
  }
  const data = result?.code === 0 || result?.code === 200 ? result.data || result : result
  return data?.page || data || {}
}

export function getRolePage(params = {}) {
  return get(roleApi.list, params).then(resolveRoleListResponse).then((page) => ({
    list: page?.list || [],
    total: page?.totalCount || page?.total || 0,
  }))
}

export function getRoleInfoApi(id) {
  return get(`${roleApi.info}/${id}`).then(resolveBusinessResponse).then((data) => data?.role || data || {})
}

export function createRoleApi(data) {
  return post(roleApi.create, data).then(resolveBusinessResponse)
}

export function updateRoleApi(data) {
  return post(roleApi.update, data).then(resolveBusinessResponse)
}

export function deleteRoleApi(roleIds) {
  return post(roleApi.delete, roleIds).then(resolveBusinessResponse)
}

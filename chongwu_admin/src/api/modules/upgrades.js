import { get, post, resolveBusinessResponse } from '@/api/http'

export const upgradeApi = {
  list: '/appinfo/list',
  save: '/appinfo/save',
  delete: '/appinfo/delete',
}

export function getUpgradePage(params = {}) {
  return get(upgradeApi.list, params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: data?.total || data?.totalCount || 0,
    }))
}

export function saveUpgradeApi(data) {
  return post(upgradeApi.save, data).then(resolveBusinessResponse)
}

export function deleteUpgradeApi(id) {
  return get(`${upgradeApi.delete}/${id}`).then(resolveBusinessResponse)
}

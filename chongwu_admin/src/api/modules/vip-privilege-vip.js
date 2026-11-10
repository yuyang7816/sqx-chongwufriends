import { get, post, resolveBusinessResponse } from '@/api/http'

export function getVipPrivilegeVipPageApi(params = {}) {
  return get('banner/selectBannerPage', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createVipPrivilegeVipApi(data = {}) {
  return post('banner/insertBanner', data).then(resolveBusinessResponse)
}

export function updateVipPrivilegeVipApi(data = {}) {
  return post('banner/updateBannerById', data).then(resolveBusinessResponse)
}

export function deleteVipPrivilegeVipApi(ids) {
  return get(`banner/deleteBannerById?ids=${ids}`).then(resolveBusinessResponse)
}

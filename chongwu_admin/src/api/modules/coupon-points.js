import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCouponPointsPageApi(params = {}) {
  return get('selfCoupon/list', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.content || data?.records || data?.list || [],
      total: Number(data?.totalElements || data?.totalCount || data?.total || 0),
    }))
}

export function createCouponPointsApi(data = {}) {
  return post('selfCoupon/save', data).then(resolveBusinessResponse)
}

export function updateCouponPointsApi(data = {}) {
  return post('selfCoupon/update', data).then(resolveBusinessResponse)
}

export function deleteCouponPointsApi(id) {
  return get('selfCoupon/delete', { id }).then(resolveBusinessResponse)
}

export function publishCouponPointsApi(data = {}) {
  return post('selfCouponIssue/save', data).then(resolveBusinessResponse)
}

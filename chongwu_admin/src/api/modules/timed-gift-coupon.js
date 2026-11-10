import { get, post, resolveBusinessResponse } from '@/api/http'

export function getTimedGiftCouponPageApi(params = {}) {
  return get('couponActivity/selectCouponActivityList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function createTimedGiftCouponApi(data = {}) {
  return post('couponActivity/save', data).then(resolveBusinessResponse)
}

export function updateTimedGiftCouponApi(data = {}) {
  return post('couponActivity/update', data).then(resolveBusinessResponse)
}

export function deleteTimedGiftCouponApi(couponActivityId) {
  return post('couponActivity/delete', null, { params: { couponActivityId } }).then(resolveBusinessResponse)
}

export function getTimedGiftCouponIssuePageApi(params = {}) {
  return get('selfCouponIssue/list', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.content || data?.records || data?.list || [],
      total: Number(data?.totalElements || data?.totalCount || data?.total || 0),
    }))
}

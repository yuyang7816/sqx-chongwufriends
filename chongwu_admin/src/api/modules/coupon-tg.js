import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCouponTgPageApi(params = {}) {
  return get('message/selectMessageByType', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function adminConversionCouponApi(params = {}) {
  return post('selfCouponUser/adminConversionCoupon', null, { params }).then(resolveBusinessResponse)
}

export function getCouponTgUserPageApi(params = {}) {
  return get('user/selectUserList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

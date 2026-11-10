import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCouponUserPageApi(params = {}) {
  return get('selfCouponUser/list', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.content || data?.records || data?.list || [],
      total: Number(data?.totalElements || data?.totalCount || data?.total || 0),
    }))
}

export function invalidateCouponUserApi(couponUserId) {
  return post('selfCouponUser/updateStatus', { couponUserId, status: 3 }).then(resolveBusinessResponse)
}

export function verifyCouponUserApi(cdkCode) {
  return post('selfCouponUser/verificationCoupon', null, { params: { userId: 0, cdkCode } }).then(resolveBusinessResponse)
}

export function getCouponUserTearoomOptionsApi() {
  return get('tearoom/selectTearoomList', {
    page: 1,
    limit: 1000,
    tearoomName: '',
    city: '',
    status: '',
  })
    .then(resolveBusinessResponse)
    .then((data) => data?.list || data?.records || [])
}

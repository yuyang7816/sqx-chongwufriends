import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCouponIssuePageApi(params = {}) {
  return get('selfCouponIssue/list', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.content || data?.records || data?.list || [],
      total: Number(data?.totalElements || data?.totalCount || data?.total || 0),
    }))
}

export function updateCouponIssueApi(data = {}) {
  return post('selfCouponIssue/update', data).then(resolveBusinessResponse)
}

export function deleteCouponIssueApi(id) {
  return get('selfCouponIssue/delete', { id }).then(resolveBusinessResponse)
}

export function sendCouponToUsersApi(params = {}) {
  return post('selfCouponUser/sendUserCoupon', null, { params }).then(resolveBusinessResponse)
}

export function sendCouponToInactiveUsersApi(params = {}) {
  return post('orders/sendOrdersUserCoupon', null, { params }).then(resolveBusinessResponse)
}

export function getCouponIssueUserPageApi(params = {}) {
  return get('user/selectUserList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function getCouponIssueVipOptionsApi() {
  return get('vip/selectVipList', {
    page: 1,
    limit: 20,
    vipName: '',
    isEnable: '',
  })
    .then(resolveBusinessResponse)
    .then((data) => data?.records || data?.list || [])
}

export function getCouponIssueTearoomOptionsApi() {
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

export function getCouponIssueGoodsOptionsApi(params = {}) {
  return get('goods/selectGoodsList', params)
    .then(resolveBusinessResponse)
    .then((data) => data?.list || data?.records || [])
}

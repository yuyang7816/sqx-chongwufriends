import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCouponMakerPageApi(params = {}) {
  return get('selfCoupon/list', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.content || data?.records || data?.list || [],
      total: Number(data?.totalElements || data?.totalCount || data?.total || 0),
    }))
}

export function createCouponMakerApi(data = {}) {
  return post('selfCoupon/save', data).then(resolveBusinessResponse)
}

export function updateCouponMakerApi(data = {}) {
  return post('selfCoupon/update', data).then(resolveBusinessResponse)
}

export function publishCouponMakerApi(data = {}) {
  return post('selfCouponIssue/save', data).then(resolveBusinessResponse)
}

export function getCouponMakerTearoomOptionsApi() {
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

export function getCouponMakerGoodsOptionsApi(params = {}) {
  return get('goods/selectGoodsList', params)
    .then(resolveBusinessResponse)
    .then((data) => data?.list || data?.records || [])
}

export function getCouponMakerVipCardOptionsApi() {
  return get('vipCard/selectVipCardList', { page: 1, limit: 20 })
    .then(resolveBusinessResponse)
    .then((data) => data?.records || data?.list || [])
}

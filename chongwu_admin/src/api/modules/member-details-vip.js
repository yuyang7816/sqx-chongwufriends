import { get, post, resolveBusinessResponse } from '@/api/http'

export function getMemberVipCardPageApi(params = {}) {
  return get('vipCard/selectVipCardList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalElements || data?.totalCount || 0),
    }))
}

export function createMemberVipCardApi(data = {}) {
  return post('vipCard/insertVipCard', data).then(resolveBusinessResponse)
}

export function updateMemberVipCardApi(data = {}) {
  return post('vipCard/updateVipCard', data).then(resolveBusinessResponse)
}

export function deleteMemberVipCardApi(vipCardId) {
  return post('vipCard/deleteVipCard', null, { params: { vipCardId } }).then(resolveBusinessResponse)
}

export function getMemberVipCardCouponPageApi(params = {}) {
  return get('selfCouponIssue/list', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.content || data?.records || data?.list || [],
      total: Number(data?.totalElements || data?.totalCount || data?.total || 0),
    }))
}

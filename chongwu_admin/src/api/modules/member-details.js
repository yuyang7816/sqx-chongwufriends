import { get, post, resolveBusinessResponse } from '@/api/http'

export function getMemberVipPageApi(params = {}) {
  return get('vip/selectVipList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function createMemberVipApi(data = {}) {
  return post('vip/insertVip', data).then(resolveBusinessResponse)
}

export function updateMemberVipApi(data = {}) {
  return post('vip/updateVip', data).then(resolveBusinessResponse)
}

export function deleteMemberVipApi(vipId) {
  return post('vip/deleteVip', null, { params: { vipId } }).then(resolveBusinessResponse)
}

export function getMemberPrivilegePageApi(params = {}) {
  return get('vipPrivilege/selectVipPrivilegeList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function createMemberPrivilegeApi(data = {}) {
  return post('vipPrivilege/insertVipPrivilege', data).then(resolveBusinessResponse)
}

export function updateMemberPrivilegeApi(data = {}) {
  return post('vipPrivilege/updateVipPrivilege', data).then(resolveBusinessResponse)
}

export function deleteMemberPrivilegeApi(vipPrivilegeId) {
  return post('vipPrivilege/deleteVipPrivilege', null, { params: { vipPrivilegeId } }).then(resolveBusinessResponse)
}

export function getMemberCouponIssuePageApi(params = {}) {
  return get('selfCouponIssue/list', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.content || data?.records || data?.list || [],
      total: Number(data?.totalElements || data?.totalCount || data?.total || 0),
    }))
}

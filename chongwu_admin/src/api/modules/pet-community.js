import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/community'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetCommunityPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getPetCommunityInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

export function getPetCommunityStatsApi() {
  return post(`${PREFIX}/stats`, {}).then(resolveBusinessResponse)
}

export function getPetCommunityConfigApi() {
  return post(`${PREFIX}/config`, {}).then(resolveBusinessResponse)
}

export function savePetCommunityConfigApi(data = {}) {
  return post(`${PREFIX}/config/save`, data).then(resolveBusinessResponse)
}

export function auditPetCommunityApi(data = {}) {
  return post(`${PREFIX}/audit`, data).then(resolveBusinessResponse)
}

export function batchAuditPetCommunityApi(data = {}) {
  return post(`${PREFIX}/audit/batch`, data).then(resolveBusinessResponse)
}

export function offlinePetCommunityApi(data = {}) {
  return post(`${PREFIX}/offline`, data).then(resolveBusinessResponse)
}

export function deletePetCommunityApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

export function getPetCommunityCommentPageApi(params = {}) {
  return post(`${PREFIX}/comment/page`, {
    page: params.page || 1,
    limit: params.limit || 20,
    postId: params.postId,
    ...params,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function deletePetCommunityCommentApi(id) {
  return post(`${PREFIX}/comment/delete`, { id }).then(resolveBusinessResponse)
}

export function getPetCommunityLikePageApi(params = {}) {
  return post(`${PREFIX}/like/page`, {
    page: params.page || 1,
    limit: params.limit || 20,
    postId: params.postId,
    ...params,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getPetCommunityComplaintPageApi(params = {}) {
  return post(`${PREFIX}/complaint/page`, {
    page: params.page || 1,
    limit: params.limit || 20,
    ...params,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function handlePetCommunityComplaintApi(data = {}) {
  return post(`${PREFIX}/complaint/handle`, data).then(resolveBusinessResponse)
}

/** @deprecated 请使用 getPetCommunityComplaintPageApi */
export function getPetCommunityReportPageApi(params = {}) {
  return getPetCommunityComplaintPageApi(params)
}

/** @deprecated 请使用 handlePetCommunityComplaintApi */
export function handlePetCommunityReportApi(data = {}) {
  return handlePetCommunityComplaintApi(data)
}

import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/coupon'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetCouponPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function createPetCouponApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetCouponApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetCouponApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

export function grantPetCouponApi(data = {}) {
  return post(`${PREFIX}/grant`, data).then(resolveBusinessResponse)
}

export function grantPetCouponBatchApi(data = {}) {
  return post(`${PREFIX}/grant/batch`, data).then(resolveBusinessResponse)
}

export function getPetUserCouponPageApi(params = {}) {
  return post(`${PREFIX}/user/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

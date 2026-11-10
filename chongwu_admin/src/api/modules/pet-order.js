import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/mall/order'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetOrderPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getPetOrderInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

export function cancelPetOrderApi(data = {}) {
  return post(`${PREFIX}/cancel`, data).then(resolveBusinessResponse)
}

/** 退款接口占位，待后端实现 */
export function refundPetOrderApi(data = {}) {
  return post(`${PREFIX}/refund`, data).then(resolveBusinessResponse)
}

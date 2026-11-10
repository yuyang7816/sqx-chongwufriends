import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/exchange'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetExchangePageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function updatePetExchangeStatusApi(data = {}) {
  return post(`${PREFIX}/status`, data).then(resolveBusinessResponse)
}

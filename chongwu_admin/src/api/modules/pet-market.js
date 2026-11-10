import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/island/market'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetMarketPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function savePetMarketApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetMarketApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetMarketApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

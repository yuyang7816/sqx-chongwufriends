import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/island'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetIslandPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function updatePetIslandApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function getPetIslandInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

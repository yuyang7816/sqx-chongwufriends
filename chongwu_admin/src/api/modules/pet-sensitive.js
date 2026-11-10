import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/sensitive'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetSensitivePageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function savePetSensitiveApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetSensitiveApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetSensitiveApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

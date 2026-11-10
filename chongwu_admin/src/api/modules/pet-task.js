import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/island/task'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetTaskPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function savePetTaskApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetTaskApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetTaskApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

export function getPetTaskInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/breed'

export function getPetBreedPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || [],
      total: Number(data?.total || 0),
    }))
}

export function getPetBreedListApi(petType = '') {
  return post(`${PREFIX}/list`, petType ? { petType } : {}).then(resolveBusinessResponse)
}

export function savePetBreedApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetBreedApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetBreedApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

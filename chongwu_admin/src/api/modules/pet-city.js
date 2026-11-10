import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/city'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetCityPageApi(params = {}) {
  return post(`${PREFIX}/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
  }).then(resolveBusinessResponse).then(toPageList)
}

export function savePetCityApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetCityApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetCityApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

export function getPetCityInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

export async function listPetCitiesApi() {
  const data = await getPetCityPageApi({ page: 1, limit: 200 })
  return data.list || []
}

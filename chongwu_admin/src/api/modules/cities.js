import { get, post, resolveBusinessResponse } from '@/api/http'

export const cityApi = {
  list: 'city/selectCityList',
  create: 'city/insertCity',
  update: 'city/updateCity',
  delete: 'city/deleteCity',
}

function normalizePageResult(data) {
  if (Array.isArray(data)) {
    return { list: data, total: data.length }
  }
  if (Array.isArray(data?.list)) {
    return {
      list: data.list,
      total: Number(data.total || data.totalCount || data.list.length || 0),
    }
  }
  if (Array.isArray(data?.records)) {
    return {
      list: data.records,
      total: Number(data.total || data.totalCount || data.records.length || 0),
    }
  }
  return { list: [], total: 0 }
}

export function getCityPage(params = {}) {
  return get(cityApi.list, params)
    .then(resolveBusinessResponse)
    .then(normalizePageResult)
}

export function createCityApi(data) {
  return post(cityApi.create, data).then(resolveBusinessResponse)
}

export function updateCityApi(data) {
  return post(cityApi.update, data).then(resolveBusinessResponse)
}

export function deleteCityApi(cityId) {
  return post(cityApi.delete, null, { params: { cityId } }).then(resolveBusinessResponse)
}

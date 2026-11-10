import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/mall'

export function getCategoryPage(params = {}) {
  return post(`${PREFIX}/category/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || 0),
    }))
    .catch(() => ({ list: [], total: 0 }))
}

export function createCategoryApi(data) {
  return post(`${PREFIX}/category/save`, data).then(resolveBusinessResponse)
}

export function updateCategoryApi(id, data) {
  return post(`${PREFIX}/category/update`, { ...data, id }).then(resolveBusinessResponse)
}

export function deleteCategoryApi(id) {
  return post(`${PREFIX}/category/delete`, { id }).then(resolveBusinessResponse)
}

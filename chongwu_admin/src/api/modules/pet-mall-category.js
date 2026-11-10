import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/mall/category'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getMallCategoryPageApi(params = {}) {
  return post(`${PREFIX}/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
    name: params.name || '',
    status: params.status !== '' && params.status !== undefined ? params.status : undefined,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getMallCategoryListApi() {
  return post(`${PREFIX}/list`, {}).then(resolveBusinessResponse)
}

export function createMallCategoryApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updateMallCategoryApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deleteMallCategoryApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

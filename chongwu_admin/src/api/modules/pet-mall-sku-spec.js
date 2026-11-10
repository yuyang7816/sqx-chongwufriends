import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/mall/sku-spec'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getMallSkuSpecPageApi(params = {}) {
  return post(`${PREFIX}/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
    keyword: params.keyword || '',
    status: params.status !== '' && params.status !== undefined ? params.status : undefined,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getMallSkuSpecListApi() {
  return post(`${PREFIX}/list`, {}).then(resolveBusinessResponse)
}

export function saveMallSkuSpecApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updateMallSkuSpecApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deleteMallSkuSpecApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

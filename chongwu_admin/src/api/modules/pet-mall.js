import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/mall'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetMallSpuPageApi(params = {}) {
  return post(`${PREFIX}/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
    keyword: params.keyword || '',
    categoryId: params.categoryId || undefined,
    bizType: params.bizType !== '' && params.bizType !== undefined ? params.bizType : undefined,
    status: params.status !== '' && params.status !== undefined ? params.status : undefined,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function savePetMallSpuApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetMallSpuApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetMallSpuApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

export function getPetMallSpuInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

const SKU_PREFIX = 'admin/pet/mall/sku'

export function getPetMallSkuPageApi(params = {}) {
  return post(`${SKU_PREFIX}/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
    spuId: params.spuId,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function savePetMallSkuApi(data = {}) {
  return post(`${SKU_PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetMallSkuApi(data = {}) {
  return post(`${SKU_PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetMallSkuApi(id) {
  return post(`${SKU_PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

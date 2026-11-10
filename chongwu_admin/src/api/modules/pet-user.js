import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/asset'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetUserAssetPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getPetUserLedgerPageApi(params = {}) {
  return post(`${PREFIX}/ledger/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getPetUserAssetByUserIdApi(userId) {
  return post(`${PREFIX}/infoByUserId`, { userId })
    .then(resolveBusinessResponse)
}

export function adjustPetUserAssetApi(params) {
  return post(`${PREFIX}/adjust`, params).then(resolveBusinessResponse)
}


import { get, post, resolveBusinessResponse } from '@/api/http'

export function getIntegralGoodsPageApi(params = {}) {
  return get('payClassify/selectPayClassifyList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createIntegralGoodsApi(data = {}) {
  return post('payClassify/insertPayClassify', data).then(resolveBusinessResponse)
}

export function updateIntegralGoodsApi(data = {}) {
  return post('payClassify/updatePayClassify', data).then(resolveBusinessResponse)
}

export function deleteIntegralGoodsApi(payClassifyId) {
  return post(`payClassify/deletePayClassify/?payClassifyId=${payClassifyId}`, null).then(resolveBusinessResponse)
}

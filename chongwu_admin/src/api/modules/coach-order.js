import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCoachOrderPageApi(params = {}) {
  return get('helpOrder/selectHelpOrderList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function cancelCoachOrderApi(helpOrderId) {
  return post('helpOrder/cancelHelpOrder', null, { params: { helpOrderId } }).then(resolveBusinessResponse)
}

export function accomplishCoachOrderApi(helpOrderId) {
  return post('helpOrder/accomplishHelpOrder', null, { params: { helpOrderId } }).then(resolveBusinessResponse)
}

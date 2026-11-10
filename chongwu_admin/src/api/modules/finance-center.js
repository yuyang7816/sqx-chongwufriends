import { get, post, resolveBusinessResponse } from '@/api/http'

export function getWithdrawPageApi(params = {}) {
  return get('cash/selectPayDetails', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function exportWithdrawPageApi(params = {}) {
  return get('cash/excelPayDetails', params, { responseType: 'blob' })
}

export function transferWithdrawApi(id) {
  return post(`cash/alipay/${id}`, {}).then(resolveBusinessResponse)
}

export function offlineTransferWithdrawApi(cashOutIds) {
  return post('cash/outCash', null, { params: { cashOutIds } }).then(resolveBusinessResponse)
}

export function rejectWithdrawApi(cashId, reason) {
  return post(`cash/refund/${cashId}/${encodeURIComponent(reason)}`, {}).then(resolveBusinessResponse)
}

export function getWithdrawStatsApi(params = {}) {
  return get('cash/statisticsCashMoney', params).then(resolveBusinessResponse)
}

export function getPaymentStatsApi(params = {}) {
  return get('cash/payMoney', params).then(resolveBusinessResponse)
}

export function getRechargePageApi(params = {}) {
  return get('cash/selectUserRecharge', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function refundRechargeApi(params = {}) {
  return post('cash/refundMoney', null, { params }).then(resolveBusinessResponse)
}

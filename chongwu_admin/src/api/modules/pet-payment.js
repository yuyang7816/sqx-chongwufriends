import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/payment'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

function fetchCallbackLogPage(body) {
  return post(`${PREFIX}/page`, body, { silentError: true })
    .then(resolveBusinessResponse)
    .then(toPageList)
    .catch(() =>
      post(`${PREFIX}/pay_callback_log/page`, body)
        .then(resolveBusinessResponse)
        .then(toPageList),
    )
}

export function getPaymentCallbackLogPageApi(params = {}) {
  const body = { page: params.page || 1, limit: params.limit || 10, ...params }
  return fetchCallbackLogPage(body)
}

export function getPaymentOrderPageApi(params = {}) {
  return post(`${PREFIX}/order/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

import { get, resolveBusinessResponse } from '@/api/http'

export function getConsumeRankingPageApi(params = {}) {
  return get('orders/selectOrdersUserMoneyList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

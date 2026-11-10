import { get, resolveBusinessResponse } from '@/api/http'

export function getOpenRankingPageApi(params = {}) {
  return get('orders/selectUserByOrdersHourList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function getOpenRankingTearoomOptionsApi() {
  return get('tearoom/selectTearoomList', {
    page: -1,
    limit: 50,
    tearoomName: '',
    userName: '',
    city: '',
    status: '',
  })
    .then(resolveBusinessResponse)
    .then((data) => data?.list || data?.records || [])
}

import { get, resolveBusinessResponse } from '@/api/http'

export function getIntegralDetailsPageApi(params = {}) {
  return get('integral/selectIntegralDetailsList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

import { get, resolveBusinessResponse } from '@/api/http'

export function getTransferGiftPageApi(params = {}) {
  return get('Details/queryUserMoneyDetails', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function getTransferGiftStatsApi(params = {}) {
  return get('Details/selectGiveTakeMoneyCount', params).then(resolveBusinessResponse)
}

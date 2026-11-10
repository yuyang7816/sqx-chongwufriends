import { get, resolveBusinessResponse } from '@/api/http'

export function getExchangeRecordPageApi(params = {}) {
  return get('cash/selectUserRechargeByUserId', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function getExchangeStatsApi(params = {}) {
  return get('cash/payUserMoney', params).then(resolveBusinessResponse)
}

import { get, resolveBusinessResponse } from '@/api/http'

export function getMerchantApplicationPageApi(params = {}) {
  return get('apply/selectApplyList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

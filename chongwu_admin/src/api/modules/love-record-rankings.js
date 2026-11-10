import { get, resolveBusinessResponse } from '@/api/http'

export function getLoveRecordRankingPageApi(params = {}) {
  return get('welfareMoney/selectWelfareCountList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function getLoveRecordTearoomOptionsApi() {
  return get('tearoom/selectTearoomList', {
    page: 1,
    limit: 1000,
    tearoomName: '',
    city: '',
    status: '',
  })
    .then(resolveBusinessResponse)
    .then((data) => data?.list || data?.records || [])
}

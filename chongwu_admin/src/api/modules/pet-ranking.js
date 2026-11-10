import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/ranking'

function toPageList(data) {
  const list = data?.records || data?.list || []
  return {
    list: Array.isArray(list) ? list : [],
    total: Number(data?.total ?? data?.totalCount ?? 0),
  }
}

export function getPetRankingPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getPetRankingPeriodsApi() {
  return post(`${PREFIX}/periods`, {}).then(resolveBusinessResponse)
}

export function getPetRankingRulesApi() {
  return post(`${PREFIX}/rules`, {}).then(resolveBusinessResponse)
}

export function updatePetRankingRulesApi(rules) {
  return post(`${PREFIX}/rules/update`, { rules }).then(resolveBusinessResponse)
}

export function getPetRankingAssistSettingsApi() {
  return post(`${PREFIX}/assist/settings`, {}).then(resolveBusinessResponse)
}

export function updatePetRankingAssistSettingsApi(data) {
  return post(`${PREFIX}/assist/settings/update`, data).then(resolveBusinessResponse)
}

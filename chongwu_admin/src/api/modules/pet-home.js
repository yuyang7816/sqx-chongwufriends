import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/home'

export function getHomeSocialInfoApi() {
  return post(`${PREFIX}/social/info`, {}).then(resolveBusinessResponse)
}

export function updateHomeSocialApi(data = {}) {
  return post(`${PREFIX}/social/update`, data).then(resolveBusinessResponse)
}

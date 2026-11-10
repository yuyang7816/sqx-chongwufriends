import { post, resolveBusinessResponse } from '@/api/http'
import { getUploadApiUrl, toPublicFileUrl } from '@/utils/tool'

const PREFIX = 'admin/pet/feed'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetFeedOrderPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function updatePetFeedOrderApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function uploadPetFeedFeedbackApi(data = {}) {
  return post(`${PREFIX}/feedback/upload`, data).then(resolveBusinessResponse)
}

export function getPetFeedOrderInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

export function uploadFeedFileApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return post(getUploadApiUrl(), formData).then(resolveBusinessResponse).then((data) => toPublicFileUrl(data))
}

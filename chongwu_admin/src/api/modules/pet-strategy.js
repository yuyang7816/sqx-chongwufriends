import { post, resolveBusinessResponse } from '@/api/http'
import { getUploadApiUrl, toPublicFileUrl } from '@/utils/tool'

const PREFIX = 'admin/pet/strategy'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetStrategyPageApi(params = {}) {
  const payload = {
    page: params.page || 1,
    limit: params.limit || 10,
  }
  if (params.categoryId !== '' && params.categoryId != null) payload.categoryId = params.categoryId
  if (params.status !== '' && params.status != null) payload.status = params.status
  if (params.title) payload.title = params.title
  if (params.tag) payload.tag = params.tag
  return post(`${PREFIX}/page`, payload)
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getPetStrategyCategoriesApi() {
  return post(`${PREFIX}/categories`, {}).then(resolveBusinessResponse).then((data) => data || [])
}

export function savePetStrategyApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetStrategyApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetStrategyApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

export function getPetStrategyInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

export function uploadStrategyEditorImageApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return post(getUploadApiUrl(), formData).then(resolveBusinessResponse).then((data) => {
    const url = toPublicFileUrl(data)
    if (data && typeof data === 'object') {
      return { ...data, url, path: url }
    }
    return url
  })
}

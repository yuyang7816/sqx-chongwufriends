import { get, post, resolveBusinessResponse } from '@/api/http'
import { getUploadApiUrl, toPublicFileUrl } from '@/utils/tool'

const UPLOAD_URL = getUploadApiUrl()

export const systemConfigApi = {
  list: '/sys/config/list',
  update: '/sys/config/update',
  refresh: '/sys/config/refresh',
  uploadImage: UPLOAD_URL,
}

export function getSystemConfigListApi(condition) {
  return get(systemConfigApi.list, {}, { silentError: true })
    .then(resolveBusinessResponse)
    .then((groups) => {
      const list = Array.isArray(groups) ? groups : []
      if (!condition) {
        return list.flatMap((g) => (g.sysConfigEntityList || []).map((item) => ({
          id: item.id,
          type: item.paramKey,
          value: item.paramValue,
          min: item.remark || item.paramTypeDesc || '',
          max: '',
          remark: item.remark,
          createAt: item.createTime || '',
          conditionFrom: item.paramType,
          paramKey: item.paramKey,
          paramType: item.paramType,
          paramTypeDesc: item.paramTypeDesc,
        })))
      }
      const group = list.find((g) => g.paramType === condition)
      return (group?.sysConfigEntityList || []).map((item) => ({
        id: item.id,
        type: item.paramKey,
        value: item.paramValue,
        min: item.remark || item.paramTypeDesc || '',
        max: '',
        remark: item.remark,
        createAt: item.createTime || '',
        conditionFrom: item.paramType,
        paramKey: item.paramKey,
        paramType: item.paramType,
        paramTypeDesc: item.paramTypeDesc,
      }))
    })
}

export function updateSystemConfigApi(data) {
  return post(systemConfigApi.update, {
    id: data.id,
    paramKey: data.paramKey || data.type,
    paramValue: data.value ?? data.paramValue,
    status: '1',
    remark: data.remark || '',
    paramType: data.conditionFrom || data.paramType || 'xitong',
    paramTypeDesc: data.paramTypeDesc || '',
  }).then(resolveBusinessResponse)
}

export function refreshSystemConfigApi() {
  return get(systemConfigApi.refresh).then(resolveBusinessResponse)
}

export function uploadSystemConfigImageApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return post(systemConfigApi.uploadImage, formData).then(resolveBusinessResponse).then((data) => {
    const url = toPublicFileUrl(data)
    if (data && typeof data === 'object') {
      return { ...data, url, path: url }
    }
    return url
  })
}

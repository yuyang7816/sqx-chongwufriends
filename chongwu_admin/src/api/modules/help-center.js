import { get, post, resolveBusinessResponse } from '@/api/http'
import { getUploadApiUrl, toPublicFileUrl } from '@/utils/tool'

const UPLOAD_URL = getUploadApiUrl()

export const helpCenterApi = {
  classifyList: 'help/selectHelpClassifyList',
  createClassify: 'help/insertHelpClassify',
  updateClassify: 'help/updateHelpClassify',
  deleteClassify: 'help/deleteHelpClassify',
  articleList: 'help/selectHelpWordList',
  createArticle: 'help/insertHelpWord',
  updateArticle: 'help/updateHelpWord',
  deleteArticle: 'help/deleteHelpWord',
  uploadImage: UPLOAD_URL,
}

function normalizeListResult(data) {
  if (Array.isArray(data)) {
    return { list: data, total: data.length }
  }
  if (Array.isArray(data?.list)) {
    return {
      list: data.list,
      total: Number(data.total || data.totalCount || data.list.length || 0),
    }
  }
  if (Array.isArray(data?.records)) {
    return {
      list: data.records,
      total: Number(data.total || data.totalCount || data.records.length || 0),
    }
  }
  return { list: [], total: 0 }
}

export function getHelpClassifyPage(params = {}) {
  return get(helpCenterApi.classifyList, params)
    .then(resolveBusinessResponse)
    .then(normalizeListResult)
}

export function createHelpClassifyApi(data) {
  return post(helpCenterApi.createClassify, data).then(resolveBusinessResponse)
}

export function updateHelpClassifyApi(data) {
  return post(helpCenterApi.updateClassify, data).then(resolveBusinessResponse)
}

export function deleteHelpClassifyApi(helpClassifyId) {
  return post(helpCenterApi.deleteClassify, null, { params: { helpClassifyId } }).then(resolveBusinessResponse)
}

export function getHelpWordPage(params = {}) {
  return get(helpCenterApi.articleList, params)
    .then(resolveBusinessResponse)
    .then(normalizeListResult)
}

export function createHelpWordApi(data) {
  return post(helpCenterApi.createArticle, data).then(resolveBusinessResponse)
}

export function updateHelpWordApi(data) {
  return post(helpCenterApi.updateArticle, data).then(resolveBusinessResponse)
}

export function deleteHelpWordApi(helpWordId) {
  return post(helpCenterApi.deleteArticle, null, { params: { helpWordId } }).then(resolveBusinessResponse)
}

export function uploadHelpEditorImageApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return post(helpCenterApi.uploadImage, formData).then(resolveBusinessResponse).then((data) => {
    const url = toPublicFileUrl(data)
    if (data && typeof data === 'object') {
      return { ...data, url, path: url }
    }
    return url
  })
}

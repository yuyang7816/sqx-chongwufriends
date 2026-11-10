import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuthCache, getApiBaseUrl, toPublicFileUrl } from '@/utils/tool'

const service = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 12000,
})

let redirectingToLogin = false

// 后端 SystemErrorCode.UNAUTHORIZED 的文案，用于在后端未返回 401/50002 时兜底识别未登录。
const UNAUTHORIZED_MESSAGES = ['暂未登录或token已经过期', '登录已失效，请重新登录']
// 同一错误文案在短时间内只弹一次，避免并发请求叠弹框。
const recentErrorMessages = new Map()
const DEDUP_WINDOW = 1500

function isBusinessSuccess(data) {
  return data?.code === 0 || data?.code === 200 || data?.status === 0 || data?.status === 200
}

function isUnauthorized(data) {
  if (!data) return false
  if (data.code === 401 || data.status === 401 || data.code === 50002 || data.status === 50002) {
    return true
  }
  const msg = data.msg || data.message || ''
  return UNAUTHORIZED_MESSAGES.includes(msg)
}

function showErrorOnce(message) {
  const text = message || '业务请求失败'
  const now = Date.now()
  const last = recentErrorMessages.get(text) || 0
  if (now - last < DEDUP_WINDOW) {
    return
  }
  recentErrorMessages.set(text, now)
  ElMessage.error(text)
}

function handleUnauthorized(message) {
  clearAuthCache()
  if (!redirectingToLogin) {
    redirectingToLogin = true
    ElMessage.error(message || '登录已失效，请重新登录')
    const currentPath = window.location.pathname + window.location.search
    const redirect = currentPath.startsWith('/login') ? '/dashboard' : currentPath
    window.location.replace(`/login?redirect=${encodeURIComponent(redirect)}`)
    window.setTimeout(() => {
      redirectingToLogin = false
    }, 300)
  }
}

/** 远程上传接口若仍返回 localhost，统一改写成线上可访问地址 */
function rewriteUploadPayload(payload) {
  if (!payload || typeof payload !== 'object') return payload
  const fixed = toPublicFileUrl(payload)
  if (!fixed) return payload
  return {
    ...payload,
    path: fixed,
    url: fixed,
  }
}

function maybeRewriteUploadResponse(response) {
  const url = String(response?.config?.url || '')
  const isUpload = url.includes('file/upload') || url.includes('alioss/upload')
  if (!isUpload) return response
  const data = response?.data
  if (!data || typeof data !== 'object') return response
  if (data.data && typeof data.data === 'object') {
    data.data = rewriteUploadPayload(data.data)
  }
  return response
}

service.interceptors.request.use((config) => {
  const auth = JSON.parse(window.sessionStorage.getItem('admin-auth') || '{}')
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
    config.headers.token = auth.token
  }
  const isFormData = typeof FormData !== 'undefined' && config.data instanceof FormData
  if (isFormData) {
    delete config.headers['Content-Type']
    // 上传跨域线上域名时放宽超时
    config.timeout = Math.max(config.timeout || 0, 60000)
  } else if (!config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json'
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    maybeRewriteUploadResponse(response)
    const { data } = response
    if (typeof data?.code === 'number' || typeof data?.status === 'number') {
      if (isBusinessSuccess(data)) {
        return data
      }
      if (isUnauthorized(data)) {
        handleUnauthorized(data.msg || data.message || 'invalid token')
        return Promise.reject(data)
      }
      if (!response.config?.silentError) {
        showErrorOnce(data.msg || data.message || '业务请求失败')
      }
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    const resp = error.response?.data
    if (error.response?.status === 401 || isUnauthorized(resp)) {
      handleUnauthorized(resp?.msg || resp?.message || 'invalid token')
      return Promise.reject(error)
    }
    if (!error.config?.silentError) {
      showErrorOnce(resp?.msg || resp?.message || error.message || '网络请求失败')
    }
    return Promise.reject(error)
  },
)

export function get(url, params, config = {}) {
  return service.get(url, { params, ...config })
}

export function post(url, data, config = {}) {
  return service.post(url, data, config)
}

export function resolveBusinessResponse(result) {
  if (!isBusinessSuccess(result)) {
    return Promise.reject(result)
  }
  return Promise.resolve(result.data || result)
}

export default service

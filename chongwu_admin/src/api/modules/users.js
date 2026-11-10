import { get, post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/base/user'

function toPageList(data) {
  return {
    list: data?.records || data?.list || [],
    total: Number(data?.total || data?.totalCount || 0),
  }
}

function buildListPayload(params = {}) {
  const payload = {
    page: params.page || 1,
    limit: params.limit || 10,
  }
  const phone = String(params.phone || '').trim()
  const username = String(params.username || params.userName || '').trim()
  const invitationCode = String(params.invitationCode || params.inviterCode || '').trim()
  if (phone) payload.phone = phone
  if (username) payload.username = username
  if (invitationCode) payload.invitationCode = invitationCode
  if (params.userId) payload.userId = Number(params.userId)
  if (params.status !== '' && params.status !== null && params.status !== undefined) {
    payload.status = Number(params.status)
  }
  if (params.userType !== '' && params.userType !== null && params.userType !== undefined) {
    payload.userType = Number(params.userType)
  }
  return payload
}

export function getUserPageApi(params = {}) {
  return post(`${PREFIX}/list`, buildListPayload(params))
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getUserInfoApi(userId) {
  return get(`${PREFIX}/info`, { userId }).then(resolveBusinessResponse)
}

export function getUserOverviewApi(userId) {
  return get(`${PREFIX}/overview`, { userId }).then(resolveBusinessResponse)
}

export function updateUserApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deleteUserApi(userId) {
  return post(`${PREFIX}/delete`, { userId }).then(resolveBusinessResponse)
}

export function updateUserStatusApi(userId, status) {
  return updateUserApi({ userId, status: Number(status) })
}

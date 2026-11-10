import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/adopt'

export function getAdoptPageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || [],
      total: Number(data?.total || 0),
    }))
}

export function updateAdoptStatusApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function getAdoptInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

export function getAdoptVisitPageApi(params = {}) {
  const applicationId = params.applicationId ?? params.adoptId
  return post(`${PREFIX}/visit/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
    applicationId,
  })
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || [],
      total: Number(data?.total || 0),
    }))
}

export function saveAdoptVisitApi(data = {}) {
  return post(`${PREFIX}/visit/save`, data).then(resolveBusinessResponse)
}

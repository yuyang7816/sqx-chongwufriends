import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/community/complaint-type'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getComplaintTypePageApi(params = {}) {
  return post(`${PREFIX}/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
    name: params.name || '',
    status: params.status !== '' && params.status !== undefined ? params.status : undefined,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getComplaintTypeListApi() {
  return post(`${PREFIX}/list`, {}).then(resolveBusinessResponse)
}

export function createComplaintTypeApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updateComplaintTypeApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deleteComplaintTypeApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

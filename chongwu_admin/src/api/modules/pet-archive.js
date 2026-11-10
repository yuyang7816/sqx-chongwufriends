import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/archive'

export function getPetArchivePageApi(params = {}) {
  return post(`${PREFIX}/page`, { page: params.page || 1, limit: params.limit || 10, ...params })
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || [],
      total: Number(data?.total || 0),
    }))
}

export function getPetArchiveInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

export function savePetArchiveApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updatePetArchiveApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deletePetArchiveApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

export function getPetAlbumListApi(petId) {
  return post('admin/pet/archive/album/list', { id: petId }).then(resolveBusinessResponse)
}

export function savePetAlbumApi(data = {}) {
  return post('admin/pet/archive/album/save', data).then(resolveBusinessResponse)
}

export function auditPetArchiveApi(data = {}) {
  return post(`${PREFIX}/audit`, data).then(resolveBusinessResponse)
}

export function batchAuditPetArchiveApi(data = {}) {
  return post(`${PREFIX}/batchAudit`, data).then(resolveBusinessResponse)
}

import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCertificationPageApi(params = {}) {
  return get('userCertification/queryCertification', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function auditCertificationApi(params = {}) {
  return post('userCertification/auditorUserCertification', null, { params }).then(resolveBusinessResponse)
}

export function updateCertificationApi(data) {
  return post('userCertification/updateCertification', data).then(resolveBusinessResponse)
}

export function deleteCertificationApi(id) {
  return post('userCertification/deleteCertification', null, { params: { id } }).then(resolveBusinessResponse)
}

import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCoachServicePageApi(params = {}) {
  return get('helpService/selectHelpServiceList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createCoachServiceApi(data = {}) {
  return post('helpService/insertHelpService', data).then(resolveBusinessResponse)
}

export function updateCoachServiceApi(data = {}) {
  return post('helpService/updateHelpServiceById', data).then(resolveBusinessResponse)
}

export function updateCoachServiceStatusApi(data = {}) {
  return post('helpService/updateHelpServiceStatus', data).then(resolveBusinessResponse)
}

export function deleteCoachServiceApi(helpServiceId) {
  return post('helpService/deleteHelpService', null, { params: { helpServiceId } }).then(resolveBusinessResponse)
}

export function getCoachServiceSkuPageApi(params = {}) {
  return get('helpDetailsService/selectHelpServiceSkuList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createCoachServiceSkuApi(data = {}) {
  return post('helpDetailsService/insertHelpServiceSku', data).then(resolveBusinessResponse)
}

export function updateCoachServiceSkuApi(data = {}) {
  return post('helpDetailsService/updateHelpServiceSku', data).then(resolveBusinessResponse)
}

export function deleteCoachServiceSkuApi(helpServiceSkuId) {
  return post('helpDetailsService/deleteHelpServiceSku', null, { params: { helpServiceSkuId } }).then(resolveBusinessResponse)
}

export function getCoachServiceUnitOptionsApi() {
  return get('common/type/1168')
    .then(resolveBusinessResponse)
    .then((data) => String(data?.value || '').split(',').map((item) => item.trim()).filter(Boolean))
}

export function getCoachServiceVenueOptionsApi() {
  return get('tearoom/selectTearoomList', { page: 1, limit: 1000, tearoomName: '', city: '', status: '' })
    .then(resolveBusinessResponse)
    .then((data) => data?.list || data?.records || [])
}

import { get, post, resolveBusinessResponse } from '@/api/http'

export function getDisplayCabinetPageApi(params = {}) {
  return get('smallHardware/selectSmallHardwareList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createDisplayCabinetApi(data = {}) {
  return post('smallHardware/insertSmallHardware', data).then(resolveBusinessResponse)
}

export function updateDisplayCabinetApi(data = {}) {
  return post('smallHardware/updateSmallHardware', data).then(resolveBusinessResponse)
}

export function deleteDisplayCabinetApi(id) {
  return post('smallHardware/deleteSmallHardware', null, { params: { id } }).then(resolveBusinessResponse)
}

export function getSmallGoodsPageApi(params = {}) {
  return get('smallGoods/selectSmallGoodsList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function getVenueSimplePageApi(params = {}) {
  return get('tearoom/selectTearoomList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

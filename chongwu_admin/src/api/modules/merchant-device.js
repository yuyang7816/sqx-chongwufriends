import { get, post, resolveBusinessResponse } from '@/api/http'

export function getVenueDevicePageApi(params = {}) {
  return get('tearoom/selectTearoomList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function bindVenueGateDeviceApi(params = {}) {
  return post('tearoom/insertGoodsGate', null, { params }).then(resolveBusinessResponse)
}

export function getRoomGroupListApi(params = {}) {
  return get('room/selectGroup', params)
    .then(resolveBusinessResponse)
    .then((data) => data?.result || [])
}

export function getVenueMonitorListApi(params = {}) {
  return post('admin/hardware/get_all_hardware_list', null, { params })
    .then(resolveBusinessResponse)
    .then((data) => data || [])
}

export function toggleVenueRoomsApi(params = {}) {
  return post('goods/openOrCloseRoom', null, { params }).then(resolveBusinessResponse)
}

export function getGoodsDevicePageApi(params = {}) {
  return get('goods/selectGoodsList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function changeGoodsDeviceStatusApi(params = {}) {
  return get('admin/goods/edit_goods_status', params).then(resolveBusinessResponse)
}

export function getBoundRoomPageApi(params = {}) {
  return get('room/selectRoomList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createBoundRoomApi(data = {}) {
  return post('room/insertRoom', data).then(resolveBusinessResponse)
}

export function updateBoundRoomApi(data = {}) {
  return post('room/updateRoom', data).then(resolveBusinessResponse)
}

export function deleteBoundRoomApi(roomId) {
  return post('room/deleteRoomById', null, { params: { roomId } }).then(resolveBusinessResponse)
}

export function bindGoodsInnerCodeApi(data = {}) {
  return post('goods/bindInnerCode', data).then(resolveBusinessResponse)
}

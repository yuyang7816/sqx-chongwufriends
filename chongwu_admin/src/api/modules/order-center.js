import { get, post, resolveBusinessResponse } from '@/api/http'

export function getOrderCenterPageApi(params = {}) {
  return get('orders/selectOrdersList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function exportOrderCenterApi(params = {}) {
  return get('orders/excelOrders', params, { responseType: 'blob' })
}

export function updateOrderApi(data) {
  return post('orders/updateOrders', data).then(resolveBusinessResponse)
}

export function refundOrderApi(params = {}) {
  return post('orders/refundOrdersMoney', null, { params }).then(resolveBusinessResponse)
}

export function closeOrderApi(params = {}) {
  return post('orders/shopCloseOrders', null, { params }).then(resolveBusinessResponse)
}

export function changeOrderRoomApi(params = {}) {
  return post('orders/roomChange', null, { params }).then(resolveBusinessResponse)
}

export function updateHangOrderStatusApi(params = {}) {
  return post('orders/shopUpdateOrdersStatus', null, { params }).then(resolveBusinessResponse)
}

export function getOrderVipDetailsApi(ordersNo) {
  return get('orders/selectOrdersDetailsVip', { ordersNo }).then(resolveBusinessResponse)
}

export function getOrderRenewRecordsApi(params = {}) {
  return get('message/selectMessageByType', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || data?.totalPage || 0),
    }))
}

export function getGoodsPageApi(params = {}) {
  return get('goods/selectGoodsList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

import { get, post, resolveBusinessResponse } from '@/api/http'
import { getUploadApiUrl, toPublicFileUrl } from '@/utils/tool'

export function getMerchantCenterPageApi(params = {}) {
  return get('tearoom/selectTearoomList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createMerchantApi(data) {
  return post('tearoom/insertTearoom', data).then(resolveBusinessResponse)
}

export function updateMerchantApi(data) {
  return post('tearoom/updateTearoom', data).then(resolveBusinessResponse)
}

export function updateMerchantStatusApi(params = {}) {
  return post('tearoom/updateTearoomStatus', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantDetailApi(tearoomId) {
  return get('tearoom/selectTearoomById', { tearoomId }).then(resolveBusinessResponse)
}

export function uploadMerchantImageApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return post(getUploadApiUrl(), formData).then(resolveBusinessResponse).then((data) => {
    // 优先 relativePath，拼当前 API，避免线上不可访问域名
    const url = toPublicFileUrl(data)
    if (data && typeof data === 'object') {
      return {
        ...data,
        url,
        path: url,
        // 保留 relativePath 供后续纠偏
        relativePath: data.relativePath || '',
      }
    }
    return url
  })
}

export function getMerchantGoodsPageApi(params = {}) {
  return get('goods/selectGoodsList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function getMerchantGoodsDetailApi(goodsId) {
  return get('goods/selectGoodsById', { goodsId }).then(resolveBusinessResponse)
}

export function createMerchantGoodsApi(data) {
  return post('goods/insertGoods', data).then(resolveBusinessResponse)
}

export function updateMerchantGoodsApi(data) {
  return post('goods/updateGoods', data).then(resolveBusinessResponse)
}

export function deleteMerchantGoodsApi(goodsId) {
  return post('goods/deleteGoodsById', null, { params: { goodsId } }).then(resolveBusinessResponse)
}

export function getMerchantOrderPageApi(params = {}) {
  return get('orders/selectOrdersList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function refundMerchantOrderApi(params = {}) {
  return post('orders/refundOrdersMoney', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantEmployeesApi(params = {}) {
  return get('user/selectUserList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createMerchantEmployeeApi(params = {}) {
  return post('user/insertTearoomUser', null, { params }).then(resolveBusinessResponse)
}

export function deleteMerchantEmployeeApi(params = {}) {
  return post('user/deleteTearoomUser', null, { params }).then(resolveBusinessResponse)
}

export function updateMerchantEmployeeApi(params = {}) {
  return post('user/updateTearoomUserTime', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantOpenStatsApi(params = {}) {
  return get('orders/selectShopOpenOrdersCount', params).then(resolveBusinessResponse)
}

export function getMerchantIncomeStatsApi(params = {}) {
  return get('orders/selectTearoomOrdersIncome', params).then(resolveBusinessResponse)
}

export function getMerchantRevenueSourceApi(params = {}) {
  return get('orders/selectOrdersCountByDate', params).then(resolveBusinessResponse)
}

export function getMerchantCollectWaterPageApi(params = {}) {
  return get('collectWater/selectCollectWaterList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function getMerchantGoodsTimePageApi(params = {}) {
  return get('goodsTime/selectGoodsTimeList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createMerchantGoodsTimeApi(data) {
  return post('goodsTime/insertGoodsTime', data).then(resolveBusinessResponse)
}

export function updateMerchantGoodsTimeApi(data) {
  return post('goodsTime/updateGoodsTime', data).then(resolveBusinessResponse)
}

export function deleteMerchantGoodsTimeApi(goodsTimeId) {
  return post('goodsTime/deleteGoodsTimeById', null, { params: { goodsTimeId } }).then(resolveBusinessResponse)
}

export function getMerchantGoodsSpecialTimePageApi(params = {}) {
  return get('goodsSpecialTime/selectGoodsSpecialTimeList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createMerchantGoodsSpecialTimeApi(data) {
  return post('goodsSpecialTime/insertGoodsSpecialTime', data).then(resolveBusinessResponse)
}

export function updateMerchantGoodsSpecialTimeApi(data) {
  return post('goodsSpecialTime/updateGoodsSpecialTime', data).then(resolveBusinessResponse)
}

export function deleteMerchantGoodsSpecialTimeApi(goodsSpecialTimeId) {
  return post('goodsSpecialTime/deleteGoodsSpecialTimeById', null, { params: { goodsSpecialTimeId } }).then(resolveBusinessResponse)
}

export function getMerchantRefundRulePageApi(params = {}) {
  return get('refundRule/selectRefundRuleList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createMerchantRefundRuleApi(data) {
  return post('refundRule/insertRefundRule', data).then(resolveBusinessResponse)
}

export function updateMerchantRefundRuleApi(data) {
  return post('refundRule/updateRefundRule', data).then(resolveBusinessResponse)
}

export function deleteMerchantRefundRuleApi(refundRuleId) {
  return post('refundRule/deleteRefundRuleById', null, { params: { refundRuleId } }).then(resolveBusinessResponse)
}

export function getMerchantRoomPageApi(params = {}) {
  return get('room/selectRoomList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createMerchantRoomApi(data) {
  return post('room/insertRoom', data).then(resolveBusinessResponse)
}

export function updateMerchantRoomApi(data) {
  return post('room/updateRoom', data).then(resolveBusinessResponse)
}

export function deleteMerchantRoomApi(roomId) {
  return post('room/deleteRoomById', null, { params: { roomId } }).then(resolveBusinessResponse)
}

export function getMerchantKsRefreshTokenApi(params = {}) {
  return get('app/selfCoupon/oauth2Token', params, { silentError: true })
}

export function getMerchantOrderStatusStatsApi(params = {}) {
  return get('orders/selectOrdersStatusListByTearoomId', params).then(resolveBusinessResponse)
}

export function getMerchantRevenueSummaryApi(params = {}) {
  return get('orders/selectOrdersMoneyByTearoomId', params).then(resolveBusinessResponse)
}

export function getMerchantRevenueTrendApi(params = {}) {
  return get('orders/selectOrdersDateByTearoomId', params).then(resolveBusinessResponse)
}

export function getMerchantGoodsStatsByTimeApi(params = {}) {
  return get('orders/selectTearoomOrdersByGoodsIdAndTime', params).then(resolveBusinessResponse)
}

export function getMerchantEarningDetailApi(params = {}) {
  return get('orders/selectOrdersMoneyByTearoomIdAndDate', params).then(resolveBusinessResponse)
}

export function getMerchantWriteOffStatsApi(params = {}) {
  return get('selfCouponUser/selectTearoomCount', params).then(resolveBusinessResponse)
}

export function getMerchantDurationStatsApi(params = {}) {
  return get('orders/selectOrdersGoodsHourByTearoomId', params).then(resolveBusinessResponse)
}

export function getMerchantEmployeeOpenStatsApi(params = {}) {
  return get('orders/selectYgOpenOrdersCount', params).then(resolveBusinessResponse)
}

export function getMerchantEmployeePermissionApi(params = {}) {
  return get('user/selectTearoomUserTimeByUserId', params).then(resolveBusinessResponse)
}

export function updateMerchantEmployeePermissionApi(data = {}) {
  return post('user/adminUpdateTearoomUserTime', data).then(resolveBusinessResponse)
}

export function getMerchantEmployeeWorkRecordPageApi(params = {}) {
  return get('tearoomUserRecord/selectTearoomUserRecordList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function changeMerchantOrderRoomApi(params = {}) {
  return post('orders/roomChange', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantOrderVipDetailsApi(ordersNo) {
  return get('orders/selectOrdersDetailsVip', { ordersNo }).then(resolveBusinessResponse)
}

export function getMerchantRenewRecordPageApi(params = {}) {
  return get('message/selectMessageByType', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || data?.totalPage || 0),
    }))
}

export function getMerchantCouponIssuePageApi(params = {}) {
  return get('selfCouponIssue/list', params).then(resolveBusinessResponse)
}

export function getMerchantCouponListApi(params = {}) {
  return get('selfCoupon/list', params).then(resolveBusinessResponse)
}

export function createMerchantCouponIssueApi(data = {}) {
  return post('selfCouponIssue/save', data).then(resolveBusinessResponse)
}

export function deleteMerchantCouponIssueApi(id) {
  return get('selfCouponIssue/delete', { id }).then(resolveBusinessResponse)
}

export function updateMerchantCouponIssueStatusApi(params = {}) {
  return post('admin/coupon/edit_fabu_coupon', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantQueuePageApi(params = {}) {
  return get('queueUp/selectQueueUpList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function updateMerchantQueueApi(data = {}) {
  return post('queueUp/updateQueueUp', data).then(resolveBusinessResponse)
}

export function getMerchantLoveWalletApi(params = {}) {
  return get('welfareMoney/selectWelfareMoney', params).then(resolveBusinessResponse)
}

export function updateMerchantLoveWalletApi(params = {}) {
  return post('welfareMoney/updateWelfareMoney', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantLoveWalletDetailPageApi(params = {}) {
  return get('welfareMoney/selectWelfareMoneyDetailsList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function getMerchantWelfareWordPageApi(params = {}) {
  return get('welfareMoney/selectWelfareWordList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createMerchantWelfareWordApi(data = {}) {
  return post('welfareMoney/insertWelfareWord', data).then(resolveBusinessResponse)
}

export function updateMerchantWelfareWordApi(data = {}) {
  return post('welfareMoney/updateWelfareWord', data).then(resolveBusinessResponse)
}

export function deleteMerchantWelfareWordApi(params = {}) {
  return post('welfareMoney/deleteWelfareWord', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantMemberWalletSummaryApi(params = {}) {
  return get('memberUserMoney/selectMemberUserMoneyCount', params).then(resolveBusinessResponse)
}

export function getMerchantMemberWalletPageApi(params = {}) {
  return get('memberUserMoney/selectMemberUserMoneyList', params).then(resolveBusinessResponse)
}

export function createMerchantMemberWalletApi(data = {}) {
  return post('memberUserMoney/insertMemberUserMoney', data).then(resolveBusinessResponse)
}

export function updateMerchantMemberWalletApi(data = {}) {
  return post('memberUserMoney/updateMemberUserMoney', data).then(resolveBusinessResponse)
}

export function deleteMerchantMemberWalletApi(params = {}) {
  return post('memberUserMoney/deleteMemberUserMoney', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantMemberWalletDetailPageApi(params = {}) {
  return get('memberUserMoneyDetailsDetails/selectMemberUserMoneyDetailsList', params).then(resolveBusinessResponse)
}

export function createMerchantMemberWalletDetailApi(data = {}) {
  return post('memberUserMoneyDetailsDetails/insertMemberUserMoneyDetails', data).then(resolveBusinessResponse)
}

export function updateMerchantMemberWalletDetailApi(data = {}) {
  return post('memberUserMoneyDetailsDetails/updateMemberUserMoneyDetails', data).then(resolveBusinessResponse)
}

export function deleteMerchantMemberWalletDetailApi(params = {}) {
  return post('memberUserMoneyDetailsDetails/deleteMemberUserMoneyDetails', null, { params }).then(resolveBusinessResponse)
}

export function updateMerchantMemberWalletByTypeApi(params = {}) {
  return post('memberUserMoney/updateMemberUserMoneyByType', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantMusicPageApi(params = {}) {
  return get('shopCollectMusic/selectShopCollectMusic', params).then(resolveBusinessResponse)
}

export function createMerchantMusicApi(data = {}) {
  return post('shopCollectMusic/insertShopCollectMusic', data).then(resolveBusinessResponse)
}

export function deleteMerchantMusicApi(params = {}) {
  return post('shopCollectMusic/deleteCollectMusic', null, { params }).then(resolveBusinessResponse)
}

export function getMusicLibraryPageApi(params = {}) {
  return get('musicLibrary/selectMusicLibraryList', params).then(resolveBusinessResponse)
}

export function getMusicTypePageApi(params = {}) {
  return get('musicType/selectMusicTypeList', params).then(resolveBusinessResponse)
}

export function getMerchantPrizePageApi(params = {}) {
  return get('admin/prize/selectPrizeList', params).then(resolveBusinessResponse)
}

export function createMerchantPrizeApi(data = {}) {
  return post('admin/prize/insertPrize', data).then(resolveBusinessResponse)
}

export function updateMerchantPrizeApi(data = {}) {
  return post('admin/prize/updatePrize', data).then(resolveBusinessResponse)
}

export function deleteMerchantPrizeApi(prizeId, tearoomId) {
  return post(`admin/prize/deletePrize?prizeId=${prizeId}`, null, { params: { tearoomId } }).then(resolveBusinessResponse)
}

export function getMerchantPrizeRecordPageApi(params = {}) {
  return get('admin/prizeUser/selectPrizeUserList', params).then(resolveBusinessResponse)
}

export function createMerchantPrizeRecordApi(data = {}) {
  return post('admin/prizeUser/insertPrizeUser', data).then(resolveBusinessResponse)
}

export function updateMerchantPrizeRecordApi(data = {}) {
  return post('admin/prizeUser/updatePrizeUser', data).then(resolveBusinessResponse)
}

export function getMerchantPrizeLogisticsApi(trackingNumber) {
  return get('admin/orders/getLogistics', { trackingNumber }).then(resolveBusinessResponse)
}

export function getMerchantPresetPrizeRecordPageApi(params = {}) {
  return get('admin/prizeUserSys/selectPrizeUserSysList', params).then(resolveBusinessResponse)
}

export function createMerchantPresetPrizeRecordApi(data = {}) {
  return post('admin/prizeUserSys/insertPrizeUserSys', data).then(resolveBusinessResponse)
}

export function updateMerchantPresetPrizeRecordApi(data = {}) {
  return post('admin/prizeUserSys/updatePrizeUserSys', data).then(resolveBusinessResponse)
}

export function deleteMerchantPresetPrizeRecordApi(params = {}) {
  return post('admin/prizeUserSys/deletePrizeUserSys', null, { params }).then(resolveBusinessResponse)
}

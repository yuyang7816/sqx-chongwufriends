import { get, post, resolveBusinessResponse } from '@/api/http'
import {
  deleteUserApi,
  getUserInfoApi,
  getUserPageApi,
  updateUserApi,
  updateUserStatusApi,
} from '@/api/modules/users'

export function getUserCenterPage(params = {}) {
  return getUserPageApi(params)
}

export function toggleUserStatusApi(userId, status) {
  return updateUserStatusApi(userId, status)
}

export function deleteUserCenterApi(userId) {
  return deleteUserApi(userId)
}

export function updateUserCenterApi(data) {
  return updateUserApi(data)
}

export function findUserByInvitationCodeApi(invitationCode) {
  return getUserPageApi({ invitationCode, page: 1, limit: 1 })
    .then((data) => data.list?.[0] || null)
}

export function getUserCenterDetail(userId) {
  return getUserInfoApi(userId)
}

export function getVipListApi() {
  return get('vip/selectVipList', {
    page: 1,
    limit: 200,
    vipName: '',
    isEnable: '',
  })
    .then(resolveBusinessResponse)
    .then((data) => data?.records || data?.list || [])
}

export function getVipCardListApi() {
  return get('vipCard/selectVipCardList', { page: 1, limit: 200 })
    .then(resolveBusinessResponse)
    .then((data) => data?.records || data?.list || [])
}

export function createUserCenterApi(data) {
  return post('user/insertUser', data).then(resolveBusinessResponse)
}

export function updateUserIsPayUserApi(userId, isPayUser) {
  return post('user/updateUserIsPayUser', null, { params: { userId, isPayUser } }).then(resolveBusinessResponse)
}

export function updateUserVipIndexByMoneyApi(money) {
  return post('user/updateUserVipIndexByMoney', null, { params: { money } }).then(resolveBusinessResponse)
}

export function getInviteQrApi(params = {}) {
  return get('app/invite/mpCreateQr', params, { responseType: 'blob' })
    .then((data) => {
      if (typeof Blob !== 'undefined' && data instanceof Blob) {
        return URL.createObjectURL(data)
      }
      if (typeof data === 'string') {
        return data
      }
      return data?.url || data?.qrCode || data?.imgUrl || data?.imageUrl || data?.data || ''
    })
}

export function getUserArchiveDetailApi(userId) {
  return get(`user/${userId}`).then(resolveBusinessResponse)
}

export function getUserIntegralSummaryApi(userId) {
  return get('integral/selectByUserId', { userId }).then(resolveBusinessResponse)
}

export function getUserVipValueSummaryApi(userId) {
  return get('userVipDetails/selectUserVipDetailsByUserId', { userId }).then(resolveBusinessResponse)
}

export function getUserMessagesApi(params = {}) {
  return get('message/selectMessageByUserId', params).then(resolveBusinessResponse)
}

export function getUserWithdrawalsApi(params = {}) {
  return get('cash/selectPayDetails', params).then(resolveBusinessResponse)
}

export function getUserWalletDetailsApi(params = {}) {
  return get('Details/balanceDetailed', params).then(resolveBusinessResponse)
}

export function getUserIntegralDetailsApi(params = {}) {
  return get('integral/details', params).then(resolveBusinessResponse)
}

export function getUserPaymentsApi(params = {}) {
  return get('cash/selectUserRechargeByUserId', params).then(resolveBusinessResponse)
}

export function getUserTeamMembersApi(params = {}) {
  return get('orders/selectTeamUserList', params).then(resolveBusinessResponse)
}

export function getUserVipValueDetailsApi(params = {}) {
  return get('userVipDetails/selectUserVipDetailsList', params).then(resolveBusinessResponse)
}

export function setUserVipCardApi(userId, vipCardId) {
  return post('vipCard/setUserVipCard', null, { params: { userId, vipCardId } }).then(resolveBusinessResponse)
}

export function updateUserIntegralApi(params = {}) {
  return post('integral/updateUserIntegral', null, { params }).then(resolveBusinessResponse)
}

export function updateUserSafetyMoneyApi(params = {}) {
  return post('user/updateSafetyMoney', null, { params }).then(resolveBusinessResponse)
}

export function updateUserCreditScoreApi(params = {}) {
  return post('admin/creditRecord/updateUserCredit', null, { params }).then(resolveBusinessResponse)
}

export function updateUserCreateCountApi(params = {}) {
  return post('admin/createCountRecord/giveUserCreateCount', null, { params }).then(resolveBusinessResponse)
}

export function getUserPrizeGoldenSummaryApi(params = {}) {
  return get('admin/prizeGoldenUser/selectPrizeGoldenUser', params).then(resolveBusinessResponse)
}

export function updateUserPrizeGoldenApi(params = {}) {
  return post('admin/prizeGoldenUser/updatePrizeGoldenUser', null, { params }).then(resolveBusinessResponse)
}

export function addUserBalanceApi(userId, money, type, isVip = 2) {
  return post(`user/addCannotMoney/${userId}/${money}/${type}`, { isVip }).then(resolveBusinessResponse)
}

export function updateUserInviteMoneyApi(userId, money, type) {
  return post(`user/updateInviteMoney/${userId}/${money}/${type}`, {}).then(resolveBusinessResponse)
}

export function createUserVipValueApi(data) {
  return post('userVipDetails/insertUserVipDetails', data).then(resolveBusinessResponse)
}

export function deleteUserVipValueApi(userVipDetailsId) {
  return post('userVipDetails/deleteUserVipDetails', null, { params: { userVipDetailsId } }).then(resolveBusinessResponse)
}

export function getUserCommentsApi(params = {}) {
  return get('comment/selectCommentList', params).then(resolveBusinessResponse)
}

export function deleteUserCommentApi(commentId) {
  return post('comment/deleteComment', null, { params: { id: commentId } }).then(resolveBusinessResponse)
}

export function getUserCouponsApi(params = {}) {
  return get('selfCouponUser/list', params).then(resolveBusinessResponse)
}

export function getUserExchangeRecordsApi(params = {}) {
  return get('message/selectMessageByType', params).then(resolveBusinessResponse)
}

export function getUserOrdersApi(params = {}) {
  return get('orders/selectOrdersList', params).then(resolveBusinessResponse)
}

export function getUserActivitiesApi(params = {}) {
  return get('activity/selectActivityList', params).then(resolveBusinessResponse)
}

export function getUserActivityApplyApi(params = {}) {
  return get('activityApply/selectActivityApplyList', params).then(resolveBusinessResponse)
}

export function getUserSignRecordsApi(params = {}) {
  return get('message/selectActivitySignList', params).then(resolveBusinessResponse)
}

export function getUserActivityPhotosApi(params = {}) {
  return get('activityPhoto/selectActivityPhotoList', params).then(resolveBusinessResponse)
}

export function getUserShopOrdersApi(params = {}) {
  return get('admin/order/selectAllOrder', params).then(resolveBusinessResponse)
}

export function getUserChatsApi(params = {}) {
  return get('admin/question/getChatList', params).then(resolveBusinessResponse)
}

export function getUserCreditRecordsApi(params = {}) {
  return get('admin/creditRecord/getCreditRecordList', params).then(resolveBusinessResponse)
}

export function getUserDateBallPublishedApi(params = {}) {
  return get('dateBall/selectDateBallList', params).then(resolveBusinessResponse)
}

export function getUserDateBallJoinedApi(params = {}) {
  return get('dateBall/selectDateBallPeopleList', params).then(resolveBusinessResponse)
}

export function getUserLeaveMessagesApi(params = {}) {
  return get('leaveMessage/selectLeaveMessageList', params).then(resolveBusinessResponse)
}

export function getUserAddressesApi(params = {}) {
  return get('address/findByUserId', params).then(resolveBusinessResponse)
}

export function getArtificerDetailApi(artificerId) {
  return get('artificer/selectArtificerById', { artificerId }).then(resolveBusinessResponse)
}

export function getArtificerMyCommentsApi(params = {}) {
  return get('takingComment/queryMyComment', params).then(resolveBusinessResponse)
}

export function getArtificerReceivedCommentsApi(params = {}) {
  return get('takingComment/selectOrderTakingComment', params).then(resolveBusinessResponse)
}

export function getArtificerOrdersApi(params = {}) {
  return get('artificer/selectOrdersList', params).then(resolveBusinessResponse)
}

export function getArtificerProjectsApi(params = {}) {
  return get('artificer/selectArtificerMassageList', params).then(resolveBusinessResponse)
}

export function getArtificerStatsApi(params = {}) {
  return get('artificer/selectArtificerIndex', params).then(resolveBusinessResponse)
}

export function getArtificerCreditMessagesApi(params = {}) {
  return get('message/selectMessageByUserId', params).then(resolveBusinessResponse)
}

export function getArtificerLocationsApi(params = {}) {
  return get('message/selectArtificerLocation', params).then(resolveBusinessResponse)
}

export function getArtificerBusyTimesApi(params = {}) {
  return get('artificerTime/selectArtificerTimeByArtificerId', params).then(resolveBusinessResponse)
}

export function updateArtificerBusyTimesApi(params = {}) {
  return post('artificerTime/updateArtificerTime', null, { params }).then(resolveBusinessResponse)
}

export function updateArtificerApi(data) {
  return post('artificer/updateArtificers', data).then(resolveBusinessResponse)
}

export function getUserRenewRecordsApi(params = {}) {
  return get('message/selectMessageByType', params).then(resolveBusinessResponse)
}

export function getUserMusicPlaysApi(params = {}) {
  return get('musicPlay/selectMusicPlayList', params).then(resolveBusinessResponse)
}

export function getUserCollectedInfosApi(params = {}) {
  return get('informationCollect/selectInformationCollectByList', params).then(resolveBusinessResponse)
}

export function getHelpOrdersApi(params = {}) {
  return get('helpOrder/selectHelpOrderList', params).then(resolveBusinessResponse)
}

export function getCollectWaterRecordsApi(params = {}) {
  return get('collectWater/selectCollectWaterList', params).then(resolveBusinessResponse)
}

export function getPrizeGoldenDetailsApi(params = {}) {
  return get('admin/prizeGoldenUser/selectPrizeGoldenUserDetailsList', params).then(resolveBusinessResponse)
}

export function getPrizeRecordsApi(params = {}) {
  return get('admin/prizeUser/selectPrizeUserList', params).then(resolveBusinessResponse)
}

export function getPrizeRechargeRecordsApi(params = {}) {
  return get('prizeNumPay/selectPrizeNumPayList', params).then(resolveBusinessResponse)
}

export function getTearoomListApi(params = {}) {
  return get('tearoom/selectTearoomList', params).then(resolveBusinessResponse)
}

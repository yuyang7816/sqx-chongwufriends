import { get, post, resolveBusinessResponse } from '@/api/http'

export const dashboardApi = {
  overview: '/user/homeMessage',
  userPlatform: '/user/userPlatform',
  venueAnalysis: '/orders/selectTearoomOrdersCount',
  paymentStats: '/cash/payMoney',
  incomeStats: '/orders/selectPayMoneyData',
  couponStats: '/selfCouponUser/selectCouponData',
  loveOrderStats: '/orders/selectLoveOrdersData',
  dynamicStats: '/gambit/talkMap',
  writeOffStats: '/selfCouponUser/selectTearoomCount',
  loveWalletStats: '/welfareMoney/selectWelfareMoneyDetailsCountList',
  orderTrend: '/orders/selectOrdersCountStatisticsByYear',
  userGrowthTrend: '/user/selectUserCountStatisticsByTime',
  userCountTrend: '/data/selectUserCountStatisticsByTime',
  payCountTrend: '/data/selectPayCountStatisticsByTime',
  orderCountTrend: '/data/selectOrdersCountStatisticsByTime',
  couponCountTrend: '/data/selectCouponCountStatisticsByTime',
  goodsCountTrend: '/data/selectGoodsCountStatisticsByTime',
  repeatRateTrend: '/data/selectRepeatRateStatisticsByTime',
  storeGoodsTrend: '/data/selectGoodsListCountStatisticsByTime',
  tearoomList: '/tearoom/selectTearoomList',
  goodsList: '/goods/selectGoodsList',
}

export function getDashboardOverview(params = {}) {
  return get(dashboardApi.overview, params).then(resolveBusinessResponse)
}

export function getDashboardUserPlatform(params = {}) {
  return get(dashboardApi.userPlatform, params).then(resolveBusinessResponse)
}

export function getDashboardVenueAnalysis(params = {}) {
  return get(dashboardApi.venueAnalysis, params).then(resolveBusinessResponse)
}

export function getDashboardPaymentStats(params = {}) {
  return get(dashboardApi.paymentStats, params).then(resolveBusinessResponse)
}

export function getDashboardIncomeStats(params = {}) {
  return get(dashboardApi.incomeStats, params).then(resolveBusinessResponse)
}

export function getDashboardCouponStats(params = {}) {
  return get(dashboardApi.couponStats, params).then(resolveBusinessResponse)
}

export function getDashboardLoveOrderStats(params = {}) {
  return get(dashboardApi.loveOrderStats, params).then(resolveBusinessResponse)
}

export function getDashboardDynamicStats(data = {}) {
  return post(dashboardApi.dynamicStats, data).then(resolveBusinessResponse)
}

export function getDashboardWriteOffStats(params = {}) {
  return get(dashboardApi.writeOffStats, params).then(resolveBusinessResponse)
}

export function getDashboardLoveWalletStats(params = {}) {
  return get(dashboardApi.loveWalletStats, params).then(resolveBusinessResponse)
}

export function getDashboardOrderTrend(params = {}) {
  return get(dashboardApi.orderTrend, params).then(resolveBusinessResponse)
}

export function getDashboardUserGrowthTrend(params = {}) {
  return get(dashboardApi.userGrowthTrend, params).then(resolveBusinessResponse)
}

export function getDashboardUserCountTrend(params = {}) {
  return get(dashboardApi.userCountTrend, params).then(resolveBusinessResponse)
}

export function getDashboardPayCountTrend(params = {}) {
  return get(dashboardApi.payCountTrend, params).then(resolveBusinessResponse)
}

export function getDashboardOrderCountTrend(params = {}) {
  return get(dashboardApi.orderCountTrend, params).then(resolveBusinessResponse)
}

export function getDashboardCouponCountTrend(params = {}) {
  return get(dashboardApi.couponCountTrend, params).then(resolveBusinessResponse)
}

export function getDashboardGoodsCountTrend(params = {}) {
  return get(dashboardApi.goodsCountTrend, params).then(resolveBusinessResponse)
}

export function getDashboardRepeatRateTrend(params = {}) {
  return get(dashboardApi.repeatRateTrend, params).then(resolveBusinessResponse)
}

export function getDashboardStoreGoodsTrend(params = {}) {
  return get(dashboardApi.storeGoodsTrend, params).then(resolveBusinessResponse)
}

export function getDashboardTearoomList(params = {}) {
  return get(dashboardApi.tearoomList, params).then(resolveBusinessResponse)
}

export function getDashboardGoodsList(params = {}) {
  return get(dashboardApi.goodsList, params).then(resolveBusinessResponse)
}

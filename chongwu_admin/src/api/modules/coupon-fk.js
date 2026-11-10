import { get, post, resolveBusinessResponse } from '@/api/http'

export function getCouponFkTypePageApi(params = {}) {
  return get('admin/sdkType/getSdkTypeList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function saveCouponFkTypeApi(params = {}) {
  return post('admin/sdkType/saveSdkType', null, { params }).then(resolveBusinessResponse)
}

export function deleteCouponFkTypeApi(typeId) {
  return get('admin/sdkType/deleteSdkType', { typeId }).then(resolveBusinessResponse)
}

export function generateCouponFkCardApi(params = {}) {
  return post('admin/sdkInfo/saveSdkInfo', null, { params }).then(resolveBusinessResponse)
}

export function getCouponFkCardPageApi(params = {}) {
  return get('admin/sdkInfo/getSdkList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function deleteCouponFkCardsApi(sdkIds) {
  return get('admin/sdkInfo/deleteSdk', { sdkIds }).then(resolveBusinessResponse)
}

export function exportCouponFkCardsApi(params = {}) {
  return get('admin/sdkInfo/excelCouponCardList', params, { responseType: 'blob' })
}

export function getCouponFkIssuePageApi(params = {}) {
  return get('selfCouponIssue/list', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.content || data?.records || data?.list || [],
      total: Number(data?.totalElements || data?.totalCount || data?.total || 0),
    }))
}

export function getCouponFkChannelPageApi(params = {}) {
  return get('/sys/user/list', params).then((data) => ({
    list: data?.page?.list || [],
    total: Number(data?.page?.totalCount || data?.page?.total || 0),
  }))
}

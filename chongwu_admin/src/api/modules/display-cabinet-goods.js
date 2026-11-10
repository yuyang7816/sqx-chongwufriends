import { get, post, resolveBusinessResponse } from '@/api/http'

export function getDisplayCabinetGoodsPageApi(params = {}) {
  return get('smallGoods/selectSmallGoodsList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createDisplayCabinetGoodsApi(data = {}) {
  return post('smallGoods/insertSmallGoods', data).then(resolveBusinessResponse)
}

export function updateDisplayCabinetGoodsApi(data = {}) {
  return post('smallGoods/updateSmallGoods', data).then(resolveBusinessResponse)
}

export function deleteDisplayCabinetGoodsApi(id) {
  return post('smallGoods/deleteSmallGoods', null, { params: { id } }).then(resolveBusinessResponse)
}

export function getDisplayCabinetGoodsCategoryPageApi(params = {}) {
  return get('banner/selectBannerPage', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createDisplayCabinetGoodsCategoryApi(data = {}) {
  return post('banner/insertBanner', data).then(resolveBusinessResponse)
}

export function updateDisplayCabinetGoodsCategoryApi(data = {}) {
  return post('banner/updateBannerById', data).then(resolveBusinessResponse)
}

export function deleteDisplayCabinetGoodsCategoryApi(ids) {
  return get(`banner/deleteBannerById?ids=${ids}`).then(resolveBusinessResponse)
}

export function toggleDisplayCabinetGoodsCategoryApi(id) {
  return post(`banner/updateBannerStateById?id=${id}`, {}).then(resolveBusinessResponse)
}

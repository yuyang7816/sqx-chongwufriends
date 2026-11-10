import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/pet/banner'

/** 运营位：shop=商城首页 home=APP首页 adopt=领养页 */
export const BANNER_POSITION = {
  SHOP: 'shop',
  HOME: 'home',
  ADOPT: 'adopt',
}

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getOperationBannerPageApi(params = {}) {
  return post(`${PREFIX}/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
    positionCode: params.positionCode || BANNER_POSITION.SHOP,
    name: params.name || '',
    status: params.status !== '' && params.status !== undefined ? params.status : undefined,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function getOperationBannerInfoApi(id) {
  return post(`${PREFIX}/info`, { id }).then(resolveBusinessResponse)
}

export function createOperationBannerApi(data = {}) {
  return post(`${PREFIX}/save`, data).then(resolveBusinessResponse)
}

export function updateOperationBannerApi(data = {}) {
  return post(`${PREFIX}/update`, data).then(resolveBusinessResponse)
}

export function deleteOperationBannerApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}

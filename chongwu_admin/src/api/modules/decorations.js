export {
  getOperationBannerPageApi as getDecorationPageApi,
  getOperationBannerPageApi as getShopBannerPageApi,
  createOperationBannerApi as createDecorationApi,
  createOperationBannerApi as createShopBannerApi,
  updateOperationBannerApi as updateDecorationApi,
  updateOperationBannerApi as updateShopBannerApi,
  deleteOperationBannerApi as deleteDecorationApi,
  deleteOperationBannerApi as deleteShopBannerApi,
  getOperationBannerInfoApi as getShopBannerInfoApi,
  BANNER_POSITION,
} from '@/api/modules/operation-banner'

export async function toggleDecorationStatusApi(id) {
  const { getOperationBannerInfoApi, updateOperationBannerApi } = await import('@/api/modules/operation-banner')
  const row = await getOperationBannerInfoApi(id)
  const nextStatus = Number(row?.status) === 1 ? 2 : 1
  return updateOperationBannerApi({
    id: row.id,
    name: row.name,
    imageUrl: row.imageUrl,
    jumpUrl: row.jumpUrl || '',
    sort: row.sort ?? 0,
    status: nextStatus,
    positionCode: row.positionCode || 'shop',
  })
}

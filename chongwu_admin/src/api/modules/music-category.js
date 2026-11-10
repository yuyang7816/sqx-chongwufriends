import { get, post, resolveBusinessResponse } from '@/api/http'

export function getMusicCategoryPageApi(params = {}) {
  return get('musicType/selectMusicTypeList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function createMusicCategoryApi(data = {}) {
  return post('musicType/insertMusicType', data).then(resolveBusinessResponse)
}

export function updateMusicCategoryApi(data = {}) {
  return post('musicType/updateMusicType', data).then(resolveBusinessResponse)
}

export function deleteMusicCategoryApi(musicTypeId) {
  return post(`musicType/deleteMusicType?musicTypeId=${musicTypeId}`, {}).then(resolveBusinessResponse)
}

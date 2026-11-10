import { get, post, resolveBusinessResponse } from '@/api/http'

export function getMusicPlayPageApi(params = {}) {
  return get('musicPlay/selectMusicPlayList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.list || data?.records || [],
      total: Number(data?.totalCount || data?.total || 0),
    }))
}

export function createMusicPlayApi(data = {}) {
  return post('musicPlay/insertMusicPlay', data).then(resolveBusinessResponse)
}

export function updateMusicPlayApi(data = {}) {
  return post('musicPlay/updateMusicPlay', data).then(resolveBusinessResponse)
}

export function deleteMusicPlayApi(musicPlayId) {
  return post(`musicPlay/deleteMusicPlay?musicPlayId=${musicPlayId}`, {}).then(resolveBusinessResponse)
}

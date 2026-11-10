import { get, post, resolveBusinessResponse } from '@/api/http'

export function getMusicLibraryPageApi(params = {}) {
  return get('musicLibrary/selectMusicLibraryList', params)
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function createMusicLibraryApi(data = {}) {
  return post('musicLibrary/insertMusicLibrary', data).then(resolveBusinessResponse)
}

export function updateMusicLibraryApi(data = {}) {
  return post('musicLibrary/updateMusicLibrary', data).then(resolveBusinessResponse)
}

export function deleteMusicLibraryApi(musicLibraryId) {
  return post(`musicLibrary/deleteMusicLibrary?musicLibraryId=${musicLibraryId}`, {}).then(resolveBusinessResponse)
}

export function createRandomMusicLibraryApi(params = {}) {
  return post('musicLibrary/insertMusicLibraryRand', null, { params }).then(resolveBusinessResponse)
}

/**
 * 管理端宠物品种默认封面（与小程序 /static/pets 插画一致）
 */

const DOG_DEFAULT = '/static/pets/island-pet-shiba.png'
const CAT_DEFAULT = '/static/pets/island-pet-calico.png'

const BREED_AVATAR_MAP = [
  { keys: ['柴犬', '柴', 'shiba'], src: '/static/pets/island-pet-shiba.png' },
  { keys: ['金毛', '金毛寻回', 'golden'], src: '/static/pets/island-pet-golden.png' },
  { keys: ['柯基', 'corgi'], src: '/static/pets/island-pet-corgi.png' },
  { keys: ['法斗', '法牛', '法国斗牛', 'french'], src: '/static/pets/island-pet-frenchie.png' },
  { keys: ['萨摩', '萨摩耶', 'samoyed'], src: '/static/pets/island-pet-samoyed.png' },
  { keys: ['泰迪', '贵宾', '比熊', '博美', '雪纳瑞', '约克夏'], src: '/static/pets/island-pet-shiba.png' },
  { keys: ['三花', '狸花', '橘猫', '橘', '加菲', '美短', '英短', '渐层', '蓝猫', '暹罗', '波斯'], src: '/static/pets/island-pet-calico.png' },
  { keys: ['白猫', '白', '英短白'], src: '/static/pets/island-pet-white-cat.png' },
  { keys: ['布偶', '布偶猫', 'ragdoll'], src: '/static/pets/island-pet-ragdoll.png' },
]

function normalize(text) {
  return String(text || '').trim().toLowerCase()
}

export function isBreedDefaultAvatar(url) {
  const u = String(url || '').trim()
  if (!u) return true
  return u.includes('/static/pets/island-pet-') || u.endsWith('/logo.png') || u === '/static/logo.png'
}

/** 按品种/类型解析默认封面路径 */
export function resolveBreedAvatar(breed, petType = 'dog') {
  const raw = String(breed || '').trim()
  const lower = normalize(raw)
  if (raw) {
    for (const item of BREED_AVATAR_MAP) {
      if (item.keys.some((k) => raw.includes(k) || lower.includes(normalize(k)))) {
        return item.src
      }
    }
  }
  const t = normalize(petType)
  if (t.includes('cat') || t.includes('猫') || t === '2') {
    return CAT_DEFAULT
  }
  return DOG_DEFAULT
}

/** 列表/表单展示用封面：有自定义图优先，否则按品种兜底 */
export function resolveArchiveCover(pet = {}) {
  const avatar = String(pet.avatarUrl || '').trim()
  if (avatar && !isBreedDefaultAvatar(avatar)) {
    return avatar
  }
  if (avatar && isBreedDefaultAvatar(avatar) && avatar.includes('/static/pets/')) {
    return avatar
  }
  return resolveBreedAvatar(pet.breed, pet.petType)
}

export { DOG_DEFAULT, CAT_DEFAULT, BREED_AVATAR_MAP }

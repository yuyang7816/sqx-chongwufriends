/**
 * 按宠物品种/类型解析岛宅展示图与动画风格。
 * 岛宅场景优先用品种插画（适配动效）；无匹配品种时再用真实头像，最后回退犬/猫默认图。
 */

const DOG_DEFAULT = '/static/pets/island-pet-shiba.png';
const CAT_DEFAULT = '/static/pets/island-pet-calico.png';

/** @typedef {'shiba'|'golden'|'corgi'|'frenchie'|'samoyed'|'calico'|'white-cat'|'ragdoll'|'dog'|'cat'} PetAnimKind */

const BREED_AVATAR_MAP = [
	{ keys: ['柴犬', '柴', 'shiba'], src: '/static/pets/island-pet-shiba.png', kind: 'shiba', species: 'dog' },
	{ keys: ['金毛', '金毛寻回', 'golden'], src: '/static/pets/island-pet-golden.png', kind: 'golden', species: 'dog' },
	{ keys: ['柯基', 'corgi'], src: '/static/pets/island-pet-corgi.png', kind: 'corgi', species: 'dog' },
	{ keys: ['法斗', '法牛', '法国斗牛', 'french'], src: '/static/pets/island-pet-frenchie.png', kind: 'frenchie', species: 'dog' },
	{ keys: ['萨摩', '萨摩耶', 'samoyed'], src: '/static/pets/island-pet-samoyed.png', kind: 'samoyed', species: 'dog' },
	{ keys: ['泰迪', '贵宾', '比熊', '博美', '雪纳瑞', '约克夏'], src: '/static/pets/island-pet-shiba.png', kind: 'dog', species: 'dog' },
	{ keys: ['三花', '狸花', '橘猫', '橘', '加菲', '美短', '英短', '渐层', '蓝猫', '暹罗', '波斯'], src: '/static/pets/island-pet-calico.png', kind: 'calico', species: 'cat' },
	{ keys: ['白猫', '白', '英短白'], src: '/static/pets/island-pet-white-cat.png', kind: 'white-cat', species: 'cat' },
	{ keys: ['布偶', '布偶猫', 'ragdoll'], src: '/static/pets/island-pet-ragdoll.png', kind: 'ragdoll', species: 'cat' },
];

function normalize(text) {
	return String(text || '').trim().toLowerCase();
}

function isPlaceholderAvatar(url) {
	if (!url) return true;
	const u = String(url || '').trim();
	if (!u) return true;
	if (u === '/static/logo.png') return true;
	if (u.endsWith('/logo.png')) return true;
	return false;
}

function isIslandIllustration(url) {
	const u = String(url || '');
	return u.includes('/static/pets/island-pet-');
}

function matchBreedMeta(breed) {
	const raw = String(breed || '').trim();
	if (!raw) return null;
	const lower = normalize(raw);
	for (const item of BREED_AVATAR_MAP) {
		if (item.keys.some((k) => raw.includes(k) || lower.includes(normalize(k)))) {
			return item;
		}
	}
	return null;
}

function matchTypeMeta(petType) {
	const t = normalize(petType);
	if (t.includes('cat') || t.includes('猫') || t === '2') {
		return { src: CAT_DEFAULT, kind: 'cat', species: 'cat' };
	}
	return { src: DOG_DEFAULT, kind: 'dog', species: 'dog' };
}

/**
 * @param {{ petAvatar?: string, avatarUrl?: string, breed?: string, petType?: string, type?: string }} pet
 * @param {(url: string, fallback?: string) => string} [resolveMediaUrl]
 * @returns {{ src: string, kind: PetAnimKind, species: 'dog'|'cat', fallbackSrc: string }}
 */
export function resolveIslandPetVisual(pet = {}, resolveMediaUrl) {
	const typeMeta = matchTypeMeta(pet.petType || pet.type);
	const breedMeta = matchBreedMeta(pet.breed);
	const fallbackSrc = breedMeta?.src || typeMeta.src;
	const kind = breedMeta?.kind || typeMeta.kind;
	const species = breedMeta?.species || typeMeta.species;
	const avatar = pet.petAvatar || pet.avatarUrl || '';

	// 后端已下发岛宅插画时直接用
	if (isIslandIllustration(avatar)) {
		return { src: avatar, kind, species, fallbackSrc };
	}

	// 岛宅场景统一用插画，保证切换后始终有图，并适配待机/喂食动效
	if (breedMeta) {
		return { src: breedMeta.src, kind, species, fallbackSrc };
	}

	return { src: typeMeta.src, kind, species, fallbackSrc };
}

/**
 * @param {{ petAvatar?: string, avatarUrl?: string, breed?: string, petType?: string, type?: string }} pet
 * @param {(url: string, fallback?: string) => string} [resolveMediaUrl]
 */
export function resolveIslandPetImage(pet = {}, resolveMediaUrl) {
	return resolveIslandPetVisual(pet, resolveMediaUrl).src;
}

export { DOG_DEFAULT, CAT_DEFAULT, BREED_AVATAR_MAP };

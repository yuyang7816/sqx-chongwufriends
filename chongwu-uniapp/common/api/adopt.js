import { post } from '@/common/request';
import { getIdempotentKey } from '@/common/auth-guard';

export function fetchAdoptPetPage(params = {}) {
	return post('/app/pet/adopt/pet/page', {
		page: 1,
		limit: 50,
		adoptable: 1,
		auditStatus: 1,
		...params
	});
}

export function fetchAdoptPetInfo(id) {
	return post('/app/pet/adopt/pet/info', { id });
}

export function fetchMyAdoptPage(params = {}) {
	return post('/app/pet/adopt/my/page', { page: 1, limit: 20, ...params });
}

export function fetchMyAdoptInfo(id) {
	return post('/app/pet/adopt/my/info', { id });
}

export function submitAdoptApplication(data) {
	return post('/app/pet/adopt/save', {
		...data,
		idempotentKey: getIdempotentKey('adopt')
	}, { silentError: true });
}

/** 进行中的申请状态 */
export const ADOPT_ACTIVE_STATUSES = [1, 2, 3, 4, 5];

export const ADOPT_STATUS_MAP = {
	1: '申请中',
	2: '申请中',
	3: '申请中',
	4: '申请中',
	5: '申请中',
	6: '通过',
	7: '拒绝'
};

export function resolveAdoptStatusLabel(status) {
	const n = Number(status);
	if (n >= 1 && n <= 5) return '申请中';
	return ADOPT_STATUS_MAP[n] || '未知';
}

/** 查询当前用户进行中申请对应的 petId -> status */
export async function fetchMyActiveApplyMap() {
	const token = uni.getStorageSync('token');
	if (!token) return {};
	try {
		const res = await fetchMyAdoptPage({ page: 1, limit: 100 });
		const map = {};
		(res?.records || []).forEach((item) => {
			const petId = Number(item?.petId);
			const status = Number(item?.status);
			if (!petId || !ADOPT_ACTIVE_STATUSES.includes(status)) return;
			map[petId] = status;
		});
		return map;
	} catch (e) {
		return {};
	}
}

import { post } from '@/common/request';
import { getIdempotentKey } from '@/common/auth-guard';

export function fetchIslandInfo() {
	return post('/app/pet/island/info', {});
}

export function feedPet(feedType = '') {
	const data = { idempotentKey: getIdempotentKey('feed') };
	if (feedType) data.feedType = feedType;
	return post('/app/pet/island/feed', data);
}

export function fetchMarketItems() {
	return post('/app/pet/island/market/list', {});
}

export function buyMarketItem(itemId, payType = 'coins') {
	return post('/app/pet/island/market/buy', {
		itemId,
		payType,
		idempotentKey: getIdempotentKey('buy')
	});
}

export function fetchMarketRecords(params = {}) {
	return post('/app/pet/island/market/records', { page: 1, limit: 20, ...params });
}

export function fetchTaskList() {
	return post('/app/pet/island/task/list', {});
}

export function claimTask(taskId) {
	return post('/app/pet/island/task/claim', { taskId, idempotentKey: getIdempotentKey('claim') });
}

export function completeTask(taskId) {
	return post('/app/pet/island/task/complete', { taskId, idempotentKey: getIdempotentKey('complete') });
}

export function saveResidents(petIds = []) {
	const ids = (Array.isArray(petIds) ? petIds : [])
		.map((id) => Number(id))
		.filter((id) => Number.isFinite(id) && id > 0);
	return post('/app/pet/island/resident/save', { petIds: ids, idempotentKey: getIdempotentKey('resident') });
}

/** 常驻可选：当前用户自有（领养通过后的）宠物 */
export function fetchResidentCandidates() {
	return post('/app/pet/island/resident/candidates', {}).then((data) => {
		const list = Array.isArray(data) ? data : (data?.records || data?.list || []);
		return {
			records: list,
			list,
			total: list.length,
		};
	});
}

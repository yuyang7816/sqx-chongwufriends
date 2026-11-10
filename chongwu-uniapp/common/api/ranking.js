import { post } from '@/common/request';
import { getIdempotentKey } from '@/common/auth-guard';

export function fetchRankingPage(params = {}) {
	return post('/app/pet/ranking/page', { page: 1, limit: 20, ...params });
}

export function fetchRankingPeriods() {
	return post('/app/pet/ranking/periods', {});
}

export function fetchRankingRules() {
	return post('/app/pet/ranking/rules', {});
}

export function fetchRankingAssistSettings() {
	return post('/app/pet/ranking/assist/settings', {});
}

export function fetchRankingAssistQuota() {
	return post('/app/pet/ranking/assist/quota', {});
}

export function assistPet(petId, periodId, payType) {
	const payload = {
		petId,
		periodId,
		idempotentKey: getIdempotentKey('assist')
	};
	if (payType) {
		payload.payType = payType;
	}
	return post('/app/pet/ranking/assist', payload);
}

export function fetchMyAssists(params = {}) {
	return post('/app/pet/ranking/myAssists', { page: 1, limit: 20, ...params });
}

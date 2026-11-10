import { post, get } from '@/common/request';

export function fetchStrategyCategories() {
	return post('/app/pet/strategy/categories', {});
}

export function fetchStrategyPage(params = {}) {
	return post('/app/pet/strategy/page', { page: 1, limit: 20, ...params });
}

export function fetchStrategyInfo(id) {
	return post('/app/pet/strategy/info', { id });
}

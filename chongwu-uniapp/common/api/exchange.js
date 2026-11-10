import { post } from '@/common/request';

export function fetchExchangePage(params = {}) {
	return post('/app/pet/exchange/page', { page: 1, limit: 20, ...params });
}

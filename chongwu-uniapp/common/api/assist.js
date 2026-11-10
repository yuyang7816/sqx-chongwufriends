import { post } from '@/common/request';

export function fetchAssistList() {
	return post('/app/pet/assist/list', {});
}

export function fetchAssistPage(params = {}) {
	return post('/app/pet/assist/page', { page: 1, limit: 20, ...params });
}

export function fetchAssistSummary() {
	return post('/app/pet/assist/summary', {});
}

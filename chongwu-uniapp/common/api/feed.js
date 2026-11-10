import { post } from '@/common/request';
import { getIdempotentKey } from '@/common/auth-guard';

export function fetchFeedProducts() {
	return post('/app/pet/mall/page', { categoryId: 6, page: 1, limit: 20 });
}

export function createFeedOrder(data) {
	return post('/app/pet/feed/create', {
		...data,
		idempotentKey: getIdempotentKey('feed_order')
	});
}

export function fetchGiftRecords(params = {}) {
	return post('/app/pet/feed/page', { page: 1, limit: 20, ...params });
}

export function fetchGiftInfo(id) {
	return post('/app/pet/feed/info', { id });
}

export function fetchGiftSummary() {
	return post('/app/pet/feed/summary', {});
}

export function uploadFeedFeedback(orderId, videoUrl) {
	return post('/app/pet/feed/feedback/upload', {
		orderId,
		videoUrl,
		idempotentKey: getIdempotentKey('feedback')
	});
}

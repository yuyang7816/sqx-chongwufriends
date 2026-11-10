import { post } from '@/common/request';
import { getIdempotentKey } from '@/common/auth-guard';

export function fetchMallCategories() {
	return post('/app/pet/mall/categories', {}).then((res) => {
		const list = Array.isArray(res) ? res : [];
		return [{ id: 0, name: '全部分类' }, ...list.filter((item) => item.id !== 0)];
	});
}

export function fetchMallPage(params = {}) {
	return post('/app/pet/mall/page', { page: 1, limit: 20, ...params });
}

export function fetchMallInfo(id) {
	return post('/app/pet/mall/info', { id }, { needAuth: false });
}

export function createMallOrder(data) {
	return post('/app/pet/mall/order/create', {
		...data,
		idempotentKey: getIdempotentKey('order')
	});
}

export function createPointOrder(data) {
	return post('/app/pet/mall/order/exchange', {
		...data,
		idempotentKey: getIdempotentKey('exchange')
	});
}

export function fetchMyOrders(params = {}) {
	return post('/app/pet/mall/order/page', { page: 1, limit: 20, ...params });
}

export function cancelOrder(orderId) {
	return post('/app/pet/mall/order/cancel', { id: orderId, idempotentKey: getIdempotentKey('cancel') });
}

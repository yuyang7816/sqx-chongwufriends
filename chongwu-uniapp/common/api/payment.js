import { post } from '@/common/request';
import { getIdempotentKey, getTerminal } from '@/common/auth-guard';

export function fetchPayChannels() {
	return post('/app/pet/payment/channels', {});
}

export function createPayment(data) {
	return post('/app/pet/payment/create', {
		...data,
		terminal: getTerminal(),
		idempotentKey: getIdempotentKey('pay')
	});
}

export function queryPayment(payNo) {
	return post('/app/pet/payment/query', { payNo });
}

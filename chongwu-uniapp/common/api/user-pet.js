import { get, post } from '@/common/request';
import { getIdempotentKey } from '@/common/auth-guard';

/** GET /app/pet/asset/my — 字段：points / coins / loveValue / levelName */
export function fetchUserAsset() {
	return get('/app/pet/asset/my');
}

export function fetchNotificationConfig() {
	return get('/app/pet/user/notification');
}

export function saveNotificationConfig(data) {
	return post('/app/pet/user/notification/save', { ...data, idempotentKey: getIdempotentKey('notify') });
}

export function fetchWecomConfig() {
	return get('/app/pet/user/wecom');
}

export function submitCancelApply(reason) {
	return post('/app/pet/user/cancelApply', { reason, idempotentKey: getIdempotentKey('cancel') });
}

export function updatePetProfile(data) {
	return post('/app/pet/user/update', { ...data, idempotentKey: getIdempotentKey('profile') });
}

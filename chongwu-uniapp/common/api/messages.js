import { post } from '@/common/request';

function toPageResult(data) {
	return {
		records: data?.records || data?.list || [],
		total: Number(data?.total || 0),
		current: Number(data?.current || 1),
		pages: Number(data?.pages || 0),
	};
}

export function fetchMessagePage(params = {}) {
	return post('/app/messages/page', {
		page: params.page || 1,
		limit: params.limit || 20,
		type: params.type,
		isRead: params.isRead,
	}).then(toPageResult);
}

export function fetchUnreadMessageCount() {
	return post('/app/messages/unread/count', {}).then((data) => Number(data || 0));
}

export function markMessageRead(id) {
	return post('/app/messages/read', { id });
}

export function markAllMessagesRead(params = {}) {
	return post('/app/messages/read/all', params).then((data) => Number(data || 0));
}

export function resolveMessagePostId(item = {}) {
	if (item.dataId) return Number(item.dataId);
	const url = String(item.url || '');
	const match = url.match(/(?:post\/detail\?id=|id=)(\d+)/i);
	return match ? Number(match[1]) : null;
}

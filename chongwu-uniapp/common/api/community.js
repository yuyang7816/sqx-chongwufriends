import { post } from '@/common/request';
import { getIdempotentKey } from '@/common/auth-guard';

function toPageResult(data) {
	return {
		records: data?.records || data?.list || [],
		total: Number(data?.total || 0),
		current: Number(data?.current || 1),
		pages: Number(data?.pages || 0),
	};
}

export function fetchPostPage(params = {}) {
	return post('/app/pet/community/page', {
		page: params.page || 1,
		limit: params.limit || 10,
		...params,
	}).then(toPageResult);
}

export function fetchCommunityConfig() {
	return post('/app/pet/community/config', {});
}

export function fetchMyPostPage(params = {}) {
	return post('/app/pet/community/my/page', {
		page: params.page || 1,
		limit: params.limit || 10,
		...params,
	}).then(toPageResult);
}

export function fetchMyPostSummary() {
	return post('/app/pet/community/my/summary', {});
}

export function fetchPostInfo(id, recordView = true) {
	return post('/app/pet/community/info', { id, recordView });
}

export function publishPost(data) {
	return post('/app/pet/community/publish', {
		...data,
		idempotentKey: getIdempotentKey('post')
	});
}

export function updatePost(data) {
	return post('/app/pet/community/update', {
		...data,
		idempotentKey: getIdempotentKey('updatePost')
	});
}

export function deletePost(postId) {
	return post('/app/pet/community/delete', {
		id: postId,
		idempotentKey: getIdempotentKey('deletePost')
	});
}

export function likePost(postId) {
	return post('/app/pet/community/like', { postId, idempotentKey: getIdempotentKey('like') });
}

export function unlikePost(postId) {
	return post('/app/pet/community/unlike', { postId, idempotentKey: getIdempotentKey('unlike') });
}

export function sharePost(postId) {
	return post('/app/pet/community/share', { postId, idempotentKey: getIdempotentKey('share') });
}

export function fetchCommentPage(postId, page = 1, limit = 20) {
	return post('/app/pet/community/comment/page', { postId, page, limit }).then(toPageResult);
}

export function saveComment(postId, content) {
	return post('/app/pet/community/comment/save', {
		postId,
		content,
		idempotentKey: getIdempotentKey('comment')
	});
}

export function deleteComment(commentId) {
	return post('/app/pet/community/comment/delete', {
		id: commentId,
		idempotentKey: getIdempotentKey('deleteComment')
	});
}

export function submitComplaint(data) {
	return post('/app/pet/community/complaint', {
		...data,
		idempotentKey: getIdempotentKey('complaint'),
	});
}

export function fetchMyComplaintPage(params = {}) {
	return post('/app/pet/community/complaint/page', {
		page: params.page || 1,
		limit: params.limit || 10,
		...params,
	}).then(toPageResult);
}

/** @deprecated 请使用 submitComplaint */
export function reportContent(targetType, targetId, reason) {
	return submitComplaint({ targetType, targetId, reason });
}

export function fetchStoreList() {
	return post('/app/pet/store/list', {}).then((data) => data?.records || data?.list || data || []);
}

export function fetchPetList() {
	return post('/app/pet/archive/list', {}).then((data) => data?.records || data?.list || data || []);
}

function resolveUploadUrl(data) {
	if (!data) return '';
	if (typeof data === 'string') return data;
	return data.url || data.src || data.path || data.fileUrl || data.imgUrl || '';
}

export { resolveUploadUrl };

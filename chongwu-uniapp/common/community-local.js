const STORAGE_KEY = 'myCommunityPosts';

export function getLocalCommunityPosts() {
	const list = uni.getStorageSync(STORAGE_KEY);
	return Array.isArray(list) ? list : [];
}

export function saveLocalCommunityPosts(list = []) {
	uni.setStorageSync(STORAGE_KEY, Array.isArray(list) ? list : []);
}

export function upsertLocalCommunityPost(post = {}) {
	const list = getLocalCommunityPosts();
	const index = list.findIndex(item => String(item.id) === String(post.id));
	if (index >= 0) {
		list[index] = {
			...list[index],
			...post
		};
	} else {
		list.unshift(post);
	}
	saveLocalCommunityPosts(list);
	return list;
}

export function removeLocalCommunityPost(postId) {
	const list = getLocalCommunityPosts().filter(item => String(item.id) !== String(postId));
	saveLocalCommunityPosts(list);
	return list;
}

import { fetchUnreadMessageCount } from '@/common/api/messages.js';

/** 朋友圈 Tab 在 tabBar.list 中的 index */
export const MESSAGE_TAB_INDEX = 1;

export function clearMessageTabBadge() {
	try {
		uni.removeTabBarBadge({ index: MESSAGE_TAB_INDEX });
	} catch (e) {
		/* 非 Tab 页或部分端可能不可用 */
	}
}

export async function syncMessageTabBadge() {
	const token = uni.getStorageSync('token');
	if (!token) {
		clearMessageTabBadge();
		return 0;
	}
	try {
		const count = Number(await fetchUnreadMessageCount()) || 0;
		if (count > 0) {
			uni.setTabBarBadge({
				index: MESSAGE_TAB_INDEX,
				text: count > 99 ? '99+' : String(count),
			});
		} else {
			clearMessageTabBadge();
		}
		return count;
	} catch (e) {
		return 0;
	}
}

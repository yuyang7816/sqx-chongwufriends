import config from '@/common/config.js';
import { getMyInvitationCode } from '@/common/invite.js';

function appendQuery(path, params = {}) {
	const query = Object.keys(params)
		.filter((key) => params[key] !== undefined && params[key] !== null && String(params[key]).trim() !== '')
		.map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(String(params[key]).trim())}`)
		.join('&');
	if (!query) return path;
	return path.includes('?') ? `${path}&${query}` : `${path}?${query}`;
}

/** 商品详情小程序/页面路径（无前导域名），携带商品 id + 邀请码 */
export function getProductSharePath(productId, invitationCode) {
	const id = Number(productId || 0);
	const code = String(invitationCode || getMyInvitationCode() || '').trim();
	return appendQuery('/packageA/product/detail', {
		id,
		...(code ? { invitationCode: code } : {})
	});
}

/** H5 可访问的完整商品链接 */
export function getProductH5ShareUrl(productId, invitationCode) {
	const path = getProductSharePath(productId, invitationCode);
	let origin = String(config.H5HOST || '').replace(/\/+$/, '');
	// #ifdef H5
	if (typeof window !== 'undefined' && window.location?.origin) {
		origin = window.location.origin;
	}
	// #endif
	if (!origin) origin = 'https://cwjpy.xianmxkj.com';
	return `${origin}${path}`;
}

function copyText(text, successTitle = '链接已复制') {
	return new Promise((resolve) => {
		uni.setClipboardData({
			data: String(text || ''),
			success: () => {
				uni.showToast({ title: successTitle, icon: 'success' });
				resolve(true);
			},
			fail: () => {
				uni.showToast({ title: '复制失败', icon: 'none' });
				resolve(false);
			}
		});
	});
}

/** 复制分享文案（供页面弹框调用） */
export function copyShareText(text, successTitle = '链接已复制') {
	return copyText(text, successTitle);
}

/** 微信小程序：由 open-type=share 触发，这里仅兜底提示 */
function shareProductOnMp() {
	uni.showToast({ title: '请点击右上角分享', icon: 'none' });
}

/** App：跳转微信小程序商品页，便于原生分享 */
function shareProductOnApp({ productId, invitationCode, title, summary, imageUrl, h5Url }) {
	const path = getProductSharePath(productId, invitationCode).replace(/^\//, '');
	const appId = config.MP_APPID;
	const originalId = String(config.MP_ORIGINAL_ID || '').trim();
	const fallbackCopy = () => copyText(h5Url, '已复制H5链接，可发给好友');

	// #ifdef APP-PLUS
	const launchByWeixinSdk = () => new Promise((resolve) => {
		if (!originalId) {
			resolve(false);
			return;
		}
		try {
			plus.share.getServices((services) => {
				const weixin = (services || []).find((item) => item.id === 'weixin');
				if (!weixin || typeof weixin.launchMiniProgram !== 'function') {
					resolve(false);
					return;
				}
				weixin.launchMiniProgram({
					id: originalId,
					path,
					type: 0
				}, () => resolve(true), () => resolve(false));
			}, () => resolve(false));
		} catch (e) {
			resolve(false);
		}
	});

	const shareMiniProgramCard = () => new Promise((resolve) => {
		uni.share({
			provider: 'weixin',
			scene: 'WXSceneSession',
			type: 5,
			title: title || '商品详情',
			summary: summary || '',
			imageUrl: imageUrl || '',
			miniProgram: {
				id: originalId || appId,
				path,
				type: 0,
				webUrl: h5Url
			},
			success: () => resolve(true),
			fail: () => resolve(false)
		});
	});

	(async () => {
		if (await launchByWeixinSdk()) {
			uni.showToast({ title: '已打开小程序', icon: 'none' });
			return;
		}
		if (await shareMiniProgramCard()) return;
		await fallbackCopy();
	})();
	// #endif

	// #ifndef APP-PLUS
	fallbackCopy();
	// #endif
}

/**
 * 商品详情分享
 * - H5：由页面弹框展示链接（此处返回链接，不做系统分享）
 * - 小程序：依赖原生 button open-type=share
 * - App：跳转小程序分享（失败则复制 H5 链接）
 */
export function shareProductDetail(payload = {}) {
	const productId = payload.productId;
	const invitationCode = payload.invitationCode || getMyInvitationCode();
	const title = payload.title || '商品详情';
	const summary = payload.summary || '';
	const imageUrl = payload.imageUrl || '';
	const h5Url = getProductH5ShareUrl(productId, invitationCode);

	// #ifdef H5
	return Promise.resolve(h5Url);
	// #endif

	// #ifdef MP-WEIXIN
	shareProductOnMp();
	return Promise.resolve();
	// #endif

	// #ifdef APP-PLUS
	shareProductOnApp({ productId, invitationCode, title, summary, imageUrl, h5Url });
	return Promise.resolve();
	// #endif

	return copyText(h5Url, '链接已复制');
}

/** 小程序 onShareAppMessage 返回体 */
export function buildProductMpShareMessage(payload = {}) {
	const productId = payload.productId;
	const invitationCode = payload.invitationCode || getMyInvitationCode();
	const title = payload.title || '发现一个好物';
	const imageUrl = payload.imageUrl || '';
	return {
		title,
		path: getProductSharePath(productId, invitationCode),
		imageUrl
	};
}

/** 小程序朋友圈 query */
export function buildProductMpShareTimelineQuery(productId, invitationCode) {
	const code = String(invitationCode || getMyInvitationCode() || '').trim();
	const id = Number(productId || 0);
	return code ? `id=${id}&invitationCode=${encodeURIComponent(code)}` : `id=${id}`;
}

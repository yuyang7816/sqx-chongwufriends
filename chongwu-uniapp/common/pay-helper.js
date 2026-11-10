import { queryPayment } from '@/common/api/payment';
import { getTerminal } from '@/common/auth-guard.js';
import config from '@/common/config.js';

/**
 * 按端过滤可用支付渠道展示：
 * - 小程序：仅微信支付
 * - H5：仅支付宝
 * - APP：微信 + 支付宝
 */
export function filterPayChannelsByTerminal(channels = {}) {
	const terminal = getTerminal();
	const wechatEnabled = !!channels.wechatEnabled;
	const alipayEnabled = !!channels.alipayEnabled;
	const mockEnabled = !!channels.mockEnabled;

	if (terminal === 'mini') {
		return { wechatEnabled, alipayEnabled: false, mockEnabled, terminal };
	}
	if (terminal === 'h5') {
		return { wechatEnabled: false, alipayEnabled, mockEnabled, terminal };
	}
	return { wechatEnabled, alipayEnabled, mockEnabled, terminal };
}

/** 渠道接口失败时的端侧兜底展示 */
export function getFallbackPayChannels() {
	const terminal = getTerminal();
	if (terminal === 'h5') {
		return { wechatEnabled: false, alipayEnabled: true, mockEnabled: false, terminal };
	}
	if (terminal === 'app') {
		return { wechatEnabled: true, alipayEnabled: true, mockEnabled: false, terminal };
	}
	return { wechatEnabled: true, alipayEnabled: false, mockEnabled: false, terminal };
}

/** 现金支付默认选中项（不含积分兑换） */
export function resolveDefaultCashPayMethod(channels = {}) {
	const terminal = getTerminal();
	if (terminal === 'h5') {
		return channels.alipayEnabled ? 'alipay' : '';
	}
	if (terminal === 'mini') {
		return channels.wechatEnabled ? 'wechat' : '';
	}
	if (channels.wechatEnabled) return 'wechat';
	if (channels.alipayEnabled) return 'alipay';
	return '';
}

export function isMpWeixinTerminal() {
	return getTerminal() === 'mini';
}

/**
 * 支付完成后跳转结果页
 */
export function goPayResult(success, extra = {}) {
	const q = Object.entries({ success: success ? 1 : 0, ...extra })
		.map(([k, v]) => `${k}=${encodeURIComponent(v ?? '')}`)
		.join('&');
	uni.redirectTo({ url: `/packageA/payment/result?${q}` });
}

function parsePayParams(payVo) {
	if (!payVo?.payParams) return {};
	if (typeof payVo.payParams === 'object') return payVo.payParams;
	const raw = String(payVo.payParams);
	try {
		const parsed = JSON.parse(raw);
		if (parsed && typeof parsed === 'object') return parsed;
	} catch (e) {
		// ignore
	}
	if (raw.includes('alipay_sdk=')) {
		return { payType: 'app', orderStr: raw };
	}
	if (raw.trim().startsWith('<')) {
		return { payType: 'wap', payForm: raw };
	}
	return { raw };
}

function normalizeWxParams(params) {
	const pkg = params.package || params.packageValue;
	return {
		timeStamp: params.timeStamp,
		nonceStr: params.nonceStr,
		package: pkg,
		signType: params.signType || 'RSA',
		paySign: params.paySign
	};
}

/**
 * 小程序调起微信支付（参数由后端 payParams 解析）
 */
export function invokeWxPay(payVo) {
	return new Promise((resolve, reject) => {
		const params = parsePayParams(payVo);
		if (params.mock) {
			setTimeout(() => resolve(true), 300);
			return;
		}
		const wxParams = normalizeWxParams(params);
		// #ifdef MP-WEIXIN
		if (wxParams.timeStamp && wxParams.nonceStr && wxParams.package && wxParams.paySign) {
			uni.requestPayment({
				provider: 'wxpay',
				timeStamp: wxParams.timeStamp,
				nonceStr: wxParams.nonceStr,
				package: wxParams.package,
				signType: wxParams.signType,
				paySign: wxParams.paySign,
				success: () => resolve(true),
				fail: err => reject(err)
			});
			return;
		}
		// #endif
		reject(new Error('微信支付参数无效，请重新下单'));
	});
}

function buildAlipayWapUrl(payNo) {
	const base = String(config.APIHOST || '').replace(/\/$/, '');
	return `${base}/app/pet/payment/alipay/wap/${encodeURIComponent(payNo)}`;
}

/**
 * 调起支付宝支付
 */
export function invokeAlipay(payVo) {
	return new Promise((resolve, reject) => {
		const params = parsePayParams(payVo);
		if (params.mock) {
			setTimeout(() => resolve(true), 300);
			return;
		}
		const orderStr = params.orderStr || params.orderString;
		const tradeNO = params.tradeNO || params.tradeNo;
		const payNo = payVo?.payNo || '';

		// #ifdef MP-ALIPAY
		if (tradeNO && typeof my !== 'undefined' && my.tradePay) {
			my.tradePay({
				tradeNO,
				success: () => resolve(true),
				fail: err => reject(err)
			});
			return;
		}
		if (orderStr && typeof my !== 'undefined' && my.tradePay) {
			my.tradePay({
				orderStr,
				success: () => resolve(true),
				fail: err => reject(err)
			});
			return;
		}
		// #endif

		// #ifdef APP-PLUS
		if (orderStr) {
			uni.requestPayment({
				provider: 'alipay',
				orderInfo: orderStr,
				success: () => resolve(true),
				fail: err => reject(err)
			});
			return;
		}
		// #endif

		// #ifdef MP-WEIXIN
		if (params.payType === 'wap' && payNo) {
			const url = buildAlipayWapUrl(payNo);
			uni.navigateTo({
				url: `/packageA/payment/alipay-bridge?payNo=${encodeURIComponent(payNo)}&wapUrl=${encodeURIComponent(url)}`
			});
			resolve(true);
			return;
		}
		// #endif

		// #ifdef H5
		if (params.payForm) {
			const wrap = document.createElement('div');
			wrap.innerHTML = params.payForm;
			document.body.appendChild(wrap);
			const form = wrap.querySelector('form');
			if (form) {
				form.submit();
				resolve(true);
				return;
			}
		}
		// #endif

		reject(new Error('支付宝支付参数无效，请稍后重试'));
	});
}

export async function invokePay(payVo) {
	const channel = String(payVo?.payChannel || '').toLowerCase();
	if (channel === 'alipay' || channel === 'ali') {
		return invokeAlipay(payVo);
	}
	return invokeWxPay(payVo);
}

export async function pollPaySuccess(payNo, times = 8) {
	for (let i = 0; i < times; i++) {
		const res = await queryPayment(payNo);
		if (res?.status === 3) return true;
		await new Promise(r => setTimeout(r, 1000));
	}
	return false;
}

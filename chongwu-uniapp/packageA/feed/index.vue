<template>
	<view class="pet-page feed-page">
		<view class="pet-shell feed-shell">
			<view v-if="pageLoading" class="feed-state">
				<text class="feed-state__text">加载中...</text>
			</view>
			<view v-else-if="loadError" class="feed-state">
				<text class="feed-state__title">加载失败</text>
				<text class="feed-state__text">{{ loadError }}</text>
				<view class="feed-retry-btn" @tap="loadPage">重试</view>
			</view>
			<template v-else>
				<view class="feed-notice">
					<view class="feed-notice__icon">i</view>
					<text class="feed-notice__text">赠送后可在「我的-赠送记录」查看反馈视频，看看毛孩子吃得多开心吧！</text>
				</view>

				<view v-if="!productList.length" class="feed-empty">
					<view class="feed-empty__icon"></view>
					<text class="feed-empty__title">暂无可投喂商品</text>
					<text class="feed-empty__text">稍后再来看看吧～</text>
				</view>

				<view v-else class="feed-list">
					<view v-for="item in productList" :key="item.id" class="feed-card">
						<image class="feed-card__cover" :src="item.coverUrl || '/static/logo.png'" mode="aspectFill"></image>
						<view class="feed-card__body">
							<view class="feed-card__info">
								<text class="feed-card__name">{{ item.name }}</text>
								<text class="feed-card__desc">{{ item.desc }}</text>
								<text class="feed-card__price">¥{{ item.price.toFixed(1) }}</text>
							</view>
							<view class="feed-card__counter">
								<view class="counter-btn" :class="{ 'is-disabled': !(counts[item.id] > 0) }" @tap="decrease(item.id)">-</view>
								<text class="counter-num">{{ counts[item.id] || 0 }}</text>
								<view class="counter-btn plus" @tap="increase(item.id)">+</view>
							</view>
						</view>
					</view>
				</view>
			</template>
		</view>

		<view v-if="!pageLoading && !loadError && productList.length" class="feed-checkout-bar">
			<view class="feed-checkout-bar__summary">
				<text class="feed-checkout-bar__count">共 {{ totalCount }} 件商品</text>
				<text class="feed-checkout-bar__amount">¥ {{ totalPrice.toFixed(1) }}</text>
			</view>
			<view class="feed-checkout-bar__btn" @tap="submitGift">
				<text class="feed-checkout-bar__btn-icon">🎁</text>
				<text class="feed-checkout-bar__btn-text">下单赠送</text>
			</view>
		</view>

		<view v-if="showPopup" class="popup-mask" @tap="showPopup = false">
			<view class="gift-popup" :style="{ background: cardTheme }" @tap.stop>
				<view class="gift-popup-inner">
					<view class="gift-popup-head">
						<view class="gift-popup-title-wrap">
							<view class="gift-mail-icon">
								<up-icon name="email" size="18" color="#f07a7a"></up-icon>
								<text class="gift-mail-heart">❤</text>
							</view>
							<text class="gift-popup-title">给宠物留个言吧</text>
						</view>
						<view class="gift-popup-close" @tap="showPopup = false">
							<up-icon name="close" size="14" color="#9a8b7e"></up-icon>
						</view>
					</view>

					<text class="gift-label">收件宠物</text>
					<view class="gift-field gift-field-readonly">
						<text class="gift-field-text">{{ residentPetName || '暂无岛宠' }}</text>
					</view>

					<text class="gift-label">留言内容</text>
					<textarea
						v-model="messageToPet"
						class="gift-field gift-textarea"
						placeholder="写下你想对TA说的话～比如：要吃得开心哦！"
						:maxlength="200"
						auto-height
					/>

					<text class="gift-label">签名</text>
					<input
						v-model="signature"
						class="gift-field gift-input"
						:disabled="anonymous"
						:placeholder="anonymous ? '匿名' : '你的名字'"
					/>

					<view class="gift-anonymous" @tap="toggleAnonymous">
						<view class="gift-check" :class="{ active: anonymous }">
							<up-icon v-if="anonymous" name="checkmark" size="12" color="#fff"></up-icon>
						</view>
						<text class="gift-anonymous-text">匿名留言</text>
					</view>

					<text class="gift-label">卡片风格</text>
					<view class="gift-themes">
						<view
							v-for="item in cardThemes"
							:key="item"
							class="gift-theme-dot"
							:class="{ active: cardTheme === item }"
							:style="{ background: item }"
							@tap="cardTheme = item"
						></view>
					</view>

					<view class="gift-tip-row">
						<text class="gift-tip-heart">❤</text>
						<text class="gift-tip-text">您的留言会和礼物一起送到TA身边～</text>
					</view>

					<view class="gift-confirm" @tap="confirmGift">确认赠送</view>
				</view>
			</view>
		</view>

		<view v-if="showCheckout" class="checkout-mask" @tap="closeCheckout">
			<view class="checkout-sheet pet-card" @tap.stop>
				<text class="checkout-title">选择支付方式</text>
				<view class="checkout-summary">
					<text class="checkout-product">投喂赠送 · {{ residentPetName || '岛宠' }}</text>
					<text class="checkout-spec">共 {{ totalCount }} 件商品</text>
					<text class="checkout-amount">¥{{ totalPrice.toFixed(2) }}</text>
				</view>

				<view class="checkout-pay-list">
					<view
						v-for="item in payOptionList"
						:key="item.value"
						class="pay-option"
						:class="[
							`pay-option--${item.value}`,
							{ 'is-active': selectedPayMethod === item.value }
						]"
						@tap="selectPayMethod(item.value)"
					>
						<view class="pay-option-icon" :class="`pay-option-icon--${item.value}`">{{ item.icon }}</view>
						<view class="pay-option-main">
							<view class="pay-option-title-row">
								<text class="pay-option-title">{{ item.title }}</text>
								<text v-if="item.tag" class="pay-option-tag">{{ item.tag }}</text>
							</view>
							<text class="pay-option-desc">{{ item.desc }}</text>
						</view>
						<view class="pay-radio" :class="{ 'is-checked': selectedPayMethod === item.value }">
							<view v-if="selectedPayMethod === item.value" class="pay-radio-dot"></view>
						</view>
					</view>
				</view>

				<view v-if="!hasPayOption" class="checkout-empty">
					<text class="pet-muted">暂无可用支付方式，请联系管理员配置</text>
				</view>

				<view class="checkout-actions">
					<view class="checkout-cancel-btn" @tap="closeCheckout">取消</view>
					<view
						class="checkout-confirm-btn"
						:class="{ 'is-disabled': buying || !selectedPayMethod || !hasPayOption }"
						@tap="submitCheckout"
					>
						{{ buying ? '处理中...' : '确认支付' }}
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import {
	computed,
	reactive,
	ref
} from 'vue';
import { fetchFeedProducts, createFeedOrder } from '@/common/api/feed.js';
import { fetchIslandInfo } from '@/common/api/island.js';
import { createPayment, fetchPayChannels } from '@/common/api/payment.js';
import {
	filterPayChannelsByTerminal,
	getFallbackPayChannels,
	goPayResult,
	invokePay,
	isMpWeixinTerminal,
	pollPaySuccess,
	resolveDefaultCashPayMethod
} from '@/common/pay-helper.js';
import { ensureLogin } from '@/common/auth-guard.js';
import { resolveMediaUrl } from '@/common/media.js';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';

const productList = ref([]);
const residentPetId = ref(null);
const residentPetName = ref('');
const pageLoading = ref(false);
const loadError = ref('');
const buying = ref(false);
const showCheckout = ref(false);
const selectedPayMethod = ref('');
const payChannels = ref(getFallbackPayChannels());
const isMpWeixin = ref(isMpWeixinTerminal());

const loadPage = async () => {
	pageLoading.value = true;
	loadError.value = '';
	try {
		const list = await fetchFeedProducts();
		const records = list?.records || list || [];
		productList.value = records.map((item) => ({
			id: item.id,
			name: item.name,
			desc: item.description || item.desc || '',
			coverUrl: resolveMediaUrl(item.coverUrl, '/static/logo.png'),
			price: Number(item.price || item.pointPrice || 0)
		}));
		if (uni.getStorageSync('token')) {
			const info = await fetchIslandInfo();
			residentPetId.value = info?.residentPetId || info?.residents?.[0]?.id || null;
			residentPetName.value =
				info?.petName ||
				info?.residents?.[0]?.name ||
				info?.residents?.[0]?.petName ||
				'';
		}
	} catch (e) {
		loadError.value = e?.msg || '商品加载失败';
	} finally {
		pageLoading.value = false;
	}
};

onShow(loadPage);
onPullDownRefresh(async () => {
	try {
		await loadPage();
	} finally {
		uni.stopPullDownRefresh();
	}
});
const counts = reactive({});
const showPopup = ref(false);
const messageToPet = ref('');
const signature = ref('');
const anonymous = ref(false);
const savedSignature = ref('');
const cardThemes = ['#f9d8dc', '#f7d67c', '#cfeedd'];
const cardTheme = ref('#f9d8dc');

const getLoginUserName = () => String(uni.getStorageSync('userName') || '').trim();

const fillSignatureFromLogin = () => {
	const name = getLoginUserName();
	signature.value = name;
	savedSignature.value = name;
};

const toggleAnonymous = () => {
	anonymous.value = !anonymous.value;
	if (anonymous.value) {
		savedSignature.value = signature.value || getLoginUserName();
		signature.value = '';
	} else {
		signature.value = savedSignature.value || getLoginUserName();
	}
};

const totalCount = computed(() => Object.values(counts).reduce((sum, value) => sum + (Number(value) || 0), 0));
const totalPrice = computed(() => productList.value.reduce((sum, item) => sum + item.price * (counts[item.id] || 0), 0));

const showWechatPay = computed(() => !!payChannels.value.wechatEnabled);
const showAlipayPay = computed(() => !!payChannels.value.alipayEnabled);
const hasPayOption = computed(() => showWechatPay.value || showAlipayPay.value);
const payOptionList = computed(() => {
	const list = [];
	if (showWechatPay.value) {
		list.push({
			value: 'wechat',
			icon: '微',
			title: '微信支付',
			desc: isMpWeixin.value ? '安全快捷，小程序原生支付' : '使用微信完成付款',
			tag: isMpWeixin.value || payChannels.value.terminal === 'app' ? '推荐' : ''
		});
	}
	if (showAlipayPay.value) {
		list.push({
			value: 'alipay',
			icon: '支',
			title: '支付宝支付',
			desc: '跳转支付宝完成付款',
			tag: payChannels.value.terminal === 'h5' ? '推荐' : ''
		});
	}
	return list;
});

const loadPayMeta = async () => {
	try {
		const channels = await fetchPayChannels();
		payChannels.value = filterPayChannelsByTerminal({
			wechatEnabled: !!channels?.wechatEnabled,
			alipayEnabled: !!channels?.alipayEnabled,
			mockEnabled: !!channels?.mockEnabled
		});
	} catch (e) {
		payChannels.value = getFallbackPayChannels();
	}
};

const resolveDefaultPayMethod = () => {
	return resolveDefaultCashPayMethod(payChannels.value) || payOptionList.value[0]?.value || '';
};

const increase = id => {
	counts[id] = (counts[id] || 0) + 1;
};

const decrease = id => {
	counts[id] = Math.max((counts[id] || 0) - 1, 0);
};

const resetGiftForm = () => {
	Object.keys(counts).forEach(key => {
		counts[key] = 0;
	});
	messageToPet.value = '';
	anonymous.value = false;
	fillSignatureFromLogin();
};

const submitGift = async () => {
	if (!totalCount.value) {
		uni.showToast({
			title: '请先选择商品',
			icon: 'none'
		});
		return;
	}
	if (!ensureLogin()) return;
	if (!residentPetId.value || !residentPetName.value) {
		try {
			const info = await fetchIslandInfo();
			residentPetId.value = info?.residentPetId || info?.residents?.[0]?.id || null;
			residentPetName.value =
				info?.petName ||
				info?.residents?.[0]?.name ||
				info?.residents?.[0]?.petName ||
				'';
		} catch (e) {
			// ignore, confirm will tip again
		}
	}
	if (!residentPetId.value) {
		uni.showToast({ title: '请先设置岛宠常驻宠物', icon: 'none' });
		return;
	}
	anonymous.value = false;
	fillSignatureFromLogin();
	showPopup.value = true;
};

const confirmGift = async () => {
	if (!ensureLogin() || buying.value) return;
	if (!residentPetId.value) {
		uni.showToast({ title: '请先设置岛宠常驻宠物', icon: 'none' });
		return;
	}
	if (!messageToPet.value.trim()) {
		uni.showToast({ title: '请填写留言内容', icon: 'none' });
		return;
	}
	if (!anonymous.value && !signature.value.trim()) {
		uni.showToast({ title: '请填写签名', icon: 'none' });
		return;
	}
	await loadPayMeta();
	selectedPayMethod.value = resolveDefaultPayMethod();
	showPopup.value = false;
	showCheckout.value = true;
};

const closeCheckout = () => {
	if (buying.value) return;
	showCheckout.value = false;
	selectedPayMethod.value = '';
};

const selectPayMethod = (method) => {
	if (buying.value) return;
	selectedPayMethod.value = method;
};

const submitCheckout = () => {
	if (!ensureLogin() || buying.value) return;
	if (!selectedPayMethod.value) {
		uni.showToast({ title: '请选择支付方式', icon: 'none' });
		return;
	}
	confirmPay(selectedPayMethod.value);
};

const confirmPay = async (method) => {
	if (!ensureLogin() || buying.value) return;
	if (!residentPetId.value) {
		uni.showToast({ title: '请先设置岛宠常驻宠物', icon: 'none' });
		return;
	}

	const selected = productList.value.filter(p => (counts[p.id] || 0) > 0);
	const first = selected[0];
	const finalSignature = anonymous.value ? '匿名' : signature.value.trim();

	buying.value = true;
	try {
		const order = await createFeedOrder({
			petId: residentPetId.value,
			productName: first?.name || '投喂商品',
			amount: totalPrice.value,
			quantity: totalCount.value,
			message: messageToPet.value.trim(),
			signature: finalSignature,
			items: selected.map((p) => ({
				productId: p.id,
				quantity: counts[p.id] || 0
			}))
		});
		const orderNo = order?.orderNo;
		const pay = await createPayment({
			businessType: 'feed_order',
			bizOrderNo: orderNo,
			payChannel: method
		});
		try {
			await invokePay(pay);
			const ok = await pollPaySuccess(pay?.payNo);
			showCheckout.value = false;
			resetGiftForm();
			goPayResult(ok, { payNo: pay?.payNo, orderNo });
		} catch (payErr) {
			showCheckout.value = false;
			goPayResult(false, { payNo: pay?.payNo, orderNo });
		}
	} catch (e) {
		uni.showToast({ title: e?.message || e?.msg || '下单失败', icon: 'none' });
	} finally {
		buying.value = false;
	}
};
</script>

<style lang="scss">
.feed-shell {
	padding-bottom: 200rpx;
}

.feed-state {
	min-height: 320rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 16rpx;
}

.feed-state__title {
	font-size: 32rpx;
	font-weight: 700;
	color: #6e472b;
}

.feed-state__text {
	font-size: 26rpx;
	color: #a78f78;
	text-align: center;
}

.feed-retry-btn {
	margin-top: 8rpx;
	padding: 16rpx 44rpx;
	border-radius: 999rpx;
	background: linear-gradient(145deg, #c79662, #a67b4f);
	color: #fff;
	font-size: 26rpx;
	font-weight: 700;
}

.feed-notice {
	margin-top: 12rpx;
	display: flex;
	align-items: flex-start;
	gap: 14rpx;
	padding: 22rpx 24rpx;
	border-radius: 24rpx;
	background: #e9f6ee;
	border: 2rpx dashed #b4e2c2;
}

.feed-notice__icon {
	flex-shrink: 0;
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	background: #6fbf8a;
	color: #fff;
	font-size: 22rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
	line-height: 1;
}

.feed-notice__text {
	flex: 1;
	font-size: 24rpx;
	color: #5c8c69;
	line-height: 1.6;
}

.feed-empty {
	margin-top: 80rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12rpx;
}

.feed-empty__icon {
	width: 120rpx;
	height: 120rpx;
	border-radius: 32rpx;
	background: linear-gradient(145deg, #f7d67d, #efbb66);
	opacity: 0.7;
}

.feed-empty__title {
	font-size: 30rpx;
	font-weight: 700;
	color: #6e472b;
}

.feed-empty__text {
	font-size: 24rpx;
	color: #a78f78;
}

.feed-list {
	margin-top: 22rpx;
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.feed-card {
	display: flex;
	align-items: center;
	gap: 22rpx;
	padding: 22rpx;
	border-radius: 28rpx;
	background: #ffffff;
	border: 1rpx solid rgba(188, 146, 109, 0.12);
	box-shadow: 0 10rpx 28rpx rgba(163, 120, 79, 0.05);
}

.feed-card__cover {
	width: 160rpx;
	height: 160rpx;
	border-radius: 24rpx;
	background: #f6eadf;
	flex-shrink: 0;
}

.feed-card__body {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 14rpx;
}

.feed-card__info {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.feed-card__name {
	font-size: 30rpx;
	font-weight: 700;
	color: #6c4d36;
	line-height: 1.35;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.feed-card__desc {
	font-size: 24rpx;
	color: #b19072;
	line-height: 1.4;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.feed-card__price {
	font-size: 36rpx;
	font-weight: 800;
	color: #c79662;
	line-height: 1.2;
}

.feed-card__counter {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	gap: 18rpx;
}

.counter-btn {
	width: 52rpx;
	height: 52rpx;
	border-radius: 50%;
	background: #f3eee6;
	border: 1rpx solid #e8d8c4;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #8b613d;
	font-size: 30rpx;
	font-weight: 700;
	line-height: 1;
}

.counter-btn.is-disabled {
	color: #c9b6aa;
	background: #f7f3ee;
}

.counter-btn.plus {
	background: linear-gradient(145deg, #c79662, #a67b4f);
	color: #fff;
	border-color: transparent;
	box-shadow: 0 6rpx 14rpx rgba(199, 150, 98, 0.28);
}

.counter-num {
	min-width: 44rpx;
	text-align: center;
	font-size: 30rpx;
	font-weight: 700;
	color: #6c4d36;
}

.feed-checkout-bar {
	position: fixed;
	left: 24rpx;
	right: 24rpx;
	bottom: calc(20rpx + env(safe-area-inset-bottom));
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18rpx;
	padding: 18rpx 22rpx;
	border-radius: 28rpx;
	background: #ffffff;
	border: 1rpx solid rgba(188, 146, 109, 0.16);
	box-shadow: 0 12rpx 32rpx rgba(163, 120, 79, 0.12);
	z-index: 15;
}

.feed-checkout-bar__summary {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.feed-checkout-bar__count {
	font-size: 22rpx;
	color: #a78f78;
	line-height: 1.2;
}

.feed-checkout-bar__amount {
	font-size: 40rpx;
	font-weight: 800;
	color: #c79662;
	line-height: 1.1;
}

.feed-checkout-bar__btn {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	gap: 10rpx;
	padding: 22rpx 40rpx;
	border-radius: 999rpx;
	background: linear-gradient(145deg, #c79662, #a67b4f);
	color: #fff;
	box-shadow: 0 10rpx 24rpx rgba(199, 150, 98, 0.32);
}

.feed-checkout-bar__btn-icon {
	font-size: 30rpx;
	line-height: 1;
}

.feed-checkout-bar__btn-text {
	font-size: 30rpx;
	font-weight: 700;
	line-height: 1;
}

.popup-mask {
	position: fixed;
	inset: 0;
	background: rgba(69, 53, 42, 0.35);
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx 36rpx;
	z-index: 20;
}

.gift-popup {
	width: 100%;
	border-radius: 36rpx;
	padding: 10rpx;
	box-shadow: 0 16rpx 40rpx rgba(180, 120, 120, 0.18);
}

.gift-popup-inner {
	border: 3rpx dashed rgba(255, 255, 255, 0.95);
	border-radius: 30rpx;
	padding: 28rpx 26rpx 30rpx;
}

.gift-popup-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 18rpx;
}

.gift-popup-title-wrap {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.gift-mail-icon {
	position: relative;
	width: 44rpx;
	height: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.gift-mail-heart {
	position: absolute;
	right: -4rpx;
	top: -2rpx;
	font-size: 16rpx;
	color: #f07a7a;
	line-height: 1;
}

.gift-popup-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #5a4a42;
}

.gift-popup-close {
	width: 52rpx;
	height: 52rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.92);
	display: flex;
	align-items: center;
	justify-content: center;
}

.gift-label {
	display: block;
	font-size: 26rpx;
	color: #7a6a60;
	margin: 16rpx 0 12rpx;
}

.gift-field {
	width: 100%;
	box-sizing: border-box;
	background: rgba(255, 255, 255, 0.92);
	border-radius: 22rpx;
	padding: 22rpx 24rpx;
	font-size: 28rpx;
	color: #5a4a42;
}

.gift-field-readonly {
	min-height: 78rpx;
	display: flex;
	align-items: center;
}

.gift-field-text {
	font-size: 28rpx;
	color: #5a4a42;
}

.gift-input {
	height: 78rpx;
}

.gift-textarea {
	min-height: 160rpx;
	line-height: 1.6;
}

.gift-anonymous {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-top: 18rpx;
}

.gift-check {
	width: 34rpx;
	height: 34rpx;
	border-radius: 8rpx;
	border: 2rpx solid #c9b6aa;
	background: rgba(255, 255, 255, 0.8);
	display: flex;
	align-items: center;
	justify-content: center;
}

.gift-check.active {
	background: #c79662;
	border-color: #c79662;
}

.gift-anonymous-text {
	font-size: 26rpx;
	color: #7a6a60;
}

.gift-themes {
	display: flex;
	align-items: center;
	gap: 22rpx;
	margin-top: 4rpx;
}

.gift-theme-dot {
	width: 54rpx;
	height: 54rpx;
	border-radius: 50%;
	border: 4rpx solid rgba(255, 255, 255, 0.95);
}

.gift-theme-dot.active {
	box-shadow: 0 0 0 4rpx #c79662;
}

.gift-tip-row {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	margin-top: 28rpx;
}

.gift-tip-heart {
	font-size: 22rpx;
	color: #f07a7a;
}

.gift-tip-text {
	font-size: 24rpx;
	color: #9a7f72;
}

.gift-confirm {
	margin-top: 24rpx;
	height: 84rpx;
	border-radius: 999rpx;
	background: #c79662;
	color: #fff;
	font-size: 30rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
}

.checkout-mask {
	position: fixed;
	inset: 0;
	background: rgba(58, 40, 28, 0.42);
	display: flex;
	align-items: flex-end;
	z-index: 999;
}

.checkout-sheet {
	width: 100%;
	border-radius: 32rpx 32rpx 0 0;
	padding: 36rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
	background: #fffdf9;
	border: 2rpx solid rgba(236, 215, 191, 0.55);
	box-shadow: 0 -12rpx 40rpx rgba(163, 120, 79, 0.12);
}

.checkout-title {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #6d4c38;
	margin-bottom: 24rpx;
	text-align: center;
}

.checkout-summary {
	padding: 24rpx;
	border-radius: 24rpx;
	background: #faf3ea;
	margin-bottom: 24rpx;
}

.checkout-product {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: #6d4c38;
}

.checkout-spec {
	display: block;
	margin-top: 8rpx;
	font-size: 24rpx;
	color: #9f8a77;
}

.checkout-amount {
	display: block;
	margin-top: 12rpx;
	font-size: 36rpx;
	font-weight: 700;
	color: #c79662;
}

.checkout-pay-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.pay-option {
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 24rpx;
	border-radius: 24rpx;
	background: #ffffff;
	border: 2rpx solid #ecd7bf;
	box-shadow: 0 8rpx 20rpx rgba(163, 120, 79, 0.06);
}

.pay-option.is-active {
	border-color: #c79662;
	box-shadow: 0 10rpx 24rpx rgba(199, 150, 98, 0.16);
}

.pay-option--wechat {
	border-color: rgba(8, 198, 90, 0.28);
	background: linear-gradient(135deg, #ffffff 0%, #f3fff8 100%);
}

.pay-option--alipay {
	border-color: rgba(22, 119, 255, 0.22);
	background: linear-gradient(135deg, #ffffff 0%, #f3f8ff 100%);
}

.pay-option-icon {
	width: 72rpx;
	height: 72rpx;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	font-weight: 700;
	color: #fff;
	flex-shrink: 0;
}

.pay-option-icon--wechat {
	background: linear-gradient(180deg, #2fd074 0%, #08c65a 100%);
}

.pay-option-icon--alipay {
	background: linear-gradient(180deg, #4f9dff 0%, #1677ff 100%);
}

.pay-option-main {
	flex: 1;
	min-width: 0;
}

.pay-option-title-row {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.pay-option-title {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: #6d4c38;
}

.pay-option-tag {
	padding: 4rpx 12rpx;
	border-radius: 999rpx;
	background: rgba(8, 198, 90, 0.12);
	color: #08a84d;
	font-size: 20rpx;
	font-weight: 700;
	line-height: 1.2;
}

.pay-option-desc {
	display: block;
	margin-top: 6rpx;
	font-size: 24rpx;
	color: #9f8a77;
	line-height: 1.5;
}

.pay-radio {
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	border: 2rpx solid #d3a271;
	box-sizing: border-box;
	flex-shrink: 0;
	background: #fff;
	display: flex;
	align-items: center;
	justify-content: center;
}

.pay-radio.is-checked {
	border-color: #c79662;
	background: #c79662;
}

.pay-radio-dot {
	width: 14rpx;
	height: 14rpx;
	border-radius: 50%;
	background: #ffffff;
}

.checkout-empty {
	text-align: center;
	padding: 24rpx 0;
}

.checkout-actions {
	display: flex;
	align-items: center;
	gap: 20rpx;
	margin-top: 28rpx;
}

.checkout-cancel-btn {
	flex-shrink: 0;
	min-width: 160rpx;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 999rpx;
	background: #f5ebe0;
	color: #9f8a77;
	font-size: 28rpx;
	font-weight: 600;
}

.checkout-confirm-btn {
	flex: 1;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 999rpx;
	background: #c79662;
	color: #ffffff;
	font-size: 30rpx;
	font-weight: 700;
	box-shadow: 0 14rpx 28rpx rgba(199, 150, 98, 0.24);
}

.checkout-confirm-btn.is-disabled {
	opacity: 0.55;
}
</style>

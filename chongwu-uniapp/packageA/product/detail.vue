<template>
	<view class="pet-page product-page">
		<view class="product-nav" :style="navWrapStyle">
			<view class="product-nav__bar" :style="navBarStyle">
				<view class="product-nav__btn" @tap="goBack">
					<up-icon name="arrow-left" size="18" color="#8b613d"></up-icon>
				</view>
				<!-- #ifdef MP-WEIXIN -->
				<button
					v-if="isLoggedIn"
					class="product-nav__btn product-nav__share-btn"
					open-type="share"
					hover-class="none"
				>
					<up-icon name="share-square" size="18" color="#8b613d"></up-icon>
				</button>
				<view v-else class="product-nav__btn" @tap="shareProduct">
					<up-icon name="share-square" size="18" color="#8b613d"></up-icon>
				</view>
				<!-- #endif -->
				<!-- #ifndef MP-WEIXIN -->
				<view class="product-nav__btn" @tap="shareProduct">
					<up-icon name="share-square" size="18" color="#8b613d"></up-icon>
				</view>
				<!-- #endif -->
			</view>
		</view>

		<view v-if="pageLoading" class="product-loading" :style="{ paddingTop: contentTop + 'px' }">
			<text class="pet-muted">加载中...</text>
		</view>
		<template v-else>
			<view class="product-hero" :style="{ paddingTop: contentTop + 'px' }">
				<view class="product-hero__stage">
					<image class="product-hero__img" :src="productCover" mode="aspectFit"></image>
				</view>
			</view>

			<view class="product-body">
				<view class="product-card">
					<view class="product-head">
						<text class="product-title">{{ product.name }}</text>
						<text v-if="productTypeLabel" class="product-badge">{{ productTypeLabel }}</text>
					</view>

					<view class="price-row">
						<text class="product-price">¥{{ formatPrice(displayPrice) }}</text>
						<text v-if="showOriginalPrice" class="old-price">¥{{ formatPrice(product.originalPrice) }}</text>
					</view>

					<view v-if="canRedeem" class="points-banner">
						<view class="points-banner-icon">
							<up-icon name="coupon" size="14" color="#ffffff"></up-icon>
						</view>
						<text class="points-banner-text">可用 <text class="points-banner-num">{{ redeemPoint }}</text> 积分兑换</text>
					</view>

					<text class="product-desc">{{ product.description || '选用优质原料，搭配时蔬与主食配比，营养均衡，适口性极佳。' }}</text>

					<view v-if="specs.length" class="section-block">
						<view class="section-title">
							<view class="section-icon-wrap">
								<view class="section-ruler">
									<view class="section-ruler__body"></view>
									<view class="section-ruler__marks">
										<view class="section-ruler__mark"></view>
										<view class="section-ruler__mark is-mid"></view>
										<view class="section-ruler__mark"></view>
										<view class="section-ruler__mark is-mid"></view>
										<view class="section-ruler__mark"></view>
									</view>
								</view>
							</view>
							<text>规格选择</text>
						</view>
						<view class="spec-row">
							<view
								v-for="item in specs"
								:key="item.id || item.name"
								class="spec-pill"
								:class="{
									'is-active': currentSkuId === (item.id || item),
									'is-disabled': item.stock === 0,
									'is-static': !isMultiSku
								}"
								@tap="selectSku(item)"
							>
								{{ item.name || item }}
							</view>
						</view>
					</view>

					<view class="guide-card">
						<view class="guide-icon-wrap">
							<image class="guide-icon" src="/packageA/static/gmzy.png" mode="aspectFit"></image>
						</view>
						<view class="guide-copy">
							<text class="guide-title">购买指引</text>
							<text class="guide-text">此商品可前往门店购买 / 在线下单</text>
							<text class="guide-sub">门店现货充足，到店出示本页即可享受会员价 🐾</text>
						</view>
					</view>

					<view class="product-card-foot">
						<text class="paw">🐾</text>
						<text class="paw">🐾</text>
						<text class="paw">🐾</text>
					</view>
				</view>
			</view>

			<view class="product-buy-bar">
				<view class="product-buy-btn" :class="{ 'is-disabled': buying }" @tap="openCheckout">
					{{ buying ? '处理中...' : '立即购买' }}
				</view>
			</view>
		</template>

		<view v-if="showCheckout" class="checkout-mask" @tap="closeCheckout">
			<view class="checkout-sheet pet-card" @tap.stop>
				<text class="checkout-title">选择下单方式</text>
				<view class="checkout-summary">
					<text class="checkout-product">{{ product.name }}</text>
					<text v-if="currentSku" class="checkout-spec">规格：{{ currentSku.name }}</text>
					<text class="checkout-amount">¥{{ formatPrice(displayPrice) }}</text>
					<text v-if="canRedeem" class="checkout-points">或 {{ redeemPoint }} 积分兑换</text>
				</view>

				<view class="checkout-pay-list">
					<view v-for="item in payOptionList" :key="item.value" class="pay-option" :class="[
							`pay-option--${item.value}`,
							{ 'is-active': selectedPayMethod === item.value, 'is-disabled': item.disabled }
						]" @tap="selectPayMethod(item.value)">
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
					<view class="checkout-confirm-btn"
						:class="{ 'is-disabled': buying || !selectedPayMethod || !hasPayOption }" @tap="submitCheckout">
						{{ buying ? '处理中...' : '确认下单' }}
					</view>
				</view>
			</view>
		</view>

		<!-- #ifdef H5 -->
		<view v-if="showShareSheet" class="checkout-mask" @tap="closeShareSheet">
			<view class="checkout-sheet share-sheet pet-card" @tap.stop>
				<text class="checkout-title">分享商品</text>
				<text class="share-sheet-tip">复制下方链接发给好友</text>
				<view class="share-link-box">
					<text class="share-link-text" selectable>{{ shareLink }}</text>
				</view>
				<view class="checkout-actions">
					<view class="checkout-cancel-btn" @tap="closeShareSheet">取消</view>
					<view class="checkout-confirm-btn" @tap="copyShareLink">复制链接</view>
				</view>
			</view>
		</view>
		<!-- #endif -->
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from 'vue';
	import {
		fetchMallInfo,
		createMallOrder,
		createPointOrder
	} from '@/common/api/mall.js';
	import {
		createPayment,
		fetchPayChannels
	} from '@/common/api/payment.js';
	import {
		fetchUserAsset
	} from '@/common/api/user-pet.js';
	import {
		filterPayChannelsByTerminal,
		getFallbackPayChannels,
		goPayResult,
		invokePay,
		isMpWeixinTerminal,
		pollPaySuccess,
		resolveDefaultCashPayMethod
	} from '@/common/pay-helper.js';
	import {
		ensureLogin,
		getCurrentPageUrl
	} from '@/common/auth-guard.js';
	import {
		resolveMediaUrl
	} from '@/common/media.js';
	import {
		buildProductMpShareMessage,
		buildProductMpShareTimelineQuery,
		copyShareText,
		getProductH5ShareUrl,
		shareProductDetail
	} from '@/common/share.js';
	import {
		getMyInvitationCode,
		saveMyInvitationCode,
		savePendingInvitationCode
	} from '@/common/invite.js';
	import {
		fetchUserInfo
	} from '@/common/api/auth.js';
	import {
		onLoad,
		onShow,
		onShareAppMessage,
		onShareTimeline
	} from '@dcloudio/uni-app';

	const productId = ref(0);
	const specs = ref([]);
	const currentSkuId = ref(null);
	const productData = ref({});
	const pageLoading = ref(true);
	const buying = ref(false);
	const showCheckout = ref(false);
	const showShareSheet = ref(false);
	const shareLink = ref('');
	const selectedPayMethod = ref('');
	const userPoints = ref(0);
	const payChannels = ref(getFallbackPayChannels());
	const isMpWeixin = ref(isMpWeixinTerminal());
	const isLoggedIn = ref(!!uni.getStorageSync('token'));
	const myInvitationCode = ref(getMyInvitationCode());
	const statusBarHeight = ref(20);
	const navBarHeight = ref(44);
	const navRightGap = ref(12);

	const navWrapStyle = computed(() => ({
		paddingTop: `${statusBarHeight.value}px`
	}));
	const navBarStyle = computed(() => ({
		height: `${navBarHeight.value}px`,
		paddingRight: `${navRightGap.value}px`
	}));
	const contentTop = computed(() => statusBarHeight.value + navBarHeight.value + 8);

	const initCustomNav = () => {
		try {
			const sys = uni.getSystemInfoSync() || {};
			statusBarHeight.value = Number(sys.statusBarHeight || 20);
			navBarHeight.value = 44;
			navRightGap.value = 12;
			// #ifdef MP-WEIXIN
			const menu = uni.getMenuButtonBoundingClientRect?.();
			if (menu?.width) {
				navBarHeight.value = Math.max(44, menu.height + (menu.top - statusBarHeight.value) * 2);
				navRightGap.value = Math.max(12, (sys.windowWidth || 375) - menu.left + 8);
			}
			// #endif
		} catch (e) {
			statusBarHeight.value = 20;
			navBarHeight.value = 44;
			navRightGap.value = 12;
		}
	};

	const loadProduct = async (id) => {
		pageLoading.value = true;
		try {
			const info = await fetchMallInfo(id);
			productData.value = {
				...info,
				coverUrl: resolveMediaUrl(info?.coverUrl)
			};
			const skuList = info?.skuList || [];
			if (skuList.length) {
				specs.value = skuList.map((sku) => ({
					id: sku.id,
					name: sku.skuName || sku.name || '标准装',
					price: sku.price,
					pointPrice: sku.pointPrice,
					stock: sku.stock
				}));
				const defaultSku = skuList.find((sku) => sku.id === info.defaultSkuId) || skuList[0];
				currentSkuId.value = defaultSku?.id || skuList[0].id;
			} else {
				specs.value = [{
					id: info.defaultSkuId,
					name: '标准装'
				}];
				currentSkuId.value = info.defaultSkuId;
			}
		} catch (e) {
			uni.showToast({
				title: e?.message || '商品加载失败',
				icon: 'none'
			});
		} finally {
			pageLoading.value = false;
		}
	};

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
		if (uni.getStorageSync('token')) {
			try {
				const asset = await fetchUserAsset();
				userPoints.value = Number(asset?.points || 0);
			} catch (e) {
				userPoints.value = 0;
			}
		}
	};

	const requireLogin = () => ensureLogin(getCurrentPageUrl(), true);

	const syncLoginState = async () => {
		isLoggedIn.value = !!uni.getStorageSync('token');
		myInvitationCode.value = getMyInvitationCode();
		if (!isLoggedIn.value) return;
		if (myInvitationCode.value) return;
		try {
			const info = await fetchUserInfo();
			if (info?.invitationCode) {
				saveMyInvitationCode(info.invitationCode);
				myInvitationCode.value = info.invitationCode;
			}
		} catch (e) {
			// ignore
		}
	};

	onLoad(async (option) => {
		initCustomNav();
		const inviteCode = option.invitationCode || option.inviteCode || option.superiorInvitationCode || '';
		if (inviteCode) {
			savePendingInvitationCode(inviteCode);
		}
		productId.value = Number(option.id || 0);
		if (!productId.value) {
			uni.showToast({
				title: '商品不存在',
				icon: 'none'
			});
			return;
		}
		await Promise.all([loadProduct(productId.value), loadPayMeta(), syncLoginState()]);
	});

	onShow(() => {
		syncLoginState();
	});

	const product = computed(() => productData.value || {});
	const productCover = computed(() => product.value?.coverUrl || '/static/logo.png');
	const productPoint = computed(() => Number(product.value?.pointPrice ?? product.value?.point ?? 0));
	/** 商品绑定类型（按 bizType 展示中文） */
	const BIZ_TYPE_LABELS = {
		1: '实物商品',
		2: '投喂商品',
		3: '次卡商品',
		4: '积分兑换'
	};
	const productTypeLabel = computed(() => {
		const bizType = Number(product.value?.bizType);
		if (BIZ_TYPE_LABELS[bizType]) return BIZ_TYPE_LABELS[bizType];
		const category = String(product.value?.category || '').trim().toLowerCase();
		if (category === 'feed') return BIZ_TYPE_LABELS[2];
		return String(product.value?.category || '').trim();
	});
	const currentSku = computed(() => specs.value.find((item) => (item.id || item) === currentSkuId.value) || null);
	const isMultiSku = computed(() => {
		const mode = Number(productData.value?.specMode || 0)
		if (mode === 2) return true
		if (mode === 1) return false
		return specs.value.length > 1
	})
	const displayPrice = computed(() => {
		const skuPrice = currentSku.value?.price;
		if (skuPrice != null && Number.isFinite(Number(skuPrice))) {
			return Number(skuPrice);
		}
		return Number(product.value?.price || 0);
	});
	const redeemPoint = computed(() => {
		const skuPoint = Number(currentSku.value?.pointPrice)
		if (Number.isFinite(skuPoint) && skuPoint > 0) return skuPoint
		const spuPoint = Number(productPoint.value)
		return Number.isFinite(spuPoint) && spuPoint > 0 ? spuPoint : 0
	});
	const canRedeem = computed(() => redeemPoint.value > 0);
	const pointsEnough = computed(() => userPoints.value >= redeemPoint.value);
	const showWechatPay = computed(() => !!payChannels.value.wechatEnabled);
	const showAlipayPay = computed(() => !!payChannels.value.alipayEnabled);
	const hasPayOption = computed(() =>
		showWechatPay.value || showAlipayPay.value || canRedeem.value
	);
	const payOptionList = computed(() => {
		const list = [];
		if (showWechatPay.value) {
			list.push({
				value: 'wechat',
				icon: '微',
				title: '微信支付',
				desc: isMpWeixin.value ? '安全快捷，小程序原生支付' : '使用微信完成付款',
				tag: isMpWeixin.value || payChannels.value.terminal === 'app' ? '推荐' : '',
				disabled: false
			});
		}
		if (showAlipayPay.value) {
			list.push({
				value: 'alipay',
				icon: '支',
				title: '支付宝支付',
				desc: '跳转支付宝完成付款',
				tag: payChannels.value.terminal === 'h5' ? '推荐' : '',
				disabled: false
			});
		}
		if (canRedeem.value) {
			list.push({
				value: 'points',
				icon: '积',
				title: '积分兑换',
				desc: `需要 ${redeemPoint.value} 积分，当前余额 ${userPoints.value}`,
				tag: '',
				disabled: !pointsEnough.value
			});
		}
		return list;
	});
	const showOriginalPrice = computed(() => {
		const original = Number(product.value?.originalPrice);
		const current = Number(displayPrice.value);
		return Number.isFinite(original) && original > 0 && original > current;
	});

	const formatPrice = (value) => {
		const num = Number(value);
		return Number.isFinite(num) ? num.toFixed(2) : '0.00';
	};

	const selectSku = (item) => {
		if (!isMultiSku.value) return;
		if (!requireLogin()) return;
		if (item.stock === 0) {
			uni.showToast({
				title: '该规格已售罄',
				icon: 'none'
			});
			return;
		}
		currentSkuId.value = item.id || item;
	};

	const closeShareSheet = () => {
		showShareSheet.value = false;
	};

	const copyShareLink = async () => {
		const ok = await copyShareText(shareLink.value, '链接已复制');
		if (ok) closeShareSheet();
	};

	const shareProduct = () => {
		if (!requireLogin()) return;
		const invite = myInvitationCode.value || getMyInvitationCode();
		// #ifdef H5
		shareLink.value = getProductH5ShareUrl(productId.value, invite);
		showShareSheet.value = true;
		return;
		// #endif
		shareProductDetail({
			productId: productId.value,
			invitationCode: invite,
			title: product.value?.name || '商品详情',
			summary: product.value?.description || '',
			imageUrl: productCover.value
		});
	};

	onShareAppMessage(() => buildProductMpShareMessage({
		productId: productId.value,
		invitationCode: myInvitationCode.value || getMyInvitationCode(),
		title: product.value?.name || '发现一个好物',
		imageUrl: productCover.value
	}));

	onShareTimeline(() => ({
		title: product.value?.name || '发现一个好物',
		query: buildProductMpShareTimelineQuery(productId.value, myInvitationCode.value || getMyInvitationCode()),
		imageUrl: productCover.value
	}));

	const goBack = () => {
		const pages = getCurrentPages();
		if (pages.length > 1) {
			uni.navigateBack();
			return;
		}
		uni.switchTab({ url: '/pages/shop/shop' });
	};

	const validateBeforeCheckout = () => {
		if (!currentSkuId.value) {
			uni.showToast({
				title: '请选择规格',
				icon: 'none'
			});
			return false;
		}
		if (currentSku.value?.stock === 0) {
			uni.showToast({
				title: '该规格已售罄',
				icon: 'none'
			});
			return false;
		}
		return true;
	};

	const resolveDefaultPayMethod = () => {
		const cash = resolveDefaultCashPayMethod(payChannels.value);
		if (cash) return cash;
		const enabled = payOptionList.value.find((item) => !item.disabled);
		return enabled?.value || payOptionList.value[0]?.value || '';
	};

	const openCheckout = async () => {
		if (!requireLogin() || buying.value) return;
		if (!validateBeforeCheckout()) return;
		await loadPayMeta();
		selectedPayMethod.value = resolveDefaultPayMethod();
		showCheckout.value = true;
	};

	const closeCheckout = () => {
		if (buying.value) return;
		showCheckout.value = false;
		selectedPayMethod.value = '';
	};

	const selectPayMethod = (method) => {
		if (buying.value) return;
		const option = payOptionList.value.find((item) => item.value === method);
		if (!option) return;
		if (option.disabled) {
			uni.showToast({
				title: `积分不足，需要${redeemPoint.value}积分`,
				icon: 'none'
			});
			return;
		}
		selectedPayMethod.value = method;
	};

	const submitCheckout = () => {
		if (!requireLogin() || buying.value) return;
		if (!selectedPayMethod.value) {
			uni.showToast({
				title: '请选择支付方式',
				icon: 'none'
			});
			return;
		}
		confirmPay(selectedPayMethod.value);
	};

	const confirmPay = async (method) => {
		if (!requireLogin() || buying.value) return;
		if (!validateBeforeCheckout()) return;

		if (method === 'points') {
			if (!canRedeem.value) {
				uni.showToast({
					title: '该商品不支持积分兑换',
					icon: 'none'
				});
				return;
			}
			if (!pointsEnough.value) {
				uni.showToast({
					title: `积分不足，需要${redeemPoint.value}积分`,
					icon: 'none'
				});
				return;
			}
		}

		buying.value = true;
		try {
			if (method === 'points') {
				uni.showLoading({
					title: '兑换中'
				});
				await createPointOrder({
					spuId: productId.value,
					skuId: currentSkuId.value,
					quantity: 1
				});
				uni.hideLoading();
				showCheckout.value = false;
				uni.showToast({
					title: '积分兑换成功',
					icon: 'success'
				});
				setTimeout(() => {
					uni.navigateTo({
						url: '/packageA/exchange/list'
					});
				}, 600);
				return;
			}

			const order = await createMallOrder({
				spuId: productId.value,
				skuId: currentSkuId.value,
				quantity: 1
			});
			const orderNo = order?.orderNo || order?.bizOrderNo;
			const pay = await createPayment({
				businessType: 'mall_order',
				bizOrderNo: orderNo,
				payChannel: method
			});
			try {
				await invokePay(pay);
				const ok = await pollPaySuccess(pay?.payNo);
				showCheckout.value = false;
				goPayResult(ok, {
					payNo: pay?.payNo,
					orderNo
				});
			} catch (payErr) {
				showCheckout.value = false;
				goPayResult(false, {
					payNo: pay?.payNo,
					orderNo
				});
			}
		} catch (e) {
			uni.showToast({
				title: e?.message || e?.msg || '下单失败',
				icon: 'none'
			});
		} finally {
			uni.hideLoading();
			buying.value = false;
		}
	};
</script>

<style lang="scss">
	.product-page {
		position: relative;
		min-height: 100vh;
		background: linear-gradient(180deg, #ffe6e8 0%, #fff4ec 42%, #f8efe4 100%);
	}

	.product-nav {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 80;
		pointer-events: none;
	}

	.product-nav__bar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-left: 24rpx;
		box-sizing: border-box;
	}

	.product-nav__btn {
		pointer-events: auto;
		width: 68rpx;
		height: 68rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0 8rpx 20rpx rgba(109, 76, 56, 0.1);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.product-nav__share-btn {
		margin: 0;
		padding: 0;
		border: none;
		line-height: 1;
		overflow: hidden;

		&::after {
			border: none;
		}
	}

	.product-loading {
		padding-bottom: 80rpx;
		text-align: center;
	}

	.product-hero {
		padding-left: 48rpx;
		padding-right: 48rpx;
		padding-bottom: 36rpx;
	}

	.product-hero__stage {
		width: 520rpx;
		height: 520rpx;
		max-width: 100%;
		margin: 0 auto;
		border-radius: 40rpx;
		background: #d7f0e5;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 16rpx 36rpx rgba(110, 168, 140, 0.14);
		overflow: hidden;
	}

	.product-hero__img {
		width: 78%;
		height: 78%;
	}

	.product-body {
		padding: 0 24rpx calc(168rpx + env(safe-area-inset-bottom));
	}

	.product-card {
		margin-top: -8rpx;
		padding: 40rpx 32rpx 32rpx;
		border-radius: 40rpx;
		background: #ffffff;
		box-shadow: 0 18rpx 40rpx rgba(139, 97, 61, 0.08);
	}

	.product-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 18rpx;
		margin-bottom: 20rpx;
	}

	.product-title {
		flex: 1;
		min-width: 0;
		font-size: 44rpx;
		font-weight: 800;
		color: #5a4030;
		line-height: 1.35;
	}

	.product-badge {
		flex-shrink: 0;
		margin-top: 12rpx;
		padding: 0 16rpx;
		height: 40rpx;
		line-height: 40rpx;
		border-radius: 10rpx;
		background: #f3e6d2;
		font-size: 22rpx;
		font-weight: 600;
		color: #8b613d;
		letter-spacing: 0.5rpx;
	}

	.price-row {
		display: flex;
		align-items: baseline;
		gap: 14rpx;
		margin-bottom: 22rpx;
	}

	.product-price {
		font-size: 48rpx;
		font-weight: 800;
		color: #b88955;
		line-height: 1;
	}

	.old-price {
		font-size: 24rpx;
		color: #b9ac9a;
		text-decoration: line-through;
	}

	.points-banner {
		display: flex;
		align-items: center;
		gap: 14rpx;
		padding: 18rpx 24rpx;
		margin-bottom: 24rpx;
		border-radius: 20rpx;
		background: #f5eee4;
	}

	.points-banner-icon {
		width: 42rpx;
		height: 42rpx;
		border-radius: 50%;
		background: #b88955;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.points-banner-text {
		font-size: 26rpx;
		color: #6d4c38;
		line-height: 1.4;
	}

	.points-banner-num {
		font-size: 30rpx;
		font-weight: 800;
		color: #8b5a2b;
		margin: 0 4rpx;
	}

	.product-desc {
		display: block;
		line-height: 1.75;
		font-size: 26rpx;
		color: #8a7a6c;
		margin-bottom: 10rpx;
	}

	.section-block {
		margin-top: 32rpx;
		padding-bottom: 32rpx;
		border-bottom: 2rpx dashed rgba(210, 188, 162, 0.72);
	}

	.section-title {
		display: flex;
		align-items: center;
		gap: 12rpx;
		font-size: 28rpx;
		font-weight: 700;
		color: #5a4030;
	}

	.section-icon-wrap {
		width: 36rpx;
		height: 36rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.section-ruler {
		width: 28rpx;
		height: 18rpx;
		position: relative;
		transform: rotate(-18deg);
	}

	.section-ruler__body {
		position: absolute;
		inset: 0;
		border-radius: 6rpx;
		background: linear-gradient(180deg, #d4a574 0%, #b8864e 100%);
		box-shadow: 0 2rpx 4rpx rgba(139, 97, 61, 0.25);
	}

	.section-ruler__marks {
		position: absolute;
		left: 4rpx;
		right: 4rpx;
		top: 3rpx;
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
	}

	.section-ruler__mark {
		width: 2rpx;
		height: 8rpx;
		background: rgba(255, 248, 238, 0.95);
		border-radius: 2rpx;
	}

	.section-ruler__mark.is-mid {
		height: 5rpx;
		opacity: 0.75;
	}

	.spec-row {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
		margin-top: 24rpx;
	}

	.spec-pill {
		min-width: 136rpx;
		height: 72rpx;
		padding: 0 36rpx;
		border-radius: 18rpx;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		background: #f3ebe0;
		color: #a5896f;
		font-size: 26rpx;
		font-weight: 600;
		box-sizing: border-box;
	}

	.spec-pill.is-active,
	.spec-pill.is-static {
		background: #b88955;
		color: #ffffff;
		box-shadow: 0 8rpx 18rpx rgba(184, 137, 85, 0.22);
	}

	.spec-pill.is-disabled {
		opacity: 0.42;
	}

	.guide-card {
		display: flex;
		align-items: flex-start;
		gap: 18rpx;
		margin-top: 32rpx;
		padding: 26rpx 24rpx;
		border-radius: 24rpx;
		background: #d4efdf;
	}

	.guide-icon-wrap {
		width: 68rpx;
		height: 68rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.82);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.guide-icon {
		width: 40rpx;
		height: 40rpx;
	}

	.guide-copy {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.guide-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #5a4030;
		line-height: 1.3;
	}

	.guide-text,
	.guide-sub {
		font-size: 24rpx;
		line-height: 1.55;
		color: #6f8578;
	}

	.product-card-foot {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 20rpx;
		margin-top: 32rpx;
		opacity: 0.28;
	}

	.paw {
		font-size: 22rpx;
		line-height: 1;
	}

	.product-buy-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 50;
		padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
		background: linear-gradient(180deg, rgba(248, 239, 228, 0) 0%, rgba(248, 239, 228, 0.9) 34%, #f8efe4 100%);
	}

	.product-buy-btn {
		height: 96rpx;
		border-radius: 28rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #b88955;
		color: #ffffff;
		font-size: 32rpx;
		font-weight: 700;
		letter-spacing: 2rpx;
		box-shadow: 0 16rpx 30rpx rgba(184, 137, 85, 0.28);
	}

	.product-buy-btn.is-disabled {
		opacity: 0.55;
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

	.checkout-spec,
	.checkout-points {
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
		color: #b88955;
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
		border-color: #b88955;
		box-shadow: 0 10rpx 24rpx rgba(184, 137, 85, 0.16);
	}

	.pay-option--wechat {
		border-color: rgba(8, 198, 90, 0.28);
		background: linear-gradient(135deg, #ffffff 0%, #f3fff8 100%);
	}

	.pay-option--alipay {
		border-color: rgba(22, 119, 255, 0.22);
		background: linear-gradient(135deg, #ffffff 0%, #f3f8ff 100%);
	}

	.pay-option--points {
		border-color: rgba(184, 137, 85, 0.28);
		background: linear-gradient(135deg, #ffffff 0%, #fffaf3 100%);
	}

	.pay-option.is-disabled {
		opacity: 0.45;
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

	.pay-option-icon--points {
		background: linear-gradient(180deg, #d0a06f 0%, #b88955 100%);
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
		border-color: #b88955;
		background: #b88955;
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
		background: #b88955;
		color: #ffffff;
		font-size: 30rpx;
		font-weight: 700;
		box-shadow: 0 14rpx 28rpx rgba(184, 137, 85, 0.24);
	}

	.checkout-confirm-btn.is-disabled {
		opacity: 0.55;
	}

	.share-sheet-tip {
		display: block;
		text-align: center;
		font-size: 24rpx;
		color: #9f8a77;
		margin: -8rpx 0 24rpx;
	}

	.share-link-box {
		padding: 24rpx;
		border-radius: 24rpx;
		background: #faf3ea;
		border: 2rpx solid rgba(236, 215, 191, 0.9);
		margin-bottom: 8rpx;
		max-height: 220rpx;
		overflow-y: auto;
	}

	.share-link-text {
		display: block;
		font-size: 26rpx;
		line-height: 1.6;
		color: #6d4c38;
		word-break: break-all;
	}
</style>

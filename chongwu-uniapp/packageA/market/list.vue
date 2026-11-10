<template>
	<view class="pet-page market-page">
		<view class="pet-shell">
			<view class="market-toolbar">
				<view class="switch-bar">
					<view
						v-for="tab in typeTabs"
						:key="tab.value"
						class="switch-item"
						:class="{ active: currentType === tab.value }"
						@tap="switchType(tab.value)"
					>
						<text class="switch-emoji">{{ tab.emoji }}</text>
						<text class="switch-label">{{ tab.label }}</text>
					</view>
				</view>
				<view class="asset-board">
					<view class="asset-chip is-coins">
						<view class="asset-dot"></view>
						<text class="asset-num">{{ coins }}</text>
						<text class="asset-unit">金币</text>
					</view>
					<view class="asset-chip is-points">
						<view class="asset-dot"></view>
						<text class="asset-num">{{ points }}</text>
						<text class="asset-unit">积分</text>
					</view>
				</view>
			</view>
			<view class="records-entry" @tap="goRecords">
				<text class="records-entry-text">我的兑换</text>
				<text class="records-entry-sub">查看兑换记录与亲密度增加</text>
				<text class="records-entry-arrow">›</text>
			</view>

			<view v-if="pageLoading" class="state-box"><text class="state-text">加载中...</text></view>
			<view v-else-if="loadError" class="state-box">
				<text class="state-title">加载失败</text>
				<text class="state-text">{{ loadError }}</text>
				<view class="pet-button state-btn" @tap="loadPage">重试</view>
			</view>
			<template v-else>
				<view v-if="!visibleProducts.length" class="state-box">
					<text class="state-title">当前分类暂无道具</text>
					<text class="state-text">试试切换上方「{{ emptyHintTab }}」看看</text>
				</view>
				<view v-else class="pet-grid-2 market-grid">
					<view v-for="item in visibleProducts" :key="item.id" class="market-card">
						<view class="market-cover-wrap">
							<image v-if="item.coverUrl" class="market-cover" :src="item.coverUrl" mode="aspectFit"></image>
							<view v-else class="market-icon" :class="'tone-' + (item.id % 5)"></view>
						</view>
						<text class="market-name">{{ item.name }}</text>
						<view class="market-price-wrap">
							<text v-if="item.canPayCoins" class="market-price">{{ item.priceCoins }}金币</text>
							<text v-if="item.canPayCoins && item.canPayPoints" class="market-price-or">或</text>
							<text v-if="item.canPayPoints" class="market-price is-points">{{ item.pricePoints }}积分</text>
						</view>
						<view class="durability-row">
							<text class="durability-label">耐久</text>
							<view class="durability-track">
								<view
									class="durability-value"
									:class="item.isDecor ? 'is-lavender' : 'is-mint'"
									:style="{ width: item.durabilityPercent + '%' }"
								></view>
							</view>
							<text class="durability-num">{{ item.durabilityLabel }}</text>
						</view>
						<text class="market-favor">+{{ item.favor }} 亲密度{{ item.isDecor ? '(装饰品)' : '' }}</text>
						<view class="buy-btn" :class="{ disabled: buying }" @tap="buyItem(item)">
							<text>{{ item.canPayCoins && item.canPayPoints ? '兑换' : '购买' }}</text>
							<image class="buy-cart-icon" src="/packageA/static/market-cart.png" mode="aspectFit"></image>
						</view>
					</view>
				</view>
				<view v-if="visibleProducts.length" class="list-end">
					<text class="list-end-text">没有更多了</text>
				</view>
			</template>
		</view>

		<!-- 兑换方式弹层 -->
		<view v-if="showPaySheet" class="pay-mask" @tap="closePaySheet">
			<view class="pay-sheet" @tap.stop>
				<view class="pay-sheet-handle"></view>
				<view class="pay-sheet-head">
					<text class="pay-sheet-title">选择兑换方式</text>
					<text class="pay-sheet-sub">{{ payTarget?.name || '集市道具' }}</text>
				</view>

				<view v-if="payTarget" class="pay-target">
					<view class="pay-target-cover">
						<image
							v-if="payTarget.coverUrl"
							class="pay-target-img"
							:src="payTarget.coverUrl"
							mode="aspectFit"
						></image>
						<view v-else class="pay-target-fallback"></view>
					</view>
					<view class="pay-target-info">
						<text class="pay-target-name">{{ payTarget.name }}</text>
						<text class="pay-target-favor">+{{ payTarget.favor }} 亲密度</text>
					</view>
				</view>

				<view class="pay-options">
					<view
						v-for="opt in payOptions"
						:key="opt.value"
						class="pay-option"
						:class="[
							`is-${opt.value}`,
							{ 'is-active': selectedPayType === opt.value, 'is-disabled': opt.disabled }
						]"
						@tap="selectPayOption(opt)"
					>
						<view class="pay-option-icon">{{ opt.icon }}</view>
						<view class="pay-option-main">
							<view class="pay-option-title-row">
								<text class="pay-option-title">{{ opt.title }}</text>
								<text v-if="opt.disabled" class="pay-option-tag is-warn">余额不足</text>
								<text v-else-if="selectedPayType === opt.value" class="pay-option-tag">已选</text>
							</view>
							<text class="pay-option-desc">{{ opt.desc }}</text>
						</view>
						<view class="pay-option-price">
							<text class="pay-option-cost">{{ opt.cost }}</text>
							<text class="pay-option-unit">{{ opt.unit }}</text>
						</view>
					</view>
				</view>

				<view class="pay-actions">
					<view class="pay-cancel" @tap="closePaySheet">取消</view>
					<view
						class="pay-confirm"
						:class="{ disabled: buying || !selectedPayType || selectedPayDisabled }"
						@tap="confirmPaySheet"
					>
						{{ buying ? '兑换中...' : '确认兑换' }}
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';
import { fetchMarketItems, buyMarketItem, fetchIslandInfo } from '@/common/api/island.js';
import { resolveMediaUrl } from '@/common/media.js';
import { ensureLogin } from '@/common/auth-guard.js';

const products = ref([]);
const currentType = ref('dog');
const coins = ref(0);
const points = ref(0);
const intimacy = ref(0);
const pageLoading = ref(false);
const loadError = ref('');
const buying = ref(false);
const refreshing = ref(false);

const showPaySheet = ref(false);
const payTarget = ref(null);
const selectedPayType = ref('');

const typeTabs = [
	{ label: '汪星人', value: 'dog', emoji: '🐶' },
	{ label: '喵星人', value: 'cat', emoji: '🐱' }
];

const DECOR_TYPES = /decor|ornament|accessory|装饰/i;

const mapProduct = (item) => {
	const itemType = item.itemType || '';
	const isDecor = DECOR_TYPES.test(itemType) || Number(item.durability) === -1;
	const num = Number(item.durability);
	const safeNum = Number.isFinite(num) && num >= 0 ? num : 0;
	const priceCoins = Number(item.priceCoins ?? item.price ?? 0) || 0;
	const pricePoints = Number(item.pricePoints ?? 0) || 0;
	const canPayCoins = item.canPayCoins != null ? !!item.canPayCoins : priceCoins > 0;
	const canPayPoints = item.canPayPoints != null ? !!item.canPayPoints : pricePoints > 0;
	return {
		id: item.id,
		name: item.name,
		coverUrl: resolveMediaUrl(item.coverUrl),
		priceCoins,
		pricePoints,
		canPayCoins,
		canPayPoints,
		favor: item.favor ?? 0,
		type: item.petType || item.type || 'dog',
		itemType,
		isDecor,
		durabilityLabel: isDecor ? '∞' : String(safeNum),
		durabilityPercent: isDecor ? 100 : Math.min(100, safeNum)
	};
};

const currentTab = computed(() => typeTabs.find((tab) => tab.value === currentType.value) || typeTabs[0]);

const emptyHintTab = computed(() => {
	const other = typeTabs.find((tab) => tab.value !== currentType.value);
	return other ? other.label : currentTab.value.label;
});

const visibleProducts = computed(() => {
	if (currentType.value === 'all') return products.value;
	return products.value.filter((item) => item.type === currentType.value || item.type === 'all');
});

const payOptions = computed(() => {
	const item = payTarget.value;
	if (!item) return [];
	const list = [];
	if (item.canPayCoins) {
		const disabled = coins.value < item.priceCoins;
		list.push({
			value: 'coins',
			icon: '币',
			title: '金币兑换',
			unit: '金币',
			cost: item.priceCoins,
			desc: `当前余额 ${coins.value} 金币`,
			disabled
		});
	}
	if (item.canPayPoints) {
		const disabled = points.value < item.pricePoints;
		list.push({
			value: 'points',
			icon: '积',
			title: '积分兑换',
			unit: '积分',
			cost: item.pricePoints,
			desc: `当前余额 ${points.value} 积分`,
			disabled
		});
	}
	return list;
});

const selectedPayDisabled = computed(() => {
	const opt = payOptions.value.find((o) => o.value === selectedPayType.value);
	return !opt || opt.disabled;
});

const switchType = (value) => {
	if (currentType.value === value) return;
	currentType.value = value;
};

const loadPage = async ({ silent = false } = {}) => {
	if (!ensureLogin('/packageA/market/list', true)) return;
	if (!silent) pageLoading.value = true;
	loadError.value = '';
	try {
		const [list, info] = await Promise.all([fetchMarketItems(), fetchIslandInfo()]);
		products.value = (list || []).map(mapProduct);
		if (info) {
			coins.value = info.coins ?? 0;
			points.value = info.points ?? 0;
			intimacy.value = info.intimacy ?? 0;
		}
	} catch (e) {
		if (!products.value.length) {
			loadError.value = e?.msg || '集市加载失败';
		} else {
			uni.showToast({ title: e?.msg || '刷新失败', icon: 'none' });
		}
	} finally {
		pageLoading.value = false;
	}
};

const openPaySheet = (item) => {
	payTarget.value = item;
	const preferred = payOptionsForItem(item).find((o) => !o.disabled) || payOptionsForItem(item)[0];
	selectedPayType.value = preferred?.value || '';
	showPaySheet.value = true;
};

const payOptionsForItem = (item) => {
	const list = [];
	if (item.canPayCoins) {
		list.push({ value: 'coins', disabled: coins.value < item.priceCoins });
	}
	if (item.canPayPoints) {
		list.push({ value: 'points', disabled: points.value < item.pricePoints });
	}
	return list;
};

const closePaySheet = () => {
	if (buying.value) return;
	showPaySheet.value = false;
	payTarget.value = null;
	selectedPayType.value = '';
};

const selectPayOption = (opt) => {
	if (buying.value) return;
	if (opt.disabled) {
		uni.showToast({ title: `${opt.unit}不足`, icon: 'none' });
		return;
	}
	selectedPayType.value = opt.value;
};

const confirmPaySheet = async () => {
	if (!payTarget.value || !selectedPayType.value || selectedPayDisabled.value || buying.value) return;
	await doBuy(payTarget.value, selectedPayType.value);
};

const doBuy = async (item, payType) => {
	const need = payType === 'points' ? item.pricePoints : item.priceCoins;
	const balance = payType === 'points' ? points.value : coins.value;
	const unit = payType === 'points' ? '积分' : '金币';
	if (balance < need) {
		uni.showToast({ title: `${unit}不足`, icon: 'none' });
		return;
	}
	buying.value = true;
	try {
		const info = await buyMarketItem(item.id, payType);
		if (info) {
			coins.value = info.coins ?? coins.value;
			points.value = info.points ?? points.value;
			intimacy.value = info.intimacy ?? intimacy.value;
		}
		showPaySheet.value = false;
		payTarget.value = null;
		selectedPayType.value = '';
		uni.showToast({ title: `已兑换${item.name}`, icon: 'success' });
	} catch (e) {
		uni.showToast({ title: e?.msg || '兑换失败', icon: 'none' });
	} finally {
		buying.value = false;
	}
};

const buyItem = async (item) => {
	if (!ensureLogin('/packageA/market/list', true)) return;
	if (buying.value) return;
	if (!item.canPayCoins && !item.canPayPoints) {
		uni.showToast({ title: '该道具暂未配置价格', icon: 'none' });
		return;
	}
	// 仅一种支付方式时仍走弹层，展示余额更清晰
	openPaySheet(item);
};

const goRecords = () => {
	if (!ensureLogin('/packageA/market/records', true)) return;
	uni.navigateTo({ url: '/packageA/market/records' });
};

onShow(() => loadPage());

onPullDownRefresh(async () => {
	if (refreshing.value) {
		uni.stopPullDownRefresh();
		return;
	}
	refreshing.value = true;
	try {
		await loadPage({ silent: true });
	} finally {
		refreshing.value = false;
		uni.stopPullDownRefresh();
	}
});

onReachBottom(() => {});
</script>

<style lang="scss">
.market-page {
	background: #f6eee3;
}

.market-toolbar {
	display: flex;
	align-items: stretch;
	gap: 14rpx;
	padding-top: 8rpx;
}

.records-entry {
	margin-top: 16rpx;
	padding: 18rpx 22rpx;
	border-radius: 24rpx;
	background: linear-gradient(180deg, #fffaf4 0%, #fff3e6 100%);
	border: 1rpx solid rgba(188, 146, 109, 0.16);
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.records-entry-text {
	font-size: 28rpx;
	font-weight: 700;
	color: #6e472b;
}

.records-entry-sub {
	flex: 1;
	font-size: 22rpx;
	color: #a78f78;
}

.records-entry-arrow {
	font-size: 34rpx;
	color: #c5b5a5;
	line-height: 1;
}

.switch-bar {
	flex: 1;
	min-width: 0;
	display: flex;
	align-items: center;
	padding: 8rpx;
	border-radius: 28rpx;
	background: linear-gradient(180deg, #f8f1e6 0%, #f1e6d6 100%);
	border: 1rpx solid rgba(188, 146, 109, 0.16);
	box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.7);
	box-sizing: border-box;
}

.switch-item {
	flex: 1;
	height: 72rpx;
	padding: 0 18rpx;
	border-radius: 22rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	color: #8a6b52;
	transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.switch-item.active {
	background: linear-gradient(180deg, #c9a07a 0%, #bc926d 100%);
	color: #fff;
	box-shadow: 0 8rpx 18rpx rgba(188, 146, 109, 0.28);
}

.switch-emoji {
	font-size: 28rpx;
	line-height: 1;
}

.switch-label {
	font-size: 28rpx;
	font-weight: 700;
	line-height: 1;
}

.asset-board {
	flex-shrink: 0;
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: 8rpx;
	padding: 10rpx 12rpx;
	border-radius: 24rpx;
	background: rgba(255, 252, 246, 0.92);
	border: 1rpx solid rgba(188, 146, 109, 0.14);
	box-shadow: 0 10rpx 22rpx rgba(163, 120, 79, 0.06);
}

.asset-chip {
	display: flex;
	align-items: center;
	gap: 8rpx;
	min-width: 148rpx;
	padding: 4rpx 8rpx;
}

.asset-dot {
	width: 14rpx;
	height: 14rpx;
	border-radius: 50%;
	flex-shrink: 0;
}

.asset-chip.is-coins .asset-dot {
	background: linear-gradient(145deg, #f0c27a, #c9964e);
	box-shadow: 0 0 0 3rpx rgba(201, 150, 78, 0.18);
}

.asset-chip.is-points .asset-dot {
	background: linear-gradient(145deg, #a8bfd8, #7a91ad);
	box-shadow: 0 0 0 3rpx rgba(122, 145, 173, 0.16);
}

.asset-num {
	font-size: 24rpx;
	font-weight: 700;
	color: #8b613d;
	line-height: 1;
}

.asset-chip.is-points .asset-num {
	color: #6a7f9c;
}

.asset-unit {
	font-size: 20rpx;
	font-weight: 600;
	color: #b38a63;
	line-height: 1;
}

.asset-chip.is-points .asset-unit {
	color: #8fa0b5;
}

.market-grid {
	margin-top: 26rpx;
}

.market-card {
	text-align: center;
	padding: 28rpx 20rpx 22rpx;
	border-radius: 36rpx;
	background: rgba(255, 255, 255, 0.98);
	box-shadow: 0 10rpx 28rpx rgba(163, 120, 79, 0.06);
}

.market-cover-wrap {
	width: 120rpx;
	height: 120rpx;
	margin: 0 auto 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.market-cover {
	width: 120rpx;
	height: 120rpx;
}

.market-icon {
	width: 100rpx;
	height: 100rpx;
	border-radius: 28rpx;
}

.market-icon.tone-0 {
	background: linear-gradient(135deg, #f7d97f, #fff3c6);
}
.market-icon.tone-1 {
	background: linear-gradient(135deg, #cdeedd, #e8f6ef);
}
.market-icon.tone-2 {
	background: linear-gradient(135deg, #f7d5db, #ffe8ec);
}
.market-icon.tone-3 {
	background: linear-gradient(135deg, #d9ebf7, #eef6fb);
}
.market-icon.tone-4 {
	background: linear-gradient(135deg, #efe7da, #f6eee3);
}

.market-name {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #5b4231;
	line-height: 1.3;
	margin-bottom: 8rpx;
}

.market-price-wrap {
	display: flex;
	align-items: center;
	justify-content: center;
	flex-wrap: wrap;
	gap: 6rpx;
	margin-bottom: 16rpx;
	min-height: 40rpx;
}

.market-price {
	font-size: 26rpx;
	font-weight: 600;
	color: #b7906b;
}

.market-price.is-points {
	color: #7a91ad;
}

.market-price-or {
	font-size: 22rpx;
	color: #c4a888;
}

.durability-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 12rpx;
	padding: 0 4rpx;
}

.durability-label,
.durability-num {
	font-size: 22rpx;
	color: #a8927c;
	flex-shrink: 0;
}

.durability-track {
	flex: 1;
	height: 12rpx;
	background: #efe3d8;
	border-radius: 999rpx;
	overflow: hidden;
}

.durability-value {
	height: 100%;
	border-radius: 999rpx;
}

.durability-value.is-mint {
	background: #d1e8e2;
}

.durability-value.is-lavender {
	background: #e8dcf4;
}

.market-favor {
	display: block;
	font-size: 24rpx;
	color: #e59393;
	margin-bottom: 18rpx;
	line-height: 1.4;
}

.buy-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	height: 72rpx;
	border-radius: 999rpx;
	background: #bc926d;
	color: #fff;
	font-size: 28rpx;
	font-weight: 700;
	box-shadow: 0 10rpx 22rpx rgba(188, 146, 109, 0.28);
}

.buy-cart-icon {
	width: 36rpx;
	height: 36rpx;
	flex-shrink: 0;
}

.buy-btn.disabled {
	opacity: 0.6;
}

.state-box {
	min-height: 240rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	padding: 48rpx 24rpx;
}

.state-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #8b613d;
}

.state-text {
	font-size: 24rpx;
	color: #b38a63;
	text-align: center;
	line-height: 1.5;
}

.state-btn {
	margin-top: 8rpx;
	padding: 0 32rpx;
}

.list-end {
	padding: 28rpx 0 12rpx;
	text-align: center;
}

.list-end-text {
	font-size: 24rpx;
	color: #b38a63;
}

/* ===== 兑换弹层 ===== */
.pay-mask {
	position: fixed;
	inset: 0;
	z-index: 1000;
	background: rgba(64, 42, 24, 0.42);
	display: flex;
	align-items: flex-end;
	justify-content: center;
}

.pay-sheet {
	width: 100%;
	padding: 16rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
	border-radius: 36rpx 36rpx 0 0;
	background: linear-gradient(180deg, #fffaf3 0%, #f7efe4 100%);
	box-shadow: 0 -12rpx 40rpx rgba(91, 66, 49, 0.12);
	animation: pay-sheet-up 0.28s ease-out;
}

.pay-sheet-handle {
	width: 64rpx;
	height: 8rpx;
	margin: 0 auto 20rpx;
	border-radius: 999rpx;
	background: rgba(188, 146, 109, 0.35);
}

.pay-sheet-head {
	margin-bottom: 22rpx;
}

.pay-sheet-title {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #5b4231;
	line-height: 1.3;
}

.pay-sheet-sub {
	display: block;
	margin-top: 6rpx;
	font-size: 24rpx;
	color: #b38a63;
}

.pay-target {
	display: flex;
	align-items: center;
	gap: 18rpx;
	padding: 18rpx 20rpx;
	margin-bottom: 22rpx;
	border-radius: 24rpx;
	background: rgba(255, 255, 255, 0.86);
	border: 1rpx solid rgba(188, 146, 109, 0.14);
}

.pay-target-cover {
	width: 88rpx;
	height: 88rpx;
	border-radius: 20rpx;
	background: #f6eee3;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
	flex-shrink: 0;
}

.pay-target-img {
	width: 72rpx;
	height: 72rpx;
}

.pay-target-fallback {
	width: 48rpx;
	height: 48rpx;
	border-radius: 14rpx;
	background: linear-gradient(135deg, #f7d97f, #fff3c6);
}

.pay-target-info {
	flex: 1;
	min-width: 0;
}

.pay-target-name {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: #5b4231;
	line-height: 1.3;
}

.pay-target-favor {
	display: block;
	margin-top: 6rpx;
	font-size: 24rpx;
	color: #e59393;
}

.pay-options {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
}

.pay-option {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 22rpx 20rpx;
	border-radius: 24rpx;
	background: rgba(255, 255, 255, 0.92);
	border: 2rpx solid rgba(188, 146, 109, 0.12);
	transition: border-color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.pay-option.is-active {
	border-color: #bc926d;
	background: #fff;
	box-shadow: 0 10rpx 24rpx rgba(188, 146, 109, 0.14);
}

.pay-option.is-points.is-active {
	border-color: #8fa0b5;
	box-shadow: 0 10rpx 24rpx rgba(122, 145, 173, 0.14);
}

.pay-option.is-disabled {
	opacity: 0.55;
}

.pay-option-icon {
	width: 64rpx;
	height: 64rpx;
	border-radius: 18rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 26rpx;
	font-weight: 700;
	flex-shrink: 0;
	color: #8b613d;
	background: linear-gradient(145deg, #f7e4c8, #eed4ae);
}

.pay-option.is-points .pay-option-icon {
	color: #6a7f9c;
	background: linear-gradient(145deg, #e4ecf5, #d3deea);
}

.pay-option-main {
	flex: 1;
	min-width: 0;
}

.pay-option-title-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.pay-option-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #5b4231;
}

.pay-option-tag {
	padding: 2rpx 10rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	font-weight: 600;
	color: #bc926d;
	background: rgba(188, 146, 109, 0.12);
}

.pay-option-tag.is-warn {
	color: #c97a6a;
	background: rgba(201, 122, 106, 0.12);
}

.pay-option-desc {
	display: block;
	margin-top: 6rpx;
	font-size: 22rpx;
	color: #b38a63;
	line-height: 1.4;
}

.pay-option.is-points .pay-option-desc {
	color: #8fa0b5;
}

.pay-option-price {
	display: flex;
	align-items: baseline;
	gap: 4rpx;
	flex-shrink: 0;
}

.pay-option-cost {
	font-size: 34rpx;
	font-weight: 700;
	color: #8b613d;
	line-height: 1;
}

.pay-option.is-points .pay-option-cost {
	color: #6a7f9c;
}

.pay-option-unit {
	font-size: 22rpx;
	color: #b38a63;
}

.pay-option.is-points .pay-option-unit {
	color: #8fa0b5;
}

.pay-actions {
	display: flex;
	gap: 16rpx;
	margin-top: 28rpx;
}

.pay-cancel,
.pay-confirm {
	flex: 1;
	height: 84rpx;
	border-radius: 999rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	font-weight: 700;
}

.pay-cancel {
	color: #8b613d;
	background: rgba(255, 255, 255, 0.85);
	border: 1rpx solid rgba(188, 146, 109, 0.28);
}

.pay-confirm {
	color: #fff;
	background: linear-gradient(180deg, #c9a07a 0%, #bc926d 100%);
	box-shadow: 0 10rpx 22rpx rgba(188, 146, 109, 0.28);
}

.pay-confirm.disabled {
	opacity: 0.55;
}

@keyframes pay-sheet-up {
	from {
		transform: translateY(40rpx);
		opacity: 0.6;
	}
	to {
		transform: translateY(0);
		opacity: 1;
	}
}
</style>

<template>
	<view class="pet-page gift-page">
		<view class="pet-shell">
			<!-- 顶部汇总 -->
			<view class="gift-hero">
				<view class="gift-hero__left">
					<view class="gift-hero__title-row">
						<text class="gift-hero__paw">🐾</text>
						<text class="gift-hero__title">共赠送 <text class="gift-hero__num is-orange">{{ summary.totalCount }}</text> 次 · 已反馈 <text class="gift-hero__num is-green">{{ summary.feedbackCount }}</text> 次</text>
					</view>
					<view class="gift-hero__sub-row">
						<text class="gift-hero__tag">🏷</text>
						<text class="gift-hero__sub">最近赠送：{{ summary.latestGiftText || '--' }}</text>
					</view>
				</view>
				<view class="gift-hero__icon">🎁</view>
			</view>

			<!-- 列表 -->
			<view v-if="loading && !giftList.length" class="gift-state">加载中...</view>
			<view v-else-if="!giftList.length" class="gift-state">{{ loadError || '暂无赠送记录' }}</view>
			<view v-else class="gift-list">
				<view v-for="item in giftList" :key="item.id" class="gift-card">
					<view class="gift-card__head">
						<text class="gift-card__date">{{ item.date }}</text>
						<view class="gift-status" :class="statusInfo(item.status).cls">
							<text class="gift-status__icon">{{ statusInfo(item.status).icon }}</text>
							<text class="gift-status__text">{{ statusInfo(item.status).label }}</text>
						</view>
					</view>

					<view class="gift-card__body">
						<view class="gift-card__gift-icon">🎁</view>
						<view class="gift-card__info">
							<view class="gift-card__name-row">
								<text class="gift-card__name">{{ item.pet || '岛宠' }}</text>
								<text class="gift-card__pet-icon">🐶</text>
							</view>
							<text class="gift-card__item">{{ item.item || item.productName || '' }}<text v-if="item.quantity > 1"> ×{{ item.quantity }}</text></text>
						</view>
					</view>

					<view v-if="item.note" class="gift-card__note-row">
						<text class="gift-card__bubble">💬</text>
						<text class="gift-card__note">{{ item.note }}</text>
					</view>

					<view v-if="statusInfo(item.status).key === 'done'" class="gift-card__foot" @tap="viewFeedback(item)">
						<text class="gift-card__link">查看视频反馈</text>
						<text class="gift-card__play">▶</text>
					</view>
				</view>
			</view>

			<view v-if="loadingMore" class="gift-tip">加载更多...</view>
			<view v-else-if="finished && giftList.length" class="gift-tip">没有更多了</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';
import { fetchGiftRecords, fetchGiftSummary } from '@/common/api/feed.js';

const giftList = ref([]);
const loading = ref(false);
const loadingMore = ref(false);
const finished = ref(false);
const page = ref(1);
const loadError = ref('');
const summary = ref({
	totalCount: 0,
	feedbackCount: 0,
	pendingCount: 0,
	latestGiftText: '--'
});

const mapGift = item => ({
	id: item.id,
	date: item.date || item.createTime?.slice(0, 10) || '',
	pet: item.pet || '',
	productName: item.productName || '',
	item: item.item || '',
	quantity: item.quantity ?? 1,
	note: item.note || item.message || '',
	status: item.statusText || item.status || '待反馈',
	videoUrl: item.videoUrl || ''
});

const statusInfo = (status) => {
	const s = String(status || '');
	if (s.includes('已反馈') || s.includes('完成') || s.includes('done')) {
		return { key: 'done', cls: 'is-success', icon: '✓', label: '已反馈' };
	}
	if (s.includes('过期') || s.includes('expired')) {
		return { key: 'expired', cls: 'is-gray', icon: '⏳', label: '已过期' };
	}
	return { key: 'pending', cls: 'is-warn', icon: '⏳', label: '预计3天内反馈' };
};

const loadSummary = async () => {
	try {
		const summaryRes = await fetchGiftSummary();
		summary.value = {
			totalCount: Number(summaryRes?.totalCount || 0),
			feedbackCount: Number(summaryRes?.feedbackCount || 0),
			pendingCount: Number(summaryRes?.pendingCount || 0),
			latestGiftText: summaryRes?.latestGiftText || '--'
		};
	} catch (e) {
		uni.showToast({ title: e?.msg || '汇总加载失败', icon: 'none' });
	}
};

const loadData = async (reset = true) => {
	if (reset) {
		page.value = 1;
		finished.value = false;
		if (!giftList.value.length) giftList.value = [];
	}
	if (finished.value && !reset) return;
	if (loading.value || loadingMore.value) return;
	if (reset) loading.value = true;
	else loadingMore.value = true;
	loadError.value = '';
	try {
		const pageRes = await fetchGiftRecords({ page: page.value, limit: 20 });
		const list = (pageRes?.records || pageRes || []).map(mapGift);
		giftList.value = reset ? list : giftList.value.concat(list);
		const total = Number(pageRes?.total || 0);
		finished.value = giftList.value.length >= total || list.length < 20;
		if (!finished.value) page.value += 1;
	} catch (e) {
		if (reset) giftList.value = [];
		loadError.value = e?.msg || e?.message || '赠送记录加载失败';
		uni.showToast({ title: loadError.value, icon: 'none' });
	} finally {
		loading.value = false;
		loadingMore.value = false;
	}
};

onShow(async () => {
	await loadSummary();
	await loadData(true);
});

onPullDownRefresh(async () => {
	try {
		await Promise.all([loadSummary(), loadData(true)]);
	} finally {
		uni.stopPullDownRefresh();
	}
});

onReachBottom(() => {
	if (!loading.value && !loadingMore.value) {
		loadData(false);
	}
});

const viewFeedback = item => {
	if (statusInfo(item.status).key !== 'done') {
		uni.showToast({ title: item.status, icon: 'none' });
		return;
	}
	uni.navigateTo({ url: `/packageA/gift/video?id=${item.id}` });
};
</script>

<style lang="scss">
.gift-page {
	--gift-orange: #d97a3a;
	--gift-brown: #8b5e3c;
	--gift-green: #4f8d5c;
	--gift-border: rgba(236, 215, 191, 0.55);
}

/* 顶部汇总 */
.gift-hero {
	margin-top: 8rpx;
	background: var(--pet-panel, #fffdf9);
	border-radius: 28rpx;
	border: 2rpx dashed var(--gift-orange);
	box-shadow: var(--pet-shadow);
	padding: 30rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 20rpx;
}

.gift-hero__left {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 14rpx;
}

.gift-hero__title-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.gift-hero__paw {
	font-size: 30rpx;
}

.gift-hero__title {
	font-size: 30rpx;
	font-weight: 700;
	color: #3a2c20;
}

.gift-hero__num {
	font-size: 36rpx;
	font-weight: 800;
}

.gift-hero__num.is-orange {
	color: var(--gift-orange);
}

.gift-hero__num.is-green {
	color: var(--gift-green);
}

.gift-hero__sub-row {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.gift-hero__tag {
	font-size: 22rpx;
}

.gift-hero__sub {
	font-size: 24rpx;
	color: #8a7a68;
}

.gift-hero__icon {
	font-size: 72rpx;
	opacity: 0.85;
}

/* 状态 */
.gift-state {
	text-align: center;
	padding: 50rpx 0;
	font-size: 26rpx;
	color: #a39382;
}

.gift-tip {
	text-align: center;
	padding: 20rpx 0;
	font-size: 24rpx;
	color: #b8a99a;
}

/* 列表 */
.gift-list > view + view {
	margin-top: 18rpx;
}

.gift-card {
	padding: 26rpx;
	border-radius: 28rpx;
	background: var(--pet-panel, #fffdf9);
	border: 2rpx solid var(--gift-border);
	box-shadow: var(--pet-shadow);
}

.gift-card__head {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 18rpx;
}

.gift-card__date {
	font-size: 24rpx;
	color: #a39382;
}

.gift-status {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	height: 40rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
	font-weight: 700;
}

.gift-status.is-success {
	background: #e7f0e8;
	color: var(--gift-green);
}

.gift-status.is-warn {
	background: #fce8d2;
	color: var(--gift-brown);
}

.gift-status.is-gray {
	background: #efeae3;
	color: #8a7a68;
}

.gift-status__icon {
	font-size: 22rpx;
}

.gift-card__body {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.gift-card__gift-icon {
	width: 64rpx;
	height: 64rpx;
	border-radius: 18rpx;
	background: #fce8d2;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 34rpx;
	flex-shrink: 0;
}

.gift-card__info {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.gift-card__name-row {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.gift-card__name {
	font-size: 32rpx;
	font-weight: 700;
	color: #3a2c20;
}

.gift-card__pet-icon {
	font-size: 24rpx;
}

.gift-card__item {
	font-size: 26rpx;
	color: #6a5a48;
}

.gift-card__note-row {
	display: flex;
	align-items: flex-start;
	gap: 8rpx;
	margin-top: 16rpx;
}

.gift-card__bubble {
	font-size: 22rpx;
	flex-shrink: 0;
}

.gift-card__note {
	font-size: 24rpx;
	color: #9a8a78;
	line-height: 1.5;
}

.gift-card__foot {
	display: flex;
	align-items: center;
	gap: 8rpx;
	margin-top: 18rpx;
	padding-top: 18rpx;
	border-top: 2rpx dashed #ece3d8;
}

.gift-card__link {
	font-size: 26rpx;
	font-weight: 700;
	color: var(--gift-brown);
}

.gift-card__play {
	width: 32rpx;
	height: 32rpx;
	border-radius: 8rpx;
	background: #4a90d9;
	color: #fff;
	font-size: 18rpx;
	display: inline-flex;
	align-items: center;
	justify-content: center;
}
</style>

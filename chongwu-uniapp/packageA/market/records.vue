<template>
	<view class="pet-page records-page">
		<view class="pet-shell">
		<view class="records-hero">
			<view class="records-hero__badge">岛宅集市</view>
			<text class="records-hero__title">周边兑换记录</text>
			<text class="records-hero__desc">岛宅集市兑换记录与每次增加的亲密度</text>
		</view>

			<view v-if="loading && !recordList.length" class="state-box">
				<text class="state-text">加载中...</text>
			</view>
			<view v-else-if="!recordList.length" class="state-box">
				<view class="state-empty-icon"></view>
				<text class="state-title">{{ loadError ? '加载失败' : '暂无兑换记录' }}</text>
				<text class="state-text">{{ loadError || '去集市兑换道具，记录会出现在这里' }}</text>
			</view>
			<view v-else class="records-list">
				<view v-for="item in recordList" :key="item.id" class="record-card">
					<view class="record-cover">
						<image v-if="item.coverUrl" class="record-cover__img" :src="item.coverUrl" mode="aspectFit"></image>
						<view v-else class="record-cover__fallback"></view>
					</view>
					<view class="record-body">
						<view class="record-body__top">
							<text class="record-name">{{ item.name }}</text>
							<view class="favor-chip">
								<text class="favor-chip__text">+{{ item.favor }} 亲密度</text>
							</view>
						</view>
						<view class="record-meta">
							<view class="pay-chip" :class="item.payCoins > 0 ? 'is-coins' : 'is-points'">
								<view class="pay-chip__dot"></view>
								<text class="pay-chip__text">{{ payText(item) }}</text>
							</view>
							<text class="record-time">{{ item.createTime || item.date || '-' }}</text>
						</view>
					</view>
				</view>
			</view>

			<view v-if="loadingMore" class="list-end">
				<text class="list-end-text">加载更多...</text>
			</view>
			<view v-else-if="finished && recordList.length" class="list-end">
				<text class="list-end-text">没有更多了</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';
import { fetchMarketRecords } from '@/common/api/island.js';

const recordList = ref([]);
const loading = ref(false);
const loadingMore = ref(false);
const finished = ref(false);
const page = ref(1);
const loadError = ref('');

const mapRecord = (item) => ({
	id: item.id,
	name: item.name || '集市道具',
	coverUrl: item.coverUrl || '',
	payCoins: item.payCoins ?? 0,
	payPoints: item.payPoints ?? 0,
	favor: item.favor ?? 0,
	date: item.date || '',
	createTime: item.createTime || ''
});

const payText = (item) => {
	if (item.payCoins > 0) return `${item.payCoins} 金币`;
	if (item.payPoints > 0) return `${item.payPoints} 积分`;
	return '已兑换';
};

const loadData = async (reset = true) => {
	if (reset) {
		page.value = 1;
		finished.value = false;
	}
	if (finished.value && !reset) return;
	if (loading.value || loadingMore.value) return;
	if (reset) loading.value = true;
	else loadingMore.value = true;
	loadError.value = '';
	try {
		const res = await fetchMarketRecords({
			page: page.value,
			limit: 20
		});
		const list = (res?.records || []).map(mapRecord);
		recordList.value = reset ? list : recordList.value.concat(list);
		const total = Number(res?.total || 0);
		finished.value = recordList.value.length >= total || list.length < 20;
		if (!finished.value) page.value += 1;
	} catch (e) {
		if (reset) recordList.value = [];
		loadError.value = e?.msg || e?.message || '兑换记录加载失败';
		uni.showToast({ title: loadError.value, icon: 'none' });
	} finally {
		loading.value = false;
		loadingMore.value = false;
	}
};

onShow(() => {
	loadData(true);
});

onPullDownRefresh(async () => {
	try {
		await loadData(true);
	} finally {
		uni.stopPullDownRefresh();
	}
});

onReachBottom(() => {
	if (!loading.value && !loadingMore.value) {
		loadData(false);
	}
});
</script>

<style lang="scss">
.records-page {
	.pet-shell {
		padding-top: 8rpx;
	}
}

.records-hero {
	padding: 28rpx 24rpx 26rpx;
	border-radius: 28rpx;
	background: linear-gradient(145deg, #fffaf4 0%, #fff3e6 100%);
	border: 1rpx solid rgba(188, 146, 109, 0.16);
	box-shadow: 0 10rpx 28rpx rgba(163, 120, 79, 0.05);
}

.records-hero__badge {
	display: inline-flex;
	align-items: center;
	height: 40rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	border: 1rpx solid rgba(232, 194, 127, 0.7);
	background: rgba(255, 255, 255, 0.7);
	font-size: 22rpx;
	font-weight: 700;
	color: #8b613d;
}

.records-hero__title {
	display: block;
	margin-top: 14rpx;
	font-size: 36rpx;
	font-weight: 800;
	color: #6e472b;
	line-height: 1.3;
}

.records-hero__desc {
	display: block;
	margin-top: 8rpx;
	font-size: 24rpx;
	color: #a78f78;
	line-height: 1.5;
}

.state-box {
	margin-top: 48rpx;
	padding: 48rpx 24rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12rpx;
}

.state-empty-icon {
	width: 96rpx;
	height: 96rpx;
	border-radius: 28rpx;
	background: linear-gradient(145deg, #f7d67d, #efbb66);
	opacity: 0.85;
	margin-bottom: 8rpx;
}

.state-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #6e472b;
}

.state-text {
	font-size: 24rpx;
	color: #a78f78;
	text-align: center;
	line-height: 1.5;
}

.records-list {
	margin-top: 22rpx;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.record-card {
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 22rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.98);
	border: 1rpx solid rgba(188, 146, 109, 0.12);
	box-shadow: 0 10rpx 28rpx rgba(163, 120, 79, 0.05);
}

.record-cover {
	width: 104rpx;
	height: 104rpx;
	border-radius: 24rpx;
	background: #fff6ea;
	overflow: hidden;
	flex-shrink: 0;
}

.record-cover__img {
	width: 100%;
	height: 100%;
}

.record-cover__fallback {
	width: 100%;
	height: 100%;
	background: linear-gradient(135deg, #f7d67d, #efbb66);
}

.record-body {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 14rpx;
}

.record-body__top {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 12rpx;
}

.record-name {
	flex: 1;
	min-width: 0;
	font-size: 30rpx;
	font-weight: 700;
	color: #6c4d36;
	line-height: 1.35;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.favor-chip {
	flex-shrink: 0;
	padding: 6rpx 14rpx;
	border-radius: 999rpx;
	background: rgba(225, 120, 138, 0.12);
}

.favor-chip__text {
	font-size: 22rpx;
	font-weight: 700;
	color: #e1788a;
	line-height: 1.2;
}

.record-meta {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
}

.pay-chip {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	min-width: 0;
}

.pay-chip__dot {
	width: 14rpx;
	height: 14rpx;
	border-radius: 50%;
	flex-shrink: 0;
}

.pay-chip.is-coins .pay-chip__dot {
	background: linear-gradient(145deg, #f0c27a, #c9964e);
	box-shadow: 0 0 0 3rpx rgba(201, 150, 78, 0.18);
}

.pay-chip.is-points .pay-chip__dot {
	background: linear-gradient(145deg, #a8bfd8, #7a91ad);
	box-shadow: 0 0 0 3rpx rgba(122, 145, 173, 0.16);
}

.pay-chip__text {
	font-size: 24rpx;
	font-weight: 600;
	color: #b19072;
	line-height: 1;
}

.pay-chip.is-points .pay-chip__text {
	color: #8fa0b5;
}

.record-time {
	flex-shrink: 0;
	font-size: 22rpx;
	color: #b8a594;
	line-height: 1;
}

.list-end {
	padding: 28rpx 0 8rpx;
	text-align: center;
}

.list-end-text {
	font-size: 22rpx;
	color: #b8a594;
}
</style>

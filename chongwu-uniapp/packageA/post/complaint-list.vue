<template>
	<view class="pet-page complaint-list-page">

		<view class="pet-shell">
			<view v-for="item in list" :key="item.id" class="pet-card complaint-item">
				<view class="item-head">
					<text class="item-type">{{ item.targetTypeText || '投诉' }}</text>
					<text class="item-status"
						:class="statusClass(item.status)">{{ item.statusText || statusText(item.status) }}</text>
				</view>
				<text class="item-summary">{{ item.targetSummary || `对象编号 ${item.targetId}` }}</text>
				<text class="item-reason">{{ item.reason }}</text>
				<text class="item-time">{{ item.createTime || '' }}</text>
			</view>
			<view v-if="loading" class="pet-muted center-text">加载中...</view>
			<view v-if="!loading && !list.length" class="pet-muted center-text">暂无投诉记录</view>
			<view v-if="!loading && list.length && !hasMore" class="pet-muted center-text end-text">没有更多了</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import {
		fetchMyComplaintPage
	} from '@/common/api/community.js';
	import {
		ensureLogin
	} from '@/common/auth-guard.js';
	import {
		onLoad,
		onPullDownRefresh,
		onReachBottom,
		onShow
	} from '@dcloudio/uni-app';

	const list = ref([]);
	const loading = ref(false);
	const page = ref(1);
	const hasMore = ref(true);

	const statusText = status => (Number(status) === 2 ? '已处理' : '待处理');
	const statusClass = status => (Number(status) === 2 ? 'done' : 'pending');

	async function loadData(reset = false) {
		if (loading.value) return;
		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}
		if (!hasMore.value) return;
		loading.value = true;
		try {
			const res = await fetchMyComplaintPage({
				page: page.value,
				limit: 10
			});
			const records = res.records || [];
			list.value = reset ? records : list.value.concat(records);
			hasMore.value = page.value < (res.pages || 0);
			if (hasMore.value) page.value += 1;
		} catch (e) {
			if (reset) list.value = [];
			hasMore.value = false;
		} finally {
			loading.value = false;
		}
	}

	onLoad(() => {
		if (!ensureLogin('/packageA/post/complaint-list')) return;
	});

	onShow(() => loadData(true));
	onPullDownRefresh(async () => {
		await loadData(true);
		uni.stopPullDownRefresh();
	});
	onReachBottom(() => loadData(false));
</script>

<style lang="scss">
	.complaint-list-page {
		background: #fdf5ec;
		min-height: 100vh;
	}

	.complaint-item {
		margin-bottom: 20rpx;
		padding: 24rpx;
	}

	.item-head {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 12rpx;
	}

	.item-type {
		font-size: 28rpx;
		font-weight: 700;
		color: #6f4f39;
	}

	.item-status {
		font-size: 22rpx;
		padding: 6rpx 16rpx;
		border-radius: 999rpx;
	}

	.item-status.pending {
		background: #fff4de;
		color: #c79662;
	}

	.item-status.done {
		background: #eef8ef;
		color: #6f9a6d;
	}

	.item-summary {
		display: block;
		font-size: 26rpx;
		color: #7a5a43;
		margin-bottom: 10rpx;
	}

	.item-reason {
		display: block;
		font-size: 28rpx;
		line-height: 1.6;
		color: #5d4037;
	}

	.item-time {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		color: #baa793;
	}

	.center-text {
		text-align: center;
		padding: 40rpx 0;
	}

	.end-text {
		font-size: 24rpx;
	}
</style>
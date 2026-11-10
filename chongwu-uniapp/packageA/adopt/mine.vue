<template>
	<view class="pet-page">
		<view class="pet-shell">
			<view v-if="loading" class="pet-muted center-text">加载中...</view>
			<view v-else-if="!records.length" class="pet-muted center-text">暂无领养申请</view>
			<view v-else class="pet-list-gap">
				<view v-for="item in records" :key="item.id" class="pet-card mine-card" @tap="openDetail(item)">
					<image class="mine-cover" :src="coverUrl(item)" mode="aspectFill"></image>
					<view class="mine-main">
						<view class="mine-head">
							<text class="mine-pet">{{ item.petName || '宠物' }}</text>
							<text class="pet-status-pill" :class="statusClass(item.status)">{{ item.statusLabel || statusText(item.status) }}</text>
						</view>
						<text class="pet-muted">{{ formatTime(item.createTime) }}</text>
						<text v-if="item.status === 7 && item.rejectReason" class="reject-reason">拒绝原因：{{ item.rejectReason }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { fetchMyAdoptPage, resolveAdoptStatusLabel } from '@/common/api/adopt.js';
import { resolveMediaUrl } from '@/common/media.js';
import { ensureLogin } from '@/common/auth-guard.js';

const loading = ref(false);
const records = ref([]);

const statusText = (status) => resolveAdoptStatusLabel(status);
const statusClass = (status) => {
	if (status === 6) return 'is-success';
	if (status === 7) return 'is-danger';
	return 'is-warn';
};
const coverUrl = (item) => resolveMediaUrl(item.petImage);
const formatTime = (time) => (time ? String(time).replace('T', ' ').slice(0, 16) : '');

const loadData = async () => {
	if (!ensureLogin()) return;
	loading.value = true;
	try {
		const res = await fetchMyAdoptPage();
		records.value = res?.records || [];
	} catch (e) {
		records.value = [];
		uni.showToast({ title: e?.message || '加载失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
};

const openDetail = (item) => {
	uni.showModal({
		title: item.petName || '领养申请',
		content: [
			`状态：${item.statusLabel || statusText(item.status)}`,
			item.rejectReason ? `拒绝原因：${item.rejectReason}` : '',
			item.reason ? `摘要：${item.reason}` : ''
		].filter(Boolean).join('\n'),
		showCancel: false
	});
};

onMounted(loadData);
onShow(loadData);

onPullDownRefresh(async () => {
	try {
		await loadData();
	} finally {
		uni.stopPullDownRefresh();
	}
});
</script>

<style lang="scss">
.center-text {
	display: block;
	text-align: center;
	padding: 40rpx 0;
}

.mine-card {
	display: flex;
	gap: 20rpx;
	align-items: flex-start;
}

.mine-cover {
	width: 120rpx;
	height: 120rpx;
	border-radius: 24rpx;
	flex-shrink: 0;
	background: #f5efe6;
}

.mine-main {
	flex: 1;
	min-width: 0;
}

.mine-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
	margin-bottom: 10rpx;
}

.mine-pet {
	font-size: 34rpx;
	font-weight: 700;
}

.reject-reason {
	display: block;
	margin-top: 12rpx;
	font-size: 24rpx;
	color: #d5928d;
	line-height: 1.6;
}
</style>

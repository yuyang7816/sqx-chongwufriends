<template>
	<view class="pet-page">
		<view class="pet-shell">
			<view v-if="loading" class="pet-muted" style="text-align:center;padding:40rpx 0;">加载中...</view>
			<view v-else-if="errorText" class="pet-muted" style="text-align:center;padding:40rpx 0;">{{ errorText }}</view>
			<view v-else class="pet-card video-card">
				<text class="video-title">{{ title }}</text>
				<video v-if="videoUrl" class="video-player" :src="videoUrl" controls object-fit="contain"></video>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchGiftInfo } from '@/common/api/feed.js';

const loading = ref(true);
const errorText = ref('');
const videoUrl = ref('');
const title = ref('赠送反馈视频');

onLoad(async (options) => {
	const id = options?.id;
	if (!id) {
		errorText.value = '缺少订单信息';
		loading.value = false;
		return;
	}
	try {
		const detail = await fetchGiftInfo(id);
		title.value = `${detail?.pet || '宠物'} · ${detail?.item || detail?.productName || '赠送反馈'}`;
		videoUrl.value = detail?.videoUrl || '';
		if (!videoUrl.value) {
			errorText.value = '暂无反馈视频';
		}
	} catch (e) {
		errorText.value = e?.msg || e?.message || '加载失败';
	} finally {
		loading.value = false;
	}
});
</script>

<style lang="scss">
.video-card {
	margin-top: 8rpx;
	padding: 24rpx;
}

.video-title {
	display: block;
	font-size: 32rpx;
	font-weight: 700;
	margin-bottom: 20rpx;
}

.video-player {
	width: 100%;
	height: 420rpx;
	border-radius: 24rpx;
	background: #000;
}
</style>

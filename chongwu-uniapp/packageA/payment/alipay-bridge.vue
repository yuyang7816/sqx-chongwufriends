<template>
	<view class="pet-page bridge-page">
		<view v-if="wapUrl" class="webview-wrap">
			<web-view :src="wapUrl" @message="onMessage"></web-view>
		</view>
		<view v-else class="pet-shell center-wrap">
			<text class="pet-muted">正在准备支付页面...</text>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';
	import { pollPaySuccess, goPayResult } from '@/common/pay-helper.js';

	const wapUrl = ref('');
	const payNo = ref('');
	const orderNo = ref('');

	onLoad(async (opts) => {
		payNo.value = opts.payNo || '';
		orderNo.value = opts.orderNo || '';
		wapUrl.value = opts.wapUrl ? decodeURIComponent(opts.wapUrl) : '';
	});

	const onMessage = async () => {
		const ok = await pollPaySuccess(payNo.value, 3);
		goPayResult(ok, { payNo: payNo.value, orderNo: orderNo.value });
	};
</script>

<style lang="scss">
	.bridge-page {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}
	.webview-wrap {
		flex: 1;
		min-height: calc(100vh - 88rpx);
	}
	.center-wrap {
		padding: 120rpx 40rpx;
		text-align: center;
	}
</style>

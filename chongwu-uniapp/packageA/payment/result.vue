<template>
	<view class="pet-page result-page">
		<view class="pet-shell center-wrap">
			<up-icon :name="success ? 'checkmark-circle-fill' : 'close-circle-fill'" size="80"
				:color="success ? '#7eb89a' : '#f199a5'"></up-icon>
			<text class="result-title">{{ success ? '支付成功' : '支付失败' }}</text>
			<text class="pet-muted" v-if="payNo">单号：{{ payNo }}</text>
			<text class="pet-muted" v-if="orderNo">订单：{{ orderNo }}</text>
			<view class="btn-row">
				<view class="pet-button" @tap="goHome">返回首页</view>
				<view class="pet-button is-outline" @tap="goShop">继续逛逛</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';

	const success = ref(false);
	const payNo = ref('');
	const orderNo = ref('');

	onLoad(opts => {
		success.value = String(opts.success) === '1';
		payNo.value = opts.payNo || '';
		orderNo.value = opts.orderNo || '';
	});

	const goHome = () => uni.switchTab({ url: '/pages/index/index' });
	const goShop = () => uni.switchTab({ url: '/pages/shop/shop' });
</script>

<style lang="scss">
	.result-page .center-wrap {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 80rpx 40rpx;
		gap: 24rpx;
	}
	.result-title {
		font-size: 40rpx;
		font-weight: 700;
		margin-top: 20rpx;
	}
	.btn-row {
		display: flex;
		gap: 24rpx;
		margin-top: 48rpx;
		width: 100%;
	}
	.btn-row .pet-button {
		flex: 1;
		text-align: center;
	}
</style>

<template>
	<view class="pet-page complaint-page">
		<view class="pet-shell">
			<view v-if="targetId" class="pet-card target-card">
				<text class="section-label">投诉对象</text>
				<text class="target-type">{{ targetTypeText }}</text>
				<text v-if="targetPreview" class="target-preview">{{ targetPreview }}</text>
				<text v-else class="pet-muted">编号 {{ targetId }}</text>
			</view>

			<view class="pet-card form-card">
				<text class="section-label">投诉类型</text>
				<view class="reason-grid">
					<view v-for="item in reasonTypes" :key="item" class="reason-pill"
						:class="{ active: selectedType === item }" @tap="selectedType = item">{{ item }}</view>
				</view>

				<text class="section-label margin-top">详细说明</text>
				<view class="textarea-wrap">
					<textarea v-model="reasonDetail" class="reason-textarea" maxlength="300"
						placeholder="请描述投诉原因，便于平台核实处理" placeholder-class="reason-placeholder"></textarea>
				</view>
				<text class="count-text">{{ reasonDetail.length }}/300</text>
			</view>

			<view class="pet-card tip-card">
				<text class="tip-title">投诉须知</text>
				<text class="tip-text">请确保投诉内容真实有效，恶意投诉可能影响账号正常使用。平台会在核实后尽快处理。</text>
			</view>

			<view class="pet-button submit-btn" @tap="submit">提交投诉</view>
			<view class="link-row" @tap="goMyComplaints">
				<text>查看我的投诉记录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from 'vue';
	import {
		fetchCommunityConfig,
		fetchPostInfo,
		submitComplaint
	} from '@/common/api/community.js';
	import {
		ensureLogin
	} from '@/common/auth-guard.js';
	import {
		onLoad
	} from '@dcloudio/uni-app';

	const targetType = ref(1);
	const targetId = ref('');
	const targetPreview = ref('');
	const selectedType = ref('');
	const reasonDetail = ref('');
	const submitting = ref(false);
	const reasonTypes = ref([]);

	const targetTypeText = computed(() => (Number(targetType.value) === 2 ? '评论' : '动态'));
	const pageTitle = computed(() => (targetId.value ? '去投诉' : '我的投诉'));

	const buildReason = () => {
		const detail = reasonDetail.value.trim();
		if (!selectedType.value && !detail) return '';
		if (!detail) return selectedType.value;
		if (!selectedType.value) return detail;
		return `${selectedType.value}：${detail}`;
	};

	const loadComplaintTypes = async () => {
		try {
			const data = await fetchCommunityConfig();
			const types = Array.isArray(data?.complaintTypes) ? data.complaintTypes : [];
			reasonTypes.value = types.length ? types : ['其他'];
			if (!selectedType.value || !reasonTypes.value.includes(selectedType.value)) {
				selectedType.value = reasonTypes.value[0] || '';
			}
		} catch (e) {
			reasonTypes.value = ['其他'];
			selectedType.value = '其他';
		}
	};

	const loadTargetPreview = async () => {
		if (!targetId.value || Number(targetType.value) !== 1) return;
		try {
			const data = await fetchPostInfo(targetId.value, false);
			targetPreview.value = data?.content || '';
		} catch (e) {
			targetPreview.value = '';
		}
	};

	const submit = async () => {
		if (!ensureLogin()) return;
		if (!targetId.value) {
			uni.showToast({
				title: '缺少投诉对象',
				icon: 'none'
			});
			return;
		}
		const reason = buildReason();
		if (!reason) {
			uni.showToast({
				title: '请选择类型或填写说明',
				icon: 'none'
			});
			return;
		}
		if (submitting.value) return;
		submitting.value = true;
		try {
			await submitComplaint({
				targetType: Number(targetType.value),
				targetId: Number(targetId.value),
				complaintType: selectedType.value,
				content: reasonDetail.value.trim(),
				reason,
			});
			uni.showToast({
				title: '投诉已提交',
				icon: 'success'
			});
			setTimeout(() => uni.redirectTo({
				url: '/packageA/post/complaint-list'
			}), 600);
		} catch (e) {
			uni.showToast({
				title: e?.msg || '提交失败',
				icon: 'none'
			});
		} finally {
			submitting.value = false;
		}
	};

	const goMyComplaints = () => {
		uni.navigateTo({
			url: '/packageA/post/complaint-list'
		});
	};

	onLoad(async option => {
		if (!ensureLogin('/packageA/post/complaint')) return;
		if (option?.mode === 'list') {
			uni.redirectTo({
				url: '/packageA/post/complaint-list'
			});
			return;
		}
		targetType.value = Number(option?.targetType || 1);
		targetId.value = String(option?.targetId || option?.postId || option?.id || '');
		await Promise.all([loadComplaintTypes(), loadTargetPreview()]);
	});
</script>

<style lang="scss">
	.complaint-page {
		background: linear-gradient(180deg, #fdf5ec 0%, #fbf1e5 100%);
		min-height: 100vh;
	}

	.pet-card {
		margin-bottom: 20rpx;
		padding: 24rpx;
		border-radius: 24rpx;
		background: #fffdf9;
		border: 2rpx solid #ecd7bf;
	}

	.section-label {
		display: block;
		margin-bottom: 16rpx;
		font-size: 26rpx;
		font-weight: 700;
		color: #6f4f39;
	}

	.section-label.margin-top {
		margin-top: 24rpx;
	}

	.target-type {
		display: block;
		font-size: 24rpx;
		color: #c79662;
		margin-bottom: 10rpx;
	}

	.target-preview {
		display: block;
		font-size: 28rpx;
		line-height: 1.7;
		color: #5d4037;
	}

	.reason-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 14rpx;
	}

	.reason-pill {
		padding: 14rpx 22rpx;
		border-radius: 999rpx;
		background: #f8efe4;
		color: #8e7863;
		font-size: 24rpx;
		border: 2rpx solid transparent;
	}

	.reason-pill.active {
		background: #fff4de;
		color: #8b613d;
		border-color: #d7aa77;
	}

	.textarea-wrap {
		padding: 20rpx;
		border-radius: 20rpx;
		background: #f7f3eb;
	}

	.reason-textarea {
		width: 100%;
		min-height: 200rpx;
		font-size: 28rpx;
		line-height: 1.7;
		color: #5d4037;
	}

	.reason-placeholder {
		color: #aa9a85;
	}

	.count-text {
		display: block;
		margin-top: 10rpx;
		text-align: right;
		font-size: 22rpx;
		color: #baa793;
	}

	.tip-title {
		display: block;
		font-size: 26rpx;
		font-weight: 700;
		color: #8b613d;
		margin-bottom: 10rpx;
	}

	.tip-text {
		font-size: 24rpx;
		line-height: 1.7;
		color: #9a8d84;
	}

	.submit-btn {
		width: 100%;
		margin-top: 10rpx;
		height: 88rpx;
		line-height: 88rpx;
		text-align: center;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #cea06f, #c28a55);
		color: #fff;
		font-size: 30rpx;
		font-weight: 700;
	}

	.link-row {
		margin-top: 24rpx;
		text-align: center;
		font-size: 24rpx;
		color: #c79662;
	}
</style>
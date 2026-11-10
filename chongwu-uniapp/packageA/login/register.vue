<template>
	<view class="pet-page auth-page">
		<view class="auth-shell">
			<image class="auth-title-image" src="/packageA/static/login/title.png" mode="widthFix"></image>
			<text class="auth-sub">创建一个新账号，和它交个朋友</text>
			<view class="auth-logo-wrap">
				<image class="auth-mascot" src="/static/logo.png" mode="aspectFit"></image>
			</view>

			<view class="auth-panel">
				<view class="auth-badge">注册账号</view>
				<input v-model="phone" class="pet-input-wrap auth-input" type="number" maxlength="11" placeholder="请输入手机号"
					placeholder-class="auth-input-placeholder" />
				<view class="auth-code-row">
					<input v-model="code" class="pet-input-wrap auth-input auth-code-input" type="number" maxlength="6" placeholder="请输入验证码"
						placeholder-class="auth-input-placeholder" />
					<view class="auth-code-btn" :class="{ disabled: codeSeconds > 0 }" @tap="sendCode">
						{{ codeSeconds > 0 ? `${codeSeconds}s后重试` : '获取验证码' }}
					</view>
				</view>
				<input v-model="password" class="pet-input-wrap auth-input" password placeholder="请设置登录密码"
					placeholder-class="auth-input-placeholder" />
				<input v-model="confirmPassword" class="pet-input-wrap auth-input" password placeholder="请再次输入登录密码"
					placeholder-class="auth-input-placeholder" />
				<view class="auth-submit" @tap="submitRegister">立即注册</view>
			</view>

			<view class="auth-foot-link" @tap="goTo('/packageA/login/index')">
				<text>已经有账号？</text>
				<text class="auth-foot-link-highlight">去登录</text>
			</view>
			<view class="paw-row">
				<image class="paw-row-item" src="/packageA/static/login/zhuazhua.png" mode="widthFix"></image>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		onUnmounted,
		ref
	} from 'vue';
	import {
		onLoad,
		onUnload
	} from '@dcloudio/uni-app';
	import {
		sendPhoneCode,
		phoneRegister
	} from '@/common/api/auth.js';
	import {
		showApiErrorToast
	} from '@/common/validators.js';
	import {
		useAuthForm
	} from '@/common/use-auth-form.js';
	import {
		persistLogin
	} from '@/common/auth-session.js';
	import {
		clearPendingInvitationCode,
		getPendingInvitationCode
	} from '@/common/invite.js';

	const phone = ref('');
	const code = ref('');
	const password = ref('');
	const confirmPassword = ref('');
	const codeSeconds = ref(0);
	const redirectUrl = ref('');

	let codeTimer = null;

	const {
		trimPasswords,
		validatePhone
	} = useAuthForm({
		phone,
		code,
		password,
		confirmPassword
	});

	const clearCodeTimer = () => {
		if (codeTimer) {
			clearInterval(codeTimer);
			codeTimer = null;
		}
	};

	const startCodeCountdown = () => {
		codeSeconds.value = 60;
		clearCodeTimer();
		codeTimer = setInterval(() => {
			codeSeconds.value -= 1;
			if (codeSeconds.value <= 0) {
				codeSeconds.value = 0;
				clearCodeTimer();
			}
		}, 1000);
	};

	const resetForm = () => {
		phone.value = '';
		code.value = '';
		password.value = '';
		confirmPassword.value = '';
		codeSeconds.value = 0;
	};

	const sendCode = async () => {
		if (codeSeconds.value > 0) return;
		if (!validatePhone()) return;
		try {
			await sendPhoneCode(phone.value, 'register');
			uni.showToast({
				title: '验证码已发送',
				icon: 'none'
			});
			startCodeCountdown();
		} catch (e) {
			clearCodeTimer();
			codeSeconds.value = 0;
			showApiErrorToast(e, '验证码发送失败');
		}
	};

	const submitRegister = async () => {
		if (!validatePhone()) return;
		trimPasswords();
		if (!code.value) {
			uni.showToast({ title: '请输入验证码', icon: 'none' });
			return;
		}
		if (!password.value) {
			uni.showToast({ title: '请设置登录密码', icon: 'none' });
			return;
		}
		if (password.value !== confirmPassword.value) {
			uni.showToast({ title: '两次密码输入不一致', icon: 'none' });
			return;
		}
		try {
			const data = await phoneRegister(
				phone.value,
				code.value,
				password.value,
				getPendingInvitationCode()
			);
			uni.showToast({ title: '注册成功', icon: 'none' });
			clearPendingInvitationCode();
			resetForm();
			setTimeout(() => {
				persistLogin(data, redirectUrl.value || '/pages/my/my?from=login');
			}, 500);
		} catch (e) {
			showApiErrorToast(e, '注册失败');
		}
	};

	const goTo = url => {
		const targetUrl = redirectUrl.value ? `${url}?redirect=${encodeURIComponent(redirectUrl.value)}` : url;
		uni.navigateTo({ url: targetUrl });
	};

	onLoad(option => {
		if (option.redirect) {
			redirectUrl.value = decodeURIComponent(option.redirect);
		}
	});

	onUnload(() => {
		clearCodeTimer();
		resetForm();
	});

	onUnmounted(() => {
		clearCodeTimer();
	});
</script>

<style lang="scss">
	.auth-page {
		background: linear-gradient(180deg, #f7ede5 0%, #f7dedd 100%);
	}

	.auth-shell {
		padding: 18rpx 48rpx 70rpx;
		text-align: center;
	}

	.auth-title-image {
		width: 360rpx;
		margin: 8rpx auto 0;
	}

	.auth-sub {
		display: block;
		font-size: 24rpx;
		color: #8f7f73;
		margin-top: 8rpx;
	}

	.auth-logo-wrap {
		width: 220rpx;
		height: 180rpx;
		margin: 24rpx auto 8rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		filter: drop-shadow(12rpx 18rpx 14rpx rgba(112, 79, 45, 0.16));
	}

	.auth-mascot {
		width: 170rpx;
		height: 170rpx;
	}

	.auth-panel {
		padding: 30rpx 28rpx 34rpx;
		border-radius: 30rpx;
		background: rgba(255, 249, 243, 0.72);
		box-shadow: 0 14rpx 30rpx rgba(206, 164, 127, 0.12), inset 0 0 0 2rpx rgba(238, 224, 211, 0.9);
	}

	.auth-badge {
		width: fit-content;
		min-width: 156rpx;
		height: 54rpx;
		margin: 0 auto 24rpx;
		padding: 0 26rpx;
		border-radius: 999rpx;
		background: #c99a67;
		color: #fffdf9;
		font-size: 26rpx;
		font-weight: 700;
		line-height: 54rpx;
	}

	.auth-input {
		margin-bottom: 20rpx;
		height: 72rpx;
		padding: 0 28rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: inset 0 0 0 2rpx rgba(245, 231, 219, 0.9);
		text-align: left;
	}

	.auth-input-placeholder {
		color: #b9aaa0;
		font-size: 26rpx;
	}

	.auth-code-row {
		display: flex;
		align-items: center;
		gap: 14rpx;
		margin-bottom: 20rpx;
	}

	.auth-code-input {
		flex: 1;
		margin-bottom: 0;
	}

	.auth-code-btn {
		width: 168rpx;
		height: 72rpx;
		border-radius: 24rpx;
		background: #c99a67;
		color: #fffdf9;
		font-size: 22rpx;
		font-weight: 700;
		line-height: 72rpx;
		text-align: center;
		box-shadow: 0 10rpx 18rpx rgba(201, 154, 103, 0.2);
		flex-shrink: 0;
	}

	.auth-code-btn.disabled {
		background: #dec8b0;
	}

	.auth-submit {
		height: 88rpx;
		line-height: 88rpx;
		margin-top: 10rpx;
		border-radius: 999rpx;
		background: #c99763;
		color: #fffdf9;
		font-size: 32rpx;
		font-weight: 700;
		box-shadow: 0 16rpx 28rpx rgba(201, 151, 99, 0.22);
	}

	.auth-foot-link {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		margin: 34rpx 0 24rpx;
		font-size: 28rpx;
		color: #a89799;
	}

	.auth-foot-link-highlight {
		color: #c68f61;
		font-weight: 700;
	}

	.paw-row {
		display: flex;
		justify-content: center;
	}

	.paw-row-item {
		width: 450rpx;
	}
</style>

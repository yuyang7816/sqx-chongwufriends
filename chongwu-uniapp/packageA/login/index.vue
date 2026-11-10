<template>
	<view class="pet-page login-page">
		<view class="login-shell">
			<image class="login-title-image" src="/packageA/static/login/title.png" mode="widthFix"></image>
			<text class="login-sub">理性养宠 · 降低遗弃</text>
			<view class="login-logo-wrap">
				<image class="login-mascot" src="/static/logo.png" mode="aspectFit"></image>
			</view>
			<text class="pet-muted center-text">欢迎回来，登录后继续陪伴毛孩子</text>

			<!-- #ifdef MP-WEIXIN -->
			<view class="wx-login-panel">
				<text class="wx-login-tip">使用微信授权手机号快捷登录</text>
				<button
					class="wechat-btn wx-auth-btn"
					open-type="getPhoneNumber"
					:loading="wxLoading"
					:disabled="wxLoading"
					@getphonenumber="onWxPhoneAuth"
				>
					<image src="/packageA/static/xinxi.png" mode="widthFix"></image>
					微信授权登录
				</button>
				<view class="wechat-btn wx-silent-btn" :class="{ 'is-disabled': wxLoading }" @tap="submitWechatSilent">
					已绑定用户快捷登录
				</view>
			</view>
			<!-- #endif -->

			<!-- #ifndef MP-WEIXIN -->
			<view class="login-tabs">
				<view class="login-tab" :class="{ active: loginMode === 'password' }" @tap="switchLoginMode('password')">
					<text class="login-tab-text">手机号登录</text>
				</view>
				<view class="login-tab" :class="{ active: loginMode === 'code' }" @tap="switchLoginMode('code')">
					<text class="login-tab-text">验证码登录</text>
				</view>
			</view>

			<input v-model="phone" class="pet-input-wrap login-input" type="number" maxlength="11" placeholder="请输入手机号"
				placeholder-class="login-input-placeholder" />
			<input v-if="loginMode === 'password'" v-model="password" class="pet-input-wrap login-input" password
				placeholder="请输入密码" placeholder-class="login-input-placeholder" />
			<view v-else class="login-code-row">
				<input v-model="code" class="pet-input-wrap login-input login-code-input" type="number" maxlength="6" placeholder="请输入验证码"
					placeholder-class="login-input-placeholder" />
				<view class="login-code-btn" :class="{ disabled: codeSeconds > 0 }" @tap="sendCode">
					{{ codeSeconds > 0 ? `${codeSeconds}s后重试` : '获取验证码' }}
				</view>
			</view>

			<view class="login-submit" @tap="submitLogin">登录</view>
			<text class="forget-link" @tap="goTo('/packageA/login/forgot')">忘记密码？</text>
			<view class="register-link" @tap="goTo('/packageA/login/register')">
				<image class="register-link-image" src="/packageA/static/login/qzc.png" mode="widthFix"></image>
			</view>
			<!-- #endif -->

			<view class="paw-row">
				<image class="paw-row-item" src="/packageA/static/login/zhuazhua.png" mode="widthFix"></image>
			</view>
			<view class="agreement-row">
				<text class="agreement-text">和它交个朋友，给每个毛孩子一个家</text>
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
		phoneCodeLogin,
		phonePasswordLogin,
		sendPhoneCode,
		wxLogin,
		wxPhoneLogin
	} from '@/common/api/auth.js';
	import {
		useAuthForm
	} from '@/common/use-auth-form.js';
	import {
		showApiErrorToast
	} from '@/common/validators.js';
	import {
		persistLogin
	} from '@/common/auth-session.js';
	import {
		clearPendingInvitationCode,
		getPendingInvitationCode
	} from '@/common/invite.js';
	import {
		onLoad,
		onUnload
	} from '@dcloudio/uni-app';

	const phone = ref('');
	const password = ref('');
	const code = ref('');
	const loginMode = ref('password');
	const codeSeconds = ref(0);
	const redirectUrl = ref('');
	const wxLoading = ref(false);

	let codeTimer = null;

	const {
		trimPasswords,
		validatePhone
	} = useAuthForm({
		phone,
		code,
		password
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

	const switchLoginMode = mode => {
		if (loginMode.value === mode) return;
		loginMode.value = mode;
		resetForm();
	};

	const resetForm = () => {
		phone.value = '';
		password.value = '';
		code.value = '';
		clearCodeTimer();
		codeSeconds.value = 0;
	};

	const sendCode = async () => {
		if (codeSeconds.value > 0) return;
		if (!validatePhone()) return;
		try {
			await sendPhoneCode(phone.value, 'login');
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

	const persistLoginSuccess = (data = {}) => {
		persistLogin(data, redirectUrl.value);
	};

	onLoad(option => {
		if (option.redirect) {
			redirectUrl.value = decodeURIComponent(option.redirect);
		}
	});

	onUnload(() => {
		resetForm();
	});

	const submitLogin = async () => {
		if (!validatePhone()) return;
		trimPasswords();
		if (loginMode.value === 'password' && !password.value) {
			uni.showToast({
				title: '请输入密码',
				icon: 'none'
			});
			return;
		}
		if (loginMode.value === 'code' && !code.value) {
			uni.showToast({
				title: '请输入验证码',
				icon: 'none'
			});
			return;
		}
		try {
			const data = loginMode.value === 'password' ?
				await phonePasswordLogin(phone.value, password.value) :
				await phoneCodeLogin(phone.value, code.value);
			persistLoginSuccess(data);
		} catch (e) {
			showApiErrorToast(e, '登录失败，请检查账号信息');
		}
	};

	const getWxLoginCode = () => new Promise((resolve, reject) => {
		uni.login({
			provider: 'weixin',
			success: res => {
				if (res?.code) {
					resolve(res.code);
					return;
				}
				reject(new Error('微信登录失败'));
			},
			fail: reject
		});
	});

	const onWxPhoneAuth = async (e) => {
		// #ifdef MP-WEIXIN
		if (wxLoading.value) return;
		if (!e?.detail || e.detail.errMsg !== 'getPhoneNumber:ok' || !e.detail.code) {
			uni.showToast({
				title: '需要授权手机号才能登录',
				icon: 'none'
			});
			return;
		}
		wxLoading.value = true;
		try {
			const loginCode = await getWxLoginCode();
			const data = await wxPhoneLogin(loginCode, e.detail.code, getPendingInvitationCode());
			clearPendingInvitationCode();
			persistLoginSuccess(data);
		} catch (err) {
			showApiErrorToast(err, '微信授权登录失败');
		} finally {
			wxLoading.value = false;
		}
		// #endif
	};

	const submitWechatSilent = async () => {
		// #ifdef MP-WEIXIN
		if (wxLoading.value) return;
		wxLoading.value = true;
		try {
			const loginCode = await getWxLoginCode();
			const data = await wxLogin(loginCode);
			if (data?.bindPhone && data?.loginVO) {
				persistLoginSuccess(data.loginVO);
				return;
			}
			uni.showToast({
				title: '请使用上方微信授权登录',
				icon: 'none'
			});
		} catch (e) {
			showApiErrorToast(e, '微信登录失败');
		} finally {
			wxLoading.value = false;
		}
		// #endif
	};

	const goTo = url => {
		const targetUrl = redirectUrl.value ? `${url}?redirect=${encodeURIComponent(redirectUrl.value)}` : url;
		uni.navigateTo({
			url: targetUrl
		});
	};

	onUnmounted(() => {
		clearCodeTimer();
	});
</script>

<style lang="scss">
	.login-page {
		background: linear-gradient(180deg, #f7ede5 0%, #f7dedd 100%);
	}

	.login-shell {
		padding: 18rpx 48rpx 70rpx;
		text-align: center;
	}

	.login-title-image {
		width: 360rpx;
		margin: 8rpx auto 0;
	}

	.login-sub {
		display: block;
		font-size: 24rpx;
		color: #8f7f73;
		margin-top: 8rpx;
	}

	.login-logo-wrap {
		width: 170rpx;
		height: 170rpx;
		margin: 26rpx auto 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 36rpx;
		overflow: hidden;
		filter: drop-shadow(12rpx 18rpx 14rpx rgba(112, 79, 45, 0.16));
	}

	.login-mascot {
		width: 170rpx;
		height: 170rpx;
		border-radius: 36rpx;
		overflow: hidden;
	}

	.center-text {
		display: block;
		text-align: center;
	}

	.login-shell .pet-muted {
		font-size: 24rpx;
		color: #a39288;
	}

	.wx-login-panel {
		margin-top: 36rpx;
	}

	.wx-login-tip {
		display: block;
		font-size: 26rpx;
		color: #8f7f73;
		margin-bottom: 28rpx;
	}

	.login-tabs {
		display: flex;
		align-items: center;
		background: rgba(255, 249, 243, 0.96);
		border-radius: 28rpx;
		overflow: hidden;
		margin: 30rpx 0 26rpx;
		padding: 10rpx 12rpx;
		width: 100%;
		margin-left: auto;
		margin-right: auto;
		box-shadow: inset 0 0 0 2rpx rgba(238, 224, 211, 0.95), 0 10rpx 18rpx rgba(228, 198, 168, 0.12);
	}

	.login-tab {
		flex: 1;
		height: 70rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		color: #a59484;
		transition: all 0.2s ease;
	}

	.login-tab.active {
		background: #c99a67;
		height: 76rpx;
		box-shadow: 0 8rpx 16rpx rgba(201, 154, 103, 0.24);
		color: #ffffff;
	}

	.login-tab-text {
		font-size: 25rpx;
		font-weight: 500;
		line-height: 1;
	}

	.login-tab.active .login-tab-text {
		font-weight: 700;
	}

	.login-input {
		margin-bottom: 20rpx;
		height: 72rpx;
		padding: 0 28rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.94);
		box-shadow: inset 0 0 0 2rpx rgba(245, 231, 219, 0.85);
		text-align: left;
	}

	.login-code-row {
		display: flex;
		align-items: center;
		gap: 14rpx;
		margin-bottom: 20rpx;
	}

	.login-code-input {
		flex: 1;
		margin-bottom: 0;
	}

	.login-code-btn {
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

	.login-code-btn.disabled {
		background: #dec8b0;
		color: rgba(255, 253, 249, 0.92);
	}

	.login-input-placeholder {
		color: #b9aaa0;
		font-size: 26rpx;
	}

	.forget-link,
	.register-link {
		display: block;
	}

	.login-submit {
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 999rpx;
		background: #c99763;
		color: #fffdf9;
		font-size: 32rpx;
		font-weight: 700;
		box-shadow: 0 16rpx 28rpx rgba(201, 151, 99, 0.22);
	}

	.forget-link {
		text-align: right;
		font-size: 24rpx;
		color: #b0a1a6;
		margin: 16rpx 6rpx 48rpx 0;
	}

	.wechat-btn {
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 999rpx;
		background: #08c65a;
		color: #fff;
		font-size: 32rpx;
		font-weight: 700;
		box-shadow: 0 16rpx 28rpx rgba(8, 198, 90, 0.22);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 20rpx;

		image {
			width: 40rpx;
			height: 40rpx;
			margin-right: 10rpx;
		}
	}

	.wx-auth-btn {
		border: none;
		padding: 0;
	}

	.wx-auth-btn::after {
		border: none;
	}

	.wx-silent-btn {
		background: #ffffff;
		color: #08c65a;
		box-shadow: inset 0 0 0 2rpx rgba(8, 198, 90, 0.35);
	}

	.wx-silent-btn.is-disabled {
		opacity: 0.6;
	}

	.register-link {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		font-size: 28rpx;
		color: #a89799;
		margin: 34rpx 0 28rpx;
	}

	.register-link-image {
		width: 240rpx;
	}

	.paw-row {
		display: flex;
		justify-content: center;
		gap: 18rpx;
		margin: 0 0 24rpx;
		flex-wrap: wrap;
	}

	.paw-row-item {
		width: 450rpx;
	}

	.agreement-row {
		display: flex;
		justify-content: center;
	}

	.agreement-text {
		font-size: 22rpx;
		color: #a89799;
	}
</style>

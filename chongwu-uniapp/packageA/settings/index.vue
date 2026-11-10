<template>
	<view class="pet-page settings-page">
		<view class="pet-shell">
			<view class="pet-card pet-dashed profile-banner">
				<view class="pet-avatar profile-avatar">
					<image :src="user.avatar || user.avatarUrl || '/static/logo.png'" mode="aspectFill"></image>
				</view>
				<view class="profile-copy">
					<text class="profile-name">{{ user.userName || user.phone || '用户' }}</text>
					<text class="pet-subtitle"> {{ user.levelName  }}</text>
				</view>
				<view class="pet-button edit-btn" @tap="handleEditProfile">编辑资料</view>
				<view class="paw-mark">
					<image src="/static/image/my/maozhua.png"></image>
				</view>
			</view>

			<view class="setting-section-title">
				<image src="/packageA/static/set/tzsz.png" mode="heightFix"></image>
			</view>
			<view v-for="item in notificationItems" :key="item.key" class="pet-card setting-switch-row">
				<text>{{ item.label }}</text>
				<up-switch v-model="item.value" activeColor="#c79662" inactiveColor="#ddd2c4" @change="handleNotificationChange(item)"></up-switch>
			</view>

			<view class="setting-section-title">
				<image src="/packageA/static/set/zhaq.png" mode="heightFix"></image>
			</view>
			<view class="pet-card setting-list-item">
				<text>手机号</text>
				<text class="pet-muted">{{ formatPhone(user.phone) }}</text>
			</view>
			<view class="pet-card setting-list-item">
				<text>微信绑定</text>
				<text v-if="wechatBound" class="pet-muted">已绑定</text>
				<text v-else class="pet-muted bind-tip">未绑定</text>
			</view>
			<view class="pet-card setting-list-item" @tap="goForgotPassword">
				<text>修改密码</text>
				<up-icon name="arrow-right" size="16" color="#c5b5a5"></up-icon>
			</view>
			<view class="pet-card setting-list-item" @tap="navigate('/packageA/wecom/index')">
				<text>联系客服</text>
				<up-icon name="arrow-right" size="16" color="#c5b5a5"></up-icon>
			</view>
			<view class="pet-card setting-list-item danger-row danger-soft-row" @tap="logoutAccount">
				<text>账号注销</text>
				<up-icon name="arrow-right" size="16" color="#f28f88"></up-icon>
			</view>
			<view class="pet-card setting-list-item danger-row" @tap="logout">
				<text>退出登录</text>
				<up-icon name="arrow-right" size="16" color="#f28f88"></up-icon>
			</view>
		</view>

		<up-popup :show="editPopupVisible" mode="center" :round="10" @close="closeEditPopup">
			<view class="profile-popup">
				<view class="profile-popup-head">
					<text class="profile-popup-title">编辑资料</text>
					<text class="profile-popup-subtitle">把你的宠友名片打理得更可爱一些</text>
				</view>

				<view class="profile-popup-avatar-row">
					<view class="profile-popup-avatar" @tap="chooseAvatar">
						<image :src="editForm.avatar || editForm.avatarUrl || user.avatar || user.avatarUrl || '/static/logo.png'" mode="aspectFill"></image>
						<view class="profile-popup-avatar-badge">更换</view>
					</view>
				</view>

				<view class="profile-form-card">
					<view class="profile-form-item">
						<text class="profile-form-label">昵称</text>
						<input v-model="editForm.userName" class="profile-form-input" type="nickname" maxlength="20" placeholder="请输入昵称" placeholder-class="profile-form-placeholder" />
					</view>

					<view class="profile-form-item profile-form-picker" @tap="openBirthdayPicker">
						<text class="profile-form-label">生日</text>
						<view class="profile-form-picker-value">
							<text :class="['profile-form-input-text', { 'is-placeholder': !editForm.birthday }]">{{ editForm.birthday || '请选择生日' }}</text>
							<up-icon name="arrow-right" size="14" color="#c5b5a5"></up-icon>
						</view>
					</view>

					<view class="profile-form-item">
						<text class="profile-form-label">微信号</text>
						<input v-model="editForm.wechat" class="profile-form-input" maxlength="32" placeholder="请输入微信号" placeholder-class="profile-form-placeholder" />
					</view>
				</view>

				<view class="profile-popup-actions">
					<view class="profile-popup-btn is-ghost" @tap="closeEditPopup">取消</view>
					<view class="profile-popup-btn" @tap="saveProfile">{{ saving ? '保存中...' : '保存资料' }}</view>
				</view>
			</view>
		</up-popup>

		<up-datetime-picker
			:show="birthdayPickerVisible"
			v-model="birthdayPickerValue"
			mode="date"
			title="选择生日"
			:minDate="birthdayMinDate"
			:maxDate="birthdayMaxDate"
			:zIndex="30000"
			:closeOnClickOverlay="true"
			@confirm="handleBirthdayConfirm"
			@cancel="closeBirthdayPicker"
			@close="closeBirthdayPicker"
		></up-datetime-picker>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		ensureLogin,
		clearAuth
	} from '@/common/auth-guard.js';
	import {
		fetchNotificationConfig,
		saveNotificationConfig,
		submitCancelApply,
		updatePetProfile
	} from '@/common/api/user-pet.js';
	import {
		fetchUserInfo
	} from '@/common/api/auth.js';
	import {
		uploadImage
	} from '@/common/api/file.js';
	import {
		fetchUserAsset
	} from '@/common/api/user-pet.js';
	import {
		onMounted
	} from 'vue';
	import {
		onShow
	} from '@dcloudio/uni-app';

	const isRemoteUrl = url => /^https?:\/\//i.test(String(url || ''));

	const user = ref({
		userName: '',
		phone: '',
		userId: '',
		avatar: '',
		avatarUrl: '',
		levelName: '',
		birthday: '',
		wechat: ''
	});
	const notificationItems = ref([{
			key: 'push',
			label: '消息推送',
			value: true
		},
		{
			key: 'sms',
			label: '短信通知',
			value: true
		}
	]);
	const editPopupVisible = ref(false);
	const saving = ref(false);
	const birthdayPickerVisible = ref(false);
	const birthdayPickerValue = ref(Date.now());
	const birthdayMinDate = new Date(1970, 0, 1).getTime();
	const birthdayMaxDate = new Date(2099, 11, 31).getTime();
	const createEditForm = () => ({
		userName: '',
		birthday: '',
		wechat: '',
		avatar: '',
		avatarUrl: ''
	});
	const editForm = ref(createEditForm());
	const wechatBound = computed(() => !!(user.value.maOpenId || user.value.mpOpenId));

	const loadUserInfo = async () => {
		if (!uni.getStorageSync('token')) {
			resetUserInfo();
			return;
		}
		try {
			const [baseInfo, assetInfo] = await Promise.all([
				fetchUserInfo().catch(() => null),
				fetchUserAsset().catch(() => null)
			]);
			user.value = {
				...user.value,
				...(baseInfo || {}),
				...(assetInfo || {}),
				levelName: assetInfo?.levelName || user.value.levelName,
				birthday: baseInfo?.birthday || baseInfo?.birthDate || '',
				wechat: baseInfo?.wechat || ''
			};
		} catch (e) {
			// 保留空态兜底
		}
	};

	const syncEditForm = () => {
		editForm.value = {
			...createEditForm(),
			userName: user.value.userName || user.value.nickname || '',
			birthday: user.value.birthday || user.value.birthDate || '',
			wechat: user.value.wechat || '',
			avatar: user.value.avatar || '',
			avatarUrl: user.value.avatarUrl || user.value.avatar || ''
		};
		birthdayPickerValue.value = parseBirthdayValue(editForm.value.birthday);
	};

	const parseBirthdayValue = value => {
		if (!value) return Date.now();
		const timestamp = new Date(String(value).replace(/-/g, '/')).getTime();
		return Number.isNaN(timestamp) ? Date.now() : timestamp;
	};

	const formatBirthday = value => {
		const date = new Date(value);
		const year = date.getFullYear();
		const month = String(date.getMonth() + 1).padStart(2, '0');
		const day = String(date.getDate()).padStart(2, '0');
		return `${year}-${month}-${day}`;
	};

	const savingNotification = ref(false);

	const loadNotificationConfig = async () => {
		if (!uni.getStorageSync('token')) return;
		try {
			const cfg = await fetchNotificationConfig();
			if (cfg) {
				notificationItems.value[0].value = cfg.pushEnabled === 1;
				if (notificationItems.value[1]) notificationItems.value[1].value = cfg.smsEnabled === 1;
			}
		} catch (e) {
			uni.showToast({ title: e?.msg || '通知配置加载失败', icon: 'none' });
		}
	};

	const handleNotificationChange = async item => {
		if (savingNotification.value || !uni.getStorageSync('token')) return;
		savingNotification.value = true;
		try {
			const payload = {
				pushEnabled: notificationItems.value[0].value ? 1 : 0,
				smsEnabled: notificationItems.value[1]?.value ? 1 : 0
			};
			await saveNotificationConfig(payload);
			uni.showToast({ title: `${item.label}已更新`, icon: 'none' });
		} catch (e) {
			await loadNotificationConfig();
			uni.showToast({ title: e?.msg || '保存通知配置失败', icon: 'none' });
		} finally {
			savingNotification.value = false;
		}
	};

	onMounted(async () => {
		loadUserInfo();
		await loadNotificationConfig();
	});

	onShow(() => {
		loadUserInfo();
		loadNotificationConfig();
	});

	const goForgotPassword = () => {
		if (!ensureLogin('/packageA/settings/index')) return;
		const phone = encodeURIComponent(user.value.phone || '');
		uni.navigateTo({
			url: `/packageA/login/forgot?phone=${phone}`
		});
	};

	const navigate = path => {
		if (!ensureLogin(path)) {
			return;
		}
		uni.navigateTo({
			url: path
		});
	};

	const goLogin = () => {
		uni.navigateTo({
			url: '/packageA/login/index?redirect=%2FpackageA%2Fsettings%2Findex'
		});
	};

	const handleEditProfile = () => {
		if (!uni.getStorageSync('token')) {
			goLogin();
			return;
		}
		syncEditForm();
		editPopupVisible.value = true;
	};

	const closeEditPopup = () => {
		if (saving.value) return;
		editPopupVisible.value = false;
	};

	const openBirthdayPicker = () => {
		birthdayPickerValue.value = parseBirthdayValue(editForm.value.birthday);
		birthdayPickerVisible.value = true;
	};

	const closeBirthdayPicker = () => {
		birthdayPickerVisible.value = false;
	};

	const chooseAvatar = () => {
		uni.chooseImage({
			count: 1,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: res => {
				const file = res.tempFilePaths?.[0];
				if (!file) return;
				editForm.value.avatar = file;
				editForm.value.avatarUrl = file;
			}
		});
	};

	const handleBirthdayConfirm = e => {
		editForm.value.birthday = formatBirthday(e?.value || birthdayPickerValue.value);
		birthdayPickerVisible.value = false;
	};

	const saveProfile = async () => {
		if (saving.value) return;
		const userName = String(editForm.value.userName || '').trim();
		if (!userName) {
			uni.showToast({ title: '请输入昵称', icon: 'none' });
			return;
		}
		saving.value = true;
		try {
			let avatar = editForm.value.avatar || editForm.value.avatarUrl || user.value.avatar || '';
			if (avatar && !isRemoteUrl(avatar)) {
				avatar = await uploadImage(avatar);
			}
			await updatePetProfile({
				userName,
				avatar,
				birthday: editForm.value.birthday || '',
				wechat: String(editForm.value.wechat || '').trim()
			});
			await loadUserInfo();
			editPopupVisible.value = false;
			uni.showToast({ title: '资料已更新', icon: 'success' });
		} catch (e) {
			uni.showToast({ title: e?.msg || '保存失败', icon: 'none' });
		} finally {
			saving.value = false;
		}
	};

	const resetUserInfo = () => {
		user.value = {
			userName: '',
			phone: '',
			userId: '',
			avatar: '',
			avatarUrl: '',
			levelName: '',
			birthday: '',
			wechat: ''
		};
	};

	const formatPhone = phone => {
		if (!phone) return '--';
		return String(phone).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2');
	};

	const logout = async () => {
		uni.showModal({
			title: '提示',
			content: '确认退出当前登录账号吗？',
			confirmColor: '#c6925e',
			success: (res) => {
				if (!res.confirm) return;
				clearAuth();
				resetUserInfo();
				uni.showToast({
					title: '已退出登录',
					icon: 'none'
				});
				setTimeout(() => {
					uni.switchTab({
						url: '/pages/my/my'
					});
				}, 500);
			}
		});
	};

	const logoutAccount = async () => {
		if (!ensureLogin('/packageA/settings/index')) {
			return;
		}
		uni.showModal({
			title: '提示',
			content: '确认提交账号注销申请吗？',
			confirmColor: '#ea6d69',
			success: async (res) => {
				if (!res.confirm) return;
				try {
					await submitCancelApply('用户主动申请注销');
					clearAuth();
					resetUserInfo();
					uni.showToast({
						title: '注销申请已提交',
						icon: 'none'
					});
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/my/my'
						});
					}, 500);
				} catch (e) {
					uni.showToast({
						title: e?.msg || e?.message || '注销申请失败',
						icon: 'none'
					});
				}
			}
		});
	};
	</script>

<style lang="scss">
	.settings-page {
		background: linear-gradient(180deg, #fdf7ef 0%, #f8efe3 100%);
		min-height: 100vh;
	}

	.pet-shell {
		padding-bottom: 56rpx;
	}

	.profile-banner {
		margin-top: 12rpx;
		display: flex;
		align-items: center;
		gap: 20rpx;
		position: relative;
		padding: 28rpx 168rpx 28rpx 26rpx;
		border-radius: 32rpx;
		background: linear-gradient(180deg, #fffdf9 0%, #fff6eb 100%);
		border: 2rpx dashed rgba(204, 148, 92, 0.7);
		box-shadow: 0 16rpx 32rpx rgba(202, 148, 98, 0.1);
	}

	.profile-popup {
		width: 640rpx;
		max-width: calc(100vw - 64rpx);
		padding: 34rpx 30rpx 30rpx;
		background: linear-gradient(180deg, #fffdf9 0%, #fff4e7 100%);
		box-sizing: border-box;
		border-radius: 32rpx;
	}

	.profile-popup-head {
		text-align: center;
		margin-bottom: 22rpx;
	}

	.profile-popup-title {
		display: block;
		font-size: 34rpx;
		font-weight: 700;
		color: #6d472b;
	}

	.profile-popup-subtitle {
		display: block;
		margin-top: 10rpx;
		font-size: 22rpx;
		color: #b08a69;
	}

	.profile-popup-avatar-row {
		display: flex;
		justify-content: center;
		margin-bottom: 26rpx;
	}

	.profile-popup-avatar {
		position: relative;
		width: 148rpx;
		height: 148rpx;
		padding: 8rpx;
		border-radius: 36rpx;
		background: linear-gradient(180deg, #ffe3ab 0%, #f5c56b 100%);
		box-shadow: 0 16rpx 32rpx rgba(202, 148, 98, 0.16);
		box-sizing: border-box;

		image {
			width: 100%;
			height: 100%;
			border-radius: 28rpx;
			background: #fff8ec;
		}
	}

	.profile-popup-avatar-badge {
		position: absolute;
		right: -8rpx;
		bottom: -6rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: #fff9f2;
		font-size: 20rpx;
		font-weight: 700;
		color: #c28a55;
		box-shadow: 0 8rpx 16rpx rgba(194, 140, 86, 0.12);
	}

	.profile-form-card {
		padding: 10rpx 0;
	}

	.profile-form-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		min-height: 96rpx;
		padding: 0 26rpx;
		margin-bottom: 18rpx;
		border-radius: 24rpx;
		background: rgba(255, 250, 244, 0.98);
		border: 2rpx solid rgba(221, 190, 153, 0.4);
		box-shadow: inset 0 -8rpx 14rpx rgba(247, 230, 206, 0.42);
		box-sizing: border-box;
	}

	.profile-form-picker {
		margin-bottom: 0;
	}

	.profile-form-label {
		font-size: 26rpx;
		font-weight: 700;
		color: #7a5234;
	}

	.profile-form-input {
		flex: 1;
		margin-left: 28rpx;
		text-align: right;
		font-size: 26rpx;
		color: #7a5234;
	}

	.profile-form-placeholder,
	.profile-form-input-text.is-placeholder {
		color: #c7b09a;
	}

	.profile-form-picker-value {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.profile-form-input-text {
		font-size: 26rpx;
		color: #7a5234;
	}

	.profile-popup-actions {
		display: flex;
		gap: 18rpx;
		margin-top: 28rpx;
	}

	.profile-popup-btn {
		flex: 1;
		height: 84rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, #d9ab76 0%, #c6925e 100%);
		color: #fffdf9;
		font-size: 28rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 12rpx 24rpx rgba(194, 140, 86, 0.16);
	}

	.profile-popup-btn.is-ghost {
		background: #fff8ef;
		color: #c08b59;
		box-shadow: inset 0 0 0 2rpx rgba(211, 162, 112, 0.24);
	}

	.profile-avatar {
		width: 108rpx;
		height: 108rpx;
		border-radius: 28rpx;
		background: linear-gradient(180deg, #ffe5ab 0%, #f6c974 100%);
		padding: 6rpx;
		box-shadow: inset 0 0 0 2rpx rgba(255, 255, 255, 0.55);

		image {
			width: 100%;
			height: 100%;
			border-radius: 22rpx;
			background: #fff8ec;
		}
	}

	.profile-copy {
		flex: 1;
		min-width: 0;
	}

	.profile-name {
		display: block;
		font-size: 36rpx;
		font-weight: 700;
		line-height: 1.2;
		margin-bottom: 10rpx;
		color: #6d472b;
		word-break: break-all;
	}

	.pet-subtitle {
		display: inline-flex;
		align-items: center;
		min-height: 34rpx;
		padding: 0 14rpx;
		border-radius: 999rpx;
		background: rgba(249, 222, 177, 0.45);
		font-size: 22rpx;
		color: #b07a4d;
	}

	.edit-btn {
		position: absolute;
		right: 24rpx;
		top: 24rpx;
		height: 62rpx;
		padding: 0 26rpx;
		font-size: 24rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, #d9ab76 0%, #c6925e 100%);
		color: #fffdf9;
		box-shadow: 0 8rpx 18rpx rgba(194, 140, 86, 0.18);
	}

	.paw-mark {
		position: absolute;
		right: 30rpx;
		bottom: 16rpx;
		width: 44rpx;
		height: 44rpx;

		image {
			width: 44rpx;
			height: 44rpx;
			opacity: 0.9;
		}
	}

	.setting-section-title {
		padding: 30rpx 0 18rpx;

		image {
			width: 182rpx;
			height: 36rpx;
		}
	}

	.setting-switch-row,
	.setting-list-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		min-height: 94rpx;
		margin-bottom: 18rpx;
		padding: 0 26rpx;
		border-radius: 26rpx;
		background: linear-gradient(180deg, #fffdfa 0%, #fff7ee 100%);
		box-shadow: 0 12rpx 24rpx rgba(215, 165, 108, 0.08);
		font-size: 28rpx;
		font-weight: 500;
		color: #6f5239;
	}

	.setting-switch-row {
		padding-right: 18rpx;
	}

	.setting-list-item .pet-muted {
		font-size: 24rpx;
		color: #b8a08c;
	}

	.bind-text {
		font-size: 26rpx;
		font-weight: 600;
		color: #89ce98;
	}

	.danger-row {
		color: #ea6d69;
	}

	.danger-soft-row {
		color: #f08f74;
	}

	.danger-row :deep(.u-icon__icon) {
		color: #f08c84 !important;
	}
</style>

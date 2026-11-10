<template>
	<view class="pet-page">
		<view class="pet-shell">
			<view class="pet-card profile-card pet-dashed" @tap="handleLoginEntry">
				<view class="profile-glow profile-glow-left"></view>
				<view class="profile-glow profile-glow-right"></view>
				<view class="profile-top-row">
					<view></view>

				</view>
				<view class="profile-main-card">
					<view class="profile-main">
						<view class="pet-avatar profile-avatar">
							<image :src="user.avatar || user.avatarUrl || '/static/logo.png'" mode="aspectFill"></image>
							<view v-if="isLoggedIn" class="avatar-edit-entry" @tap.stop="handleEditProfileTap">
								<image src="/static/image/my/bianij.png" mode="aspectFit"></image>
							</view>
						</view>
						<view class="profile-info">
							<text class="profile-name">{{ user.userName || user.phone || '立即登录' }}</text>
							<text v-if="isLoggedIn" class="pet-muted profile-id-text">ID:
								{{ user.invitationCode }}</text>
							<text v-if="isLoggedIn" class="pet-tag profile-level">{{ user.level }}</text>
							<text v-if="!isLoggedIn" class="login-entry-tip">点击登录，解锁完整我的页</text>
						</view>
						<view class=" profile-setting-grid" @tap.stop="handleSettingTap" v-if="isLoggedIn">
							<image src="/static/image/my/MA.png"></image>
						</view>
					</view>
					<view class="profile-divider"></view>
					<view class="profile-meta-card">
						<view class="flex">
							<view class="profile-meta-item">
								<text class="meta-label">BIRTHDAY</text>
								<text class="meta-value">{{ displayBirthday }}</text>
							</view>

							<view class="profile-meta-item margin-left-xl">
								<text class="meta-label">WECHAT</text>
								<text class="meta-value">{{ displayWechat }}</text>
							</view>

							<view class="profile-meta-item margin-left-xl">
								<text class="meta-label">SINCE</text>
								<text class="meta-value">{{ displayJoinedAt }}</text>
							</view>
						</view>
						<image src="/static/image/my/maozhua.png"></image>
					</view>
				</view>
			</view>

			<view class="pet-card stats-card">
				<view class="stat-col">
					<text class="stat-num">{{ user.points }}</text>
					<text class="pet-muted">花瓣积分</text>
				</view>
				<view class="stat-col">
					<text class="stat-num">{{ user.coins }}</text>
					<text class="pet-muted">金币</text>
				</view>
				<view class="stat-col">
					<text class="stat-num">{{ user.love }}</text>
					<text class="pet-muted">爱心值</text>
				</view>
			</view>

			<view class="pet-card menu-item featured-menu-item" @tap="handleMenuTap('/packageA/post/mine')">
				<view class="menu-left">
					<view class="menu-icon-wrap featured-icon-wrap">
						<text class="featured-icon">🐾</text>
					</view>
					<view class="featured-copy">
						<text class="featured-title">我发布的朋友圈</text>
						<text class="featured-desc">查看、修改和删除自己发布的动态</text>
					</view>
				</view>
				<view class="menu-right">
					<text v-if="myPostCount > 0" class="menu-badge">{{ myPostCount }}</text>
					<up-icon name="arrow-right" size="16" color="#c5b5a5"></up-icon>
				</view>
			</view>

		<view v-for="item in menuList" :key="item.id" class="pet-card menu-item" @tap="handleMenuTap(item.path)">
			<view class="menu-left">
				<view class="menu-icon-wrap">
					<text v-if="item.emoji" class="menu-emoji">{{ item.emoji }}</text>
					<image v-else :src="item.iconImage" class="menu-icon-image" mode="aspectFit"></image>
				</view>
				<text>{{ item.title }}</text>
			</view>
			<view class="menu-right">
				<text v-if="item.badge" class="menu-badge">{{ item.badge }}</text>
				<up-icon name="arrow-right" size="16" color="#c5b5a5"></up-icon>
			</view>
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
		MY_MENUS
	} from '@/common/my-menus.js';
	import {
		fetchUserAsset,
		updatePetProfile
	} from '@/common/api/user-pet.js';
	import {
		fetchUserInfo
	} from '@/common/api/auth.js';
	import {
		uploadImage
	} from '@/common/api/file.js';
	import {
		fetchMyPostSummary
	} from '@/common/api/community.js';
	import {
		fetchUserCenterSummary
	} from '@/common/api/user-center.js';
	import {
		ensureLogin
	} from '@/common/auth-guard.js';
	import {
		saveMyInvitationCode
	} from '@/common/invite.js';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';

	const createGuestUser = () => ({
		userName: '立即登录',
		phone: '',
		userId: '',
		avatar: '',
		avatarUrl: '',
		level: '',
		birthday: '',
		wechat: '',
		joinedAt: '',
		points: 0,
		coins: 0,
		love: 0
	});

	const formatBirthdayDisplay = value => {
		if (!value) return '--';
		const matched = String(value).match(/^(\d{4})-(\d{2})-(\d{2})/);
		if (matched) {
			return `${matched[2]} / ${matched[3]}`;
		}
		return value;
	};

	const formatJoinedAtDisplay = value => {
		if (!value) return '--';
		const text = String(value).replace('T', ' ').slice(0, 10);
		return text.replace(/-/g, '.');
	};

	const createMenuList = (summary = {}) => MY_MENUS.map(item => ({
		...item,
		iconImage: item.path.includes('/assist/') ? '/static/image/my/zhuli.png' : (item.path.includes(
				'/gift/') ? '/static/image/my/zssp.png' :
			(item.path.includes('/coupon/') ? '/static/image/my/wdjb.png' :
				(item.path.includes('/market/records') || item.path.includes('/exchange/') ? '/static/image/my/zbdh.png' :
					'/static/image/my/shezhi.png'))),
		badge: item.badgeKey && Number(summary[item.badgeKey] || 0) > 0 ? String(summary[item.badgeKey]) : ''
	}));

	const user = ref(createGuestUser());
	const menuList = ref(createMenuList());
	const myPostCount = ref(0);
	const isLoggedIn = ref(false);
	const profileLoading = ref(false);
	let profileLoadSeq = 0;
	let pendingWelcome = false;
	const displayBirthday = computed(() => formatBirthdayDisplay(user.value.birthday));
	const displayWechat = computed(() => user.value.wechat || '--');
	const displayJoinedAt = computed(() => formatJoinedAtDisplay(user.value.joinedAt || user.value.createTime));

	const resetGuestState = () => {
		user.value = createGuestUser();
		menuList.value = createMenuList();
		myPostCount.value = 0;
		isLoggedIn.value = false;
		profileLoading.value = false;
	};

	const showLoadError = (label, e) => {
		const msg = e?.msg || e?.message || `${label}加载失败`;
		uni.showToast({ title: msg, icon: 'none' });
	};

	const loadProfile = async () => {
		const token = uni.getStorageSync('token');
		if (!token) {
			uni.hideLoading();
			resetGuestState();
			return;
		}
		const seq = ++profileLoadSeq;
		isLoggedIn.value = true;
		profileLoading.value = true;
		uni.showLoading({ title: '加载中', mask: true });
		menuList.value = createMenuList();
		const errors = [];
		try {
			const results = await Promise.allSettled([
				fetchUserInfo(),
				fetchUserAsset(),
				fetchUserCenterSummary()
			]);
			if (seq !== profileLoadSeq) return;
			const [baseInfoRes, assetRes, centerSummaryRes] = results;
			if (baseInfoRes.status === 'fulfilled' && baseInfoRes.value) {
				const baseInfo = baseInfoRes.value;
				user.value = {
					...user.value,
					...baseInfo,
					userName: baseInfo.userName || baseInfo.nickName || user.value.userName,
					avatar: baseInfo.avatar || baseInfo.avatarUrl || user.value.avatar,
					birthday: baseInfo.birthday || baseInfo.birthDate || user.value.birthday,
					wechat: baseInfo.wechat || user.value.wechat,
					joinedAt: baseInfo.createTime || user.value.joinedAt
				};
				if (baseInfo.invitationCode) {
					saveMyInvitationCode(baseInfo.invitationCode);
				}
			} else if (baseInfoRes.status === 'rejected') {
				errors.push('用户信息');
			}
			if (assetRes.status === 'fulfilled' && assetRes.value) {
				const asset = assetRes.value;
				user.value.points = asset.points ?? user.value.points;
				user.value.coins = asset.coins ?? user.value.coins;
				user.value.love = asset.loveValue ?? user.value.love;
				user.value.level = asset.levelName;
			} else if (assetRes.status === 'rejected') {
				errors.push('资产');
			}
			if (centerSummaryRes.status === 'fulfilled' && centerSummaryRes.value) {
				menuList.value = createMenuList(centerSummaryRes.value);
			} else if (centerSummaryRes.status === 'rejected') {
				errors.push('菜单汇总');
			}
			try {
				const summary = await fetchMyPostSummary();
				if (seq !== profileLoadSeq) return;
				myPostCount.value = Number(summary?.total || 0);
			} catch (e) {
				if (seq !== profileLoadSeq) return;
				myPostCount.value = 0;
			}
		} catch (e) {
			if (seq !== profileLoadSeq) return;
			showLoadError('我的页', e);
		} finally {
			if (seq === profileLoadSeq) {
				profileLoading.value = false;
				uni.hideLoading();
				if (pendingWelcome) {
					pendingWelcome = false;
					uni.showToast({ title: '欢迎回来', icon: 'none' });
				}
			}
		}
		if (seq === profileLoadSeq && errors.length) {
			showLoadError(errors.join('、'), { message: '请稍后重试' });
		}
	};

	onLoad(option => {
		pendingWelcome = option?.from === 'login';
		loadProfile();
	});

	onShow(() => {
		loadProfile();
	});

	const goLogin = () => {
		uni.navigateTo({
			url: '/packageA/login/index?redirect=%2Fpages%2Fmy%2Fmy'
		});
	};

	const handleLoginEntry = () => {
		if (isLoggedIn.value) return;
		goLogin();
	};

	const handleSettingTap = () => {
		if (!isLoggedIn.value) {
			goLogin();
			return;
		}
		goTo('/packageA/settings/index');
	};

	const handleEditProfileTap = () => {
		if (!isLoggedIn.value) {
			goLogin();
			return;
		}
		syncEditForm();
		editPopupVisible.value = true;
	};

	// ===== 编辑资料弹框（与设置页一致） =====
	const isRemoteUrl = url => /^https?:\/\//i.test(String(url || ''));
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

	const handleBirthdayConfirm = e => {
		editForm.value.birthday = formatBirthday(e?.value || birthdayPickerValue.value);
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
			await loadProfile();
			editPopupVisible.value = false;
			uni.showToast({ title: '资料已更新', icon: 'success' });
		} catch (e) {
			uni.showToast({ title: e?.msg || '保存失败', icon: 'none' });
		} finally {
			saving.value = false;
		}
	};

	const handleMenuTap = url => {
		if (!isLoggedIn.value && !ensureLogin(url)) {
			return;
		}
		goTo(url);
	};

	const goTo = url => {
		uni.navigateTo({
			url
		});
	};
</script>

<style lang="scss">
	.profile-card {
		margin-top: 8rpx;
		position: relative;
		padding: 18rpx;
		border-width: 0;
		background: linear-gradient(180deg, #f6e8d7 0%, #f3dfc7 100%);
		box-shadow: 0 18rpx 36rpx rgba(171, 119, 67, 0.1);
		border: 4rpx solid #c99866;
		border-bottom-width: 10rpx;
		border-radius: 34rpx;
		overflow: hidden;
	}

	.profile-glow {
		position: absolute;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.48);
		filter: blur(10rpx);
	}

	.profile-glow-left {
		width: 220rpx;
		height: 220rpx;
		left: -72rpx;
		top: 120rpx;
	}

	.profile-glow-right {
		width: 180rpx;
		height: 180rpx;
		right: -40rpx;
		top: -20rpx;
	}

	.profile-top-row {
		position: relative;
		z-index: 2;
		display: flex;
		align-items: center;
		justify-content: flex-end;
		margin-bottom: 12rpx;
	}

	.profile-setting {
		width: 64rpx;
		height: 64rpx;
		border-radius: 50%;
		background: #fffaf4;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 18rpx rgba(208, 162, 112, 0.14);
	}

	.profile-setting image {
		width: 36rpx;
		height: 36rpx;
	}

	.profile-setting-grid {
		width: 64rpx;
		height: 64rpx;

		image {
			width: 64rpx;
			height: 64rpx;
		}

	}

	.profile-main {
		display: flex;
		align-items: flex-start;
		gap: 22rpx;
		justify-content: space-between;
	}

	.profile-main-card {
		position: relative;
		z-index: 2;
		padding: 18rpx 26rpx 22rpx;
		border-radius: 26rpx;
	}

	.profile-avatar {
		width: 118rpx;
		height: 118rpx;
		border-radius: 26rpx;

		padding: 0;
		border: 2rpx solid #FFFFFF;
		flex-shrink: 0;
		position: relative;
		overflow: visible;
	}

	.profile-avatar image {
		border-radius: 24rpx;
	}

	.avatar-edit-entry {
		position: absolute;
		right: -12rpx;
		bottom: -12rpx;
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;

		display: flex;
		align-items: center;
		justify-content: center;
	}

	.avatar-edit-entry image {
		width: 44rpx;
		height: 44rpx;
	}

	.profile-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
		padding-top: 6rpx;
		margin-left: 10rpx;
	}

	.login-entry-tip {
		font-size: 22rpx;
		color: #b58d6a;
		line-height: 1.5;
	}

	.login-entry-button {
		width: 136rpx;
		height: 54rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, #f4c289 0%, #eaab66 100%);
		color: #fffdf8;
		font-size: 24rpx;
		font-weight: 700;
		line-height: 54rpx;
		text-align: center;
		box-shadow: 0 8rpx 18rpx rgba(211, 144, 76, 0.24);
	}

	.profile-name {
		font-size: 32rpx;
		font-weight: 700;
		color: #6e472b;
		line-height: 1.1;
	}

	.profile-info .pet-muted {
		color: #b08a68;
	}

	.profile-id-text {
		font-size: 26rpx;
	}

	.profile-level {
		width: fit-content;
		padding: 0rpx 18rpx;
		background: #f1cb72;
		color: #7c522c;
		font-size: 22rpx;
		font-weight: 700;
	}

	.profile-action-row {
		display: flex;
		align-items: flex-start;
		align-self: stretch;
		padding-top: 6rpx;
	}

	.profile-link-chip {
		display: flex;
		align-items: center;
		gap: 8rpx;
		height: 54rpx;
		padding: 0 16rpx;
		border-radius: 999rpx;
		background: #fff4e8;
		font-size: 22rpx;
		font-weight: 600;
		color: #9c6f45;
		white-space: nowrap;
		border: 2rpx solid #f0dcc7;
	}

	.profile-divider {
		height: 2rpx;
		margin: 20rpx 0 18rpx;
		border-top: 2rpx dashed rgba(202, 151, 99, 0.65);
	}

	.profile-meta-card {
		position: relative;
		margin-top: 0;
		background: transparent;
		border-radius: 0;
		padding: 6rpx 4rpx 2rpx;
		display: flex;
		align-items: flex-end;
		justify-content: space-between;

		image {
			width: 50rpx;
			height: 50rpx;
		}
	}

	.profile-meta-item {
		flex: 1;
	}

	.meta-label {
		display: block;
		font-size: 22rpx;
		color: rgba(120, 72, 35, 0.62);

	}

	.meta-value {
		font-size: 26rpx;
		font-weight: 700;
		color: #714522;

	}

	.profile-meta-divider {
		width: 2rpx;
		height: 52rpx;
		margin: 0 26rpx;
		background: rgba(147, 94, 53, 0.16);
	}


	.profile-setting-grid :deep(.u-icon__icon) {
		font-weight: 700;
	}

	.profile-paw,
	.profile-setting,
	.profile-link-chip,
	.login-entry-button {
		flex-shrink: 0;
	}

	.profile-heart-badge {
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.stats-card {
		margin: 24rpx 0 28rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 20rpx 0;
		background: linear-gradient(180deg, #fffaf4 0%, #fff3e6 100%);
		border-radius: 32rpx;
		box-shadow: 0 16rpx 32rpx rgba(201, 149, 106, 0.08);
	}

	.stat-col {
		flex: 1;
		text-align: center;
		position: relative;
		padding: 18rpx 0;
	}

	.stat-col:not(:last-child)::after {
		content: '';
		position: absolute;
		right: 0;
		top: 18rpx;
		width: 2rpx;
		height: 72rpx;
		background: linear-gradient(180deg, transparent, rgba(220, 183, 146, 0.8), transparent);
	}

	.stat-num {
		display: block;
		font-size: 44rpx;
		font-weight: 700;
		color: #b97a44;
		margin-bottom: 12rpx;
	}

	.stats-card .pet-muted {
		font-size: 24rpx;
		color: #a78f78;
	}

	.menu-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 22rpx;
		padding: 30rpx 24rpx;
		border-radius: 30rpx;
		box-shadow: 0 14rpx 28rpx rgba(201, 149, 106, 0.08);
	}

	.featured-menu-item {
		margin-bottom: 22rpx;
		background: linear-gradient(135deg, #fff7ef 0%, #fff0dd 100%);
		border: 2rpx solid rgba(211, 162, 112, 0.22);
	}

	.menu-left,
	.menu-right {
		display: flex;
		align-items: center;
		gap: 18rpx;
		font-size: 30rpx;
		font-weight: 600;
	}

	.menu-icon-wrap {
		width: 68rpx;
		height: 68rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 20rpx;
		background: #fff6ea;
		box-shadow: inset 0 -8rpx 16rpx rgba(241, 213, 180, 0.38);
	}

	.featured-icon-wrap {
		background: linear-gradient(135deg, #f7d67d, #efbb66);
		box-shadow: none;
	}

	.featured-icon {
		font-size: 30rpx;
	}

	.featured-copy {
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	.featured-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #6c4d36;
	}

	.featured-desc {
		font-size: 22rpx;
		color: #b19072;
	}

	.menu-icon-image {
		width: 36rpx;
		height: 36rpx;
	}

	.menu-emoji {
		font-size: 34rpx;
		line-height: 1;
	}

	.menu-badge {
		min-width: 38rpx;
		height: 38rpx;
		padding: 0 12rpx;
		border-radius: 999rpx;
		background: #f1647d;
		color: #fff;
		font-size: 22rpx;
		line-height: 38rpx;
		text-align: center;
	}

	/* 编辑资料弹框（与设置页一致） */
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
</style>

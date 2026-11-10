<template>
	<view class="pet-page post-create-page">
		<up-navbar
			:title="pageTitle"
			:placeholder="true"
			:border="false"
			bgColor="#FFF8F0"
			titleStyle="color:#C4956A;font-size:32rpx;font-weight:500"
			leftIcon="arrow-left"
			leftIconColor="#8b613d"
			:rightText="submitText"
			:autoBack="true"
			@rightClick="publishPost"
		></up-navbar>
		<view v-if="pageLoading" class="post-loading-mask">
			<text class="post-loading-text">加载动态中...</text>
		</view>
		<view class="pet-shell post-shell">
			<view class="post-panel post-media-panel">
				<view class="post-block-head">
					<view class="post-title-wrap">
						<text class="post-title-icon">📷</text>
						<text class="post-block-title">照片 / 视频</text>
					</view>
					<text class="post-count">{{ uploadFiles.length }}/9</text>
				</view>
				
				<view class="post-media-grid">
					<view v-for="(item, index) in uploadFiles" :key="item + index" class="post-media-card" @tap="previewMedia(index)">
						<video
							v-if="isVideo(item)"
							:src="item"
							class="post-media-video"
							:show-center-play-btn="true"
							:controls="false"
							object-fit="cover"
						></video>
						<image v-else :src="item" mode="aspectFill"></image>
						<view class="post-media-delete" @tap.stop="removeImage(index)">
							<up-icon name="close" size="12" color="#FFFFFF"></up-icon>
						</view>
					</view>
					<view v-if="uploadFiles.length < 9" class="upload-box" @tap="mockUpload">
						<up-icon name="camera" size="28" color="#8b613d"></up-icon>
						<text class="upload-box-title">点击上传</text>
						<text class="upload-box-txt">照片/视频</text>
					</view>
				</view>
			</view>

			<view class="post-panel post-editor-panel">
				<view class="post-panel-chip"></view>
				<view class="post-editor-box">
					<view class="post-editor-tip">公开一下你和毛孩子的日常吧~ 🐾</view>
					<view class="post-textarea-wrap">
						<textarea v-model="content" class="post-textarea" maxlength="500" placeholder="写下你和宠物的故事"
							placeholder-class="post-textarea-placeholder"></textarea>
					</view>
					<view class="post-block-foot">
						<view class="flex align-center">
							<text class="post-title-icon margin-right-xs">🐾</text>
							<text class="post-foot-tip">写下和宠物的故事</text>
						</view>
						<text class="post-foot-count">{{ content.length }}/500</text>
					</view>
				</view>
			</view>

			<view class="post-panel option-panel">
				<view class="post-panel-chip is-left"></view>
				<view class="post-section-head">
					<text class="post-title-icon">🐾</text>
					<text class="post-block-title">关联宠物</text>
				</view>
				<view class="post-select-row">
					<view class="post-select-pill">
						<text class="post-select-emoji">🐶</text>
						<text class="post-select-text">{{ pets.find(item => String(item.id) === selectedPetId)?.name || '选择宠物' }}</text>
						<up-icon name="arrow-down" size="12" color="#c09a73"></up-icon>
					</view>
				</view>
				<view class="post-option-row compact-row">
					<view v-for="item in pets" :key="item.id" class="post-mini-pill"
						:class="{ 'is-active': selectedPetId === String(item.id) }" @tap="selectedPetId = String(item.id)">{{ item.name }}</view>
				</view>
			</view>

			<view class="post-panel option-panel">
				<view class="post-panel-chip is-left"></view>
				<view class="post-section-head">
					<text class="post-title-icon">📍</text>
					<text class="post-block-title">位置</text>
				</view>
				<view class="post-select-row">
					<view class="post-select-pill is-location">
						<text class="post-select-emoji">📌</text>
						<text class="post-select-text">{{ stores.find(item => String(item.id) === selectedStoreId)?.name || '选择门店' }}</text>
						<up-icon name="arrow-down" size="12" color="#c09a73"></up-icon>
					</view>
				</view>
				<view class="post-option-row compact-row">
					<view v-for="item in stores" :key="item.id" class="post-mini-pill"
						:class="{ 'is-active': selectedStoreId === String(item.id) }" @tap="selectedStoreId = String(item.id)">{{ item.name }}</view>
				</view>
			</view>

			<view class="post-panel option-panel">
				<view class="post-panel-chip is-right"></view>
				<view class="post-section-head">
					<text class="post-title-icon">👁️</text>
					<text class="post-block-title">谁可以看</text>
				</view>
				<view class="post-option-row visibility-row">
					<view v-for="item in visibleOptions" :key="item" class="post-visibility-pill"
						:class="{ 'is-active': visibility === item }" @tap="visibility = item">
						<text class="post-visibility-dot">{{ visibilityIconMap[item] }}</text>
						<text>{{ item }}</text>
					</view>
				</view>
				<view class="post-toggle-list">
					<view class="toggle-row">
						<view class="toggle-label-wrap">
							<image class="toggle-icon" src="/packageA/static/post/yxpl.png" mode="widthFix"></image>
							<text class="toggle-label">允许评论</text>
						</view>
						<up-switch v-model="allowComment" activeColor="#c79662"></up-switch>
					</view>
					<view class="toggle-row">
						<view class="toggle-label-wrap">
							<image class="toggle-icon" src="/packageA/static/post/yxdz.png" mode="widthFix"></image>
							<text class="toggle-label">允许点赞</text>
						</view>
						<up-switch v-model="allowLike" activeColor="#c79662"></up-switch>
					</view>
				</view>
			</view>

			<view class="post-submit-wrap">
				<view class="agreement-check-row" @tap="agreed = !agreed">
					<view class="agreement-checkbox" :class="{ checked: agreed }">
						<up-icon v-if="agreed" name="checkmark" size="12" color="#fff"></up-icon>
					</view>
					<text class="post-submit-note">我已阅读并同意</text>
					<text class="post-agreement-link" @tap.stop="goAgreement">社区公约</text>
				</view>
				<view class="pet-button post-submit-btn" @tap="publishPost">
					<text class="post-submit-icon">📘</text>
					<text>{{ submitText }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import {
		fetchPetList,
		fetchPostInfo,
		fetchStoreList,
		publishPost as publishPostApi,
		updatePost as updatePostApi
	} from '@/common/api/community.js';
	import { fetchArchiveInfo } from '@/common/api/archive.js';
	import { uploadImage } from '@/common/api/file.js';
	import { ensureLogin } from '@/common/auth-guard.js';
	import { onLoad } from '@dcloudio/uni-app';

	const postId = ref('');
	const pageTitle = ref('发布动态');
	const submitText = ref('发布');
	const pageLoading = ref(false);

	const content = ref('');
	const visibility = ref('公开');
	const visibleOptions = ['公开', '仅自己'];
	const visibilityIconMap = {
		'公开': '🌍',
		'仅自己': '🔒'
	};
	const pets = ref([]);
	const stores = ref([]);
	const selectedPetId = ref('');
	const selectedStoreId = ref('');
	const allowComment = ref(true);
	const allowLike = ref(true);
	const uploadFiles = ref([]);
	const mediaType = ref(1);
	const publishing = ref(false);
	const agreed = ref(false);

	const VIDEO_EXT = ['.mp4', '.mov', '.m4v', '.webm'];
	const isEditMode = computed(() => Boolean(postId.value));
	const isVideo = url => VIDEO_EXT.some(ext => String(url).toLowerCase().includes(ext));

	const mapVisibilityText = value => {
		if (String(value) === '2') return '仅自己';
		return '公开';
	};

	const normalizePet = item => ({
		...item,
		name: item?.name || item?.petName || `宠物${item?.id || ''}`
	});

	const normalizeStore = item => ({
		...item,
		name: item?.name || item?.storeName || `门店${item?.id || ''}`
	});

	const resolveMediaType = (urls = []) => {
		if (urls.some(url => isVideo(url))) return 2;
		return 1;
	};

	const fillPostForm = data => {
		content.value = data?.content || '';
		uploadFiles.value = data?.mediaUrls
			? String(data.mediaUrls).split(',').filter(Boolean)
			: (data?.mediaList || []);
		visibility.value = mapVisibilityText(data?.visibility);
		allowComment.value = Number(data?.allowComment) !== 0;
		allowLike.value = Number(data?.allowLike) !== 0;
		selectedPetId.value = data?.petId ? String(data.petId) : '';
		selectedStoreId.value = data?.storeId ? String(data.storeId) : '';
		mediaType.value = Number(data?.mediaType || resolveMediaType(uploadFiles.value));
	};

	const mergeSelectOptions = detail => {
		if (detail?.petId) {
			const petIdText = String(detail.petId);
			if (!pets.value.some(item => String(item.id) === petIdText)) {
				pets.value.unshift({
					id: detail.petId,
					name: detail.petName || `宠物${detail.petId}`
				});
			}
		}
		if (detail?.storeId) {
			const storeIdText = String(detail.storeId);
			if (!stores.value.some(item => String(item.id) === storeIdText)) {
				stores.value.unshift({
					id: detail.storeId,
					name: detail.storeName || `门店${detail.storeId}`
				});
			}
		}
	};

	const applyCreateDefaults = (initialPetId = '') => {
		if (initialPetId) {
			selectedPetId.value = String(initialPetId);
			return;
		}
		if (!selectedPetId.value && pets.value.length) {
			selectedPetId.value = String(pets.value[0].id);
		}
		if (!selectedStoreId.value && stores.value.length) {
			selectedStoreId.value = String(stores.value[0].id);
		}
	};

	const applyInitialPet = async (initialPetId) => {
		if (!initialPetId) return;
		const petIdText = String(initialPetId);
		if (!pets.value.some(item => String(item.id) === petIdText)) {
			try {
				const info = await fetchArchiveInfo(initialPetId);
				if (info?.id) {
					pets.value.unshift(normalizePet(info));
				}
			} catch (e) {
				pets.value.unshift({ id: Number(initialPetId), name: `宠物${initialPetId}` });
			}
		}
		selectedPetId.value = petIdText;
	};

	const resolveEditId = option => {
		const raw = option?.id || option?.postId;
		return raw ? String(raw) : '';
	};

	const loadPageData = async (editId, initialPetId = '') => {
		pageLoading.value = Boolean(editId);
		try {
			const [petList, storeList, detail] = await Promise.all([
				fetchPetList(),
				fetchStoreList(),
				editId ? fetchPostInfo(editId, false) : Promise.resolve(null)
			]);
			pets.value = (Array.isArray(petList) ? petList : []).map(normalizePet);
			stores.value = (Array.isArray(storeList) ? storeList : []).map(normalizeStore);
			if (detail) {
				mergeSelectOptions(detail);
				fillPostForm(detail);
			} else if (initialPetId) {
				await applyInitialPet(initialPetId);
			} else {
				applyCreateDefaults();
			}
		} catch (e) {
			if (editId) {
				uni.showToast({ title: e?.msg || '动态加载失败', icon: 'none' });
				setTimeout(() => uni.navigateBack(), 800);
			} else {
				pets.value = [];
				stores.value = [];
			}
		} finally {
			pageLoading.value = false;
		}
	};

	onLoad(async option => {
		if (!ensureLogin('/packageA/post/create')) return;
		const editId = resolveEditId(option);
		const initialPetId = option?.petId ? String(option.petId) : '';
		if (editId) {
			postId.value = editId;
			pageTitle.value = '修改动态';
			submitText.value = '保存并提交';
		}
		await loadPageData(editId, initialPetId);
	});

	const mockUpload = () => {
		if (uploadFiles.value.length >= 9) {
			uni.showToast({ title: '最多上传 9 个内容', icon: 'none' });
			return;
		}
		const remain = 9 - uploadFiles.value.length;
		const appendFiles = paths => {
			uploadFiles.value = [...uploadFiles.value, ...(paths || [])].slice(0, 9);
		};
		if (typeof uni.chooseMedia === 'function') {
			uni.chooseMedia({
				count: remain,
				mediaType: ['image', 'video'],
				sourceType: ['album', 'camera'],
				maxDuration: 60,
				success: res => {
					const paths = (res.tempFiles || []).map(item => item.tempFilePath).filter(Boolean);
					appendFiles(paths);
				}
			});
			return;
		}
		uni.showActionSheet({
			itemList: ['选择图片', '选择视频'],
			success: res => {
				if (res.tapIndex === 0) {
					uni.chooseImage({
						count: remain,
						sizeType: ['compressed'],
						sourceType: ['album', 'camera'],
						success: r => appendFiles(r.tempFilePaths || [])
					});
				} else {
					uni.chooseVideo({
						sourceType: ['album', 'camera'],
						maxDuration: 60,
						success: r => {
							if (r.tempFilePath) appendFiles([r.tempFilePath]);
						}
					});
				}
			}
		});
	};

	const goAgreement = () => {
		uni.navigateTo({ url: '/packageA/post/rules' });
	};

	const removeImage = index => {
		uploadFiles.value.splice(index, 1);
	};

	const previewMedia = index => {
		const current = uploadFiles.value[index];
		if (isVideo(current)) {
			uni.navigateTo({ url: `/pages/webView/webView?url=${encodeURIComponent(current)}` });
			return;
		}
		const imageUrls = uploadFiles.value.filter(url => !isVideo(url));
		if (!imageUrls.length) return;
		uni.previewImage({
			current,
			urls: imageUrls
		});
	};

	const uploadMediaFiles = async () => {
		const urls = [];
		const failed = [];
		for (let i = 0; i < uploadFiles.value.length; i += 1) {
			const file = uploadFiles.value[i];
			if (/^https?:\/\//i.test(file)) {
				urls.push(file);
				continue;
			}
			try {
				const url = await uploadImage(file);
				urls.push(url);
			} catch (e) {
				failed.push(i + 1);
			}
		}
		if (failed.length) {
			uni.showToast({ title: `第 ${failed.join('、')} 个文件上传失败`, icon: 'none' });
			throw new Error('upload failed');
		}
		return urls;
	};

	const publishPost = async () => {
		if (!agreed.value) {
			uni.showToast({ title: '请先同意社区公约', icon: 'none' });
			return;
		}
		if (!content.value.trim()) {
			uni.showToast({ title: '先写点内容吧', icon: 'none' });
			return;
		}
		if (publishing.value) return;
		publishing.value = true;
		try {
			const mediaUrls = await uploadMediaFiles();
			const payload = {
				content: content.value.trim(),
				mediaUrls: mediaUrls.join(','),
				mediaType: resolveMediaType(mediaUrls),
				storeId: selectedStoreId.value ? Number(selectedStoreId.value) : undefined,
				petId: selectedPetId.value ? Number(selectedPetId.value) : undefined,
				visibility: visibility.value === '仅自己' ? 2 : 1,
				allowComment: allowComment.value ? 1 : 0,
				allowLike: allowLike.value ? 1 : 0
			};
			if (isEditMode.value) {
				await updatePostApi({ ...payload, id: Number(postId.value) });
			} else {
				await publishPostApi(payload);
			}
			uni.showToast({
				title: isEditMode.value ? '修改已提交，等待审核' : '动态已发布，等待审核',
				icon: 'success'
			});
			setTimeout(() => {
				if (isEditMode.value) {
					uni.navigateBack();
				} else {
					uni.redirectTo({ url: '/packageA/post/mine?tab=pending' });
				}
			}, 500);
		} catch (e) {
			uni.showToast({ title: e?.msg || '发布失败', icon: 'none' });
		} finally {
			publishing.value = false;
		}
	};
</script>

<style lang="scss">
	.post-create-page {
		background: linear-gradient(180deg, #fdf5ec 0%, #fbf1e5 100%);
	}

	.post-shell {
		padding-bottom: 40rpx;
	}

	.post-panel {
		position: relative;
		margin-top: 20rpx;
		padding: 24rpx;
		background: rgba(255, 253, 249, 0.98);
		border-radius: 30rpx;
		border: 2rpx solid rgba(236, 215, 191, 0.7);
		box-shadow: 0 14rpx 34rpx rgba(171, 123, 79, 0.08);
	}

	.post-panel-chip {
		position: absolute;
		top: -2rpx;
		left: 50%;
		transform: translateX(-50%);
		width: 62rpx;
		height: 16rpx;
		border-radius: 0 0 10rpx 10rpx;
		background: #f7df9a;
	}

	.post-panel-chip.is-left {
		left: 44rpx;
		transform: none;
	}

	.post-panel-chip.is-right {
		left: auto;
		right: 28rpx;
		transform: none;
	}

	.post-block-head,
	.post-block-foot,
	.toggle-row,
	.post-section-head,
	.toggle-label-wrap,
	.post-title-wrap,
	.post-select-pill {
		display: flex;
		align-items: center;
	}

	.post-block-head,
	.post-block-foot,
	.toggle-row {
		justify-content: space-between;
	}

	.post-title-wrap,
	.post-section-head {
		gap: 10rpx;
	}

	.toggle-icon {
		width:35rpx;
		height: 35rpx;
	}

	.post-title-icon,

	.post-select-emoji,
	.post-visibility-dot {
		font-size: 22rpx;
		line-height: 1;
	}

	.post-block-title {
		font-size: 26rpx;
		font-weight: 700;
		color: #6f4f39;
	}

	.post-count,
	.post-foot-count,
	.post-foot-tip {
		font-size: 24rpx;
		color: #baa793;
	}

	.post-media-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
		margin-top: 18rpx;
	}

	.upload-box,
	.post-media-card {
		position: relative;
		width: 200rpx;
		height: 200rpx;
		border-radius: 24rpx;
		overflow: hidden;
	}

	.upload-box {
		border: 3rpx dashed #e0bf95;
		background: linear-gradient(180deg, #fffaf3 0%, #fff4e5 100%);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
	}

	.upload-box-title {
		font-size: 24rpx;
		font-weight: 700;
		color: #8b613d;
	}

	.upload-box-txt {
		font-size: 22rpx;
		color: #c09a73;
	}

	.post-media-card image,
	.post-media-video {
		width: 100%;
		height: 100%;
		display: block;
	}

	.post-loading-mask {
		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		z-index: 99;
		display: flex;
		align-items: center;
		justify-content: center;
		background: rgba(253, 245, 236, 0.72);
	}

	.post-loading-text {
		font-size: 28rpx;
		color: #8b613d;
	}

	.post-media-delete {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		background: #8b613d;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 16rpx rgba(139, 97, 61, 0.16);
		z-index: 2;
	}

	.post-editor-panel {
		padding-top: 30rpx;
	}

	.post-editor-box {
		box-sizing: border-box;
		padding: 18rpx 18rpx 14rpx;
		border-radius: 24rpx;
		background: linear-gradient(180deg, #fbfaf7 0%, #f7f3eb 100%);
	}

	.post-editor-tip {
		margin-bottom: 14rpx;
		font-size: 22rpx;
		font-weight: 600;
		color: #9c8872;
	}

	.post-textarea-wrap {
		padding: 22rpx 24rpx;
		border-radius: 22rpx;
		background: linear-gradient(180deg, #f4ead8 0%, #f3e6d1 100%);
		overflow: hidden;
	}

	.post-textarea {
		display: block;
		width: auto;
		min-height: 180rpx;
		font-size: 26rpx;
		line-height: 1.8;
		color: #6d4c38;
	}

	.post-textarea-placeholder {
		color: #aa9a85;
		font-size: 26rpx;
		line-height: 1.8;
	}

	.post-block-foot {
		margin-top: 10rpx;
	}

	.option-panel {
		padding-top: 28rpx;
	}

	.post-select-row {
		margin-top: 16rpx;
	}

	.post-select-pill {
		box-sizing: border-box;
		justify-content: space-between;
		gap: 10rpx;
		width: 100%;
		height: 66rpx;
		padding: 0 22rpx;
		border-radius: 999rpx;
		background: #fff8ef;
		border: 2rpx solid #e6caab;
	}

	.post-select-pill.is-location {
		background: #fff9f3;
	}

	.post-select-text {
		flex: 1;
		font-size: 24rpx;
		color: #7e5b44;
	}

	.post-option-row {
		display: flex;
		flex-wrap: wrap;
		gap: 14rpx;
		margin-top: 16rpx;
	}

	.compact-row {
		margin-top: 18rpx;
	}

	.post-mini-pill,
	.post-visibility-pill {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		height: 54rpx;
		padding: 0 18rpx;
		border-radius: 999rpx;
		font-size: 22rpx;
		color: #8e7863;
		background: #f8efe4;
		border: 2rpx solid transparent;
	}

	.post-mini-pill.is-active,
	.post-visibility-pill.is-active {
		background: #fff4de;
		color: #8b613d;
		border-color: #d7aa77;
	}

	.visibility-row {
		margin-top: 18rpx;
	}

	.post-toggle-list {
		margin-top: 18rpx;
		padding-top: 6rpx;
	}

	.toggle-row {
		padding: 16rpx 0;
		border-top: 2rpx dashed #ecd7bf;
	}

	.toggle-row:first-child {
		border-top: none;
	}

	.toggle-label-wrap {
		gap: 10rpx;
	}

	.toggle-label {
		font-size: 24rpx;
		color: #7a5a43;
	}

	.post-submit-wrap {
		margin-top: 26rpx;
		padding: 0 10rpx 8rpx;
		text-align: center;
	}

	.post-submit-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		height: 88rpx;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #cea06f, #c28a55);
		box-shadow: 0 16rpx 28rpx rgba(194, 138, 85, 0.24);
	}

	.post-submit-icon {
		font-size: 24rpx;
		line-height: 1;
	}

	.post-submit-note {
		font-size: 18rpx;
		color: #b8ab99;
	}

	.post-agreement-row {
		margin-top: 14rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
	}

	.agreement-check-row {
		margin-bottom: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
	}

	.agreement-checkbox {
		width: 32rpx;
		height: 32rpx;
		border-radius: 8rpx;
		border: 2rpx solid #d7aa77;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #fff;
	}

	.agreement-checkbox.checked {
		background: #c79662;
		border-color: #c79662;
	}

	.post-agreement-link {
		font-size: 18rpx;
		font-weight: 700;
		color: #c79662;
		text-decoration: underline;
		text-underline-offset: 4rpx;
	}

	@media screen and (max-width: 360px) {

		.upload-box,
		.post-media-card {
			width: calc((100vw - 112rpx) / 3);
			height: calc((100vw - 112rpx) / 3);
		}

		.post-option-row {
			gap: 10rpx;
		}
	}
</style>

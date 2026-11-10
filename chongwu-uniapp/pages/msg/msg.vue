<template>
	<view class="pet-page">
		<up-navbar :placeholder="true" :border="false" bgColor="#FFF8F0" leftIcon="" title="">
			<template #left>
				<view class="home-main-title">宠物朋友圈</view>
			</template>
			<template #right>
				<view class="navbar-actions">
					<view class="navbar-action" @tap="openNotifications">
						<up-icon name="bell" size="28" color="#c79662"></up-icon>
						<view v-if="unreadCount > 0" class="notify-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
					</view>
					<view class="navbar-action" @tap="goCreate">
						<up-icon name="camera" size="28" color="#c79662"></up-icon>
					</view>
				</view>
			</template>
		</up-navbar>
		<view class="pet-shell">
			<view v-for="post in posts" :key="post.id" class="pet-card post-card" @tap="goDetail(post)">
				<view class="post-head">
					<view class="pet-avatar">
						<image :src="post.avatar || '/static/logo.png'" mode="aspectFill"></image>
					</view>
					<view class="post-user">
						<text class="post-name">{{ post.userName || post.name }}</text>
						<text class="pet-muted">{{ post.timeText || post.time }} · {{ metaLine(post) }}</text>
					</view>
				</view>
				<text class="post-content">{{ post.content }}</text>
				<view v-if="post.mediaList?.length" class="post-gallery">
					<view v-for="(media, idx) in post.mediaList" :key="idx" class="pet-thumb gallery-item"
						@tap.stop="previewMedia(post, idx)">
						<video v-if="isVideo(media)" :src="media" class="gallery-video" :show-center-play-btn="true"
							:controls="false" object-fit="cover"></video>
						<image v-else :src="media" mode="aspectFill"></image>
					</view>
				</view>
				<view class="post-action-row">
					<view
						class="post-action"
						:class="{ disabled: Number(post.allowLike) === 0 }"
						@tap.stop="toggleLike(post)"
					>
						<up-icon :name="post.liked ? 'heart-fill' : 'heart'" size="24"
							:color="post.liked ? '#f199a5' : '#b6ac9f'"></up-icon>
						<text>{{ post.likeCount ?? post.likes }}</text>
					</view>
					<view
						class="post-action"
						:class="{ disabled: Number(post.allowComment) === 0 }"
						@tap.stop="openComment(post)"
					>
						<up-icon name="chat" size="24" color="#b6ac9f"></up-icon>
						<text>{{ post.commentCount ?? post.comments }}</text>
					</view>
					<view class="post-action share" @tap.stop="sharePost(post)">
						<up-icon name="share" size="24" color="#b6ac9f"></up-icon>
					</view>
				</view>
			</view>
			<view v-if="loading" class="pet-muted center-text">加载中...</view>
			<view v-if="!loading && !posts.length" class="pet-muted center-text">暂无动态</view>
		</view>

		<up-popup :show="commentVisible" mode="bottom" round="16" @close="commentVisible = false">
			<view class="comment-panel">
				<text class="comment-panel-title">评论 ({{ commentTotal }})</text>
				<scroll-view scroll-y class="comment-scroll" @scrolltolower="loadMoreComments">
					<view v-for="c in commentList" :key="c.id" class="comment-item">
						<image class="comment-avatar" :src="c.displayAvatar" mode="aspectFill"></image>
						<view class="comment-bubble">
							<view class="comment-head">
								<text class="comment-user">{{ c.displayName }}<text v-if="c.replyName" class="comment-reply"> 回复 {{ c.replyName }}</text></text>
								<text v-if="c.isMine" class="comment-delete" @tap="removeComment(c)">删除</text>
							</view>
							<text class="comment-text">{{ c.content }}</text>
						</view>
					</view>
					<view v-if="commentLoading" class="pet-muted center-text">加载中...</view>
					<view v-if="!commentLoading && !commentList.length" class="pet-muted center-text">暂无评论，来抢沙发吧</view>
					<view v-if="commentHasMore && !commentLoading" class="load-more-text" @tap="loadMoreComments">加载更多</view>
				</scroll-view>
				<view v-if="currentPost && Number(currentPost.allowComment) !== 0" class="comment-input-row">
					<input v-model="commentText" class="comment-input" placeholder="说点什么..." confirm-type="send"
						@confirm="submitComment" />
					<view class="pet-button is-mini" @tap="submitComment">发送</view>
				</view>
			</view>
		</up-popup>

		<up-popup :show="notifyVisible" mode="bottom" round="16" @close="notifyVisible = false">
			<view class="comment-panel notify-panel">
				<view class="notify-header">
					<text class="comment-panel-title">互动消息</text>
					<text v-if="notifyList.some(item => item.isRead === 0)" class="notify-read-all" @tap="markAllNotificationsRead">全部已读</text>
				</view>
				<scroll-view scroll-y class="comment-scroll">
					<view v-for="item in notifyList" :key="item.id" class="notify-item" @tap="openNotifyItem(item)">
						<view class="notify-main">
							<text class="notify-title">{{ item.title || '消息通知' }}</text>
							<text class="notify-content">{{ item.content || '-' }}</text>
							<text class="notify-time">{{ item.createTime || '' }}</text>
						</view>
						<view v-if="item.isRead === 0" class="notify-dot"></view>
					</view>
					<view v-if="!notifyLoading && !notifyList.length" class="pet-muted center-text">暂无消息</view>
					<view v-if="notifyLoading" class="pet-muted center-text">加载中...</view>
				</scroll-view>
			</view>
		</up-popup>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import {
		fetchPostInfo,
		fetchPostPage,
		likePost,
		unlikePost,
		sharePost as sharePostApi,
		fetchCommentPage,
		saveComment,
		deleteComment
	} from '@/common/api/community.js';
	import {
		fetchMessagePage,
		markMessageRead,
		markAllMessagesRead,
		resolveMessagePostId
	} from '@/common/api/messages.js';
	import { syncMessageTabBadge } from '@/common/message-badge.js';
	import { ensureLogin } from '@/common/auth-guard.js';
	import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';

	const posts = ref([]);
	const loading = ref(false);
	const page = ref(1);
	const hasMore = ref(true);

	const commentVisible = ref(false);
	const commentList = ref([]);
	const commentTotal = ref(0);
	const commentText = ref('');
	const commentPage = ref(1);
	const commentHasMore = ref(false);
	const commentLoading = ref(false);
	const currentPost = ref(null);

	const notifyVisible = ref(false);
	const notifyList = ref([]);
	const notifyLoading = ref(false);
	const unreadCount = ref(0);
	const currentUserId = Number(uni.getStorageSync('userId') || 0);

	const VIDEO_EXT = ['.mp4', '.mov', '.m4v', '.webm'];

	const isVideo = url => VIDEO_EXT.some(ext => String(url).toLowerCase().includes(ext));

	const metaLine = (post) => {
		const parts = [];
		if (post.petName) parts.push(post.petName);
		const store = post.storeName || post.store;
		if (store) parts.push(store);
		return parts.join(' · ') || '宠物朋友圈';
	};

	const getCommentName = item => item.userName || item.nickname || item.name || (item.userId ? `用户${item.userId}` : '用户');
	const getReplyName = item => item.replyUserName || item.replyNickname || item.replyName || item.toUserName ||
		item.targetUserName || item.parentUserName || '';
	const getCommentAvatar = item => item.avatar || item.avatarUrl || item.userAvatar || item.userAvatarUrl ||
		item.userHead || item.userHeadImg || '/static/logo.png';

	const normalize = (list = []) => list.map(item => ({
		...item,
		liked: !!item.liked,
		mediaList: item.mediaUrls ? String(item.mediaUrls).split(',').filter(Boolean) : (item.mediaList || [])
	}));

	const normalizeComments = (list = []) => list.map(item => ({
		...item,
		displayName: getCommentName(item),
		replyName: getReplyName(item),
		displayAvatar: getCommentAvatar(item),
		isMine: currentUserId && Number(item.userId) === currentUserId
	}));

	const loadPosts = async (reset = false) => {
		if (loading.value) return;
		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}
		if (!hasMore.value) return;
		loading.value = true;
		try {
			const res = await fetchPostPage({ page: page.value, limit: 10 });
			const records = normalize(res?.records || res || []);
			posts.value = reset ? records : posts.value.concat(records);
			hasMore.value = records.length >= 10;
			if (hasMore.value) page.value += 1;
		} catch (e) {
			if (reset) posts.value = [];
		} finally {
			loading.value = false;
		}
	};

	const loadComments = async (postId, reset = false) => {
		if (reset) {
			commentPage.value = 1;
			commentHasMore.value = true;
		}
		if (!commentHasMore.value && !reset) return;
		commentLoading.value = true;
		try {
			const res = await fetchCommentPage(postId, commentPage.value, 20);
			const records = normalizeComments(res?.records || res || []);
			commentTotal.value = res?.total ?? records.length;
			commentList.value = reset ? records : commentList.value.concat(records);
			const pages = res?.pages || 0;
			commentHasMore.value = commentPage.value < pages;
			if (commentHasMore.value) commentPage.value += 1;
		} catch (e) {
			if (reset) {
				commentList.value = [];
				commentTotal.value = 0;
			}
		} finally {
			commentLoading.value = false;
		}
	};

	const loadMoreComments = () => {
		if (!currentPost.value?.id) return;
		loadComments(currentPost.value.id, false);
	};

	const refreshPostInList = async postId => {
		try {
			const data = await fetchPostInfo(postId, false);
			const idx = posts.value.findIndex(p => p.id === postId);
			if (idx >= 0 && data) {
				posts.value[idx] = normalize([data])[0];
			}
			if (currentPost.value?.id === postId) {
				currentPost.value = normalize([data])[0];
			}
		} catch (e) { /* ignore */ }
	};

	const loadUnreadCount = async () => {
		if (!uni.getStorageSync('token')) {
			unreadCount.value = 0;
			return;
		}
		try {
			unreadCount.value = await syncMessageTabBadge();
		} catch (e) {
			unreadCount.value = 0;
			uni.showToast({ title: e?.msg || e?.message || '未读数加载失败', icon: 'none' });
		}
	};

	const loadNotifications = async () => {
		notifyLoading.value = true;
		try {
			const res = await fetchMessagePage({ page: 1, limit: 50, type: 2 });
			notifyList.value = res?.records || [];
		} catch (e) {
			notifyList.value = [];
			uni.showToast({ title: e?.msg || e?.message || '消息加载失败', icon: 'none' });
		} finally {
			notifyLoading.value = false;
		}
	};

	const markAllNotificationsRead = async (showToast = true) => {
		try {
			await markAllMessagesRead({ type: 2 });
			notifyList.value = notifyList.value.map(item => ({ ...item, isRead: 1 }));
			unreadCount.value = await syncMessageTabBadge();
			if (showToast) {
				uni.showToast({ title: '已全部标记已读', icon: 'success' });
			}
		} catch (e) {
			uni.showToast({ title: e?.msg || e?.message || '操作失败', icon: 'none' });
		}
	};

	const openNotifications = async () => {
		if (!ensureLogin()) return;
		notifyVisible.value = true;
		await loadNotifications();
		if (notifyList.value.some(item => item.isRead === 0)) {
			await markAllNotificationsRead(false);
		}
	};

	const openNotifyItem = async item => {
		if (!item) return;
		const postId = resolveMessagePostId(item);
		if (item.isRead === 0 && item.id) {
			try {
				await markMessageRead(item.id);
				item.isRead = 1;
				unreadCount.value = await syncMessageTabBadge();
			} catch (e) {
				uni.showToast({ title: e?.msg || e?.message || '标记已读失败', icon: 'none' });
			}
		}
		notifyVisible.value = false;
		if (postId) {
			uni.navigateTo({ url: `/packageA/post/detail?id=${postId}` });
			return;
		}
		uni.showToast({ title: '暂无法跳转关联动态', icon: 'none' });
	};

	const goDetail = post => {
		if (!post?.id) return;
		uni.navigateTo({ url: `/packageA/post/detail?id=${post.id}` });
	};

	onShow(async () => {
		await loadPosts(true);
		await loadUnreadCount();
	});
	onPullDownRefresh(async () => {
		await loadPosts(true);
		await loadUnreadCount();
		uni.stopPullDownRefresh();
	});
	onReachBottom(() => loadPosts(false));

	const goCreate = () => {
		if (!ensureLogin('/packageA/post/create')) return;
		uni.navigateTo({ url: '/packageA/post/create' });
	};

	const toggleLike = async post => {
		if (!ensureLogin('/pages/msg/msg')) return;
		if (Number(post.allowLike) === 0) {
			uni.showToast({ title: '该动态不允许点赞', icon: 'none' });
			return;
		}
		try {
			if (post.liked) {
				await unlikePost(post.id);
			} else {
				await likePost(post.id);
			}
			await refreshPostInList(post.id);
		} catch (e) {
			await refreshPostInList(post.id);
			uni.showToast({ title: e?.msg || '操作失败', icon: 'none' });
		}
	};

	const sharePost = async post => {
		try {
			await sharePostApi(post.id);
		} catch (e) { /* ignore */ }
		const link = `/packageA/post/detail?id=${post.id}`;
		uni.setClipboardData({
			data: link,
			success: () => uni.showToast({ title: '链接已复制', icon: 'success' })
		});
	};

	const openComment = async post => {
		if (!ensureLogin()) return;
		if (Number(post.allowComment) === 0) {
			uni.showToast({ title: '该动态不允许评论', icon: 'none' });
			return;
		}
		currentPost.value = post;
		commentVisible.value = true;
		commentText.value = '';
		await loadComments(post.id, true);
	};

	const submitComment = async () => {
		if (!commentText.value.trim() || !currentPost.value) return;
		try {
			await saveComment(currentPost.value.id, commentText.value.trim());
			commentText.value = '';
			await refreshPostInList(currentPost.value.id);
			await loadComments(currentPost.value.id, true);
			uni.showToast({ title: '评论成功', icon: 'success' });
		} catch (e) {
			uni.showToast({ title: e?.msg || '评论失败', icon: 'none' });
		}
	};

	const removeComment = async c => {
		uni.showModal({
			title: '删除评论',
			content: '确认删除这条评论吗？',
			success: async res => {
				if (!res.confirm) return;
				try {
					await deleteComment(c.id);
					await refreshPostInList(currentPost.value.id);
					await loadComments(currentPost.value.id, true);
					uni.showToast({ title: '已删除', icon: 'success' });
				} catch (e) {
					uni.showToast({ title: e?.msg || '删除失败', icon: 'none' });
				}
			}
		});
	};
	const previewMedia = (post, index) => {
		const urls = (post.mediaList || []).filter(u => !isVideo(u));
		const current = post.mediaList[index];
		if (isVideo(current)) {
			uni.navigateTo({
				url: `/pages/webView/webView?url=${encodeURIComponent(current)}`
			});
			return;
		}
		if (urls.length) {
			uni.previewImage({ urls, current });
		}
	};
</script>

<style lang="scss">
	.post-card { margin-bottom: 24rpx; }
	.post-head { display: flex; gap: 18rpx; margin-bottom: 18rpx; }
	.post-user { flex: 1; }
	.post-name { display: block; font-size: 30rpx; font-weight: 700; }
	.post-content { font-size: 30rpx; line-height: 1.7; color: #5d4037; }
	.post-gallery { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12rpx; margin: 18rpx 0; }
	.gallery-item { height: 200rpx; border-radius: 20rpx; overflow: hidden; }
	.gallery-item image, .gallery-video { width: 100%; height: 100%; }
	.post-action-row { display: flex; gap: 28rpx; margin-top: 12rpx; color: #9a8d84; }
	.post-action { display: flex; align-items: center; gap: 8rpx; }
	.post-action.disabled { opacity: 0.45; }
	.post-action.share { margin-left: auto; }
	.center-text { text-align: center; padding: 40rpx 0; }
	.comment-panel { padding: 28rpx; max-height: 70vh; display: flex; flex-direction: column; }
	.comment-panel-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; }
	.comment-scroll { flex: 1; max-height: 45vh; }
	.comment-item { display: flex; align-items: flex-start; gap: 16rpx; padding: 10rpx 0; }
	.comment-avatar {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: #f1e8df;
		flex-shrink: 0;
		margin-top: 6rpx;
	}
	.comment-bubble {
		max-width: calc(100% - 72rpx);
		padding: 16rpx 22rpx;
		border-radius: 18rpx;
		background: #f3efeb;
	}
	.comment-user { display: block; font-size: 28rpx; font-weight: 700; color: #4e4037; margin-bottom: 8rpx; }
	.comment-head { display: flex; align-items: center; justify-content: space-between; gap: 12rpx; margin-bottom: 8rpx; }
	.comment-head .comment-user { margin-bottom: 0; }
	.comment-delete { font-size: 22rpx; color: #d17d77; flex-shrink: 0; }
	.comment-reply { font-weight: 500; color: #7f726a; }
	.comment-text { font-size: 28rpx; line-height: 1.6; color: #6a5d55; }
	.comment-input-row { display: flex; gap: 16rpx; align-items: center; margin-top: 20rpx; }
	.comment-input {
		flex: 1;
		height: 72rpx;
		background: #f9f1e7;
		border-radius: 36rpx;
		padding: 0 24rpx;
		font-size: 28rpx;
	}
	.pet-button.is-mini { padding: 0 28rpx; height: 72rpx; line-height: 72rpx; font-size: 28rpx; }
	.navbar-actions { display: flex; align-items: center; gap: 12rpx; }
	.navbar-action {
		width: 64rpx;
		height: 64rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
	}
	.notify-badge {
		position: absolute;
		top: 0;
		right: -4rpx;
		min-width: 28rpx;
		height: 28rpx;
		padding: 0 6rpx;
		border-radius: 999rpx;
		background: #f56c6c;
		color: #fff;
		font-size: 18rpx;
		line-height: 28rpx;
		text-align: center;
	}
	.load-more-text { text-align: center; padding: 16rpx 0; color: #c79662; font-size: 24rpx; }
	.notify-panel .comment-scroll { max-height: 55vh; }
	.notify-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 20rpx;
	}
	.notify-read-all {
		font-size: 24rpx;
		color: #c79662;
	}
	.notify-item {
		display: flex;
		align-items: flex-start;
		gap: 12rpx;
		padding: 18rpx 0;
		border-bottom: 1rpx solid #f0e8df;
	}
	.notify-main { flex: 1; }
	.notify-title { display: block; font-size: 28rpx; font-weight: 700; color: #4e4037; }
	.notify-content { display: block; margin-top: 8rpx; font-size: 26rpx; line-height: 1.5; color: #6a5d55; }
	.notify-time { display: block; margin-top: 8rpx; font-size: 22rpx; color: #a89b92; }
	.notify-dot {
		width: 14rpx;
		height: 14rpx;
		border-radius: 50%;
		background: #f56c6c;
		margin-top: 10rpx;
		flex-shrink: 0;
	}
</style>

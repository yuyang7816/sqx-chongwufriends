<template>
	<view class="pet-page">
		<view v-if="pageLoading" class="pet-muted center-text">加载中...</view>
		<view v-else-if="!post" class="pet-muted center-text">动态不存在或无权查看</view>
		<view v-else class="pet-shell">
			<view class="pet-card post-card">
				<view class="post-head">
					<view class="pet-avatar">
						<image :src="post.avatar || '/static/logo.png'" mode="aspectFill"></image>
					</view>
					<view class="post-user">
						<text class="post-name">{{ post.userName || post.name || '用户' }}</text>
						<text class="pet-muted">{{ post.timeText || post.createTime || '' }} · {{ post.storeName || '' }}</text>
					</view>
					<view class="post-more" @tap="goComplaint">
						<up-icon name="more-dot-fill" size="20" color="#b6ac9f"></up-icon>
					</view>
				</view>
				<text class="post-content">{{ post.content }}</text>
				<view v-if="post.mediaList?.length" class="post-gallery">
					<view
						v-for="(media, idx) in post.mediaList"
						:key="idx"
						class="pet-thumb gallery-item"
						@tap="previewMedia(idx)"
					>
						<video
							v-if="isVideo(media)"
							:src="media"
							class="gallery-video"
							:show-center-play-btn="true"
							:controls="false"
							object-fit="cover"
						></video>
						<image v-else :src="media" mode="aspectFill"></image>
					</view>
				</view>
				<view v-if="post.rejectReason" class="reject-box">
					<text class="reject-label">审核说明：</text>
					<text>{{ post.rejectReason }}</text>
				</view>
				<view class="post-action-row">
					<view
						class="post-action"
						:class="{ disabled: !canLike }"
						@tap="toggleLike"
					>
						<up-icon :name="post.liked ? 'heart-fill' : 'heart'" size="24"
							:color="post.liked ? '#f199a5' : '#b6ac9f'"></up-icon>
						<text>{{ post.likeCount ?? 0 }}</text>
					</view>
					<view
						class="post-action"
						:class="{ disabled: !canComment }"
						@tap="focusComment"
					>
						<up-icon name="chat" size="24" color="#b6ac9f"></up-icon>
						<text>{{ post.commentCount ?? 0 }}</text>
					</view>
					<view class="post-action share" @tap="doShare">
						<up-icon name="share" size="24" color="#b6ac9f"></up-icon>
					</view>
				</view>
			</view>

			<view class="pet-card comment-section">
				<text class="section-title">评论 ({{ commentTotal }})</text>
				<view v-for="c in commentList" :key="c.id" class="comment-item">
					<image class="comment-avatar" :src="c.displayAvatar" mode="aspectFill"></image>
					<view class="comment-bubble">
						<view class="comment-head">
							<text class="comment-user">{{ c.displayName }}</text>
							<text
								v-if="c.isMine"
								class="comment-delete"
								@tap="removeComment(c)"
							>删除</text>
						</view>
						<text class="comment-text">{{ c.content }}</text>
					</view>
				</view>
				<view v-if="commentLoading" class="pet-muted center-text">加载中...</view>
				<view v-if="!commentLoading && !commentList.length" class="pet-muted center-text">暂无评论</view>
				<view v-if="commentHasMore" class="load-more" @tap="loadMoreComments">加载更多评论</view>
			</view>

			<view v-if="canComment" class="comment-input-bar">
				<input
					v-model="commentText"
					class="comment-input"
					placeholder="说点什么..."
					confirm-type="send"
					@confirm="submitComment"
				/>
				<view class="pet-button is-mini" @tap="submitComment">发送</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import {
	fetchCommentPage,
	fetchPostInfo,
	deleteComment,
	likePost,
	unlikePost,
	sharePost as sharePostApi,
	saveComment
} from '@/common/api/community.js';
import { ensureLogin } from '@/common/auth-guard.js';
import { onLoad } from '@dcloudio/uni-app';

const postId = ref('');
const post = ref(null);
const pageLoading = ref(true);
const commentList = ref([]);
const commentTotal = ref(0);
const commentPage = ref(1);
const commentHasMore = ref(false);
const commentLoading = ref(false);
const commentText = ref('');

const VIDEO_EXT = ['.mp4', '.mov', '.m4v', '.webm'];
const currentUserId = computed(() => Number(uni.getStorageSync('userId') || 0));
const canLike = computed(() => Number(post.value?.allowLike) !== 0);
const canComment = computed(() => Number(post.value?.allowComment) !== 0);

const isVideo = url => VIDEO_EXT.some(ext => String(url).toLowerCase().includes(ext));

const normalizePost = data => ({
	...data,
	liked: !!data?.liked,
	mediaList: data?.mediaUrls ? String(data.mediaUrls).split(',').filter(Boolean) : (data?.mediaList || [])
});

const getCommentName = item => item.userName || item.nickname || item.name || (item.userId ? `用户${item.userId}` : '用户');
const getCommentAvatar = item => item.avatar || item.avatarUrl || item.userAvatar || '/static/logo.png';

const normalizeComments = (list = []) => list.map(item => ({
	...item,
	displayName: getCommentName(item),
	displayAvatar: getCommentAvatar(item),
	isMine: currentUserId.value && Number(item.userId) === currentUserId.value
}));

const loadPost = async () => {
	pageLoading.value = true;
	try {
		const data = await fetchPostInfo(postId.value, true);
		post.value = normalizePost(data);
	} catch (e) {
		post.value = null;
		uni.showToast({ title: e?.msg || '加载失败', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
};

const loadComments = async (reset = false) => {
	if (!postId.value) return;
	if (reset) {
		commentPage.value = 1;
		commentHasMore.value = true;
	}
	if (!commentHasMore.value && !reset) return;
	commentLoading.value = true;
	try {
		const res = await fetchCommentPage(postId.value, commentPage.value, 20);
		const records = normalizeComments(res?.records || []);
		commentTotal.value = res?.total ?? records.length;
		commentList.value = reset ? records : commentList.value.concat(records);
		const pages = res?.pages || 0;
		commentHasMore.value = commentPage.value < pages;
		if (commentHasMore.value) commentPage.value += 1;
	} finally {
		commentLoading.value = false;
	}
};

const refreshPostCounts = async () => {
	try {
		const data = await fetchPostInfo(postId.value, false);
		if (post.value && data) {
			post.value.likeCount = data.likeCount;
			post.value.commentCount = data.commentCount;
			post.value.liked = !!data.liked;
		}
	} catch (e) { /* ignore */ }
};

const toggleLike = async () => {
	if (!ensureLogin(detailLink())) return;
	if (!canLike.value) {
		uni.showToast({ title: '该动态不允许点赞', icon: 'none' });
		return;
	}
	try {
		if (post.value.liked) {
			await unlikePost(post.value.id);
		} else {
			await likePost(post.value.id);
		}
		await refreshPostCounts();
	} catch (e) {
		await refreshPostCounts();
		uni.showToast({ title: e?.msg || '操作失败', icon: 'none' });
	}
};

const focusComment = () => {
	if (!canComment.value) {
		uni.showToast({ title: '该动态不允许评论', icon: 'none' });
	}
};

const submitComment = async () => {
	if (!ensureLogin(detailLink())) return;
	if (!canComment.value) {
		uni.showToast({ title: '该动态不允许评论', icon: 'none' });
		return;
	}
	if (!commentText.value.trim()) return;
	try {
		await saveComment(post.value.id, commentText.value.trim());
		commentText.value = '';
		await refreshPostCounts();
		await loadComments(true);
		uni.showToast({ title: '评论成功', icon: 'success' });
	} catch (e) {
		uni.showToast({ title: e?.msg || '评论失败', icon: 'none' });
	}
};

const loadMoreComments = () => loadComments(false);

const removeComment = async c => {
	uni.showModal({
		title: '删除评论',
		content: '确认删除这条评论吗？',
		success: async res => {
			if (!res.confirm) return;
			try {
				await deleteComment(c.id);
				await refreshPostCounts();
				await loadComments(true);
				uni.showToast({ title: '已删除', icon: 'success' });
			} catch (e) {
				uni.showToast({ title: e?.msg || '删除失败', icon: 'none' });
			}
		}
	});
};

const detailLink = () => `/packageA/post/detail?id=${postId.value}`;

const doShare = async () => {
	try {
		await sharePostApi(post.value.id);
	} catch (e) { /* ignore */ }
	// #ifdef MP-WEIXIN
	uni.showShareMenu({ withShareTicket: true });
	// #endif
	const link = detailLink();
	uni.setClipboardData({
		data: link,
		success: () => uni.showToast({ title: '链接已复制', icon: 'success' })
	});
};

const goComplaint = () => {
	if (!post.value?.id) return;
	uni.navigateTo({
		url: `/packageA/post/complaint?targetType=1&targetId=${encodeURIComponent(post.value.id)}`
	});
};



const previewMedia = index => {
	const current = post.value.mediaList[index];
	if (isVideo(current)) {
		uni.navigateTo({ url: `/pages/webView/webView?url=${encodeURIComponent(current)}` });
		return;
	}
	const urls = post.value.mediaList.filter(u => !isVideo(u));
	if (urls.length) uni.previewImage({ urls, current });
};

onLoad(async option => {
	postId.value = String(option?.id || option?.postId || '');
	if (!postId.value) {
		pageLoading.value = false;
		return;
	}
	await loadPost();
	await loadComments(true);
});
</script>

<style lang="scss">
.post-card { margin-bottom: 24rpx; }
.post-head { display: flex; gap: 18rpx; margin-bottom: 18rpx; align-items: flex-start; }
.post-user { flex: 1; min-width: 0; }
.post-name { display: block; font-size: 30rpx; font-weight: 700; }
.post-more { padding: 8rpx; }
.post-content { font-size: 30rpx; line-height: 1.7; color: #5d4037; }
.post-gallery { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12rpx; margin: 18rpx 0; }
.gallery-item { height: 200rpx; border-radius: 20rpx; overflow: hidden; }
.gallery-item image, .gallery-video { width: 100%; height: 100%; }
.reject-box {
	margin-top: 12rpx;
	padding: 16rpx 20rpx;
	border-radius: 16rpx;
	background: #fff4ef;
	color: #c27a6f;
	font-size: 24rpx;
	line-height: 1.6;
}
.reject-label { font-weight: 700; }
.post-action-row { display: flex; gap: 28rpx; margin-top: 12rpx; color: #9a8d84; }
.post-action { display: flex; align-items: center; gap: 8rpx; }
.post-action.share { margin-left: auto; }
.post-action.disabled { opacity: 0.45; }
.section-title { display: block; font-size: 30rpx; font-weight: 700; margin-bottom: 16rpx; }
.comment-section { margin-bottom: 120rpx; }
.comment-item { display: flex; gap: 16rpx; padding: 12rpx 0; }
.comment-avatar { width: 56rpx; height: 56rpx; border-radius: 50%; flex-shrink: 0; }
.comment-bubble { flex: 1; min-width: 0; }
.comment-head { display: flex; align-items: center; justify-content: space-between; gap: 12rpx; margin-bottom: 8rpx; }
.comment-user { font-size: 28rpx; font-weight: 700; color: #4e4037; }
.comment-delete { font-size: 22rpx; color: #d17d77; }
.comment-text { font-size: 28rpx; line-height: 1.6; color: #6a5d55; }
.comment-input-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	display: flex;
	gap: 16rpx;
	align-items: center;
	padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
	background: #fffaf3;
	border-top: 2rpx solid #ecd7bf;
}
.comment-input {
	flex: 1;
	height: 72rpx;
	padding: 0 24rpx;
	border-radius: 999rpx;
	background: #f3efeb;
	font-size: 28rpx;
}
.load-more { text-align: center; padding: 20rpx 0; color: #c79662; font-size: 24rpx; }
.center-text { text-align: center; padding: 40rpx 0; }
</style>

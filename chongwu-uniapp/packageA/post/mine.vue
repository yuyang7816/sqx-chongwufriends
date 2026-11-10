<script setup>
import { computed, ref } from 'vue';
import {
	deletePost as deletePostApi,
	fetchCommentPage,
	fetchMyPostPage,
	fetchMyPostSummary
} from '@/common/api/community.js';
import { ensureLogin } from '@/common/auth-guard.js';
import { onLoad, onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';

const posts = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = 10;
const total = ref(0);
const hasMore = ref(true);
const activeTab = ref('all');

const summary = ref({
	total: 0,
	pendingCount: 0,
	approvedCount: 0,
	rejectedCount: 0
});

const tabList = computed(() => [
	{ key: 'all', label: '全部', count: summary.value.total },
	{ key: '0', label: '待审核', count: summary.value.pendingCount },
	{ key: '1', label: '已通过', count: summary.value.approvedCount },
	{ key: '2', label: '已拒绝', count: summary.value.rejectedCount }
]);

const commentVisible = ref(false);
const commentLoading = ref(false);
const commentList = ref([]);
const commentTotal = ref(0);
const commentPage = ref(1);
const commentHasMore = ref(false);
const currentPost = ref(null);

const VIDEO_EXT = ['.mp4', '.mov', '.m4v', '.webm'];
const myDisplayName = computed(() => uni.getStorageSync('phone') || '我');

const isVideo = url => VIDEO_EXT.some(ext => String(url).toLowerCase().includes(ext));

const normalize = (list = []) => list.map(item => ({
	...item,
	mediaList: item.mediaUrls ? String(item.mediaUrls).split(',').filter(Boolean) : (item.mediaList || [])
}));

const getCommentName = item => item.userName || item.nickname || item.name || (item.userId ? `用户${item.userId}` : '用户');
const getCommentAvatar = item => item.avatar || item.avatarUrl || item.userAvatar || '/static/logo.png';

const normalizeComments = (list = []) => list.map(item => ({
	...item,
	displayName: getCommentName(item),
	displayAvatar: getCommentAvatar(item)
}));

const statusText = post => {
	if (post.auditStatusText && post.statusText) {
		return `${post.auditStatusText} · ${post.statusText}`;
	}
	const auditMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' };
	const statusMap = { 1: '正常', 2: '已下架' };
	return `${auditMap[post.auditStatus] || '待审核'} · ${statusMap[post.status] || '正常'}`;
};

const metaText = post => {
	const parts = [post.timeText || '刚刚'];
	if (post.petName) parts.push(post.petName);
	if (post.storeName) parts.push(post.storeName);
	if (post.visibilityText) parts.push(post.visibilityText);
	return parts.join(' · ');
};

async function loadSummary() {
	try {
		const data = await fetchMyPostSummary();
		summary.value = {
			total: Number(data?.total || 0),
			pendingCount: Number(data?.pendingCount || 0),
			approvedCount: Number(data?.approvedCount || 0),
			rejectedCount: Number(data?.rejectedCount || 0)
		};
	} catch (e) {
		summary.value = { total: 0, pendingCount: 0, approvedCount: 0, rejectedCount: 0 };
	}
}

async function loadPosts(reset = false) {
	if (loading.value) return;
	if (reset) {
		page.value = 1;
		hasMore.value = true;
		total.value = 0;
	}
	if (!hasMore.value) return;

	loading.value = true;
	try {
		const params = { page: page.value, limit: pageSize };
		if (activeTab.value !== 'all') {
			params.auditStatus = Number(activeTab.value);
		}
		const res = await fetchMyPostPage(params);
		const records = normalize(res.records || []);
		total.value = res.total || 0;
		posts.value = reset ? records : posts.value.concat(records);
		hasMore.value = page.value < (res.pages || 0);
		if (hasMore.value) page.value += 1;
	} catch (e) {
		if (reset) {
			posts.value = [];
			total.value = 0;
		}
		hasMore.value = false;
	} finally {
		loading.value = false;
	}
}

async function reloadAll() {
	await loadSummary();
	await loadPosts(true);
}

function switchTab(key) {
	if (activeTab.value === key) return;
	activeTab.value = key;
	loadPosts(true);
}

const canEditPost = post => post?.canEdit || Number(post?.status) !== 2;

const buildEditUrl = postId => `/packageA/post/create?id=${encodeURIComponent(postId)}&mode=edit`;

const goCreate = () => {
	if (!ensureLogin('/packageA/post/create')) return;
	uni.navigateTo({ url: '/packageA/post/create' });
};

const editPost = post => {
	if (!post?.id) return;
	if (!ensureLogin(buildEditUrl(post.id))) return;
	if (!canEditPost(post)) {
		uni.showToast({ title: '已下架动态不可编辑', icon: 'none' });
		return;
	}
	uni.navigateTo({ url: buildEditUrl(post.id) });
};

const confirmDelete = post => {
	uni.showModal({
		title: '删除动态',
		content: '删除后不可恢复，确认删除这条动态吗？',
		success: async res => {
			if (!res.confirm) return;
			try {
				await deletePostApi(post.id);
				posts.value = posts.value.filter(item => item.id !== post.id);
				total.value = Math.max(0, total.value - 1);
				await loadSummary();
				uni.showToast({ title: '已删除', icon: 'success' });
			} catch (e) {
				uni.showToast({ title: e?.msg || '删除失败', icon: 'none' });
			}
		}
	});
};

const goDetail = post => {
	if (!post?.id) return;
	uni.navigateTo({ url: `/packageA/post/detail?id=${post.id}` });
};

const openComments = async post => {
	currentPost.value = post;
	commentVisible.value = true;
	commentPage.value = 1;
	commentHasMore.value = true;
	commentList.value = [];
	commentTotal.value = 0;
	await loadComments(true);
};

const loadComments = async (reset = false) => {
	if (!currentPost.value?.id) return;
	if (reset) {
		commentPage.value = 1;
		commentHasMore.value = true;
	}
	if (!commentHasMore.value && !reset) return;
	commentLoading.value = true;
	try {
		const res = await fetchCommentPage(currentPost.value.id, commentPage.value, 20);
		const records = normalizeComments(res.records || []);
		commentTotal.value = res.total || records.length;
		commentList.value = reset ? records : commentList.value.concat(records);
		const pages = res.pages || 0;
		commentHasMore.value = commentPage.value < pages;
		if (commentHasMore.value) commentPage.value += 1;
	} finally {
		commentLoading.value = false;
	}
};

const loadMoreComments = () => loadComments(false);

const showLikeInfo = post => {
	uni.showToast({ title: `共 ${post.likeCount ?? 0} 人点赞`, icon: 'none' });
};

const previewMedia = (post, index) => {
	const urls = (post.mediaList || []).filter(u => !isVideo(u));
	const current = post.mediaList[index];
	if (isVideo(current)) {
		uni.navigateTo({ url: `/pages/webView/webView?url=${encodeURIComponent(current)}` });
		return;
	}
	if (urls.length) {
		uni.previewImage({ urls, current });
	}
};

onLoad((option) => {
	if (option?.tab === 'pending') {
		activeTab.value = '0';
	}
});

onShow(() => {
	if (!ensureLogin('/packageA/post/mine')) return;
	reloadAll();
});

onPullDownRefresh(async () => {
	await reloadAll();
	uni.stopPullDownRefresh();
});

onReachBottom(() => loadPosts(false));
</script>

<template>
	<view class="pet-page">
		<up-navbar
			title="我发布的朋友圈"
			:placeholder="true"
			:border="false"
			bgColor="#fdf5ec"
			titleStyle="color:#C4956A;font-size:32rpx;font-weight:500"
			leftIcon="arrow-left"
			leftIconColor="#8b613d"
			rightIcon="plus"
			:autoBack="true"
			@rightClick="goCreate"
		></up-navbar>
		<view class="pet-shell">
			<scroll-view scroll-x class="tab-scroll" show-scrollbar="false">
				<view class="tab-scroll-inner">
					<view
						v-for="item in tabList"
						:key="item.key"
						class="mine-tab"
						:class="{ active: activeTab === item.key }"
						@tap="switchTab(item.key)"
					>
						<text>{{ item.label }}</text>
						<text class="mine-tab-count">{{ item.count }}</text>
					</view>
				</view>
			</scroll-view>

			<view v-for="post in posts" :key="post.id" class="pet-card post-card">
				<view class="post-head" @tap="goDetail(post)">
					<view class="pet-avatar">
						<image :src="post.avatar || '/static/logo.png'" mode="aspectFill"></image>
					</view>
					<view class="post-user">
						<text class="post-name">{{ post.userName || post.name || myDisplayName }}</text>
						<text class="pet-muted">{{ metaText(post) }}</text>
						<text class="post-status">{{ statusText(post) }}</text>
					</view>
					<view class="post-manage-row">
						<text
							v-if="canEditPost(post)"
							class="post-manage-btn"
							@tap.stop="editPost(post)"
						>修改</text>
						<text class="post-manage-btn danger" @tap.stop="confirmDelete(post)">删除</text>
					</view>
				</view>
				<view class="post-editable" @tap="goDetail(post)">
					<text class="post-content">{{ post.content }}</text>
					<text v-if="Number(post.auditStatus) === 2 && post.rejectReason" class="reject-reason">拒绝原因：{{ post.rejectReason }}</text>
				</view>
				<view v-if="post.mediaList?.length" class="post-gallery">
					<view
						v-for="(media, idx) in post.mediaList"
						:key="idx"
						class="pet-thumb gallery-item"
						@tap.stop="previewMedia(post, idx)"
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
				<view class="post-action-row">
					<view class="post-action readonly" @tap.stop="showLikeInfo(post)">
						<up-icon name="heart" size="24" color="#b6ac9f"></up-icon>
						<text>{{ post.likeCount ?? 0 }}</text>
					</view>
					<view class="post-action" @tap.stop="openComments(post)">
						<up-icon name="chat" size="24" color="#b6ac9f"></up-icon>
						<text>{{ post.commentCount ?? 0 }}</text>
					</view>
				</view>
			</view>

			<view v-if="loading" class="pet-muted center-text">加载中...</view>
			<view v-if="!loading && !posts.length" class="pet-muted center-text">你还没有发布过动态</view>
			<view v-if="!loading && posts.length && !hasMore" class="pet-muted center-text end-text">没有更多了</view>
		</view>

		<up-popup :show="commentVisible" mode="bottom" round="16" @close="commentVisible = false">
			<view class="comment-panel">
				<text class="comment-panel-title">评论 ({{ commentTotal }})</text>
				<scroll-view scroll-y class="comment-scroll">
					<view v-if="commentLoading" class="pet-muted center-text">加载中...</view>
					<view v-for="c in commentList" :key="c.id" class="comment-item">
						<image class="comment-avatar" :src="c.displayAvatar" mode="aspectFill"></image>
						<view class="comment-bubble">
							<text class="comment-user">{{ c.displayName }}</text>
							<text class="comment-text">{{ c.content }}</text>
						</view>
					</view>
					<view v-if="!commentLoading && !commentList.length" class="pet-muted center-text">暂无评论</view>
					<view v-if="commentHasMore && !commentLoading" class="load-more-text" @tap="loadMoreComments">加载更多</view>
				</scroll-view>
			</view>
		</up-popup>
	</view>
</template>

<style lang="scss">
.tab-scroll {
	white-space: nowrap;
	margin-bottom: 20rpx;
}

.tab-scroll-inner {
	display: inline-flex;
	gap: 16rpx;
	padding-right: 24rpx;
}

.mine-tab {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	padding: 14rpx 24rpx;
	border-radius: 999rpx;
	background: #f8efe4;
	color: #8e7863;
	font-size: 24rpx;
}

.mine-tab.active {
	background: #fff4de;
	color: #8b613d;
	font-weight: 700;
}

.mine-tab-count {
	font-size: 22rpx;
	color: #c79662;
}

.post-card { margin-bottom: 24rpx; }
.post-editable { cursor: pointer; }
.post-head { display: flex; gap: 18rpx; margin-bottom: 18rpx; align-items: flex-start; }
.post-user { flex: 1; min-width: 0; }
.post-name { display: block; font-size: 30rpx; font-weight: 700; }
.post-status { display: block; margin-top: 6rpx; font-size: 22rpx; color: #c79662; }
.post-content { font-size: 30rpx; line-height: 1.7; color: #5d4037; }
.reject-reason { display: block; margin-top: 12rpx; font-size: 24rpx; color: #c27a6f; line-height: 1.5; }
.load-more-text { text-align: center; padding: 16rpx 0; color: #c79662; font-size: 24rpx; }
.post-gallery { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12rpx; margin: 18rpx 0; }
.gallery-item { height: 200rpx; border-radius: 20rpx; overflow: hidden; }
.gallery-item image, .gallery-video { width: 100%; height: 100%; }
.post-action-row { display: flex; gap: 28rpx; margin-top: 12rpx; color: #9a8d84; }
.post-action { display: flex; align-items: center; gap: 8rpx; }
.post-action.readonly { opacity: 0.88; }
.post-manage-row { display: flex; gap: 18rpx; align-items: center; }
.post-manage-btn {
	padding: 10rpx 18rpx;
	border-radius: 999rpx;
	background: #f8efe4;
	font-size: 22rpx;
	font-weight: 600;
	color: #9f7855;
}
.post-manage-btn.danger {
	background: #fff0ef;
	color: #d17d77;
}
.center-text { text-align: center; padding: 40rpx 0; }
.end-text { padding-top: 0; font-size: 24rpx; }
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
}
.comment-bubble {
	max-width: calc(100% - 72rpx);
	padding: 16rpx 22rpx;
	border-radius: 18rpx;
	background: #f3efeb;
}
.comment-user { display: block; font-size: 28rpx; font-weight: 700; color: #4e4037; margin-bottom: 8rpx; }
.comment-text { font-size: 28rpx; line-height: 1.6; color: #6a5d55; }
</style>

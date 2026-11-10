package com.sqx.app.pet.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.sqx.app.pet.store.db.service.IPetStoreService;
import com.sqx.app.pet.store.domain.entity.PetStore;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.common.service.SensitiveWordService;
import com.sqx.app.pet.community.db.mapper.CommunityCommentMapper;
import com.sqx.app.pet.community.db.mapper.CommunityLikeMapper;
import com.sqx.app.pet.community.db.service.*;
import com.sqx.app.pet.community.domain.bo.*;
import com.sqx.app.pet.community.domain.entity.*;
import com.sqx.app.pet.community.domain.vo.CommunityCommentVo;
import com.sqx.app.pet.community.domain.vo.CommunityLikeVo;
import com.sqx.app.pet.community.domain.vo.CommunityPostVo;
import com.sqx.app.pet.community.domain.vo.CommunityConfigVo;
import com.sqx.app.pet.community.domain.vo.CommunityReportVo;
import com.sqx.app.pet.community.domain.vo.CommunityStatsVo;
import com.sqx.app.pet.community.domain.vo.MyPostSummaryVo;
import com.sqx.app.pet.community.service.CommunityInteractionService;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommunityInteractionServiceImpl implements CommunityInteractionService {

    @Resource
    private ICommunityPostService iCommunityPostService;
    @Resource
    private ICommunityCommentService iCommunityCommentService;
    @Resource
    private ICommunityLikeService iCommunityLikeService;
    @Resource
    private CommunityLikeMapper communityLikeMapper;
    @Resource
    private CommunityCommentMapper communityCommentMapper;
    @Resource
    private ICommunityViewLogService iCommunityViewLogService;
    @Resource
    private ICommunityReportService iCommunityReportService;
    @Resource
    private ICommunityComplaintTypeService iCommunityComplaintTypeService;
    @Resource
    private SensitiveWordService sensitiveWordService;
    @Resource
    private IPetStoreService iPetStoreService;
    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private IUserService iUserService;
    @Resource
    private INotificationService notificationService;
    @Resource
    private ISysConfigService iSysConfigService;
    @Resource
    private com.sqx.app.pet.island.service.IslandTaskService islandTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityPostVo publishPost(Long userId, PostPublishBo bo) {
        validatePublishContent(bo.getContent(), bo.getMediaUrls());
        sensitiveWordService.checkText(bo.getContent(), "post");
        if (StringUtils.isNotBlank(bo.getUserName())) {
            sensitiveWordService.checkText(bo.getUserName(), "nickname");
        }
        CommunityPost post = new CommunityPost();
        BeanUtils.copyProperties(bo, post);
        post.setUserId(userId);
        if (StringUtils.isBlank(post.getUserName()) && userId != null) {
            UserEntity user = iUserService.getById(userId);
            if (user != null) {
                post.setUserName(StringUtils.defaultIfBlank(user.getUserName(), user.getPhone()));
            }
        }
        post.setAuditStatus(isCommunityAuditEnabled() ? 0 : 1);
        post.setRejectReason(null);
        post.setStatus(1);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        iCommunityPostService.save(post);
        try {
            islandTaskService.autoCompleteByAction(userId, "post");
        } catch (Exception ignore) {
            // 任务自动完成失败不影响发帖主流程
        }
        return toPostVo(post, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityPostVo updatePost(Long userId, PostUpdateBo bo) {
        CommunityPost post = getPostOrThrow(bo.getId());
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new BizException(PetError.POST_NOT_FOUND);
        }
        validatePublishContent(bo.getContent(), bo.getMediaUrls());
        sensitiveWordService.checkText(bo.getContent(), "post");
        post.setStoreId(bo.getStoreId());
        post.setPetId(bo.getPetId());
        post.setContent(bo.getContent());
        post.setMediaUrls(bo.getMediaUrls());
        post.setMediaType(bo.getMediaType());
        post.setVisibility(bo.getVisibility());
        post.setAllowLike(bo.getAllowLike());
        post.setAllowComment(bo.getAllowComment());
        post.setAuditStatus(isCommunityAuditEnabled() ? 0 : 1);
        post.setRejectReason(null);
        iCommunityPostService.updateById(post);
        return toPostVo(post, false);
    }

    @Override
    public IPage<CommunityPostVo> pageFeed(Long userId, PostPageBo bo) {
        Page<CommunityPost> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CommunityPost> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(CommunityPost::getAuditStatus, 1)
                .eq(CommunityPost::getStatus, 1)
                .eq(bo.getStoreId() != null, CommunityPost::getStoreId, bo.getStoreId())
                .eq(bo.getPetId() != null, CommunityPost::getPetId, bo.getPetId())
                .eq(bo.getUserId() != null, CommunityPost::getUserId, bo.getUserId())
                .and(userId == null, wrapper -> wrapper.eq(CommunityPost::getVisibility, 1))
                .and(userId != null, wrapper -> wrapper.eq(CommunityPost::getVisibility, 1)
                        .or().eq(CommunityPost::getUserId, userId))
                .orderByDesc(CommunityPost::getCreateTime);
        return buildPostVoPage(userId, iCommunityPostService.page(page, qw));
    }

    @Override
    public IPage<CommunityPostVo> pageMyPosts(Long userId, PostPageBo bo) {
        Page<CommunityPost> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CommunityPost> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(CommunityPost::getUserId, userId)
                .eq(Objects.nonNull(bo.getAuditStatus()), CommunityPost::getAuditStatus, bo.getAuditStatus())
                .orderByDesc(CommunityPost::getCreateTime);
        return buildPostVoPage(userId, iCommunityPostService.page(page, qw));
    }

    @Override
    public MyPostSummaryVo myPostSummary(Long userId) {
        MyPostSummaryVo vo = new MyPostSummaryVo();
        long total = iCommunityPostService.lambdaQuery().eq(CommunityPost::getUserId, userId).count();
        long pending = iCommunityPostService.lambdaQuery()
                .eq(CommunityPost::getUserId, userId)
                .eq(CommunityPost::getAuditStatus, 0)
                .count();
        long approved = iCommunityPostService.lambdaQuery()
                .eq(CommunityPost::getUserId, userId)
                .eq(CommunityPost::getAuditStatus, 1)
                .count();
        long rejected = iCommunityPostService.lambdaQuery()
                .eq(CommunityPost::getUserId, userId)
                .eq(CommunityPost::getAuditStatus, 2)
                .count();
        vo.setTotal(total);
        vo.setPendingCount(pending);
        vo.setApprovedCount(approved);
        vo.setRejectedCount(rejected);
        return vo;
    }

    @Override
    public CommunityPostVo info(Long userId, PostInfoBo bo) {
        CommunityPost post = getPostOrThrow(bo.getId());
        ensureCanView(post, userId);
        boolean liked = userId != null && iCommunityLikeService.lambdaQuery()
                .eq(CommunityLike::getPostId, bo.getId())
                .eq(CommunityLike::getUserId, userId)
                .exists();
        if (shouldRecordView(bo) && (bo.getDeviceId() != null || userId != null)) {
            ViewBo viewBo = new ViewBo();
            viewBo.setPostId(bo.getId());
            viewBo.setDeviceId(bo.getDeviceId());
            recordView(userId, viewBo);
            post = iCommunityPostService.getById(bo.getId());
        }
        return toPostVo(post, liked,
                loadStoreNameMap(java.util.Collections.singletonList(post)),
                loadUserNameMap(java.util.Collections.singletonList(post)),
                loadUserAvatarMap(java.util.Collections.singletonList(post)),
                loadPetNameMap(java.util.Collections.singletonList(post)),
                loadCommentCountMap(java.util.Collections.singletonList(post)),
                loadLikeCountMap(java.util.Collections.singletonList(post)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordView(Long userId, ViewBo bo) {
        CommunityPost post = getPostOrThrow(bo.getPostId());
        ensureCanView(post, userId);
        QueryWrapper<CommunityViewLog> qw = new QueryWrapper<>();
        qw.lambda().eq(CommunityViewLog::getPostId, bo.getPostId());
        if (userId != null) {
            qw.lambda().eq(CommunityViewLog::getUserId, userId);
        } else if (StringUtils.isNotBlank(bo.getDeviceId())) {
            qw.lambda().eq(CommunityViewLog::getDeviceId, bo.getDeviceId());
        } else {
            return;
        }
        if (iCommunityViewLogService.count(qw) > 0) {
            return;
        }
        CommunityViewLog log = new CommunityViewLog();
        log.setPostId(bo.getPostId());
        log.setUserId(userId);
        log.setDeviceId(bo.getDeviceId());
        iCommunityViewLogService.save(log);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        iCommunityPostService.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long userId, LikeBo bo) {
        CommunityPost post = getPostOrThrow(bo.getPostId());
        ensureCanInteract(post, userId);
        if (post.getAllowLike() != null && post.getAllowLike() == 0) {
            throw new BizException(PetError.LIKE_NOT_ALLOWED);
        }
        CommunityLike existing = communityLikeMapper.selectAnyByPostAndUser(bo.getPostId(), userId);
        boolean likedNew = false;
        if (existing != null) {
            if (existing.getIsDeleted() == null || existing.getIsDeleted() == 0) {
                return;
            }
            communityLikeMapper.restoreById(existing.getId());
            likedNew = true;
        } else {
            CommunityLike like = new CommunityLike();
            like.setPostId(bo.getPostId());
            like.setUserId(userId);
            iCommunityLikeService.save(like);
            likedNew = true;
        }
        if (likedNew) {
            refreshLikeCount(bo.getPostId());
            notifyPostInteraction(post, userId, "赞了你的动态", null);
            try {
                islandTaskService.autoCompleteByAction(userId, "like");
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long userId, LikeBo bo) {
        CommunityPost post = getPostOrThrow(bo.getPostId());
        ensureCanInteract(post, userId);
        int removed = communityLikeMapper.physicalDeleteByPostAndUser(bo.getPostId(), userId);
        if (removed > 0) {
            refreshLikeCount(bo.getPostId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityCommentVo saveComment(Long userId, CommentSaveBo bo) {
        CommunityPost post = getPostOrThrow(bo.getPostId());
        ensureCanInteract(post, userId);
        if (post.getAllowComment() != null && post.getAllowComment() == 0) {
            throw new BizException(PetError.COMMENT_NOT_ALLOWED);
        }
        sensitiveWordService.checkText(bo.getContent(), "comment");
        String content = StringUtils.trimToEmpty(bo.getContent());
        CommunityComment duplicate = iCommunityCommentService.lambdaQuery()
                .eq(CommunityComment::getPostId, bo.getPostId())
                .eq(CommunityComment::getUserId, userId)
                .eq(CommunityComment::getContent, content)
                .orderByDesc(CommunityComment::getCreateTime)
                .last("limit 1")
                .one();
        if (duplicate != null) {
            CommunityCommentVo vo = new CommunityCommentVo();
            BeanUtils.copyProperties(duplicate, vo);
            UserEntity user = userId != null ? iUserService.getById(userId) : null;
            vo.setUserName(resolveUserDisplayName(user));
            vo.setAvatar(user != null ? user.getAvatar() : null);
            return vo;
        }
        CommunityComment comment = new CommunityComment();
        BeanUtils.copyProperties(bo, comment);
        comment.setContent(content);
        comment.setUserId(userId);
        iCommunityCommentService.save(comment);
        refreshCommentCount(bo.getPostId());
        CommunityCommentVo vo = new CommunityCommentVo();
        BeanUtils.copyProperties(comment, vo);
        UserEntity user = userId != null ? iUserService.getById(userId) : null;
        vo.setUserName(StringUtils.isNotBlank(bo.getUserName()) ? bo.getUserName() : resolveUserDisplayName(user));
        vo.setAvatar(user != null ? user.getAvatar() : null);
        notifyPostInteraction(post, userId, "评论了你的动态", bo.getContent());
        try {
            islandTaskService.autoCompleteByAction(userId, "comment");
        } catch (Exception ignore) {
        }
        return vo;
    }

    @Override
    public IPage<CommunityCommentVo> pageComment(CommentPageBo bo) {
        Page<CommunityComment> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CommunityComment> qw = new QueryWrapper<>();
        qw.lambda().eq(CommunityComment::getPostId, bo.getPostId()).orderByDesc(CommunityComment::getCreateTime);
        IPage<CommunityComment> iPage = iCommunityCommentService.page(page, qw);
        List<CommunityCommentVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            List<Long> userIds = iPage.getRecords().stream()
                    .map(CommunityComment::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, UserEntity> userMap = loadUserMapByUserIds(userIds);
            for (CommunityComment record : iPage.getRecords()) {
                CommunityCommentVo vo = new CommunityCommentVo();
                BeanUtils.copyProperties(record, vo);
                UserEntity user = record.getUserId() != null ? userMap.get(record.getUserId()) : null;
                vo.setUserName(resolveUserDisplayName(user));
                vo.setAvatar(user != null ? user.getAvatar() : null);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public IPage<CommunityLikeVo> pageLike(CommentPageBo bo) {
        Page<CommunityLike> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CommunityLike> qw = new QueryWrapper<>();
        qw.lambda().eq(CommunityLike::getPostId, bo.getPostId()).orderByDesc(CommunityLike::getCreateTime);
        IPage<CommunityLike> iPage = iCommunityLikeService.page(page, qw);
        List<CommunityLikeVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            List<Long> userIds = iPage.getRecords().stream()
                    .map(CommunityLike::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, UserEntity> userMap = loadUserMapByUserIds(userIds);
            for (CommunityLike record : iPage.getRecords()) {
                CommunityLikeVo vo = new CommunityLikeVo();
                BeanUtils.copyProperties(record, vo);
                UserEntity user = record.getUserId() != null ? userMap.get(record.getUserId()) : null;
                vo.setUserName(resolveUserDisplayName(user));
                vo.setAvatar(user != null ? user.getAvatar() : null);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, CommentDeleteBo bo) {
        CommunityComment comment = iCommunityCommentService.getById(bo.getId());
        if (comment == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        iCommunityCommentService.removeById(bo.getId());
        refreshCommentCount(comment.getPostId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteComment(AdminCommentDeleteBo bo) {
        CommunityComment comment = iCommunityCommentService.getById(bo.getId());
        if (comment == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        iCommunityCommentService.removeById(bo.getId());
        refreshCommentCount(comment.getPostId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void share(Long userId, ShareBo bo) {
        CommunityPost post = getPostOrThrow(bo.getPostId());
        ensureCanInteract(post, userId);
        post.setShareCount((post.getShareCount() == null ? 0 : post.getShareCount()) + 1);
        iCommunityPostService.updateById(post);
        try {
            islandTaskService.autoCompleteByAction(userId, "share");
        } catch (Exception ignore) {
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void report(Long userId, ReportBo bo) {
        ComplaintBo complaintBo = new ComplaintBo();
        complaintBo.setTargetType(bo.getTargetType());
        complaintBo.setTargetId(bo.getTargetId());
        complaintBo.setReason(StringUtils.defaultIfBlank(bo.getReason(), "用户举报"));
        complaint(userId, complaintBo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complaint(Long userId, ComplaintBo bo) {
        String reason = resolveComplaintReason(bo);
        if (StringUtils.isBlank(reason)) {
            throw new BizException(PetError.COMPLAINT_REASON_REQUIRED);
        }
        long exists = iCommunityReportService.lambdaQuery()
                .eq(CommunityReport::getUserId, userId)
                .eq(CommunityReport::getTargetType, bo.getTargetType())
                .eq(CommunityReport::getTargetId, bo.getTargetId())
                .eq(CommunityReport::getStatus, 1)
                .count();
        if (exists > 0) {
            throw new BizException(PetError.COMPLAINT_DUPLICATE);
        }
        CommunityReport report = new CommunityReport();
        report.setTargetType(bo.getTargetType());
        report.setTargetId(bo.getTargetId());
        report.setReason(reason);
        report.setUserId(userId);
        report.setStatus(1);
        iCommunityReportService.save(report);
    }

    @Override
    public IPage<CommunityReportVo> pageMyComplaints(Long userId, CommunityReportPageBo bo) {
        if (userId == null) {
            throw new BizException(PetError.POST_NOT_FOUND);
        }
        bo.setUserId(userId);
        return buildComplaintVoPage(bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long userId, PostDeleteBo bo) {
        CommunityPost post = getPostOrThrow(bo.getId());
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new BizException(PetError.POST_NOT_FOUND);
        }
        cascadeDeletePost(bo.getId());
    }

    @Override
    public CommunityConfigVo communityConfig() {
        CommunityConfigVo vo = new CommunityConfigVo();
        vo.setAuditSwitch(readAuditSwitch());
        vo.setComplaintTypes(readComplaintTypes());
        return vo;
    }

    @Override
    public void saveCommunityConfig(CommunityConfigUpdateBo bo) {
        if (bo == null) {
            return;
        }
        if (StringUtils.isNotBlank(bo.getAuditSwitch())) {
            upsertSysConfig("community_audit_switch", bo.getAuditSwitch().trim(), "社区发布审核开关1开0关");
        }
        if (bo.getComplaintTypes() != null) {
            List<String> types = normalizeComplaintTypes(bo.getComplaintTypes());
            if (CollectionUtils.isEmpty(types)) {
                throw new BizException(PetError.COMPLAINT_TYPE_INVALID);
            }
            syncComplaintTypes(types);
            upsertSysConfig("community_complaint_types", String.join(",", types), "朋友圈投诉类型，逗号分隔");
        }
    }

    @Override
    public IPage<CommunityPostVo> adminPage(CommunityPostPageBo bo) {
        Page<CommunityPost> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CommunityPost> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(Objects.nonNull(bo.getAuditStatus()), CommunityPost::getAuditStatus, bo.getAuditStatus())
                .eq(Objects.nonNull(bo.getStatus()), CommunityPost::getStatus, bo.getStatus())
                .eq(Objects.nonNull(bo.getUserId()), CommunityPost::getUserId, bo.getUserId())
                .eq(Objects.nonNull(bo.getStoreId()), CommunityPost::getStoreId, bo.getStoreId())
                .like(StringUtils.isNotBlank(bo.getUserName()), CommunityPost::getUserName, bo.getUserName())
                .like(StringUtils.isNotBlank(bo.getKeyword()), CommunityPost::getContent, bo.getKeyword())
                .ge(bo.getStartTime() != null, CommunityPost::getCreateTime, bo.getStartTime())
                .le(bo.getEndTime() != null, CommunityPost::getCreateTime, bo.getEndTime())
                .orderByDesc(CommunityPost::getCreateTime);
        return buildPostVoPage(null, iCommunityPostService.page(page, qw));
    }

    @Override
    public CommunityPostVo adminInfo(CommunityPostInfoBo bo) {
        CommunityPost post = getPostOrThrow(bo.getId());
        return toPostVo(post, false);
    }

    @Override
    public CommunityStatsVo adminStats() {
        CommunityStatsVo vo = new CommunityStatsVo();
        vo.setPendingCount(iCommunityPostService.lambdaQuery().eq(CommunityPost::getAuditStatus, 0).count());
        vo.setApprovedCount(iCommunityPostService.lambdaQuery()
                .eq(CommunityPost::getAuditStatus, 1)
                .eq(CommunityPost::getStatus, 1)
                .count());
        vo.setRejectedCount(iCommunityPostService.lambdaQuery().eq(CommunityPost::getAuditStatus, 2).count());
        vo.setOfflineCount(iCommunityPostService.lambdaQuery()
                .eq(CommunityPost::getAuditStatus, 1)
                .eq(CommunityPost::getStatus, 2)
                .count());
        vo.setTotalCount(iCommunityPostService.count());
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        vo.setTodayCount(iCommunityPostService.lambdaQuery()
                .ge(CommunityPost::getCreateTime, todayStart)
                .count());
        vo.setAuditSwitch(readAuditSwitch());
        return vo;
    }

    @Override
    public void audit(CommunityAuditBo bo) {
        CommunityPost post = getPostOrThrow(bo.getId());
        post.setAuditStatus(bo.getAuditStatus());
        if (Objects.equals(bo.getAuditStatus(), 1)) {
            post.setStatus(1);
            post.setRejectReason(null);
            notifyPostAuthor(post, "动态审核通过", "你发布的动态已通过审核，现已公开展示。");
        } else if (Objects.equals(bo.getAuditStatus(), 2)) {
            post.setRejectReason(StringUtils.trimToNull(bo.getRejectReason()));
            String reason = StringUtils.defaultIfBlank(bo.getRejectReason(), "未填写原因");
            notifyPostAuthor(post, "动态审核未通过", "你的动态未通过审核，原因：" + reason);
        }
        iCommunityPostService.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAudit(CommunityBatchAuditBo bo) {
        if (CollectionUtils.isEmpty(bo.getIds())) {
            return;
        }
        for (Long id : bo.getIds()) {
            CommunityAuditBo single = new CommunityAuditBo();
            single.setId(id);
            single.setAuditStatus(bo.getAuditStatus());
            single.setRejectReason(bo.getRejectReason());
            audit(single);
        }
    }

    @Override
    public void offline(CommunityOfflineBo bo) {
        CommunityPost post = getPostOrThrow(bo.getId());
        post.setStatus(2);
        iCommunityPostService.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(CommunityPostInfoBo bo) {
        getPostOrThrow(bo.getId());
        cascadeDeletePost(bo.getId());
    }

    @Override
    public IPage<CommunityReportVo> adminReportPage(CommunityReportPageBo bo) {
        return adminComplaintPage(bo);
    }

    @Override
    public IPage<CommunityReportVo> adminComplaintPage(CommunityReportPageBo bo) {
        return buildComplaintVoPage(bo);
    }

    @Override
    public void adminReportHandle(CommunityReportHandleBo bo) {
        adminComplaintHandle(bo);
    }

    @Override
    public void adminComplaintHandle(CommunityReportHandleBo bo) {
        CommunityReport report = iCommunityReportService.getById(bo.getId());
        if (report == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        report.setStatus(bo.getStatus() != null ? bo.getStatus() : 2);
        iCommunityReportService.updateById(report);
    }

    private IPage<CommunityReportVo> buildComplaintVoPage(CommunityReportPageBo bo) {
        Page<CommunityReport> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CommunityReport> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(bo.getTargetType() != null, CommunityReport::getTargetType, bo.getTargetType())
                .eq(bo.getStatus() != null, CommunityReport::getStatus, bo.getStatus())
                .eq(bo.getUserId() != null, CommunityReport::getUserId, bo.getUserId())
                .eq(bo.getTargetId() != null, CommunityReport::getTargetId, bo.getTargetId())
                .like(StringUtils.isNotBlank(bo.getKeyword()), CommunityReport::getReason, bo.getKeyword())
                .orderByDesc(CommunityReport::getCreateTime);
        IPage<CommunityReport> iPage = iCommunityReportService.page(page, qw);
        List<CommunityReportVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            List<Long> userIds = iPage.getRecords().stream()
                    .map(CommunityReport::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> userNameMap = loadUserNameMapByUserIds(userIds);
            Map<String, String> targetSummaryMap = loadComplaintTargetSummaryMap(iPage.getRecords());
            for (CommunityReport record : iPage.getRecords()) {
                CommunityReportVo vo = new CommunityReportVo();
                BeanUtils.copyProperties(record, vo);
                vo.setUserName(userNameMap.get(record.getUserId()));
                vo.setTargetTypeText(resolveComplaintTargetTypeText(record.getTargetType()));
                vo.setStatusText(resolveComplaintStatusText(record.getStatus()));
                vo.setTargetSummary(targetSummaryMap.get(buildComplaintTargetKey(record.getTargetType(), record.getTargetId())));
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    private String buildComplaintTargetKey(Integer targetType, Long targetId) {
        return String.valueOf(targetType) + ":" + String.valueOf(targetId);
    }

    private String resolveComplaintTargetTypeText(Integer targetType) {
        if (Objects.equals(targetType, 1)) {
            return "动态";
        }
        if (Objects.equals(targetType, 2)) {
            return "评论";
        }
        return "其他";
    }

    private String resolveComplaintStatusText(Integer status) {
        if (Objects.equals(status, 1)) {
            return "待处理";
        }
        if (Objects.equals(status, 2)) {
            return "已处理";
        }
        return "未知";
    }

    private Map<String, String> loadComplaintTargetSummaryMap(List<CommunityReport> records) {
        Map<String, String> summaryMap = new HashMap<>();
        if (CollectionUtils.isEmpty(records)) {
            return summaryMap;
        }
        List<Long> postIds = records.stream()
                .filter(item -> Objects.equals(item.getTargetType(), 1) && item.getTargetId() != null)
                .map(CommunityReport::getTargetId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(postIds)) {
            for (CommunityPost post : iCommunityPostService.listByIds(postIds)) {
                summaryMap.put(buildComplaintTargetKey(1, post.getId()), abbreviateText(post.getContent(), 80));
            }
        }
        List<Long> commentIds = records.stream()
                .filter(item -> Objects.equals(item.getTargetType(), 2) && item.getTargetId() != null)
                .map(CommunityReport::getTargetId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(commentIds)) {
            for (CommunityComment comment : iCommunityCommentService.listByIds(commentIds)) {
                summaryMap.put(buildComplaintTargetKey(2, comment.getId()), abbreviateText(comment.getContent(), 80));
            }
        }
        return summaryMap;
    }

    private String abbreviateText(String text, int maxLen) {
        if (StringUtils.isBlank(text)) {
            return "-";
        }
        String value = text.trim();
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...";
    }

    private void cascadeDeletePost(Long postId) {
        if (postId == null) {
            return;
        }
        communityLikeMapper.physicalDeleteByPostId(postId);
        iCommunityCommentService.lambdaUpdate().eq(CommunityComment::getPostId, postId).remove();
        iCommunityViewLogService.lambdaUpdate().eq(CommunityViewLog::getPostId, postId).remove();
        iCommunityReportService.lambdaUpdate()
                .eq(CommunityReport::getTargetType, 1)
                .eq(CommunityReport::getTargetId, postId)
                .remove();
        iCommunityPostService.removeById(postId);
    }

    private IPage<CommunityPostVo> buildPostVoPage(Long userId, IPage<CommunityPost> iPage) {
        Set<Long> likedPostIds = loadLikedPostIds(userId, iPage.getRecords());
        Map<Long, Integer> commentCountMap = loadCommentCountMap(iPage.getRecords());
        Map<Long, Integer> likeCountMap = loadLikeCountMap(iPage.getRecords());
        List<CommunityPostVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            Map<Long, String> storeNameMap = loadStoreNameMap(iPage.getRecords());
            Map<Long, String> userNameMap = loadUserNameMap(iPage.getRecords());
            Map<Long, String> userAvatarMap = loadUserAvatarMap(iPage.getRecords());
            Map<Long, String> petNameMap = loadPetNameMap(iPage.getRecords());
            for (CommunityPost record : iPage.getRecords()) {
                CommunityPostVo vo = toPostVo(record, likedPostIds.contains(record.getId()), storeNameMap, userNameMap, userAvatarMap, petNameMap, commentCountMap, likeCountMap);
                vo.setViewCount(null);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    private Map<Long, Integer> loadLikeCountMap(List<CommunityPost> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return java.util.Collections.emptyMap();
        }
        List<Long> postIds = posts.stream()
                .map(CommunityPost::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(postIds)) {
            return java.util.Collections.emptyMap();
        }
        Map<Long, Integer> map = new HashMap<>();
        List<Map<String, Object>> rows = communityLikeMapper.countGroupByPostIds(postIds);
        for (Map<String, Object> row : rows) {
            Object postIdObj = row.get("postId");
            Object cntObj = row.get("cnt");
            if (postIdObj != null && cntObj != null) {
                map.put(Long.valueOf(String.valueOf(postIdObj)), Integer.parseInt(String.valueOf(cntObj)));
            }
        }
        return map;
    }

    private void syncLikeCountIfNeeded(CommunityPost post, int actualCount) {
        if (post == null || post.getId() == null) {
            return;
        }
        Integer stored = post.getLikeCount();
        if (stored == null || stored != actualCount) {
            post.setLikeCount(actualCount);
            iCommunityPostService.updateById(post);
        }
    }

    private Map<Long, Integer> loadCommentCountMap(List<CommunityPost> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return java.util.Collections.emptyMap();
        }
        List<Long> postIds = posts.stream()
                .map(CommunityPost::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(postIds)) {
            return java.util.Collections.emptyMap();
        }
        Map<Long, Integer> map = new HashMap<>();
        List<Map<String, Object>> rows = communityCommentMapper.countGroupByPostIds(postIds);
        for (Map<String, Object> row : rows) {
            Object postIdObj = row.get("postId");
            Object cntObj = row.get("cnt");
            if (postIdObj != null && cntObj != null) {
                map.put(Long.valueOf(String.valueOf(postIdObj)), Integer.parseInt(String.valueOf(cntObj)));
            }
        }
        return map;
    }

    private void syncCommentCountIfNeeded(CommunityPost post, int actualCount) {
        if (post == null || post.getId() == null) {
            return;
        }
        Integer stored = post.getCommentCount();
        if (stored == null || stored != actualCount) {
            post.setCommentCount(actualCount);
            iCommunityPostService.updateById(post);
        }
    }

    private CommunityPost getPostOrThrow(Long id) {
        CommunityPost post = iCommunityPostService.getById(id);
        if (post == null) {
            throw new BizException(PetError.POST_NOT_FOUND);
        }
        return post;
    }

    private Set<Long> loadLikedPostIds(Long userId, List<CommunityPost> posts) {
        if (userId == null || CollectionUtils.isEmpty(posts)) {
            return java.util.Collections.emptySet();
        }
        List<Long> postIds = posts.stream().map(CommunityPost::getId).collect(Collectors.toList());
        return iCommunityLikeService.lambdaQuery()
                .eq(CommunityLike::getUserId, userId)
                .in(CommunityLike::getPostId, postIds)
                .list()
                .stream()
                .map(CommunityLike::getPostId)
                .collect(Collectors.toSet());
    }

    private Map<Long, String> loadStoreNameMap(List<CommunityPost> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return java.util.Collections.emptyMap();
        }
        List<Long> storeIds = posts.stream()
                .map(CommunityPost::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(storeIds)) {
            return java.util.Collections.emptyMap();
        }
        Map<Long, String> map = new HashMap<>();
        for (PetStore store : iPetStoreService.listByIds(storeIds)) {
            map.put(store.getId(), store.getName());
        }
        return map;
    }

    private Map<Long, String> loadUserNameMap(List<CommunityPost> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return java.util.Collections.emptyMap();
        }
        List<Long> userIds = posts.stream()
                .map(CommunityPost::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return loadUserNameMapByUserIds(userIds);
    }

    private Map<Long, UserEntity> loadUserMapByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return java.util.Collections.emptyMap();
        }
        Map<Long, UserEntity> map = new HashMap<>();
        for (UserEntity user : iUserService.listByIds(userIds)) {
            map.put(user.getUserId(), user);
        }
        return map;
    }

    private Map<Long, String> loadUserNameMapByUserIds(List<Long> userIds) {
        Map<Long, UserEntity> userMap = loadUserMapByUserIds(userIds);
        Map<Long, String> map = new HashMap<>();
        userMap.forEach((id, user) -> map.put(id, resolveUserDisplayName(user)));
        return map;
    }

    private Map<Long, String> loadUserAvatarMap(List<CommunityPost> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return java.util.Collections.emptyMap();
        }
        List<Long> userIds = posts.stream()
                .map(CommunityPost::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserEntity> userMap = loadUserMapByUserIds(userIds);
        Map<Long, String> map = new HashMap<>();
        userMap.forEach((id, user) -> map.put(id, user.getAvatar()));
        return map;
    }

    private Map<Long, String> loadPetNameMap(List<CommunityPost> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return java.util.Collections.emptyMap();
        }
        List<Long> petIds = posts.stream()
                .map(CommunityPost::getPetId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(petIds)) {
            return java.util.Collections.emptyMap();
        }
        Map<Long, String> map = new HashMap<>();
        for (PetArchive pet : iPetArchiveService.listByIds(petIds)) {
            map.put(pet.getId(), pet.getName());
        }
        return map;
    }

    private String resolveUserDisplayName(UserEntity user) {
        if (user == null) {
            return null;
        }
        return StringUtils.defaultIfBlank(user.getUserName(), user.getPhone());
    }

    private void notifyPostInteraction(CommunityPost post, Long actorUserId, String action, String detail) {
        if (post == null || post.getUserId() == null || Objects.equals(post.getUserId(), actorUserId)) {
            return;
        }
        UserEntity actor = actorUserId != null ? iUserService.getById(actorUserId) : null;
        String actorName = StringUtils.defaultIfBlank(resolveUserDisplayName(actor), "用户");
        String content = actorName + " " + action;
        if (StringUtils.isNotBlank(detail)) {
            content += "：" + abbreviateText(detail, 50);
        }
        NotificationRequest request = new NotificationRequest();
        request.setUserId(post.getUserId());
        request.setTitle("朋友圈互动");
        request.setContent(content);
        request.setType(2);
        request.setDataId(post.getId());
        request.setUrl(buildPostDetailUrl(post.getId()));
        notificationService.send(request, NotificationChannel.INSTATION.code());
        UserEntity owner = iUserService.getById(post.getUserId());
        if (owner != null && StringUtils.isNotBlank(owner.getCid())) {
            request.setCid(owner.getCid());
            request.setMsgNo("community_" + post.getId() + "_" + System.currentTimeMillis());
            notificationService.send(request, NotificationChannel.APP.code());
        }
    }

    private void notifyPostAuthor(CommunityPost post, String title, String content) {
        if (post == null || post.getUserId() == null) {
            return;
        }
        NotificationRequest request = new NotificationRequest();
        request.setUserId(post.getUserId());
        request.setTitle(title);
        request.setContent(content);
        request.setType(2);
        request.setDataId(post.getId());
        request.setUrl(buildPostDetailUrl(post.getId()));
        notificationService.send(request, NotificationChannel.INSTATION.code());
        UserEntity owner = iUserService.getById(post.getUserId());
        if (owner != null && StringUtils.isNotBlank(owner.getCid())) {
            request.setCid(owner.getCid());
            request.setMsgNo("community_audit_" + post.getId());
            notificationService.send(request, NotificationChannel.APP.code());
        }
    }

    private String buildPostDetailUrl(Long postId) {
        return "/packageA/post/detail?id=" + postId;
    }

    private boolean isCommunityAuditEnabled() {
        return Objects.equals("1", readAuditSwitch());
    }

    private String readAuditSwitch() {
        String value = ConfigUtil.getConfigValueOrDefault("community_audit_switch");
        return StringUtils.defaultIfBlank(value, "1");
    }

    private static final String DEFAULT_COMPLAINT_TYPES = "垃圾广告,违法违规,人身攻击,不实信息,其他";

    private List<String> readComplaintTypes() {
        List<CommunityComplaintType> list = iCommunityComplaintTypeService.lambdaQuery()
                .eq(CommunityComplaintType::getStatus, 1)
                .orderByAsc(CommunityComplaintType::getSort)
                .orderByAsc(CommunityComplaintType::getId)
                .list();
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream()
                    .map(CommunityComplaintType::getName)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }
        String value = ConfigUtil.getConfigValueOrDefault("community_complaint_types");
        String raw = StringUtils.defaultIfBlank(value, DEFAULT_COMPLAINT_TYPES);
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private void syncComplaintTypes(List<String> types) {
        List<CommunityComplaintType> existing = iCommunityComplaintTypeService.list();
        Map<String, CommunityComplaintType> existingMap = existing.stream()
                .collect(Collectors.toMap(CommunityComplaintType::getName, item -> item, (a, b) -> a));
        int sort = 0;
        for (String type : types) {
            CommunityComplaintType entity = existingMap.get(type);
            if (entity == null) {
                entity = new CommunityComplaintType();
                entity.setName(type);
                entity.setStatus(1);
            }
            entity.setSort(sort++);
            entity.setStatus(1);
            if (entity.getId() == null) {
                iCommunityComplaintTypeService.save(entity);
            } else {
                iCommunityComplaintTypeService.updateById(entity);
            }
            existingMap.remove(type);
        }
        if (!existingMap.isEmpty()) {
            iCommunityComplaintTypeService.removeByIds(
                    existingMap.values().stream().map(CommunityComplaintType::getId).collect(Collectors.toList()));
        }
    }

    private List<String> normalizeComplaintTypes(List<String> types) {
        if (CollectionUtils.isEmpty(types)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String type : types) {
            String text = StringUtils.trimToNull(type);
            if (text == null) {
                continue;
            }
            if (text.length() > 20) {
                throw new BizException(PetError.COMPLAINT_TYPE_INVALID);
            }
            if (!result.contains(text)) {
                result.add(text);
            }
            if (result.size() > 20) {
                throw new BizException(PetError.COMPLAINT_TYPE_INVALID);
            }
        }
        return result;
    }

    private String resolveComplaintReason(ComplaintBo bo) {
        if (StringUtils.isNotBlank(bo.getComplaintType())) {
            String type = bo.getComplaintType().trim();
            if (!readComplaintTypes().contains(type)) {
                throw new BizException(PetError.COMPLAINT_TYPE_INVALID);
            }
            String content = StringUtils.trimToNull(bo.getContent());
            if (content == null) {
                return type;
            }
            return type + "：" + content;
        }
        return StringUtils.trimToNull(bo.getReason());
    }

    private void upsertSysConfig(String paramKey, String paramValue, String remark) {
        SysConfigEntity entity = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, paramKey));
        if (entity == null) {
            entity = new SysConfigEntity();
            entity.setParamKey(paramKey);
            entity.setStatus("1");
            entity.setParamType("community");
            entity.setParamTypeDesc("朋友圈");
            entity.setRemark(remark);
            entity.setParamValue(paramValue);
            iSysConfigService.save(entity);
        } else {
            entity.setParamValue(paramValue);
            if (StringUtils.isNotBlank(remark)) {
                entity.setRemark(remark);
            }
            iSysConfigService.updateById(entity);
        }
        iSysConfigService.loadSysConfig();
    }

    private boolean isPostOwner(CommunityPost post, Long userId) {
        return post != null && userId != null && Objects.equals(post.getUserId(), userId);
    }

    private boolean canViewPost(CommunityPost post, Long userId) {
        if (post == null) {
            return false;
        }
        if (isPostOwner(post, userId)) {
            return true;
        }
        if (!Objects.equals(post.getAuditStatus(), 1) || !Objects.equals(post.getStatus(), 1)) {
            return false;
        }
        return !Objects.equals(post.getVisibility(), 2);
    }

    private void validatePublishContent(String content, String mediaUrls) {
        if (StringUtils.length(content) > 500) {
            throw new BizException("内容不能超过500字");
        }
        if (StringUtils.isBlank(mediaUrls)) {
            return;
        }
        long mediaCount = Arrays.stream(mediaUrls.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .count();
        if (mediaCount > 9) {
            throw new BizException("媒体不能超过9个");
        }
    }

    private void ensureCanView(CommunityPost post, Long userId) {
        if (!canViewPost(post, userId)) {
            throw new BizException(PetError.POST_NOT_FOUND);
        }
    }

    private void ensureCanInteract(CommunityPost post, Long userId) {
        if (userId == null) {
            throw new BizException(PetError.POST_NOT_FOUND);
        }
        ensureCanView(post, userId);
    }

    private void refreshLikeCount(Long postId) {
        if (postId == null) {
            return;
        }
        int count = iCommunityLikeService.lambdaQuery().eq(CommunityLike::getPostId, postId).count().intValue();
        CommunityPost update = new CommunityPost();
        update.setId(postId);
        update.setLikeCount(count);
        iCommunityPostService.updateById(update);
    }

    private void refreshCommentCount(Long postId) {
        if (postId == null) {
            return;
        }
        int count = iCommunityCommentService.lambdaQuery().eq(CommunityComment::getPostId, postId).count().intValue();
        CommunityPost update = new CommunityPost();
        update.setId(postId);
        update.setCommentCount(count);
        iCommunityPostService.updateById(update);
    }

    private String formatTimeText(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + "分钟前";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "小时前";
        }
        long days = duration.toDays();
        if (days < 7) {
            return days + "天前";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private CommunityPostVo toPostVo(CommunityPost post, boolean liked, Map<Long, String> storeNameMap,
                                       Map<Long, String> userNameMap, Map<Long, String> userAvatarMap,
                                       Map<Long, String> petNameMap, Map<Long, Integer> commentCountMap,
                                       Map<Long, Integer> likeCountMap) {
        CommunityPostVo vo = new CommunityPostVo();
        BeanUtils.copyProperties(post, vo);
        vo.setLiked(liked);
        vo.setTimeText(formatTimeText(post.getCreateTime()));
        if (commentCountMap != null && post.getId() != null && commentCountMap.containsKey(post.getId())) {
            int actualCommentCount = commentCountMap.get(post.getId());
            vo.setCommentCount(actualCommentCount);
            syncCommentCountIfNeeded(post, actualCommentCount);
        }
        if (likeCountMap != null && post.getId() != null && likeCountMap.containsKey(post.getId())) {
            int actualLikeCount = likeCountMap.get(post.getId());
            vo.setLikeCount(actualLikeCount);
            syncLikeCountIfNeeded(post, actualLikeCount);
        }
        if (post.getUserId() != null && userNameMap != null && userNameMap.containsKey(post.getUserId())) {
            vo.setUserName(userNameMap.get(post.getUserId()));
        }
        if (post.getUserId() != null) {
            vo.setAvatar(userAvatarMap.get(post.getUserId()));
        }
        if (post.getStoreId() != null) {
            vo.setStoreName(storeNameMap.get(post.getStoreId()));
        }
        if (post.getPetId() != null) {
            vo.setPetName(petNameMap.get(post.getPetId()));
        }
        fillPostDisplayFields(vo, post);
        return vo;
    }

    private boolean shouldRecordView(PostInfoBo bo) {
        return bo.getRecordView() == null || Boolean.TRUE.equals(bo.getRecordView());
    }

    private void fillPostDisplayFields(CommunityPostVo vo, CommunityPost post) {
        vo.setAuditStatusText(resolveAuditStatusText(post.getAuditStatus()));
        vo.setStatusText(resolvePostStatusText(post.getStatus()));
        vo.setVisibilityText(resolveVisibilityText(post.getVisibility()));
        vo.setCanEdit(Objects.equals(post.getStatus(), 1));
    }

    private String resolveAuditStatusText(Integer auditStatus) {
        if (Objects.equals(auditStatus, 1)) {
            return "已通过";
        }
        if (Objects.equals(auditStatus, 2)) {
            return "已拒绝";
        }
        return "待审核";
    }

    private String resolvePostStatusText(Integer status) {
        return Objects.equals(status, 1) ? "正常" : "已下架";
    }

    private String resolveVisibilityText(Integer visibility) {
        return Objects.equals(visibility, 2) ? "仅自己" : "公开";
    }

    private CommunityPostVo toPostVo(CommunityPost post, boolean liked) {
        return toPostVo(post, liked,
                loadStoreNameMap(java.util.Collections.singletonList(post)),
                loadUserNameMap(java.util.Collections.singletonList(post)),
                loadUserAvatarMap(java.util.Collections.singletonList(post)),
                loadPetNameMap(java.util.Collections.singletonList(post)),
                loadCommentCountMap(java.util.Collections.singletonList(post)),
                loadLikeCountMap(java.util.Collections.singletonList(post)));
    }
}

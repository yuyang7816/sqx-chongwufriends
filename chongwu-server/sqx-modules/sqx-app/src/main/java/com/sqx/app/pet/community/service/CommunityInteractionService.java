package com.sqx.app.pet.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.community.domain.bo.*;
import com.sqx.app.pet.community.domain.vo.CommunityConfigVo;
import com.sqx.app.pet.community.domain.vo.CommunityCommentVo;
import com.sqx.app.pet.community.domain.vo.CommunityLikeVo;
import com.sqx.app.pet.community.domain.vo.CommunityPostVo;
import com.sqx.app.pet.community.domain.vo.CommunityReportVo;
import com.sqx.app.pet.community.domain.vo.CommunityStatsVo;
import com.sqx.app.pet.community.domain.vo.MyPostSummaryVo;

public interface CommunityInteractionService {

    CommunityPostVo publishPost(Long userId, PostPublishBo bo);

    CommunityPostVo updatePost(Long userId, PostUpdateBo bo);

    IPage<CommunityPostVo> pageFeed(Long userId, PostPageBo bo);

    IPage<CommunityPostVo> pageMyPosts(Long userId, PostPageBo bo);

    MyPostSummaryVo myPostSummary(Long userId);

    CommunityPostVo info(Long userId, PostInfoBo bo);

    void recordView(Long userId, ViewBo bo);

    void like(Long userId, LikeBo bo);

    void unlike(Long userId, LikeBo bo);

    CommunityCommentVo saveComment(Long userId, CommentSaveBo bo);

    IPage<CommunityCommentVo> pageComment(CommentPageBo bo);

    IPage<CommunityLikeVo> pageLike(CommentPageBo bo);

    void deleteComment(Long userId, CommentDeleteBo bo);

    void adminDeleteComment(AdminCommentDeleteBo bo);

    void share(Long userId, ShareBo bo);

    void report(Long userId, ReportBo bo);

    void complaint(Long userId, ComplaintBo bo);

    IPage<CommunityReportVo> pageMyComplaints(Long userId, CommunityReportPageBo bo);

    void deletePost(Long userId, PostDeleteBo bo);

    CommunityConfigVo communityConfig();

    void saveCommunityConfig(CommunityConfigUpdateBo bo);

    IPage<CommunityPostVo> adminPage(CommunityPostPageBo bo);

    CommunityPostVo adminInfo(CommunityPostInfoBo bo);

    CommunityStatsVo adminStats();

    void audit(CommunityAuditBo bo);

    void batchAudit(CommunityBatchAuditBo bo);

    void offline(CommunityOfflineBo bo);

    void adminDelete(CommunityPostInfoBo bo);

    IPage<CommunityReportVo> adminReportPage(CommunityReportPageBo bo);

    IPage<CommunityReportVo> adminComplaintPage(CommunityReportPageBo bo);

    void adminReportHandle(CommunityReportHandleBo bo);

    void adminComplaintHandle(CommunityReportHandleBo bo);
}

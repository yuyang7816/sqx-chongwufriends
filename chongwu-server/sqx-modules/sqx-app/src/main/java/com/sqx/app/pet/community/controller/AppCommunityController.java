package com.sqx.app.pet.community.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.community.domain.bo.*;
import com.sqx.app.pet.community.domain.vo.CommunityConfigVo;
import com.sqx.app.pet.community.domain.vo.CommunityCommentVo;
import com.sqx.app.pet.community.domain.vo.CommunityPostVo;
import com.sqx.app.pet.community.domain.vo.CommunityReportVo;
import com.sqx.app.pet.community.domain.vo.MyPostSummaryVo;
import com.sqx.app.pet.community.service.CommunityInteractionService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "App-Community", description = "社区动态互动")
@RestController
@RequestMapping("/app/pet/community")
public class AppCommunityController {

    @Resource
    private CommunityInteractionService communityInteractionService;

    @Login
    @Idempotent
    @Operation(summary = "更新动态")
    @PostMapping("update")
    public Result<CommunityPostVo> update(@RequestBody @Validated PostUpdateBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(communityInteractionService.updatePost(userId, bo));
    }

    @Login
    @Operation(summary = "我的动态统计")
    @PostMapping("my/summary")
    public Result<MyPostSummaryVo> mySummary() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(communityInteractionService.myPostSummary(userId));
    }

    @Login
    @Operation(summary = "我的动态分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityPostVo.class)))
    @PostMapping("my/page")
    public Result<IPage<CommunityPostVo>> myPage(@RequestBody @Validated PostPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(communityInteractionService.pageMyPosts(userId, bo));
    }

    @Login
    @Operation(summary = "社区配置")
    @PostMapping("config")
    public Result<CommunityConfigVo> config() {
        return Result.success(communityInteractionService.communityConfig());
    }

    @Operation(summary = "动态分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityPostVo.class)))
    @PostMapping("page")
    public Result<IPage<CommunityPostVo>> page(@RequestBody @Validated PostPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(communityInteractionService.pageFeed(userId, bo));
    }

    @Operation(summary = "动态详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityPostVo.class)))
    @PostMapping("info")
    public Result<CommunityPostVo> info(@RequestBody @Validated PostInfoBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(communityInteractionService.info(userId, bo));
    }

    @Login
    @Idempotent
    @Operation(summary = "发布动态")
    @PostMapping("publish")
    public Result<CommunityPostVo> publish(@RequestBody @Validated PostPublishBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(communityInteractionService.publishPost(userId, bo));
    }

    @Login
    @Idempotent
    @Operation(summary = "删除动态")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated PostDeleteBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        communityInteractionService.deletePost(userId, bo);
        return Result.success();
    }

    @Login
    @Idempotent
    @Operation(summary = "点赞")
    @PostMapping("like")
    public Result<Void> like(@RequestBody @Validated LikeBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        communityInteractionService.like(userId, bo);
        return Result.success();
    }

    @Login
    @Idempotent
    @Operation(summary = "取消点赞")
    @PostMapping("unlike")
    public Result<Void> unlike(@RequestBody @Validated LikeBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        communityInteractionService.unlike(userId, bo);
        return Result.success();
    }

    @Operation(summary = "评论分页")
    @PostMapping("comment/page")
    public Result<IPage<CommunityCommentVo>> commentPage(@RequestBody @Validated CommentPageBo bo) {
        return Result.success(communityInteractionService.pageComment(bo));
    }

    @Login
    @Idempotent
    @Operation(summary = "发表评论")
    @PostMapping("comment/save")
    public Result<CommunityCommentVo> commentSave(@RequestBody @Validated CommentSaveBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(communityInteractionService.saveComment(userId, bo));
    }

    @Login
    @Idempotent
    @Operation(summary = "删除评论")
    @PostMapping("comment/delete")
    public Result<Void> commentDelete(@RequestBody @Validated CommentDeleteBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        communityInteractionService.deleteComment(userId, bo);
        return Result.success();
    }

    @Login
    @Idempotent
    @Operation(summary = "分享")
    @PostMapping("share")
    public Result<Void> share(@RequestBody @Validated ShareBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        communityInteractionService.share(userId, bo);
        return Result.success();
    }

    @Login
    @Idempotent
    @Operation(summary = "举报（兼容）")
    @PostMapping("report")
    public Result<Void> report(@RequestBody @Validated ReportBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        communityInteractionService.report(userId, bo);
        return Result.success();
    }

    @Login
    @Idempotent
    @Operation(summary = "提交投诉")
    @PostMapping("complaint")
    public Result<Void> complaint(@RequestBody @Validated ComplaintBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        communityInteractionService.complaint(userId, bo);
        return Result.success();
    }

    @Login
    @Operation(summary = "我的投诉分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityReportVo.class)))
    @PostMapping("complaint/page")
    public Result<IPage<CommunityReportVo>> complaintPage(@RequestBody @Validated CommunityReportPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(communityInteractionService.pageMyComplaints(userId, bo));
    }
}

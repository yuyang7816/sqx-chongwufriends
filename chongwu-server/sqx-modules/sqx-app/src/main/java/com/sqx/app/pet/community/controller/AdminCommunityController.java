package com.sqx.app.pet.community.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.community.domain.bo.*;
import com.sqx.app.pet.community.domain.vo.CommunityCommentVo;
import com.sqx.app.pet.community.domain.vo.CommunityConfigVo;
import com.sqx.app.pet.community.domain.vo.CommunityLikeVo;
import com.sqx.app.pet.community.domain.vo.CommunityPostVo;
import com.sqx.app.pet.community.domain.vo.CommunityReportVo;
import com.sqx.app.pet.community.domain.vo.CommunityStatsVo;
import com.sqx.app.pet.community.service.CommunityInteractionService;
import com.sqx.common.core.domain.Result;
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

@Tag(name = "Admin-Community", description = "社区动态管理")
@RestController
@RequestMapping("/admin/pet/community")
public class AdminCommunityController {

    @Resource
    private CommunityInteractionService communityInteractionService;

    @Operation(summary = "分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityPostVo.class)))
    @PostMapping("page")
    public Result<IPage<CommunityPostVo>> page(@RequestBody @Validated CommunityPostPageBo bo) {
        return Result.success(communityInteractionService.adminPage(bo));
    }

    @Operation(summary = "详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityPostVo.class)))
    @PostMapping("info")
    public Result<CommunityPostVo> info(@RequestBody @Validated CommunityPostInfoBo bo) {
        return Result.success(communityInteractionService.adminInfo(bo));
    }

    @Operation(summary = "统计")
    @PostMapping("stats")
    public Result<CommunityStatsVo> stats() {
        return Result.success(communityInteractionService.adminStats());
    }

    @Operation(summary = "社区配置")
    @PostMapping("config")
    public Result<CommunityConfigVo> config() {
        return Result.success(communityInteractionService.communityConfig());
    }

    @Operation(summary = "保存社区配置")
    @PostMapping("config/save")
    public Result<Void> saveConfig(@RequestBody @Validated CommunityConfigUpdateBo bo) {
        communityInteractionService.saveCommunityConfig(bo);
        return Result.success();
    }

    @Operation(summary = "审核")
    @PostMapping("audit")
    public Result<Void> audit(@RequestBody @Validated CommunityAuditBo bo) {
        communityInteractionService.audit(bo);
        return Result.success();
    }

    @Operation(summary = "批量审核")
    @PostMapping("audit/batch")
    public Result<Void> batchAudit(@RequestBody @Validated CommunityBatchAuditBo bo) {
        communityInteractionService.batchAudit(bo);
        return Result.success();
    }

    @Operation(summary = "下架")
    @PostMapping("offline")
    public Result<Void> offline(@RequestBody @Validated CommunityOfflineBo bo) {
        communityInteractionService.offline(bo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated CommunityPostInfoBo bo) {
        communityInteractionService.adminDelete(bo);
        return Result.success();
    }

    @Operation(summary = "评论分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityCommentVo.class)))
    @PostMapping("comment/page")
    public Result<IPage<CommunityCommentVo>> commentPage(@RequestBody @Validated CommentPageBo bo) {
        return Result.success(communityInteractionService.pageComment(bo));
    }

    @Operation(summary = "点赞分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityLikeVo.class)))
    @PostMapping("like/page")
    public Result<IPage<CommunityLikeVo>> likePage(@RequestBody @Validated CommentPageBo bo) {
        return Result.success(communityInteractionService.pageLike(bo));
    }

    @Operation(summary = "删除评论")
    @PostMapping("comment/delete")
    public Result<Void> commentDelete(@RequestBody @Validated AdminCommentDeleteBo bo) {
        communityInteractionService.adminDeleteComment(bo);
        return Result.success();
    }

    @Operation(summary = "举报分页（兼容）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityReportVo.class)))
    @PostMapping("report/page")
    public Result<IPage<CommunityReportVo>> reportPage(@RequestBody @Validated CommunityReportPageBo bo) {
        return Result.success(communityInteractionService.adminComplaintPage(bo));
    }

    @Operation(summary = "处理举报（兼容）")
    @PostMapping("report/handle")
    public Result<Void> reportHandle(@RequestBody @Validated CommunityReportHandleBo bo) {
        communityInteractionService.adminComplaintHandle(bo);
        return Result.success();
    }

    @Operation(summary = "投诉分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommunityReportVo.class)))
    @PostMapping("complaint/page")
    public Result<IPage<CommunityReportVo>> complaintPage(@RequestBody @Validated CommunityReportPageBo bo) {
        return Result.success(communityInteractionService.adminComplaintPage(bo));
    }

    @Operation(summary = "处理投诉")
    @PostMapping("complaint/handle")
    public Result<Void> complaintHandle(@RequestBody @Validated CommunityReportHandleBo bo) {
        communityInteractionService.adminComplaintHandle(bo);
        return Result.success();
    }
}

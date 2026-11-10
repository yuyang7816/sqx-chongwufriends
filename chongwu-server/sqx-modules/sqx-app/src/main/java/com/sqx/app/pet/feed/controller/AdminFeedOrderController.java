package com.sqx.app.pet.feed.controller;

import com.sqx.app.pet.feed.domain.bo.*;
import com.sqx.app.pet.feed.domain.vo.FeedOrderVo;
import com.sqx.app.pet.feed.service.FeedOrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

@Tag(name = "Admin-Feed Order", description = "Admin-Feed Order")
@RestController
@RequestMapping("/admin/pet/feed")
public class AdminFeedOrderController {

    @Resource
    private FeedOrderService feedOrder;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated FeedOrderSaveBo bo) {
        feedOrder.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = FeedOrderVo.class)))
    @PostMapping("page")
    public Result<IPage<FeedOrderVo>> page(@RequestBody @Validated FeedOrderPageBo bo) {
        return Result.success(feedOrder.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = FeedOrderVo.class)))
    @PostMapping("info")
    public Result<FeedOrderVo> info(@RequestBody @Validated FeedOrderInfoBo bo) {
        return Result.success(feedOrder.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated FeedOrderUpdateBo bo) {
        feedOrder.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated FeedOrderInfoBo bo) {
        feedOrder.delete(bo);
        return Result.success();
    }

    @Operation(summary = "上传反馈视频")
    @PostMapping("feedback/upload")
    public Result<Void> feedbackUpload(@RequestBody @Validated FeedFeedbackUploadBo bo) {
        feedOrder.adminUploadFeedback(bo);
        return Result.success();
    }
}

package com.sqx.app.pet.feed.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.feed.domain.bo.*;
import com.sqx.app.pet.feed.domain.vo.FeedGiftSummaryVo;
import com.sqx.app.pet.feed.domain.vo.FeedOrderVo;
import com.sqx.app.pet.feed.service.FeedOrderService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "App-Feed Order", description = "投喂订单")
@RestController
@RequestMapping("/app/pet/feed")
public class AppFeedOrderController {

    @Resource
    private FeedOrderService feedOrderService;

    @Login
    @Idempotent
    @Operation(summary = "创建投喂订单")
    @PostMapping("create")
    public Result<FeedOrderVo> create(@RequestBody @Validated FeedOrderCreateBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(feedOrderService.createForUser(userId, bo));
    }

    @Login
    @Operation(summary = "赠送记录汇总")
    @PostMapping("summary")
    public Result<FeedGiftSummaryVo> summary() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(feedOrderService.giftSummary(userId));
    }

    @Login
    @Operation(summary = "我的礼物/投喂记录")
    @PostMapping("page")
    public Result<IPage<FeedOrderVo>> page(@RequestBody @Validated FeedOrderPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(feedOrderService.listMyGifts(userId, bo));
    }

    @Login
    @Operation(summary = "订单详情")
    @PostMapping("info")
    public Result<FeedOrderVo> info(@RequestBody @Validated FeedOrderInfoBo bo) {
        return Result.success(feedOrderService.info(bo));
    }

    @Login
    @Idempotent
    @Operation(summary = "上传反馈视频")
    @PostMapping("feedback/upload")
    public Result<Void> feedbackUpload(@RequestBody @Validated FeedFeedbackUploadBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        feedOrderService.uploadFeedback(userId, bo);
        return Result.success();
    }
}

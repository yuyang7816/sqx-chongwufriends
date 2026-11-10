package com.sqx.app.pet.marketing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.marketing.domain.bo.AssistPageBo;
import com.sqx.app.pet.marketing.domain.vo.AssistSummaryVo;
import com.sqx.app.pet.marketing.domain.vo.MarketingAssistVo;
import com.sqx.app.pet.marketing.service.MarketingRankService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "App-Assist", description = "助力记录")
@RestController
@RequestMapping("/app/pet/assist")
public class AppPetAssistController {

    @Resource
    private MarketingRankService marketingRankService;

    @Login
    @Operation(summary = "我的助力列表")
    @PostMapping("list")
    public Result<List<MarketingAssistVo>> list() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(marketingRankService.myAssistRecords(userId));
    }

    @Login
    @Operation(summary = "我的助力分页")
    @PostMapping("page")
    public Result<IPage<MarketingAssistVo>> page(@RequestBody AssistPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(marketingRankService.myAssistPage(userId, bo));
    }

    @Login
    @Operation(summary = "我的助力汇总")
    @PostMapping("summary")
    public Result<AssistSummaryVo> summary() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(marketingRankService.assistSummary(userId));
    }
}

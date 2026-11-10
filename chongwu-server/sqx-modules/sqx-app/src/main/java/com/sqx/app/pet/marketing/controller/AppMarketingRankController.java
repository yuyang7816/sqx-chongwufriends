package com.sqx.app.pet.marketing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.marketing.domain.bo.RankingAssistBo;
import com.sqx.app.pet.marketing.domain.bo.RankingPageBo;
import com.sqx.app.pet.marketing.domain.vo.MarketingAssistVo;
import com.sqx.app.pet.marketing.domain.vo.MarketingRankVo;
import com.sqx.app.pet.marketing.domain.vo.RankingAssistQuotaVo;
import com.sqx.app.pet.marketing.domain.vo.RankingAssistSettingsVo;
import com.sqx.app.pet.marketing.domain.vo.RankingPeriodOverviewVo;
import com.sqx.app.pet.marketing.service.MarketingRankService;
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
import java.util.List;

@Tag(name = "App-Ranking", description = "打榜")
@RestController
@RequestMapping("/app/pet/ranking")
public class AppMarketingRankController {

    @Resource
    private MarketingRankService marketingRankService;

    @Operation(summary = "当前榜单")
    @PostMapping("page")
    public Result<IPage<MarketingRankVo>> page(@RequestBody @Validated RankingPageBo bo) {
        return Result.success(marketingRankService.currentRanking(bo));
    }

    @Operation(summary = "周期概览(本周/上周+规则)")
    @PostMapping("periods")
    public Result<RankingPeriodOverviewVo> periods() {
        return Result.success(marketingRankService.periodOverview());
    }

    @Operation(summary = "打榜规则")
    @PostMapping("rules")
    public Result<String> rules() {
        return Result.success(marketingRankService.getRankingRules());
    }

    @Operation(summary = "打榜次数与付费配置")
    @PostMapping("assist/settings")
    public Result<RankingAssistSettingsVo> assistSettings() {
        return Result.success(marketingRankService.getAssistSettings());
    }

    @Login
    @Operation(summary = "我的打榜额度")
    @PostMapping("assist/quota")
    public Result<RankingAssistQuotaVo> assistQuota() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(marketingRankService.assistQuota(userId));
    }

    @Login
    @Idempotent
    @Operation(summary = "助力")
    @PostMapping("assist")
    public Result<Void> assist(@RequestBody @Validated RankingAssistBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        marketingRankService.assist(userId, bo);
        return Result.success();
    }

    @Login
    @Operation(summary = "我的助力记录")
    @PostMapping("assist/my")
    public Result<List<MarketingAssistVo>> myAssists() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(marketingRankService.myAssistRecords(userId));
    }

    @Login
    @Operation(summary = "我的助力记录(兼容路径)")
    @PostMapping("myAssists")
    public Result<List<MarketingAssistVo>> myAssistsAlias() {
        return myAssists();
    }
}

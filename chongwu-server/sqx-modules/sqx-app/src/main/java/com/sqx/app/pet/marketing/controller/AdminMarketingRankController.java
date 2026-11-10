package com.sqx.app.pet.marketing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.marketing.domain.bo.RankingAssistSettingsBo;
import com.sqx.app.pet.marketing.domain.bo.RankingPageBo;
import com.sqx.app.pet.marketing.domain.bo.RankingRulesUpdateBo;
import com.sqx.app.pet.marketing.domain.vo.MarketingRankVo;
import com.sqx.app.pet.marketing.domain.vo.RankingAssistSettingsVo;
import com.sqx.app.pet.marketing.domain.vo.RankingPeriodOverviewVo;
import com.sqx.app.pet.marketing.service.MarketingRankService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "Admin-Ranking", description = "打榜管理")
@RestController
@RequestMapping("/admin/pet/ranking")
public class AdminMarketingRankController {

    @Resource
    private MarketingRankService marketingRankService;

    @Operation(summary = "榜单分页")
    @PostMapping("page")
    public Result<IPage<MarketingRankVo>> page(@RequestBody @Validated RankingPageBo bo) {
        return Result.success(marketingRankService.currentRanking(bo));
    }

    @Operation(summary = "周期概览")
    @PostMapping("periods")
    public Result<RankingPeriodOverviewVo> periods() {
        return Result.success(marketingRankService.periodOverview());
    }

    @Operation(summary = "打榜规则")
    @PostMapping("rules")
    public Result<String> rules() {
        return Result.success(marketingRankService.getRankingRules());
    }

    @Operation(summary = "更新打榜规则")
    @PostMapping("rules/update")
    public Result<Void> updateRules(@RequestBody @Validated RankingRulesUpdateBo bo) {
        marketingRankService.updateRankingRules(bo.getRules());
        return Result.success();
    }

    @Operation(summary = "打榜次数与付费配置")
    @PostMapping("assist/settings")
    public Result<RankingAssistSettingsVo> assistSettings() {
        return Result.success(marketingRankService.getAssistSettings());
    }

    @Operation(summary = "更新打榜次数与付费配置")
    @PostMapping("assist/settings/update")
    public Result<Void> updateAssistSettings(@RequestBody @Validated RankingAssistSettingsBo bo) {
        marketingRankService.updateAssistSettings(bo);
        return Result.success();
    }
}

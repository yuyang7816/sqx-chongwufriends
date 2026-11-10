package com.sqx.app.pet.marketing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "打榜周期概览")
public class RankingPeriodOverviewVo implements Serializable {
    private MarketingRankPeriodVo currentWeek;
    private MarketingRankPeriodVo previousWeek;
    private String rules;
    @Schema(description = "打榜次数与付费配置")
    private RankingAssistSettingsVo assistSettings;
}

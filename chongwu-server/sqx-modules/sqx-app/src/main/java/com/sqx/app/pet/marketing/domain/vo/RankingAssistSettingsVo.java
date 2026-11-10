package com.sqx.app.pet.marketing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "打榜次数与付费配置")
public class RankingAssistSettingsVo implements Serializable {
    @Schema(description = "每人每日免费打榜次数")
    private Integer dailyFreeLimit;
    @Schema(description = "免费次数用尽后，打榜一次消耗积分")
    private Integer pointsCost;
    @Schema(description = "免费次数用尽后，打榜一次消耗金币")
    private Integer coinsCost;
}

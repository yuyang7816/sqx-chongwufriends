package com.sqx.app.pet.marketing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户打榜额度")
public class RankingAssistQuotaVo implements Serializable {
    @Schema(description = "每人每日免费打榜次数")
    private Integer dailyFreeLimit;
    @Schema(description = "今日已打榜次数（含付费）")
    private Integer todayUsed;
    @Schema(description = "今日剩余免费次数")
    private Integer freeLeft;
    @Schema(description = "付费打榜一次消耗积分")
    private Integer pointsCost;
    @Schema(description = "付费打榜一次消耗金币")
    private Integer coinsCost;
    @Schema(description = "当前积分余额")
    private Integer points;
    @Schema(description = "当前金币余额")
    private Integer coins;
}

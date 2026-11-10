package com.sqx.app.pet.marketing.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "打榜次数与付费配置更新")
public class RankingAssistSettingsBo {
    @NotNull(message = "每日免费次数不能为空")
    @Min(value = 0, message = "每日免费次数不能小于0")
    @Max(value = 999, message = "每日免费次数不能大于999")
    private Integer dailyFreeLimit;

    @NotNull(message = "积分消耗不能为空")
    @Min(value = 0, message = "积分消耗不能小于0")
    @Max(value = 999999, message = "积分消耗过大")
    private Integer pointsCost;

    @NotNull(message = "金币消耗不能为空")
    @Min(value = 0, message = "金币消耗不能小于0")
    @Max(value = 999999, message = "金币消耗过大")
    private Integer coinsCost;
}

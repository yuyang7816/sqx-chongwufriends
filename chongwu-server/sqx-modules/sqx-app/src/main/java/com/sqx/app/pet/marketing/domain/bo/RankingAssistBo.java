package com.sqx.app.pet.marketing.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "助力")
public class RankingAssistBo {
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    private Long periodId;
    private String assistType;
    @Schema(description = "付费方式：points/coins；免费次数未用完时可省略")
    private String payType;
}

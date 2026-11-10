package com.sqx.app.pet.marketing.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "打榜规则更新")
public class RankingRulesUpdateBo {
    @NotBlank(message = "规则内容不能为空")
    private String rules;
}

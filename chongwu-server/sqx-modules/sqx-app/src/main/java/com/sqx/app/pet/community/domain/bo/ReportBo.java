package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "举报")
public class ReportBo {
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;
    @NotNull(message = "目标ID不能为空")
    private Long targetId;
    private String reason;
}

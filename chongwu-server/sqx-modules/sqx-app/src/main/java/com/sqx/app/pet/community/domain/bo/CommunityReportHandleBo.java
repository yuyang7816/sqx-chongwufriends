package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "处理投诉")
public class CommunityReportHandleBo {
    @NotNull(message = "ID不能为空")
    private Long id;
    @Schema(description = "2已处理")
    private Integer status;
}

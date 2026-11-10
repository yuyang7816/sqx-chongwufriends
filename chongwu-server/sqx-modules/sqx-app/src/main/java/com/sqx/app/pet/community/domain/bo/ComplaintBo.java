package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "提交投诉")
public class ComplaintBo {
    @NotNull(message = "目标类型不能为空")
    @Schema(description = "1动态 2评论")
    private Integer targetType;
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @Schema(description = "投诉类型（配置项）")
    private String complaintType;

    @Schema(description = "投诉详细说明")
    private String content;

    @Schema(description = "投诉原因（兼容旧版，优先使用 complaintType + content）")
    private String reason;
}

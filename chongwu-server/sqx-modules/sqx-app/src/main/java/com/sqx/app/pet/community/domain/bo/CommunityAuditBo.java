package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "动态审核")
public class CommunityAuditBo {
    @NotNull(message = "ID不能为空")
    private Long id;
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;
    @Schema(description = "拒绝原因")
    private String rejectReason;
}

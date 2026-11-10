package com.sqx.app.pet.archive.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "PetArchiveAuditBo")
public class PetArchiveAuditBo {
    @NotNull(message = "档案ID不能为空")
    private Long id;
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;
    @Schema(description = "拒绝原因")
    private String rejectReason;
}

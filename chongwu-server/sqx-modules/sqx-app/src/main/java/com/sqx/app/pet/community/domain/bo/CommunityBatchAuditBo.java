package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "批量审核动态")
public class CommunityBatchAuditBo {
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;
    @Schema(description = "拒绝原因")
    private String rejectReason;
}

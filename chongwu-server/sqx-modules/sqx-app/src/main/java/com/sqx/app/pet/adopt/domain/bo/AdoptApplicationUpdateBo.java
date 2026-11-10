package com.sqx.app.pet.adopt.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AdoptApplicationUpdateBo")
public class AdoptApplicationUpdateBo {
    private Long id;
    @Schema(description = "状态 1-7")
    private Integer status;
    private String rejectReason;
    private String remark;
}

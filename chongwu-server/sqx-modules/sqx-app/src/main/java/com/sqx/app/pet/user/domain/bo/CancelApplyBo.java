package com.sqx.app.pet.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "注销申请")
public class CancelApplyBo {
    private String reason;
}

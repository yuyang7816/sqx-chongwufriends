package com.sqx.app.pet.asset.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "UserAssetInfoByUserIdBo")
public class UserAssetInfoByUserIdBo {
    @NotNull(message = "userId不能为空")
    @Schema(description = "用户ID")
    private Long userId;
}

package com.sqx.app.pet.asset.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "UserAssetAdjustBo")
public class UserAssetAdjustBo {
    @NotNull(message = "userId不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @NotNull(message = "资产类型不能为空")
    @Schema(description = "资产类型：points/coins/love/flowers")
    private String assetType;

    @Schema(description = "操作类型：add 增加 / reduce 减少 / set 设为")
    private String opType;

    @NotNull(message = "变动数量不能为空")
    @Schema(description = "变动数量（set 时为目标值）")
    private Integer amount;

    @Schema(description = "备注/原因")
    private String reason;
}

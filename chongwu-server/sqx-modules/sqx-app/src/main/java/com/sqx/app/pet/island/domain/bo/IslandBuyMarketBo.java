package com.sqx.app.pet.island.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "购买集市道具")
public class IslandBuyMarketBo {
    @NotNull(message = "道具ID不能为空")
    private Long itemId;

    @Schema(description = "支付方式：coins=金币 / points=积分；二选一。未传时自动择优")
    private String payType;
}

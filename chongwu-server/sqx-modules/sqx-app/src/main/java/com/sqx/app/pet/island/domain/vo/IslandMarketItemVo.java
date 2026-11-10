package com.sqx.app.pet.island.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "IslandMarketItemVo")
public class IslandMarketItemVo implements Serializable {
    private Long id;
    private String name;
    private String coverUrl;
    private String itemType;
    private Integer priceCoins;
    private Integer pricePoints;
    @Schema(description = "是否可用金币兑换")
    private Boolean canPayCoins;
    @Schema(description = "是否可用积分兑换")
    private Boolean canPayPoints;
    private String effectDesc;
    private Integer durability;
    private Integer favor;
    private String petType;
    private Integer status;
    private Integer sort;
}

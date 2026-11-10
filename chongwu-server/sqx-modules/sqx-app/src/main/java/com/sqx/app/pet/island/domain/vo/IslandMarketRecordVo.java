package com.sqx.app.pet.island.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "集市兑换记录")
public class IslandMarketRecordVo implements Serializable {
    private Long id;
    private Long itemId;
    private String name;
    private String coverUrl;
    private String itemType;
    private Integer payCoins;
    private Integer payPoints;
    @Schema(description = "本次增加的亲密度")
    private Integer favor;
    private String date;
    private String createTime;
}

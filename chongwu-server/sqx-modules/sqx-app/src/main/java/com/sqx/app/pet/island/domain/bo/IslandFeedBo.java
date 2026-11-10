package com.sqx.app.pet.island.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "投喂岛宠")
public class IslandFeedBo {
    /** water-水碗 food-食盆，空则综合投喂 */
    private String feedType;
    private Boolean useAsset;
    private String assetType;
    private Integer assetAmount;
}

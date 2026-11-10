package com.sqx.app.pet.island.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "岛宅页面操作按钮")
public class IslandActionVo implements Serializable {
    private String title;
    private String emoji;
    private String path;
    /** navigate-跳转 feed-投喂 feed_water-水碗 feed_food-食盆 */
    private String actionType;
    private Boolean enabled;
}

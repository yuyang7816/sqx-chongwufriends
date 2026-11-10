package com.sqx.app.pet.island.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "IslandTaskVo")
public class IslandTaskVo implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String rewardDesc;
    private Integer requiredLevel;
    private String taskGroup;
    private String actionType;
    private Integer rewardCoins;
    private Integer rewardPoints;
    private Integer sort;
    private Integer status;
    private Integer recordStatus;
}

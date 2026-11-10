package com.sqx.app.pet.island.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "常驻宠物")
public class IslandResidentVo implements Serializable {
    private Long id;
    private String name;
    private String avatarUrl;
    private String petType;
    private String breed;
    private String storeName;
}

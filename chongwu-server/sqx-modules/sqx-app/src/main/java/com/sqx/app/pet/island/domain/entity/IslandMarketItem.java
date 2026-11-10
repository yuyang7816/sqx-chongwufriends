package com.sqx.app.pet.island.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("island_market_item")
public class IslandMarketItem implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("name")
    private String name;
    @TableField("cover_url")
    private String coverUrl;
    @TableField("item_type")
    private String itemType;
    @TableField("price_coins")
    private Integer priceCoins;
    @TableField("price_points")
    private Integer pricePoints;
    @TableField("effect_desc")
    private String effectDesc;
    @TableField("effect_json")
    private String effectJson;
    @TableField("durability")
    private Integer durability;
    @TableField("favor")
    private Integer favor;
    @TableField("pet_type")
    private String petType;
    @TableField("status")
    private Integer status;
    @TableField("sort")
    private Integer sort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}

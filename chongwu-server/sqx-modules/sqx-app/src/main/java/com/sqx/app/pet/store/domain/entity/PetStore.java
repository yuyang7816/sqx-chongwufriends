package com.sqx.app.pet.store.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("pet_store")
@Schema(name = "PetStore", description = "门店")
public class PetStore implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "城市ID")
    @TableField("city_id")
    private Long cityId;

    @Schema(description = "门店名称")
    @TableField("name")
    private String name;

    @Schema(description = "地址")
    @TableField("address")
    private String address;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "展示色")
    @TableField("tone")
    private String tone;

    @Schema(description = "封面图")
    @TableField("cover_url")
    private String coverUrl;

    @Schema(description = "营业状态 1营业 0休息")
    @TableField("business_status")
    private Integer businessStatus;

    @Schema(description = "首页推荐 1是")
    @TableField("featured")
    private Integer featured;

    @Schema(description = "排序")
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

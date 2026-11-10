package com.sqx.app.pet.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("mall_spu")
public class MallSpu implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("name")
    private String name;
    @TableField("category")
    private String category;
    @TableField("category_id")
    private Long categoryId;
    @TableField("cover_url")
    private String coverUrl;
    @TableField("description")
    private String description;
    @TableField("price")
    private BigDecimal price;
    @TableField("original_price")
    private BigDecimal originalPrice;
    @TableField("activity_price")
    private BigDecimal activityPrice;
    @TableField("vip_price")
    private BigDecimal vipPrice;
    @TableField("point_price")
    private Integer pointPrice;
    @TableField("biz_type")
    private Integer bizType;
    @TableField("status")
    private Integer status;
    @TableField("sort")
    private Integer sort;
    @TableField("spec_mode")
    private Integer specMode;
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

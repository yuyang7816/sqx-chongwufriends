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
@TableName("mall_sku")
public class MallSku implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("spu_id")
    private Long spuId;
    @TableField("spec_id")
    private Long specId;
    @TableField("sku_name")
    private String skuName;
    @TableField("price")
    private BigDecimal price;
    @TableField("point_price")
    private Integer pointPrice;
    @TableField("stock")
    private Integer stock;
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

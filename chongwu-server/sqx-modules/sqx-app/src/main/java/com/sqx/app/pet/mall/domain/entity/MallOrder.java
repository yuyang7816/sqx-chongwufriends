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
@TableName("mall_order")
public class MallOrder implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("order_no")
    private String orderNo;
    @TableField("user_id")
    private Long userId;
    @TableField("spu_id")
    private Long spuId;
    @TableField("sku_id")
    private Long skuId;
    @TableField("quantity")
    private Integer quantity;
    @TableField("pay_amount")
    private BigDecimal payAmount;
    @TableField("point_amount")
    private Integer pointAmount;
    @TableField("pay_type")
    private Integer payType;
    @TableField("biz_type")
    private Integer bizType;
    @TableField("pay_status")
    private Integer payStatus;
    @TableField("status")
    private Integer status;
    @TableField("remark")
    private String remark;
    @TableField("idempotent_key")
    private String idempotentKey;
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

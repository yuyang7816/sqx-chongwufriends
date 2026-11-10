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
@TableName("coupon_template")
public class CouponTemplate implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("title")
    private String title;
    @TableField("amount_desc")
    private String amountDesc;
    @TableField("min_amount")
    private BigDecimal minAmount;
    @TableField("coupon_type")
    private String couponType;
    @TableField("valid_days")
    private Integer validDays;
    @TableField("status")
    private Integer status;
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

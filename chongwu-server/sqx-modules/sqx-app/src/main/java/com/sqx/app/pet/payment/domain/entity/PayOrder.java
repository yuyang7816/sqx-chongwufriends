package com.sqx.app.pet.payment.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("pay_order")
public class PayOrder implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("pay_no")
    private String payNo;
    @TableField("biz_order_no")
    private String bizOrderNo;
    @TableField("user_id")
    private Long userId;
    @TableField("business_type")
    private String businessType;
    @TableField("terminal")
    private String terminal;
    @TableField("pay_channel")
    private String payChannel;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("status")
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("pay_time")
    private LocalDateTime payTime;
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

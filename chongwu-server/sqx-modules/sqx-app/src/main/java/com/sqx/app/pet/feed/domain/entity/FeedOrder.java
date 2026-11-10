package com.sqx.app.pet.feed.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("feed_order")
public class FeedOrder implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("order_no")
    private String orderNo;
    @TableField("user_id")
    private Long userId;
    @TableField("pet_id")
    private Long petId;
    @TableField("product_name")
    private String productName;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("quantity")
    private Integer quantity;
    @TableField("message")
    private String message;
    @TableField("status")
    private Integer status;
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

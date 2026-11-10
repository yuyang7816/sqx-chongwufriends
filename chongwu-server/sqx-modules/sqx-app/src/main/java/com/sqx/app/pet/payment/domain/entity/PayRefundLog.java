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
@TableName("pay_refund_log")
public class PayRefundLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("pay_no")
    private String payNo;
    @TableField("refund_no")
    private String refundNo;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("status")
    private Integer status;
    @TableField("request_body")
    private String requestBody;
    @TableField("response_body")
    private String responseBody;
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

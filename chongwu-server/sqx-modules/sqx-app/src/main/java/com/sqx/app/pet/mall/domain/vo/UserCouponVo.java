package com.sqx.app.pet.mall.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "UserCouponVo")
public class UserCouponVo implements Serializable {
    private Long id;
    private Long templateId;
    private Integer status;
    private String statusText;
    private Long orderId;
    private String title;
    private String amount;
    private String desc;
    private String expire;
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}

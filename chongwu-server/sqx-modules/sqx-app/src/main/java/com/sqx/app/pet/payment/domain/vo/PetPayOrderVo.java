package com.sqx.app.pet.payment.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "PetPayOrderVo")
public class PetPayOrderVo implements Serializable {
    private String payNo;
    private String bizOrderNo;
    private String businessType;
    private BigDecimal amount;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    @Schema(description = "调起支付参数JSON")
    private String payParams;
    private String payChannel;
    private String terminal;
}

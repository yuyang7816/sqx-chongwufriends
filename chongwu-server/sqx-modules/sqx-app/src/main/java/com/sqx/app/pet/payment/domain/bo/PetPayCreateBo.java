package com.sqx.app.pet.payment.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建支付单")
public class PetPayCreateBo {
    @NotBlank(message = "业务订单号不能为空")
    private String bizOrderNo;
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    @NotBlank(message = "支付渠道不能为空")
    private String payChannel;
    private String terminal;
}

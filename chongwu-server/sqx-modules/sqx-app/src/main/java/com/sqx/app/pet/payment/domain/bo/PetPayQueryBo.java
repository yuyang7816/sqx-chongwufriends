package com.sqx.app.pet.payment.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "查询支付状态")
public class PetPayQueryBo {
    @NotBlank(message = "支付单号不能为空")
    private String payNo;
}

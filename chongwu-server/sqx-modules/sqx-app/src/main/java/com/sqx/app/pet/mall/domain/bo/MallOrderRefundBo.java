package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "订单退款")
public class MallOrderRefundBo {
    @NotNull
    private Long id;
    private String reason;
}

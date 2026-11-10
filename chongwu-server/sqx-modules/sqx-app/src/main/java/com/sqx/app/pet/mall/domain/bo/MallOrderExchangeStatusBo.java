package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "兑换单状态更新")
public class MallOrderExchangeStatusBo {
    @NotNull(message = "订单ID不能为空")
    private Long id;
    @NotNull(message = "状态不能为空")
    private Integer status;
}

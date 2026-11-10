package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "订单详情")
public class MallOrderInfoBo {
    @NotNull(message = "ID不能为空")
    private Long id;
}

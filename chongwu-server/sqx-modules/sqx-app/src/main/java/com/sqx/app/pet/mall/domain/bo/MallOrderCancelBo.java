package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "取消订单")
public class MallOrderCancelBo {
    @NotNull(message = "订单ID不能为空")
    private Long id;

    /** 兼容前端 orderId 字段 */
    public void setOrderId(Long orderId) {
        this.id = orderId;
    }
}

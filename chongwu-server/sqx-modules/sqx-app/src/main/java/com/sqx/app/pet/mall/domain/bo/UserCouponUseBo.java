package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "使用优惠券")
public class UserCouponUseBo {
    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}

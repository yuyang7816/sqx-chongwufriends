package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "向用户发放优惠券")
public class UserCouponGrantBo {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotNull(message = "券模板ID不能为空")
    private Long templateId;
    private Integer count;
}

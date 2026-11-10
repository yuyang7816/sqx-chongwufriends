package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "创建商城订单")
public class MallOrderCreateBo {
    private Long skuId;
    private Long spuId;
    private String skuName;
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
    private Long couponId;
    private String remark;
}

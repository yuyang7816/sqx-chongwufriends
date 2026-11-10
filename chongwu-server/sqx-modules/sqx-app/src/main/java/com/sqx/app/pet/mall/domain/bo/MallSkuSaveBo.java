package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "MallSkuSaveBo")
public class MallSkuSaveBo {
    @NotNull(message = "商品ID不能为空")
    private Long spuId;
    @NotBlank(message = "规格名称不能为空")
    private String skuName;
    private BigDecimal price;
    private Integer pointPrice;
    private Integer stock;
}

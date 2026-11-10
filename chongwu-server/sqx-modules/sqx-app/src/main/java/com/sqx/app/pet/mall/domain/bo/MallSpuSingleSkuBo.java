package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Schema(description = "单规格SKU信息")
public class MallSpuSingleSkuBo {
    private Long id;
    @NotBlank(message = "规格名称不能为空")
    private String skuName;
    private BigDecimal price;
    private Integer pointPrice;
    private Integer stock;
}

package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "多规格商品SKU")
public class MallSpuSkuBo {
    private Long id;
    private Long specId;
    private String skuName;
    private BigDecimal price;
    private Integer pointPrice;
    private Integer stock;
}

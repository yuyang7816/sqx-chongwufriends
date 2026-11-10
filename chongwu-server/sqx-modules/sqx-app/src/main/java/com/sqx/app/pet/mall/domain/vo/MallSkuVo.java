package com.sqx.app.pet.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "MallSkuVo")
public class MallSkuVo implements Serializable {
    private Long id;
    private Long spuId;
    private Long specId;
    private String skuName;
    private BigDecimal price;
    private Integer pointPrice;
    private Integer stock;
}

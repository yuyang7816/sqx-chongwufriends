package com.sqx.app.pet.mall.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class MallSkuSpecValueItem implements Serializable {
    /** 规格值名称，如：100g、常温 */
    private String name;
    /** 该规格值售价 */
    private BigDecimal price;
    /** 该规格值积分价 */
    private Integer pointPrice;
    /** 该规格值默认库存 */
    private Integer stock;
}

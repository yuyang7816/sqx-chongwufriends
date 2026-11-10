package com.sqx.app.pet.mall.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class MallSkuSpecRuleItem implements Serializable {
    /** 规格名，如：大杯、温度、份量 */
    private String value;
    /** 规格值名称汇总（逗号分隔，兼容展示） */
    private String detail;
    /** 规格值明细（含各自售价/积分/库存） */
    private List<MallSkuSpecValueItem> items;
}

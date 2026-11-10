package com.sqx.app.pet.mall.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqx.app.pet.mall.domain.handler.MallSkuSpecRuleValueTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@TableName(value = "mall_sku_spec", autoResultMap = true)
public class MallSkuSpec implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 规格标题，如：饮料、鲜食份量 */
    @TableField("spec_name")
    private String specName;
    /**
     * 规格组列表，DB 列 rule_value 存 JSON。
     * autoResultMap + TypeHandler 保证查询能反序列化并回显到前端。
     */
    @TableField(value = "rule_value", typeHandler = MallSkuSpecRuleValueTypeHandler.class)
    private List<MallSkuSpecRuleItem> ruleValue;
    @TableField("price")
    private BigDecimal price;
    @TableField("point_price")
    private Integer pointPrice;
    @TableField("default_stock")
    private Integer defaultStock;
    @TableField("sort")
    private Integer sort;
    @TableField("status")
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}

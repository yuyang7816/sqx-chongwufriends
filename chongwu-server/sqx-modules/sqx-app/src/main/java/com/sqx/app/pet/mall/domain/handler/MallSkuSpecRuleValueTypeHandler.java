package com.sqx.app.pet.mall.domain.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.sqx.app.pet.mall.domain.entity.MallSkuSpecRuleItem;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * mall_sku_spec.rule_value ↔ List&lt;MallSkuSpecRuleItem&gt;
 */
public class MallSkuSpecRuleValueTypeHandler extends AbstractJsonTypeHandler<List<MallSkuSpecRuleItem>> {

    @Override
    protected List<MallSkuSpecRuleItem> parse(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<MallSkuSpecRuleItem> list = JSON.parseObject(json, new TypeReference<List<MallSkuSpecRuleItem>>() {});
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    @Override
    protected String toJson(List<MallSkuSpecRuleItem> obj) {
        return JSON.toJSONString(obj == null ? new ArrayList<>() : obj);
    }
}

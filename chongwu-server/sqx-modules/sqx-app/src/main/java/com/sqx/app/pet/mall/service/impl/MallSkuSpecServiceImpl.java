package com.sqx.app.pet.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.mall.db.service.IMallSkuService;
import com.sqx.app.pet.mall.db.service.IMallSkuSpecService;
import com.sqx.app.pet.mall.domain.bo.MallSkuSpecPageBo;
import com.sqx.app.pet.mall.domain.entity.MallSku;
import com.sqx.app.pet.mall.domain.entity.MallSkuSpec;
import com.sqx.app.pet.mall.domain.entity.MallSkuSpecRuleItem;
import com.sqx.app.pet.mall.domain.entity.MallSkuSpecValueItem;
import com.sqx.app.pet.mall.service.MallSkuSpecService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class MallSkuSpecServiceImpl implements MallSkuSpecService {

    @Resource
    private IMallSkuSpecService iMallSkuSpecService;
    @Resource
    private IMallSkuService iMallSkuService;

    @Override
    public IPage<MallSkuSpec> pageInfo(MallSkuSpecPageBo bo) {
        Page<MallSkuSpec> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<MallSkuSpec> qw = new QueryWrapper<>();
        qw.lambda()
                .like(StringUtils.isNotBlank(bo.getKeyword()), MallSkuSpec::getSpecName, bo.getKeyword())
                .eq(bo.getStatus() != null, MallSkuSpec::getStatus, bo.getStatus())
                .orderByAsc(MallSkuSpec::getSort)
                .orderByDesc(MallSkuSpec::getCreateTime);
        return iMallSkuSpecService.page(page, qw);
    }

    @Override
    public List<MallSkuSpec> listEnabled() {
        return iMallSkuSpecService.lambdaQuery()
                .eq(MallSkuSpec::getStatus, 1)
                .orderByAsc(MallSkuSpec::getSort)
                .orderByAsc(MallSkuSpec::getId)
                .list();
    }

    @Override
    public void saveInfo(MallSkuSpec spec) {
        normalizeAndValidate(spec);
        iMallSkuSpecService.save(spec);
    }

    @Override
    public void updateInfo(MallSkuSpec spec) {
        if (spec.getId() == null) {
            throw new BizException("规格ID不能为空");
        }
        normalizeAndValidate(spec);
        iMallSkuSpecService.updateById(spec);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BizException("规格ID不能为空");
        }
        long used = iMallSkuService.lambdaQuery().eq(MallSku::getSpecId, id).count();
        if (used > 0) {
            throw new BizException("该规格已被商品引用，无法删除");
        }
        MallSkuSpec spec = iMallSkuSpecService.getById(id);
        if (spec == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        iMallSkuSpecService.removeById(id);
    }

    private void normalizeAndValidate(MallSkuSpec spec) {
        if (StringUtils.isBlank(spec.getSpecName())) {
            throw new BizException("请输入规格标题");
        }
        spec.setSpecName(spec.getSpecName().trim());
        if (spec.getSort() == null) {
            spec.setSort(0);
        }
        if (spec.getStatus() == null) {
            spec.setStatus(1);
        }
        if (spec.getDefaultStock() == null) {
            spec.setDefaultStock(0);
        }
        if (spec.getPrice() == null) {
            spec.setPrice(BigDecimal.ZERO);
        }
        if (spec.getPointPrice() == null) {
            spec.setPointPrice(0);
        }
        List<MallSkuSpecRuleItem> rules = normalizeRuleValue(spec.getRuleValue(), spec);
        if (CollectionUtils.isEmpty(rules)) {
            throw new BizException("请至少添加一种规格");
        }
        spec.setRuleValue(rules);
    }

    public static List<MallSkuSpecRuleItem> parseRuleValueJson(String json) {
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

    public static List<MallSkuSpecRuleItem> normalizeRuleValue(List<MallSkuSpecRuleItem> source, MallSkuSpec defaults) {
        List<MallSkuSpecRuleItem> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(source)) {
            return result;
        }
        for (MallSkuSpecRuleItem item : source) {
            if (item == null || StringUtils.isBlank(item.getValue())) {
                continue;
            }
            Set<String> names = new LinkedHashSet<>();
            if (CollectionUtils.isNotEmpty(item.getItems())) {
                for (MallSkuSpecValueItem valueItem : item.getItems()) {
                    if (valueItem != null && StringUtils.isNotBlank(valueItem.getName())) {
                        names.add(valueItem.getName().trim());
                    }
                }
            }
            if (StringUtils.isNotBlank(item.getDetail())) {
                for (String part : item.getDetail().split(",")) {
                    String name = StringUtils.trimToEmpty(part);
                    if (StringUtils.isNotBlank(name)) {
                        names.add(name);
                    }
                }
            }
            if (names.isEmpty()) {
                continue;
            }
            MallSkuSpecRuleItem normalized = new MallSkuSpecRuleItem();
            normalized.setValue(item.getValue().trim());
            normalized.setDetail(String.join(",", names));
            // 规格库只保留属性名；价格在商品侧按笛卡尔积填写
            normalized.setItems(null);
            result.add(normalized);
        }
        return result;
    }

    public static List<MallSkuSpecRuleItem> normalizeRuleValue(List<MallSkuSpecRuleItem> source) {
        return normalizeRuleValue(source, null);
    }

    /**
     * 按多个规格组做笛卡尔积，生成 SKU 名称列表。
     * 例：份量[100g,200g] × 口味[原味,牛肉] => 100g-原味, 100g-牛肉, 200g-原味, 200g-牛肉
     */
    public static List<MallSkuSpecValueItem> expandSkuValues(MallSkuSpec spec) {
        List<MallSkuSpecValueItem> result = new ArrayList<>();
        if (spec == null) {
            return result;
        }
        List<MallSkuSpecRuleItem> rules = normalizeRuleValue(spec.getRuleValue(), spec);
        if (CollectionUtils.isEmpty(rules)) {
            if (StringUtils.isNotBlank(spec.getSpecName())) {
                result.add(buildValue(spec.getSpecName().trim(), spec));
            }
            return result;
        }

        List<List<String>> dimensions = new ArrayList<>();
        for (MallSkuSpecRuleItem rule : rules) {
            List<String> values = new ArrayList<>();
            for (String part : rule.getDetail().split(",")) {
                String name = StringUtils.trimToEmpty(part);
                if (StringUtils.isNotBlank(name)) {
                    values.add(name);
                }
            }
            if (!values.isEmpty()) {
                dimensions.add(values);
            }
        }
        if (dimensions.isEmpty()) {
            return result;
        }

        List<String> combos = cartesianProduct(dimensions);
        for (String name : combos) {
            result.add(buildValue(name, spec));
        }
        return result;
    }

    private static MallSkuSpecValueItem buildValue(String name, MallSkuSpec spec) {
        MallSkuSpecValueItem item = new MallSkuSpecValueItem();
        item.setName(name);
        item.setPrice(spec.getPrice() != null ? spec.getPrice() : BigDecimal.ZERO);
        item.setPointPrice(spec.getPointPrice() != null ? spec.getPointPrice() : 0);
        item.setStock(spec.getDefaultStock() != null ? spec.getDefaultStock() : 0);
        return item;
    }

    private static List<String> cartesianProduct(List<List<String>> dimensions) {
        List<String> result = new ArrayList<>();
        result.add("");
        for (List<String> dim : dimensions) {
            List<String> next = new ArrayList<>();
            for (String prefix : result) {
                for (String value : dim) {
                    next.add(StringUtils.isBlank(prefix) ? value : (prefix + "-" + value));
                }
            }
            result = next;
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    public static List<String> expandSkuNames(MallSkuSpec spec) {
        List<String> names = new ArrayList<>();
        for (MallSkuSpecValueItem item : expandSkuValues(spec)) {
            names.add(item.getName());
        }
        return names;
    }
}

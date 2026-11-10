package com.sqx.app.pet.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.mall.db.service.IMallCategoryService;
import com.sqx.app.pet.mall.db.service.IMallSkuService;
import com.sqx.app.pet.mall.db.service.IMallSkuSpecService;
import com.sqx.app.pet.mall.db.service.IMallSpuService;
import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.entity.MallCategory;
import com.sqx.app.pet.mall.domain.entity.MallSku;
import com.sqx.app.pet.mall.domain.entity.MallSkuSpec;
import com.sqx.app.pet.mall.domain.entity.MallSkuSpecValueItem;
import com.sqx.app.pet.mall.domain.entity.MallSpu;
import com.sqx.app.pet.mall.domain.vo.MallSkuVo;
import com.sqx.app.pet.mall.domain.vo.MallSpuVo;
import com.sqx.app.pet.mall.service.MallSpuService;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MallSpuServiceImpl implements MallSpuService {

    private static final int SPEC_MODE_SINGLE = 1;
    private static final int SPEC_MODE_MULTI = 2;

    @Resource
    private IMallSpuService iMallSpuService;
    @Resource
    private IMallSkuService iMallSkuService;
    @Resource
    private IMallSkuSpecService iMallSkuSpecService;
    @Resource
    private IMallCategoryService iMallCategoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(MallSpuSaveBo bo) {
        MallSpu entity = new MallSpu();
        BeanUtils.copyProperties(bo, entity);
        fillCategoryName(entity);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getSpecMode() == null) {
            entity.setSpecMode(SPEC_MODE_SINGLE);
        }
        iMallSpuService.save(entity);
        syncSkus(entity, bo.getSingleSku(), bo.getSpecIds(), bo.getSkuList());
    }

    @Override
    public IPage<MallSpuVo> pageInfo(MallSpuPageBo bo) {
        Page<MallSpu> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<MallSpu> qw = buildPageQuery(bo, true);
        return toVoPage(iMallSpuService.page(page, qw));
    }

    @Override
    public IPage<MallSpuVo> adminPageInfo(MallSpuPageBo bo) {
        Page<MallSpu> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<MallSpu> qw = buildPageQuery(bo, false);
        return toVoPage(iMallSpuService.page(page, qw));
    }

    private QueryWrapper<MallSpu> buildPageQuery(MallSpuPageBo bo, boolean onlyOnline) {
        QueryWrapper<MallSpu> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(onlyOnline, MallSpu::getStatus, 1)
                .eq(!onlyOnline && bo.getStatus() != null, MallSpu::getStatus, bo.getStatus())
                .eq(bo.getCategoryId() != null, MallSpu::getCategoryId, bo.getCategoryId())
                .eq(bo.getBizType() != null, MallSpu::getBizType, bo.getBizType())
                .like(StringUtils.isNotBlank(bo.getCategory()), MallSpu::getCategory, bo.getCategory())
                .like(StringUtils.isNotBlank(bo.getKeyword()), MallSpu::getName, bo.getKeyword())
                .orderByAsc(MallSpu::getSort)
                .orderByDesc(MallSpu::getCreateTime);
        return qw;
    }

    private IPage<MallSpuVo> toVoPage(IPage<MallSpu> iPage) {
        List<MallSpuVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (MallSpu record : iPage.getRecords()) {
                MallSpuVo vo = new MallSpuVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public MallSpuVo info(MallSpuInfoBo bo) {
        MallSpu entity = iMallSpuService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        MallSpuVo vo = new MallSpuVo();
        BeanUtils.copyProperties(entity, vo);
        List<MallSku> skuList = iMallSkuService.lambdaQuery()
                .eq(MallSku::getSpuId, entity.getId())
                .orderByAsc(MallSku::getId)
                .list();
        List<MallSkuVo> skuVoList = new ArrayList<>();
        List<Long> specIds = new ArrayList<>();
        Set<Long> seenSpecIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(skuList)) {
            for (MallSku sku : skuList) {
                MallSkuVo skuVo = new MallSkuVo();
                BeanUtils.copyProperties(sku, skuVo);
                skuVoList.add(skuVo);
                if (sku.getSpecId() != null && seenSpecIds.add(sku.getSpecId())) {
                    specIds.add(sku.getSpecId());
                }
            }
            vo.setDefaultSkuId(skuList.get(0).getId());
            if (vo.getPrice() == null) {
                vo.setPrice(skuList.get(0).getPrice());
            }
            if (vo.getPointPrice() == null) {
                vo.setPointPrice(skuList.get(0).getPointPrice());
            }
        }
        if (vo.getSpecMode() == null) {
            vo.setSpecMode(skuList.size() > 1 ? SPEC_MODE_MULTI : SPEC_MODE_SINGLE);
        }
        vo.setSpecIds(specIds);
        vo.setSkuList(skuVoList);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(MallSpuUpdateBo bo) {
        MallSpu entity = iMallSpuService.getById(bo.getId());
        if (entity == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        BeanUtils.copyProperties(bo, entity);
        fillCategoryName(entity);
        if (entity.getSpecMode() == null) {
            entity.setSpecMode(SPEC_MODE_SINGLE);
        }
        iMallSpuService.updateById(entity);
        syncSkus(entity, bo.getSingleSku(), bo.getSpecIds(), bo.getSkuList());
    }

    @Override
    public void delete(MallSpuInfoBo bo) {
        iMallSpuService.removeById(bo.getId());
        iMallSkuService.lambdaUpdate().eq(MallSku::getSpuId, bo.getId()).remove();
    }

    @Override
    public List<MallSpuVo> list() {
        List<MallSpu> list = iMallSpuService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            MallSpuVo vo = new MallSpuVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private void fillCategoryName(MallSpu entity) {
        // 投喂商品固定 category=feed，与 App / 种子数据约定一致
        if (Objects.equals(entity.getBizType(), 2)) {
            entity.setCategory("feed");
            return;
        }
        if (StringUtils.isNotBlank(entity.getCategory())) {
            return;
        }
        if (entity.getCategoryId() == null) {
            return;
        }
        MallCategory category = iMallCategoryService.getById(entity.getCategoryId());
        if (category != null) {
            entity.setCategory(category.getName());
        }
    }

    private void syncSkus(MallSpu spu, MallSpuSingleSkuBo singleSku, List<Long> specIds, List<MallSpuSkuBo> skuList) {
        if (Objects.equals(spu.getSpecMode(), SPEC_MODE_MULTI)) {
            syncMultiSkus(spu, specIds, skuList);
            return;
        }
        syncSingleSku(spu, singleSku);
    }

    private void syncSingleSku(MallSpu spu, MallSpuSingleSkuBo singleSku) {
        List<MallSku> existing = iMallSkuService.lambdaQuery()
                .eq(MallSku::getSpuId, spu.getId())
                .orderByAsc(MallSku::getId)
                .list();
        if (existing.size() > 1) {
            for (int i = 1; i < existing.size(); i++) {
                iMallSkuService.removeById(existing.get(i).getId());
            }
        }
        MallSku sku = existing.isEmpty() ? new MallSku() : existing.get(0);
        sku.setSpuId(spu.getId());
        sku.setSpecId(null);
        if (singleSku != null && StringUtils.isNotBlank(singleSku.getSkuName())) {
            sku.setSkuName(singleSku.getSkuName().trim());
            sku.setPrice(singleSku.getPrice() != null ? singleSku.getPrice() : spu.getPrice());
            sku.setPointPrice(singleSku.getPointPrice() != null ? singleSku.getPointPrice() : spu.getPointPrice());
            sku.setStock(singleSku.getStock() != null ? singleSku.getStock() : 0);
        } else {
            sku.setSkuName(StringUtils.defaultIfBlank(sku.getSkuName(), "标准装"));
            sku.setPrice(spu.getPrice());
            sku.setPointPrice(spu.getPointPrice());
            if (sku.getStock() == null) {
                sku.setStock(999);
            }
        }
        if (sku.getId() == null) {
            iMallSkuService.save(sku);
        } else {
            iMallSkuService.updateById(sku);
        }
    }

    private void syncMultiSkus(MallSpu spu, List<Long> specIds, List<MallSpuSkuBo> skuList) {
        if (CollectionUtils.isEmpty(specIds)) {
            throw new BizException("多规格商品请至少选择一个规格");
        }
        List<DesiredSku> desiredList = new ArrayList<>();
        Set<String> desiredKeys = new HashSet<>();

        // 优先使用前端提交的笛卡尔积 SKU（含各自售价/积分/库存）
        if (CollectionUtils.isNotEmpty(skuList)) {
            for (MallSpuSkuBo skuBo : skuList) {
                if (skuBo == null || StringUtils.isBlank(skuBo.getSkuName())) {
                    continue;
                }
                Long specId = skuBo.getSpecId() != null ? skuBo.getSpecId() : specIds.get(0);
                String skuName = skuBo.getSkuName().trim();
                String key = specId + "#" + skuName;
                if (!desiredKeys.add(key)) {
                    continue;
                }
                DesiredSku item = new DesiredSku();
                item.specId = specId;
                item.skuName = skuName;
                item.price = skuBo.getPrice() != null ? skuBo.getPrice() : spu.getPrice();
                item.pointPrice = skuBo.getPointPrice() != null ? skuBo.getPointPrice() : spu.getPointPrice();
                item.stock = skuBo.getStock() != null ? skuBo.getStock() : 0;
                desiredList.add(item);
            }
        }

        // 未提交时，按模板规格组笛卡尔积自动生成
        if (desiredList.isEmpty()) {
            for (Long specId : specIds) {
                MallSkuSpec spec = iMallSkuSpecService.getById(specId);
                if (spec == null || !Objects.equals(spec.getStatus(), 1)) {
                    throw new BizException("规格不存在或已禁用: " + specId);
                }
                List<MallSkuSpecValueItem> skuValues = MallSkuSpecServiceImpl.expandSkuValues(spec);
                if (CollectionUtils.isEmpty(skuValues)) {
                    throw new BizException("规格「" + spec.getSpecName() + "」未配置规格值");
                }
                for (MallSkuSpecValueItem skuValue : skuValues) {
                    String skuName = skuValue.getName();
                    String key = specId + "#" + skuName;
                    if (!desiredKeys.add(key)) {
                        continue;
                    }
                    DesiredSku item = new DesiredSku();
                    item.specId = specId;
                    item.skuName = skuName;
                    item.price = skuValue.getPrice() != null ? skuValue.getPrice() : (spec.getPrice() != null ? spec.getPrice() : spu.getPrice());
                    item.pointPrice = skuValue.getPointPrice() != null ? skuValue.getPointPrice() : (spec.getPointPrice() != null ? spec.getPointPrice() : spu.getPointPrice());
                    item.stock = skuValue.getStock() != null ? skuValue.getStock() : (spec.getDefaultStock() != null ? spec.getDefaultStock() : 0);
                    desiredList.add(item);
                }
            }
        }

        if (desiredList.isEmpty()) {
            throw new BizException("多规格商品请至少配置一个SKU");
        }

        List<MallSku> existing = iMallSkuService.lambdaQuery()
                .eq(MallSku::getSpuId, spu.getId())
                .list();
        Map<String, MallSku> existingMap = new HashMap<>();
        for (MallSku sku : existing) {
            if (sku.getSpecId() == null || StringUtils.isBlank(sku.getSkuName())) {
                iMallSkuService.removeById(sku.getId());
                continue;
            }
            existingMap.put(sku.getSpecId() + "#" + sku.getSkuName().trim(), sku);
        }

        Set<String> keepKeys = new HashSet<>();
        for (DesiredSku desired : desiredList) {
            String key = desired.specId + "#" + desired.skuName;
            keepKeys.add(key);
            MallSku sku = existingMap.get(key);
            if (sku == null) {
                sku = new MallSku();
                sku.setSpuId(spu.getId());
                sku.setSpecId(desired.specId);
                sku.setSkuName(desired.skuName);
                sku.setPrice(desired.price);
                sku.setPointPrice(desired.pointPrice);
                sku.setStock(desired.stock);
                iMallSkuService.save(sku);
            } else {
                sku.setSkuName(desired.skuName);
                sku.setPrice(desired.price);
                sku.setPointPrice(desired.pointPrice);
                sku.setStock(desired.stock);
                iMallSkuService.updateById(sku);
            }
        }
        for (Map.Entry<String, MallSku> entry : existingMap.entrySet()) {
            if (!keepKeys.contains(entry.getKey())) {
                iMallSkuService.removeById(entry.getValue().getId());
            }
        }
    }

    private static class DesiredSku {
        private Long specId;
        private String skuName;
        private java.math.BigDecimal price;
        private Integer pointPrice;
        private Integer stock;
    }
}

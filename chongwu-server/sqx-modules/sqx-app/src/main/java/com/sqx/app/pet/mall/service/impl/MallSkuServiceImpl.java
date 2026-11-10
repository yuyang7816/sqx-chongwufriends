package com.sqx.app.pet.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.mall.db.service.IMallSkuService;
import com.sqx.app.pet.mall.db.service.IMallSpuService;
import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.entity.MallSku;
import com.sqx.app.pet.mall.domain.entity.MallSpu;
import com.sqx.app.pet.mall.domain.vo.MallSkuVo;
import com.sqx.app.pet.mall.service.MallSkuService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MallSkuServiceImpl implements MallSkuService {

    @Resource
    private IMallSkuService iMallSkuService;
    @Resource
    private IMallSpuService iMallSpuService;

    @Override
    public IPage<MallSkuVo> pageInfo(MallSkuPageBo bo) {
        assertSpuExists(bo.getSpuId());
        Page<MallSku> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<MallSku> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(MallSku::getSpuId, bo.getSpuId())
                .orderByAsc(MallSku::getId);
        IPage<MallSku> skuPage = iMallSkuService.page(page, qw);
        List<MallSkuVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuPage.getRecords())) {
            for (MallSku record : skuPage.getRecords()) {
                MallSkuVo vo = new MallSkuVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(skuPage, voList);
    }

    @Override
    public void saveInfo(MallSkuSaveBo bo) {
        MallSpu spu = assertSpuExists(bo.getSpuId());
        MallSku entity = new MallSku();
        entity.setSpuId(bo.getSpuId());
        entity.setSkuName(bo.getSkuName().trim());
        entity.setPrice(bo.getPrice() != null ? bo.getPrice() : spu.getPrice());
        entity.setPointPrice(bo.getPointPrice() != null ? bo.getPointPrice() : spu.getPointPrice());
        entity.setStock(bo.getStock() != null ? bo.getStock() : 0);
        iMallSkuService.save(entity);
    }

    @Override
    public void updateInfo(MallSkuUpdateBo bo) {
        MallSku entity = iMallSkuService.getById(bo.getId());
        if (entity == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        entity.setSkuName(bo.getSkuName().trim());
        if (bo.getPrice() != null) {
            entity.setPrice(bo.getPrice());
        }
        if (bo.getPointPrice() != null) {
            entity.setPointPrice(bo.getPointPrice());
        }
        if (bo.getStock() != null) {
            entity.setStock(bo.getStock());
        }
        iMallSkuService.updateById(entity);
    }

    @Override
    public void delete(MallSkuInfoBo bo) {
        MallSku entity = iMallSkuService.getById(bo.getId());
        if (entity == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        long count = iMallSkuService.lambdaQuery()
                .eq(MallSku::getSpuId, entity.getSpuId())
                .count();
        if (count <= 1) {
            throw new BizException("商品至少保留一个规格");
        }
        iMallSkuService.removeById(bo.getId());
    }

    private MallSpu assertSpuExists(Long spuId) {
        MallSpu spu = iMallSpuService.getById(spuId);
        if (Objects.isNull(spu)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        return spu;
    }
}

package com.sqx.app.pet.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.store.db.service.IPetStoreService;
import com.sqx.app.pet.store.domain.bo.*;
import com.sqx.app.pet.store.domain.entity.PetStore;
import com.sqx.app.pet.store.domain.vo.PetStoreVo;
import com.sqx.app.pet.store.service.PetStoreService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PetStoreServiceImpl implements PetStoreService {

    @Resource
    private IPetStoreService iPetStoreService;

    @Override
    public void saveInfo(PetStoreSaveBo bo) {
        PetStore entity = new PetStore();
        BeanUtils.copyProperties(bo, entity);
        if (entity.getFeatured() == null) {
            entity.setFeatured(0);
        }
        iPetStoreService.save(entity);
    }

    @Override
    public IPage<PetStoreVo> pageInfo(PetStorePageBo bo) {
        Page<PetStore> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<PetStore> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(Objects.nonNull(bo.getCityId()), PetStore::getCityId, bo.getCityId())
                .like(StringUtils.isNotBlank(bo.getName()), PetStore::getName, bo.getName())
                .eq(Objects.nonNull(bo.getBusinessStatus()), PetStore::getBusinessStatus, bo.getBusinessStatus())
                .orderByAsc(PetStore::getSort);
        IPage<PetStore> iPage = iPetStoreService.page(page, qw);
        List<PetStoreVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (PetStore record : iPage.getRecords()) {
                PetStoreVo vo = new PetStoreVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public PetStoreVo info(PetStoreInfoBo bo) {
        PetStore entity = iPetStoreService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("not found");
        }
        PetStoreVo vo = new PetStoreVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(PetStoreUpdateBo bo) {
        PetStore entity = new PetStore();
        BeanUtils.copyProperties(bo, entity);
        iPetStoreService.updateById(entity);
    }

    @Override
    public void delete(PetStoreInfoBo bo) {
        iPetStoreService.removeById(bo.getId());
    }

    @Override
    public List<PetStoreVo> list() {
        List<PetStore> list = iPetStoreService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            PetStoreVo vo = new PetStoreVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PetStoreVo> listByCityId(Long cityId) {
        QueryWrapper<PetStore> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(PetStore::getCityId, cityId)
                .orderByDesc(PetStore::getFeatured)
                .orderByAsc(PetStore::getSort);
        List<PetStore> list = iPetStoreService.list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            PetStoreVo vo = new PetStoreVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}

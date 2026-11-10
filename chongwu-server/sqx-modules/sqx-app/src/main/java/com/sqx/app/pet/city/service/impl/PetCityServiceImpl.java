package com.sqx.app.pet.city.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.city.db.service.IPetCityService;
import com.sqx.app.pet.city.domain.bo.*;
import com.sqx.app.pet.city.domain.entity.PetCity;
import com.sqx.app.pet.city.domain.vo.PetCityVo;
import com.sqx.app.pet.city.service.PetCityService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PetCityServiceImpl implements PetCityService {

    @Resource
    private IPetCityService iPetCityService;

    @Override
    public void saveInfo(PetCitySaveBo bo) {
        PetCity entity = new PetCity();
        BeanUtils.copyProperties(bo, entity);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        iPetCityService.save(entity);
    }

    @Override
    public IPage<PetCityVo> pageInfo(PetCityPageBo bo) {
        Page<PetCity> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<PetCity> qw = new QueryWrapper<>();
        qw.lambda().orderByDesc(PetCity::getCreateTime);
        IPage<PetCity> iPage = iPetCityService.page(page, qw);
        List<PetCityVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (PetCity record : iPage.getRecords()) {
                PetCityVo vo = new PetCityVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public PetCityVo info(PetCityInfoBo bo) {
        PetCity entity = iPetCityService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("not found");
        }
        PetCityVo vo = new PetCityVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(PetCityUpdateBo bo) {
        PetCity entity = new PetCity();
        BeanUtils.copyProperties(bo, entity);
        iPetCityService.updateById(entity);
    }

    @Override
    public void delete(PetCityInfoBo bo) {
        iPetCityService.removeById(bo.getId());
    }

    @Override
    public List<PetCityVo> list() {
        List<PetCity> list = iPetCityService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            PetCityVo vo = new PetCityVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PetCityVo> listEnabled() {
        QueryWrapper<PetCity> qw = new QueryWrapper<>();
        qw.lambda().eq(PetCity::getStatus, 1).orderByAsc(PetCity::getSort);
        List<PetCity> list = iPetCityService.list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            PetCityVo vo = new PetCityVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}

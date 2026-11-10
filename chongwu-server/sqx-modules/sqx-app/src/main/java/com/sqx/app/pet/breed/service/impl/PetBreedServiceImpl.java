package com.sqx.app.pet.breed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.breed.db.service.IPetBreedDictService;
import com.sqx.app.pet.breed.domain.bo.*;
import com.sqx.app.pet.breed.domain.entity.PetBreedDict;
import com.sqx.app.pet.breed.domain.vo.PetBreedVo;
import com.sqx.app.pet.breed.service.PetBreedService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PetBreedServiceImpl implements PetBreedService {

    @Resource
    private IPetBreedDictService iPetBreedDictService;

    @Override
    public void saveInfo(PetBreedSaveBo bo) {
        validateUnique(bo.getPetType(), bo.getName(), null);
        PetBreedDict entity = new PetBreedDict();
        BeanUtils.copyProperties(bo, entity);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        iPetBreedDictService.save(entity);
    }

    @Override
    public void updateInfo(PetBreedUpdateBo bo) {
        validateUnique(bo.getPetType(), bo.getName(), bo.getId());
        PetBreedDict entity = new PetBreedDict();
        BeanUtils.copyProperties(bo, entity);
        iPetBreedDictService.updateById(entity);
    }

    @Override
    public void delete(PetBreedInfoBo bo) {
        iPetBreedDictService.removeById(bo.getId());
    }

    @Override
    public IPage<PetBreedVo> pageInfo(PetBreedPageBo bo) {
        Page<PetBreedDict> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<PetBreedDict> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(StringUtils.isNotBlank(bo.getPetType()), PetBreedDict::getPetType, bo.getPetType())
                .like(StringUtils.isNotBlank(bo.getName()), PetBreedDict::getName, bo.getName())
                .eq(Objects.nonNull(bo.getStatus()), PetBreedDict::getStatus, bo.getStatus())
                .orderByAsc(PetBreedDict::getPetType)
                .orderByAsc(PetBreedDict::getSort);
        IPage<PetBreedDict> iPage = iPetBreedDictService.page(page, qw);
        List<PetBreedVo> voList = new ArrayList<>();
        for (PetBreedDict record : iPage.getRecords()) {
            voList.add(toVo(record));
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public List<PetBreedVo> listEnabled(String petType) {
        QueryWrapper<PetBreedDict> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(PetBreedDict::getStatus, 1)
                .eq(StringUtils.isNotBlank(petType), PetBreedDict::getPetType, petType)
                .orderByAsc(PetBreedDict::getSort);
        return iPetBreedDictService.list(qw).stream().map(this::toVo).collect(Collectors.toList());
    }

    private PetBreedVo toVo(PetBreedDict entity) {
        PetBreedVo vo = new PetBreedVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private void validateUnique(String petType, String name, Long excludeId) {
        if (StringUtils.isBlank(petType) || StringUtils.isBlank(name)) {
            return;
        }
        long count = iPetBreedDictService.lambdaQuery()
                .eq(PetBreedDict::getPetType, petType.trim())
                .eq(PetBreedDict::getName, name.trim())
                .ne(excludeId != null, PetBreedDict::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BizException("该类型下品种已存在");
        }
    }
}

package com.sqx.app.pet.strategy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.strategy.db.service.IPetStrategyCategoryService;
import com.sqx.app.pet.strategy.db.service.IPetStrategyService;
import com.sqx.app.pet.strategy.db.service.IStrategyReadLogService;
import com.sqx.app.pet.strategy.domain.bo.*;
import com.sqx.app.pet.strategy.domain.entity.PetStrategy;
import com.sqx.app.pet.strategy.domain.entity.PetStrategyCategory;
import com.sqx.app.pet.strategy.domain.entity.StrategyReadLog;
import com.sqx.app.pet.strategy.domain.vo.PetStrategyCategoryVo;
import com.sqx.app.pet.strategy.domain.vo.PetStrategyVo;
import com.sqx.app.pet.strategy.service.PetStrategyService;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PetStrategyServiceImpl implements PetStrategyService {

    @Resource
    private IPetStrategyService iPetStrategyService;
    @Resource
    private IPetStrategyCategoryService iPetStrategyCategoryService;
    @Resource
    private IStrategyReadLogService iStrategyReadLogService;

    @Override
    public void saveInfo(PetStrategySaveBo bo) {
        PetStrategy entity = new PetStrategy();
        BeanUtils.copyProperties(bo, entity);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getViewCount() == null) {
            entity.setViewCount(0);
        }
        if (entity.getLikeCount() == null) {
            entity.setLikeCount(0);
        }
        iPetStrategyService.save(entity);
    }

    @Override
    public IPage<PetStrategyVo> pageInfo(PetStrategyPageBo bo) {
        Page<PetStrategy> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<PetStrategy> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(bo.getStatus() != null, PetStrategy::getStatus, bo.getStatus())
                .eq(bo.getCategoryId() != null, PetStrategy::getCategoryId, bo.getCategoryId())
                .like(StringUtils.isNotBlank(bo.getTitle()), PetStrategy::getTitle, bo.getTitle())
                .like(StringUtils.isNotBlank(bo.getTag()), PetStrategy::getTag, bo.getTag())
                .orderByAsc(PetStrategy::getSort)
                .orderByDesc(PetStrategy::getCreateTime);
        IPage<PetStrategy> iPage = iPetStrategyService.page(page, qw);
        Map<Long, String> categoryNameMap = loadCategoryNameMap(iPage.getRecords());
        List<PetStrategyVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (PetStrategy record : iPage.getRecords()) {
                PetStrategyVo vo = toVo(record, categoryNameMap);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public PetStrategyVo info(PetStrategyInfoBo bo) {
        return infoWithRead(null, bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PetStrategyVo infoWithRead(Long userId, PetStrategyInfoBo bo) {
        PetStrategy entity = iPetStrategyService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        if (userId != null) {
            boolean exists = iStrategyReadLogService.lambdaQuery()
                    .eq(StrategyReadLog::getStrategyId, bo.getId())
                    .eq(StrategyReadLog::getUserId, userId)
                    .exists();
            if (!exists) {
                StrategyReadLog log = new StrategyReadLog();
                log.setStrategyId(bo.getId());
                log.setUserId(userId);
                iStrategyReadLogService.save(log);
                entity.setViewCount((entity.getViewCount() == null ? 0 : entity.getViewCount()) + 1);
                iPetStrategyService.updateById(entity);
            }
        }
        Map<Long, String> categoryNameMap = loadCategoryNameMap(Collections.singletonList(entity));
        return toVo(entity, categoryNameMap);
    }

    @Override
    public void updateInfo(PetStrategyUpdateBo bo) {
        PetStrategy entity = iPetStrategyService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        BeanUtils.copyProperties(bo, entity);
        iPetStrategyService.updateById(entity);
    }

    @Override
    public void delete(PetStrategyInfoBo bo) {
        iPetStrategyService.removeById(bo.getId());
    }

    @Override
    public List<PetStrategyVo> list() {
        QueryWrapper<PetStrategy> qw = new QueryWrapper<>();
        qw.lambda().eq(PetStrategy::getStatus, 1).orderByAsc(PetStrategy::getSort);
        List<PetStrategy> list = iPetStrategyService.list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, String> categoryNameMap = loadCategoryNameMap(list);
        return list.stream().map(e -> toVo(e, categoryNameMap)).collect(Collectors.toList());
    }

    @Override
    public List<PetStrategyCategoryVo> categories() {
        QueryWrapper<PetStrategyCategory> qw = new QueryWrapper<>();
        qw.lambda().eq(PetStrategyCategory::getStatus, 1).orderByAsc(PetStrategyCategory::getSort);
        return iPetStrategyCategoryService.list(qw).stream().map(this::toCategoryVo).collect(Collectors.toList());
    }

    @Override
    public List<PetStrategyCategoryVo> adminCategories() {
        QueryWrapper<PetStrategyCategory> qw = new QueryWrapper<>();
        qw.lambda().orderByAsc(PetStrategyCategory::getSort).orderByAsc(PetStrategyCategory::getId);
        return iPetStrategyCategoryService.list(qw).stream().map(this::toCategoryVo).collect(Collectors.toList());
    }

    private PetStrategyCategoryVo toCategoryVo(PetStrategyCategory category) {
        PetStrategyCategoryVo vo = new PetStrategyCategoryVo();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    private PetStrategyVo toVo(PetStrategy entity, Map<Long, String> categoryNameMap) {
        PetStrategyVo vo = new PetStrategyVo();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getCategoryId() != null && categoryNameMap != null) {
            vo.setCategoryName(categoryNameMap.get(entity.getCategoryId()));
        }
        return vo;
    }

    private Map<Long, String> loadCategoryNameMap(List<PetStrategy> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> categoryIds = records.stream()
                .map(PetStrategy::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return iPetStrategyCategoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(PetStrategyCategory::getId, PetStrategyCategory::getName, (a, b) -> a));
    }
}

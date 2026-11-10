package com.sqx.app.pet.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.marketing.db.service.IOperationBannerService;
import com.sqx.app.pet.marketing.domain.bo.*;
import com.sqx.app.pet.marketing.domain.entity.OperationBanner;
import com.sqx.app.pet.marketing.domain.vo.OperationBannerVo;
import com.sqx.app.pet.marketing.service.OperationBannerService;
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
public class OperationBannerServiceImpl implements OperationBannerService {

    @Resource
    private IOperationBannerService iOperationBannerService;

    @Override
    public void saveInfo(OperationBannerSaveBo bo) {
        OperationBanner entity = new OperationBanner();
        BeanUtils.copyProperties(bo, entity);
        if (StringUtils.isBlank(entity.getPositionCode())) {
            entity.setPositionCode("shop");
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        iOperationBannerService.save(entity);
    }

    @Override
    public IPage<OperationBannerVo> pageInfo(OperationBannerPageBo bo) {
        Page<OperationBanner> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<OperationBanner> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(StringUtils.isNotBlank(bo.getPositionCode()), OperationBanner::getPositionCode, bo.getPositionCode())
                .like(StringUtils.isNotBlank(bo.getName()), OperationBanner::getName, bo.getName())
                .eq(Objects.nonNull(bo.getStatus()), OperationBanner::getStatus, bo.getStatus())
                .orderByAsc(OperationBanner::getSort)
                .orderByDesc(OperationBanner::getCreateTime);
        IPage<OperationBanner> iPage = iOperationBannerService.page(page, qw);
        List<OperationBannerVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (OperationBanner record : iPage.getRecords()) {
                OperationBannerVo vo = new OperationBannerVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public OperationBannerVo info(OperationBannerInfoBo bo) {
        OperationBanner entity = iOperationBannerService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("not found");
        }
        OperationBannerVo vo = new OperationBannerVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(OperationBannerUpdateBo bo) {
        OperationBanner entity = new OperationBanner();
        BeanUtils.copyProperties(bo, entity);
        iOperationBannerService.updateById(entity);
    }

    @Override
    public void delete(OperationBannerInfoBo bo) {
        iOperationBannerService.removeById(bo.getId());
    }

    @Override
    public List<OperationBannerVo> list() {
        List<OperationBanner> list = iOperationBannerService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            OperationBannerVo vo = new OperationBannerVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<OperationBannerVo> listByPositionCode(String positionCode) {
        QueryWrapper<OperationBanner> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(OperationBanner::getPositionCode, positionCode)
                .eq(OperationBanner::getStatus, 1)
                .orderByAsc(OperationBanner::getSort);
        List<OperationBanner> list = iOperationBannerService.list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            OperationBannerVo vo = new OperationBannerVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void incrementClick(Long bannerId) {
        if (bannerId == null) {
            return;
        }
        OperationBanner entity = iOperationBannerService.getById(bannerId);
        if (entity == null) {
            return;
        }
        int count = entity.getClickCount() == null ? 0 : entity.getClickCount();
        entity.setClickCount(count + 1);
        iOperationBannerService.updateById(entity);
    }
}

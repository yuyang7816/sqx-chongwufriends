package com.sqx.app.pet.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.community.db.service.ICommunityPostService;
import com.sqx.app.pet.community.domain.bo.*;
import com.sqx.app.pet.community.domain.entity.CommunityPost;
import com.sqx.app.pet.community.domain.vo.CommunityPostVo;
import com.sqx.app.pet.community.service.CommunityPostService;
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
public class CommunityPostServiceImpl implements CommunityPostService {

    @Resource
    private ICommunityPostService iCommunityPostService;

    @Override
    public void saveInfo(CommunityPostSaveBo bo) {
        CommunityPost entity = new CommunityPost();
        BeanUtils.copyProperties(bo, entity);
        iCommunityPostService.save(entity);
    }

    @Override
    public IPage<CommunityPostVo> pageInfo(CommunityPostPageBo bo) {
        Page<CommunityPost> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CommunityPost> qw = new QueryWrapper<>();
        qw.lambda().orderByDesc(CommunityPost::getCreateTime);
        IPage<CommunityPost> iPage = iCommunityPostService.page(page, qw);
        List<CommunityPostVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (CommunityPost record : iPage.getRecords()) {
                CommunityPostVo vo = new CommunityPostVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public CommunityPostVo info(CommunityPostInfoBo bo) {
        CommunityPost entity = iCommunityPostService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("not found");
        }
        CommunityPostVo vo = new CommunityPostVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(CommunityPostUpdateBo bo) {
        CommunityPost entity = new CommunityPost();
        BeanUtils.copyProperties(bo, entity);
        iCommunityPostService.updateById(entity);
    }

    @Override
    public void delete(CommunityPostInfoBo bo) {
        iCommunityPostService.removeById(bo.getId());
    }

    @Override
    public List<CommunityPostVo> list() {
        List<CommunityPost> list = iCommunityPostService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            CommunityPostVo vo = new CommunityPostVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}

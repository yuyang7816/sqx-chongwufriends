package com.sqx.app.pet.island.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.support.UserIdResolveHelper;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.sqx.app.pet.island.db.service.IIslandPetService;
import com.sqx.app.pet.island.domain.bo.*;
import com.sqx.app.pet.island.domain.entity.IslandPet;
import com.sqx.app.pet.island.domain.vo.IslandPetVo;
import com.sqx.app.pet.island.service.IslandPetService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class IslandPetServiceImpl implements IslandPetService {

    @Resource
    private IIslandPetService iIslandPetService;
    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private UserIdResolveHelper userIdResolveHelper;

    @Override
    public void saveInfo(IslandPetSaveBo bo) {
        IslandPet entity = new IslandPet();
        BeanUtils.copyProperties(bo, entity);
        iIslandPetService.save(entity);
    }

    @Override
    public IPage<IslandPetVo> pageInfo(IslandPetPageBo bo) {
        Page<IslandPet> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        List<Long> userIds = userIdResolveHelper.resolveOptional(bo.getUserId(), bo.getPhone(), bo.getUsername());
        if (userIds != null && userIds.isEmpty()) {
            return PageUtil.copyPage(page, Collections.emptyList());
        }
        QueryWrapper<IslandPet> qw = new QueryWrapper<>();
        qw.lambda()
                .in(userIds != null, IslandPet::getUserId, userIds)
                .orderByDesc(IslandPet::getCreateTime);
        IPage<IslandPet> iPage = iIslandPetService.page(page, qw);
        List<IslandPetVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (IslandPet record : iPage.getRecords()) {
                IslandPetVo vo = new IslandPetVo();
                BeanUtils.copyProperties(record, vo);
                fillResidentPetInfo(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public IslandPetVo info(IslandPetInfoBo bo) {
        IslandPet entity = iIslandPetService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("not found");
        }
        IslandPetVo vo = new IslandPetVo();
        BeanUtils.copyProperties(entity, vo);
        fillResidentPetInfo(entity, vo);
        return vo;
    }

    /** 从常驻列表/常驻ID解析宠物档案，补全名称、品种、性别等展示字段 */
    private void fillResidentPetInfo(IslandPet record, IslandPetVo vo) {
        Long mainPetId = resolveMainResidentPetId(record);
        if (mainPetId == null) {
            return;
        }
        PetArchive archive = iPetArchiveService.getById(mainPetId);
        if (archive == null) {
            return;
        }
        vo.setResidentPetId(mainPetId);
        vo.setPetName(archive.getName());
        vo.setPetAvatar(archive.getAvatarUrl());
        vo.setPetType(archive.getPetType());
        vo.setBreed(archive.getBreed());
        vo.setGender(archive.getGender());
    }

    private Long resolveMainResidentPetId(IslandPet pet) {
        if (StringUtils.isNotBlank(pet.getResidentPetIds())) {
            return Arrays.stream(pet.getResidentPetIds().split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Long::valueOf)
                    .findFirst()
                    .orElse(pet.getResidentPetId());
        }
        return pet.getResidentPetId();
    }

    @Override
    public void updateInfo(IslandPetUpdateBo bo) {
        if (bo == null || bo.getId() == null) {
            return;
        }
        iIslandPetService.lambdaUpdate()
                .eq(IslandPet::getId, bo.getId())
                .set(bo.getHunger() != null, IslandPet::getHunger, bo.getHunger())
                .set(bo.getClean() != null, IslandPet::getClean, bo.getClean())
                .set(bo.getMood() != null, IslandPet::getMood, bo.getMood())
                .set(bo.getVitality() != null, IslandPet::getVitality, bo.getVitality())
                .set(bo.getIntimacy() != null, IslandPet::getIntimacy, bo.getIntimacy())
                .set(bo.getDailyProgress() != null, IslandPet::getDailyProgress, bo.getDailyProgress())
                .set(bo.getResidentPetId() != null, IslandPet::getResidentPetId, bo.getResidentPetId())
                .set(bo.getResidentPetIds() != null, IslandPet::getResidentPetIds, bo.getResidentPetIds())
                .set(IslandPet::getFeedCooldownUntil, bo.getFeedCooldownUntil())
                .set(IslandPet::getFoodBowlFilled, bo.getFoodBowlFilled() == null ? 0 : bo.getFoodBowlFilled())
                .set(IslandPet::getWaterBowlFilled, bo.getWaterBowlFilled() == null ? 0 : bo.getWaterBowlFilled())
                .update();
    }

    @Override
    public void delete(IslandPetInfoBo bo) {
        iIslandPetService.removeById(bo.getId());
    }

    @Override
    public List<IslandPetVo> list() {
        List<IslandPet> list = iIslandPetService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            IslandPetVo vo = new IslandPetVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void initForUser(Long userId) {
        QueryWrapper<IslandPet> qw = new QueryWrapper<>();
        qw.lambda().eq(IslandPet::getUserId, userId);
        if (iIslandPetService.count(qw) > 0) {
            return;
        }
        IslandPet islandPet = new IslandPet();
        islandPet.setUserId(userId);
        islandPet.setHunger(50);
        islandPet.setClean(50);
        islandPet.setMood(50);
        islandPet.setVitality(50);
        islandPet.setDailyProgress(50);
        islandPet.setStatDate(java.time.LocalDate.now());
        islandPet.setFoodBowlFilled(0);
        islandPet.setWaterBowlFilled(0);
        islandPet.setIntimacy(0);
        iIslandPetService.save(islandPet);
    }
}

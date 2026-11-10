package com.sqx.app.pet.adopt.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.pet.adopt.db.service.IAdoptApplicationService;
import com.sqx.app.pet.adopt.domain.bo.*;
import com.sqx.app.pet.adopt.domain.entity.AdoptApplication;
import com.sqx.app.pet.adopt.domain.vo.AdoptApplicationVo;
import com.sqx.app.pet.adopt.service.AdoptApplicationService;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.island.db.service.IIslandPetService;
import com.sqx.app.pet.island.domain.entity.IslandPet;
import com.sqx.app.pet.island.service.IslandPetService;
import com.sqx.app.pet.userpet.service.UserPetService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdoptApplicationServiceImpl implements AdoptApplicationService {

    private static final List<Integer> ACTIVE_STATUSES = Arrays.asList(1, 2, 3, 4, 5);

    private static final Map<Integer, String> STATUS_LABELS = new HashMap<>();

    static {
        // 统一三态：申请中 / 通过 / 拒绝（历史 2~5 也展示为申请中）
        STATUS_LABELS.put(1, "申请中");
        STATUS_LABELS.put(2, "申请中");
        STATUS_LABELS.put(3, "申请中");
        STATUS_LABELS.put(4, "申请中");
        STATUS_LABELS.put(5, "申请中");
        STATUS_LABELS.put(6, "通过");
        STATUS_LABELS.put(7, "拒绝");
    }

    private static String resolveStatusLabel(Integer status) {
        if (status == null) {
            return "未知";
        }
        if (status >= 1 && status <= 5) {
            return "申请中";
        }
        return STATUS_LABELS.getOrDefault(status, "未知");
    }

    @Resource
    private IAdoptApplicationService iAdoptApplicationService;
    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private IMessagesService iMessagesService;
    @Resource
    private UserPetService userPetService;
    @Resource
    private IslandPetService islandPetService;
    @Resource
    private IIslandPetService iIslandPetService;

    @Override
    public void saveInfo(AdoptApplicationSaveBo bo) {
        throw new BizException("请使用用户端提交接口");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdoptApplicationVo submitForUser(Long userId, AdoptApplicationSaveBo bo) {
        PetArchive pet = iPetArchiveService.getById(bo.getPetId());
        if (pet == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        if (!Objects.equals(pet.getAdoptable(), 1)) {
            throw new BizException("该宠物暂不可领养");
        }
        if (!Objects.equals(pet.getAuditStatus(), 1)) {
            throw new BizException("宠物档案未通过审核");
        }
        boolean exists = iAdoptApplicationService.lambdaQuery()
                .eq(AdoptApplication::getUserId, userId)
                .eq(AdoptApplication::getPetId, bo.getPetId())
                .in(AdoptApplication::getStatus, ACTIVE_STATUSES)
                .exists();
        if (exists) {
            throw new BizException("您已有进行中的领养申请");
        }
        AdoptApplication entity = new AdoptApplication();
        BeanUtils.copyProperties(bo, entity);
        entity.setUserId(userId);
        entity.setStatus(1);
        if (StringUtils.isBlank(entity.getRealName())) {
            entity.setRealName(bo.getPetName());
        }
        entity.setFormJson(JSON.toJSONString(bo));
        if (StringUtils.isBlank(entity.getReason())) {
            entity.setReason(buildReasonSummary(bo));
        }
        iAdoptApplicationService.save(entity);
        sendAdoptNotice(userId, entity.getId(), "领养申请已提交",
                "您申请领养「" + pet.getName() + "」已提交，请耐心等待审核。",
                "/packageA/adopt/mine");
        return toVo(entity, pet.getName());
    }

    private String buildReasonSummary(AdoptApplicationSaveBo bo) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(bo.getHousing())) {
            sb.append("居住条件:").append(bo.getHousing()).append("；");
        }
        if (bo.getHasExperience() != null) {
            sb.append("养宠经验:").append(Boolean.TRUE.equals(bo.getHasExperience()) ? "有" : "无").append("；");
        }
        if (bo.getHasOtherPet() != null) {
            sb.append("其他宠物:").append(Boolean.TRUE.equals(bo.getHasOtherPet()) ? "有" : "无").append("；");
        }
        if (StringUtils.isNotBlank(bo.getChallenge())) {
            sb.append("挑战:").append(bo.getChallenge());
        }
        return sb.length() == 0 ? "领养申请" : sb.toString();
    }

    @Override
    public IPage<AdoptApplicationVo> pageInfo(AdoptApplicationPageBo bo) {
        Page<AdoptApplication> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<AdoptApplication> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(bo.getUserId() != null, AdoptApplication::getUserId, bo.getUserId())
                .eq(bo.getStatus() != null, AdoptApplication::getStatus, bo.getStatus())
                .eq(bo.getPetId() != null, AdoptApplication::getPetId, bo.getPetId())
                .like(StringUtils.isNotBlank(bo.getPhone()), AdoptApplication::getPhone, bo.getPhone())
                .orderByDesc(AdoptApplication::getCreateTime);
        IPage<AdoptApplication> iPage = iAdoptApplicationService.page(page, qw);
        List<AdoptApplicationVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            Map<Long, String> nameMap = loadPetNameMap(iPage.getRecords());
            for (AdoptApplication record : iPage.getRecords()) {
                voList.add(toVo(record, nameMap.get(record.getPetId())));
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    private Map<Long, String> loadPetNameMap(List<AdoptApplication> records) {
        Set<Long> petIds = records.stream()
                .map(AdoptApplication::getPetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (petIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return iPetArchiveService.listByIds(petIds).stream()
                .collect(Collectors.toMap(PetArchive::getId, PetArchive::getName, (a, b) -> a));
    }

    @Override
    public AdoptApplicationVo info(AdoptApplicationInfoBo bo) {
        AdoptApplication entity = iAdoptApplicationService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        String petName = null;
        if (entity.getPetId() != null) {
            PetArchive pet = iPetArchiveService.getById(entity.getPetId());
            if (pet != null) {
                petName = pet.getName();
            }
        }
        return toVo(entity, petName);
    }

    @Override
    public AdoptApplicationVo myInfo(Long userId, AdoptApplicationInfoBo bo) {
        AdoptApplicationVo vo = info(bo);
        if (!Objects.equals(vo.getUserId(), userId)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(AdoptApplicationUpdateBo bo) {
        AdoptApplication old = iAdoptApplicationService.getById(bo.getId());
        if (old == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        if (bo.getStatus() != null && Objects.equals(bo.getStatus(), 7)
                && StringUtils.isBlank(bo.getRejectReason())) {
            throw new BizException("请填写拒绝原因");
        }
        AdoptApplication entity = new AdoptApplication();
        BeanUtils.copyProperties(bo, entity);
        iAdoptApplicationService.updateById(entity);

        Integer targetStatus = bo.getStatus() != null ? bo.getStatus() : old.getStatus();
        // 通过时始终补写归属（兼容历史已通过但未写入 user_pet 的情况）
        if (Objects.equals(targetStatus, 6) && old.getPetId() != null) {
            PetArchive pet = new PetArchive();
            pet.setId(old.getPetId());
            pet.setAdoptable(0);
            pet.setStatusLabel("已领养");
            iPetArchiveService.updateById(pet);
            userPetService.grantAdoptedPet(old.getUserId(), old.getPetId(), old.getId());
            ensureDefaultResident(old.getUserId(), old.getPetId());
            if (bo.getStatus() != null && !Objects.equals(bo.getStatus(), old.getStatus())) {
                rejectOtherActiveApplications(old.getPetId(), old.getId());
            }
        }

        if (bo.getStatus() == null || Objects.equals(bo.getStatus(), old.getStatus())) {
            return;
        }
        notifyStatusChange(old, bo.getStatus(), bo.getRejectReason());
    }

    private void ensureDefaultResident(Long userId, Long petId) {
        if (userId == null || petId == null) {
            return;
        }
        islandPetService.initForUser(userId);
        IslandPet island = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        if (island == null) {
            return;
        }
        if (island.getResidentPetId() != null) {
            return;
        }
        iIslandPetService.lambdaUpdate()
                .eq(IslandPet::getId, island.getId())
                .set(IslandPet::getResidentPetId, petId)
                .set(IslandPet::getResidentPetIds, String.valueOf(petId))
                .update();
    }

    private void rejectOtherActiveApplications(Long petId, Long keepApplicationId) {
        List<AdoptApplication> others = iAdoptApplicationService.lambdaQuery()
                .eq(AdoptApplication::getPetId, petId)
                .in(AdoptApplication::getStatus, ACTIVE_STATUSES)
                .ne(keepApplicationId != null, AdoptApplication::getId, keepApplicationId)
                .list();
        if (CollectionUtils.isEmpty(others)) {
            return;
        }
        for (AdoptApplication other : others) {
            AdoptApplication update = new AdoptApplication();
            update.setId(other.getId());
            update.setStatus(7);
            update.setRejectReason("该宠物已被其他用户领养");
            iAdoptApplicationService.updateById(update);
            notifyStatusChange(other, 7, "该宠物已被其他用户领养");
        }
    }

    private void notifyStatusChange(AdoptApplication application, Integer newStatus, String rejectReason) {
        if (application.getUserId() == null || newStatus == null) {
            return;
        }
        String petName = "宠物";
        if (application.getPetId() != null) {
            PetArchive pet = iPetArchiveService.getById(application.getPetId());
            if (pet != null && StringUtils.isNotBlank(pet.getName())) {
                petName = pet.getName();
            }
        }
        String statusLabel = resolveStatusLabel(newStatus);
        String title = "领养申请" + statusLabel;
        String content;
        if (Objects.equals(newStatus, 7)) {
            content = "您申请领养「" + petName + "」未通过审核。原因：" + StringUtils.defaultString(rejectReason, "请联系客服了解详情");
        } else if (Objects.equals(newStatus, 6)) {
            content = "恭喜！您申请领养「" + petName + "」已通过，宠物已加入您的岛宅，可前往常驻宠物设置查看。";
        } else {
            content = "您申请领养「" + petName + "」状态为：" + statusLabel + "，请耐心等待审核。";
        }
        sendAdoptNotice(application.getUserId(), application.getId(), title, content, "/packageA/adopt/mine");
    }

    private void sendAdoptNotice(Long userId, Long applicationId, String title, String content, String url) {
        Messages msg = new Messages();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setUrl(url);
        msg.setType(2);
        msg.setDataId(applicationId);
        msg.setIsRead(0);
        iMessagesService.save(msg);
    }

    @Override
    public void delete(AdoptApplicationInfoBo bo) {
        iAdoptApplicationService.removeById(bo.getId());
    }

    @Override
    public List<AdoptApplicationVo> list() {
        List<AdoptApplication> list = iAdoptApplicationService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, String> nameMap = loadPetNameMap(list);
        return list.stream()
                .map(item -> toVo(item, nameMap.get(item.getPetId())))
                .collect(Collectors.toList());
    }

    private AdoptApplicationVo toVo(AdoptApplication entity, String petName) {
        AdoptApplicationVo vo = new AdoptApplicationVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setPetName(petName);
        vo.setStatusLabel(resolveStatusLabel(entity.getStatus()));
        parsePetImage(entity, vo);
        return vo;
    }

    private void parsePetImage(AdoptApplication entity, AdoptApplicationVo vo) {
        if (StringUtils.isBlank(entity.getFormJson())) {
            return;
        }
        try {
            AdoptApplicationSaveBo form = JSON.parseObject(entity.getFormJson(), AdoptApplicationSaveBo.class);
            if (form != null && StringUtils.isNotBlank(form.getPetImage())) {
                vo.setPetImage(form.getPetImage());
            }
        } catch (Exception ignored) {
            // ignore malformed form json
        }
    }
}

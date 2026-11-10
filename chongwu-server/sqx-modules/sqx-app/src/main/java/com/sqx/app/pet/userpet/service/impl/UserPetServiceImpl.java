package com.sqx.app.pet.userpet.service.impl;

import com.sqx.app.pet.adopt.db.service.IAdoptApplicationService;
import com.sqx.app.pet.adopt.domain.entity.AdoptApplication;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;
import com.sqx.app.pet.store.db.service.IPetStoreService;
import com.sqx.app.pet.store.domain.entity.PetStore;
import com.sqx.app.pet.userpet.db.service.IUserPetService;
import com.sqx.app.pet.userpet.domain.entity.UserPet;
import com.sqx.app.pet.userpet.service.UserPetService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserPetServiceImpl implements UserPetService {

    @Resource
    private IUserPetService iUserPetService;
    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private IPetStoreService iPetStoreService;
    @Lazy
    @Resource
    private IAdoptApplicationService iAdoptApplicationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantAdoptedPet(Long userId, Long petId, Long applicationId) {
        if (userId == null || petId == null) {
            return;
        }
        boolean exists = iUserPetService.lambdaQuery()
                .eq(UserPet::getUserId, userId)
                .eq(UserPet::getPetId, petId)
                .eq(UserPet::getStatus, 1)
                .exists();
        if (exists) {
            return;
        }
        UserPet entity = new UserPet();
        entity.setUserId(userId);
        entity.setPetId(petId);
        entity.setApplicationId(applicationId);
        entity.setSource("adopt");
        entity.setStatus(1);
        iUserPetService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromCompletedAdopts(Long userId) {
        if (userId == null) {
            return;
        }
        List<AdoptApplication> completed = iAdoptApplicationService.lambdaQuery()
                .eq(AdoptApplication::getUserId, userId)
                .eq(AdoptApplication::getStatus, 6)
                .list();
        if (CollectionUtils.isEmpty(completed)) {
            return;
        }
        for (AdoptApplication app : completed) {
            grantAdoptedPet(userId, app.getPetId(), app.getId());
        }
    }

    @Override
    public List<PetArchiveVo> listMyPets(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        // 先同步已通过但未写入归属的历史数据
        syncFromCompletedAdopts(userId);
        List<UserPet> ownerships = iUserPetService.lambdaQuery()
                .eq(UserPet::getUserId, userId)
                .eq(UserPet::getStatus, 1)
                .orderByAsc(UserPet::getId)
                .list();
        if (CollectionUtils.isEmpty(ownerships)) {
            return Collections.emptyList();
        }
        List<Long> petIds = ownerships.stream()
                .map(UserPet::getPetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (petIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, PetArchive> archiveMap = iPetArchiveService.listByIds(petIds).stream()
                .collect(Collectors.toMap(PetArchive::getId, a -> a, (a, b) -> a));
        Set<Long> storeIds = archiveMap.values().stream()
                .map(PetArchive::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> storeNameMap = storeIds.isEmpty()
                ? Collections.emptyMap()
                : iPetStoreService.listByIds(storeIds).stream()
                .collect(Collectors.toMap(PetStore::getId, PetStore::getName, (a, b) -> a));

        List<PetArchiveVo> result = new ArrayList<>();
        for (Long petId : petIds) {
            PetArchive archive = archiveMap.get(petId);
            if (archive == null) {
                continue;
            }
            PetArchiveVo vo = new PetArchiveVo();
            BeanUtils.copyProperties(archive, vo);
            if (archive.getStoreId() != null) {
                vo.setStoreName(storeNameMap.get(archive.getStoreId()));
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public boolean ownsPet(Long userId, Long petId) {
        if (userId == null || petId == null) {
            return false;
        }
        return iUserPetService.lambdaQuery()
                .eq(UserPet::getUserId, userId)
                .eq(UserPet::getPetId, petId)
                .eq(UserPet::getStatus, 1)
                .exists();
    }

    @Override
    public Long getFirstPetId(Long userId) {
        if (userId == null) {
            return null;
        }
        UserPet first = iUserPetService.lambdaQuery()
                .eq(UserPet::getUserId, userId)
                .eq(UserPet::getStatus, 1)
                .orderByAsc(UserPet::getId)
                .last("LIMIT 1")
                .one();
        return first == null ? null : first.getPetId();
    }
}

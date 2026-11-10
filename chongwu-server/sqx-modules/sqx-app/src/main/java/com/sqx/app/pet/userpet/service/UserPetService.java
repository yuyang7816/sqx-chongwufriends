package com.sqx.app.pet.userpet.service;

import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;

import java.util.List;

public interface UserPetService {
    /** 领养完成后授予用户宠物归属 */
    void grantAdoptedPet(Long userId, Long petId, Long applicationId);

    /** 将已通过的领养申请同步到用户宠物归属（修复历史遗漏） */
    void syncFromCompletedAdopts(Long userId);

    /** 当前用户有效自有宠物档案列表（按获得时间升序，第一只为默认） */
    List<PetArchiveVo> listMyPets(Long userId);

    /** 是否拥有该宠物 */
    boolean ownsPet(Long userId, Long petId);

    /** 用户第一只自有宠物 ID，无则 null */
    Long getFirstPetId(Long userId);
}

package com.sqx.app.pet.store.service;

import com.sqx.app.pet.store.domain.bo.*;
import com.sqx.app.pet.store.domain.vo.PetStoreVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface PetStoreService {
    void saveInfo(PetStoreSaveBo bo);
    IPage<PetStoreVo> pageInfo(PetStorePageBo bo);
    PetStoreVo info(PetStoreInfoBo bo);
    void updateInfo(PetStoreUpdateBo bo);
    void delete(PetStoreInfoBo bo);
    List<PetStoreVo> list();

    List<PetStoreVo> listByCityId(Long cityId);
}

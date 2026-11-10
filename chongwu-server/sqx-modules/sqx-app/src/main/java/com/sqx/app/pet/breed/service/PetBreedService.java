package com.sqx.app.pet.breed.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.breed.domain.bo.*;
import com.sqx.app.pet.breed.domain.vo.PetBreedVo;

import java.util.List;

public interface PetBreedService {
    void saveInfo(PetBreedSaveBo bo);

    void updateInfo(PetBreedUpdateBo bo);

    void delete(PetBreedInfoBo bo);

    IPage<PetBreedVo> pageInfo(PetBreedPageBo bo);

    List<PetBreedVo> listEnabled(String petType);
}

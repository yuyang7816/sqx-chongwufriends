package com.sqx.app.pet.island.service;

import com.sqx.app.pet.island.domain.bo.*;
import com.sqx.app.pet.island.domain.vo.IslandPetVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface IslandPetService {
    void saveInfo(IslandPetSaveBo bo);
    IPage<IslandPetVo> pageInfo(IslandPetPageBo bo);
    IslandPetVo info(IslandPetInfoBo bo);
    void updateInfo(IslandPetUpdateBo bo);
    void delete(IslandPetInfoBo bo);
    List<IslandPetVo> list();

    void initForUser(Long userId);
}

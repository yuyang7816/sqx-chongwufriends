package com.sqx.app.pet.strategy.service;

import com.sqx.app.pet.strategy.domain.bo.*;
import com.sqx.app.pet.strategy.domain.vo.PetStrategyCategoryVo;
import com.sqx.app.pet.strategy.domain.vo.PetStrategyVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface PetStrategyService {
    void saveInfo(PetStrategySaveBo bo);
    IPage<PetStrategyVo> pageInfo(PetStrategyPageBo bo);
    PetStrategyVo info(PetStrategyInfoBo bo);
    void updateInfo(PetStrategyUpdateBo bo);
    void delete(PetStrategyInfoBo bo);
    List<PetStrategyVo> list();

    List<PetStrategyCategoryVo> categories();

    /** 管理端分类（含停用） */
    List<PetStrategyCategoryVo> adminCategories();

    PetStrategyVo infoWithRead(Long userId, PetStrategyInfoBo bo);
}

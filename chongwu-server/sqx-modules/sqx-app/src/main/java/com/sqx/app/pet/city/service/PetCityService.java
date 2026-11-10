package com.sqx.app.pet.city.service;

import com.sqx.app.pet.city.domain.bo.*;
import com.sqx.app.pet.city.domain.vo.PetCityVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface PetCityService {
    void saveInfo(PetCitySaveBo bo);
    IPage<PetCityVo> pageInfo(PetCityPageBo bo);
    PetCityVo info(PetCityInfoBo bo);
    void updateInfo(PetCityUpdateBo bo);
    void delete(PetCityInfoBo bo);
    List<PetCityVo> list();

    List<PetCityVo> listEnabled();
}

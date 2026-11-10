package com.sqx.app.pet.adopt.service;

import com.sqx.app.pet.adopt.domain.bo.*;
import com.sqx.app.pet.adopt.domain.vo.AdoptApplicationVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface AdoptApplicationService {
    void saveInfo(AdoptApplicationSaveBo bo);
    IPage<AdoptApplicationVo> pageInfo(AdoptApplicationPageBo bo);
    AdoptApplicationVo info(AdoptApplicationInfoBo bo);
    void updateInfo(AdoptApplicationUpdateBo bo);
    void delete(AdoptApplicationInfoBo bo);
    List<AdoptApplicationVo> list();

    AdoptApplicationVo submitForUser(Long userId, AdoptApplicationSaveBo bo);

    AdoptApplicationVo myInfo(Long userId, AdoptApplicationInfoBo bo);
}

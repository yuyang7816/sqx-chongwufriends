package com.sqx.app.pet.home.service;

import com.sqx.app.pet.home.domain.bo.HomeSocialUpdateBo;
import com.sqx.app.pet.home.domain.vo.HomeConfigVo;
import com.sqx.app.pet.home.domain.vo.HomeSocialEntryVo;

public interface HomeConfigService {
    HomeConfigVo getConfig();

    HomeSocialEntryVo getSocialAdmin();

    void updateSocial(HomeSocialUpdateBo bo);
}

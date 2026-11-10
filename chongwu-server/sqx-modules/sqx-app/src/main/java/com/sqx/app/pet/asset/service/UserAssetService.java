package com.sqx.app.pet.asset.service;

import com.sqx.app.pet.asset.domain.bo.*;
import com.sqx.app.pet.asset.domain.vo.UserAssetVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface UserAssetService {
    void saveInfo(UserAssetSaveBo bo);
    IPage<UserAssetVo> pageInfo(UserAssetPageBo bo);
    UserAssetVo info(UserAssetInfoBo bo);
    void updateInfo(UserAssetUpdateBo bo);
    void delete(UserAssetInfoBo bo);
    List<UserAssetVo> list();

    void initForUser(Long userId);

    UserAssetVo infoByUserId(Long userId);

    void adjust(UserAssetAdjustBo bo);
}

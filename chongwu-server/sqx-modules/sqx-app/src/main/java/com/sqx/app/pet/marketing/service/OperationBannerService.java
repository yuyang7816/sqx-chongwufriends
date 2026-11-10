package com.sqx.app.pet.marketing.service;

import com.sqx.app.pet.marketing.domain.bo.*;
import com.sqx.app.pet.marketing.domain.vo.OperationBannerVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface OperationBannerService {
    void saveInfo(OperationBannerSaveBo bo);
    IPage<OperationBannerVo> pageInfo(OperationBannerPageBo bo);
    OperationBannerVo info(OperationBannerInfoBo bo);
    void updateInfo(OperationBannerUpdateBo bo);
    void delete(OperationBannerInfoBo bo);
    List<OperationBannerVo> list();

    List<OperationBannerVo> listByPositionCode(String positionCode);

    void incrementClick(Long bannerId);
}

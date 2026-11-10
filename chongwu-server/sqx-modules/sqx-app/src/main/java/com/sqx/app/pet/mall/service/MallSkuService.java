package com.sqx.app.pet.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.vo.MallSkuVo;

public interface MallSkuService {
    IPage<MallSkuVo> pageInfo(MallSkuPageBo bo);

    void saveInfo(MallSkuSaveBo bo);

    void updateInfo(MallSkuUpdateBo bo);

    void delete(MallSkuInfoBo bo);
}

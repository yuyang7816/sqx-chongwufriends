package com.sqx.app.pet.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.mall.domain.bo.MallSkuSpecPageBo;
import com.sqx.app.pet.mall.domain.entity.MallSkuSpec;

import java.util.List;

public interface MallSkuSpecService {
    IPage<MallSkuSpec> pageInfo(MallSkuSpecPageBo bo);

    List<MallSkuSpec> listEnabled();

    void saveInfo(MallSkuSpec spec);

    void updateInfo(MallSkuSpec spec);

    void delete(Long id);
}

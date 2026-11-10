package com.sqx.app.pet.mall.service;

import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.vo.MallSpuVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface MallSpuService {
    void saveInfo(MallSpuSaveBo bo);
    IPage<MallSpuVo> pageInfo(MallSpuPageBo bo);
    IPage<MallSpuVo> adminPageInfo(MallSpuPageBo bo);
    MallSpuVo info(MallSpuInfoBo bo);
    void updateInfo(MallSpuUpdateBo bo);
    void delete(MallSpuInfoBo bo);
    List<MallSpuVo> list();
}

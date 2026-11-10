package com.sqx.app.pet.community.service;

import com.sqx.app.pet.community.domain.bo.*;
import com.sqx.app.pet.community.domain.vo.CommunityPostVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface CommunityPostService {
    void saveInfo(CommunityPostSaveBo bo);
    IPage<CommunityPostVo> pageInfo(CommunityPostPageBo bo);
    CommunityPostVo info(CommunityPostInfoBo bo);
    void updateInfo(CommunityPostUpdateBo bo);
    void delete(CommunityPostInfoBo bo);
    List<CommunityPostVo> list();
}

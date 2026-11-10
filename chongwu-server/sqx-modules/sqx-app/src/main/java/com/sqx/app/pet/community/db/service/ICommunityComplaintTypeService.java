package com.sqx.app.pet.community.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.pet.community.domain.entity.CommunityComplaintType;

public interface ICommunityComplaintTypeService extends IService<CommunityComplaintType> {

    void saveType(CommunityComplaintType entity);

    void updateType(CommunityComplaintType entity);
}

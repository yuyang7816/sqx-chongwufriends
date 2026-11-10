package com.sqx.app.pet.community.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.community.db.mapper.CommunityComplaintTypeMapper;
import com.sqx.app.pet.community.db.service.ICommunityComplaintTypeService;
import com.sqx.app.pet.community.domain.entity.CommunityComplaintType;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

@Service
public class ICommunityComplaintTypeServiceImpl extends ServiceImpl<CommunityComplaintTypeMapper, CommunityComplaintType>
        implements ICommunityComplaintTypeService {

    @Override
    public void saveType(CommunityComplaintType entity) {
        CommunityComplaintType existing = baseMapper.selectByNameIncludeDeleted(entity.getName());
        if (existing == null) {
            save(entity);
            return;
        }
        if (existing.getIsDeleted() == null || existing.getIsDeleted() == 0) {
            throw new BizException("投诉类型名称已存在");
        }
        baseMapper.restoreById(existing.getId(), entity.getSort(), entity.getStatus());
    }

    @Override
    public void updateType(CommunityComplaintType entity) {
        CommunityComplaintType existing = baseMapper.selectByNameIncludeDeleted(entity.getName());
        if (existing != null && !existing.getId().equals(entity.getId())) {
            if (existing.getIsDeleted() != null && existing.getIsDeleted() != 0) {
                baseMapper.physicalDeleteById(existing.getId());
            } else {
                throw new BizException("投诉类型名称已存在");
            }
        }
        updateById(entity);
    }
}

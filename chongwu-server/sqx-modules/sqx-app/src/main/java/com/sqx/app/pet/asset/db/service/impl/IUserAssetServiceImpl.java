package com.sqx.app.pet.asset.db.service.impl;

import com.sqx.app.pet.asset.db.mapper.UserAssetMapper;
import com.sqx.app.pet.asset.db.service.IUserAssetService;
import com.sqx.app.pet.asset.domain.entity.UserAsset;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IUserAssetServiceImpl extends ServiceImpl<UserAssetMapper, UserAsset> implements IUserAssetService {
}

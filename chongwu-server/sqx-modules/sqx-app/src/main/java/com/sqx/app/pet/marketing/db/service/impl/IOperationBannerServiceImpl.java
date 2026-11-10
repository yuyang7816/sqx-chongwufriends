package com.sqx.app.pet.marketing.db.service.impl;

import com.sqx.app.pet.marketing.db.mapper.OperationBannerMapper;
import com.sqx.app.pet.marketing.db.service.IOperationBannerService;
import com.sqx.app.pet.marketing.domain.entity.OperationBanner;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IOperationBannerServiceImpl extends ServiceImpl<OperationBannerMapper, OperationBanner> implements IOperationBannerService {
}

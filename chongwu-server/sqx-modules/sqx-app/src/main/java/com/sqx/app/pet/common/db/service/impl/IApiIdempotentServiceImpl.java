package com.sqx.app.pet.common.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.common.db.mapper.ApiIdempotentMapper;
import com.sqx.app.pet.common.db.service.IApiIdempotentService;
import com.sqx.app.pet.common.domain.entity.ApiIdempotent;
import org.springframework.stereotype.Service;

@Service
public class IApiIdempotentServiceImpl extends ServiceImpl<ApiIdempotentMapper, ApiIdempotent> implements IApiIdempotentService {
}

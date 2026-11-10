package com.sqx.app.pet.mall.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.mall.db.mapper.CouponTemplateMapper;
import com.sqx.app.pet.mall.db.service.ICouponTemplateService;
import com.sqx.app.pet.mall.domain.entity.CouponTemplate;
import org.springframework.stereotype.Service;

@Service
public class ICouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplate> implements ICouponTemplateService {
}

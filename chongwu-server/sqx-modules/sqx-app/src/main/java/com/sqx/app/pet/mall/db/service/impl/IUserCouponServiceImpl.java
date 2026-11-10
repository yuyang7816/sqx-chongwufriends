package com.sqx.app.pet.mall.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.mall.db.mapper.UserCouponMapper;
import com.sqx.app.pet.mall.db.service.IUserCouponService;
import com.sqx.app.pet.mall.domain.entity.UserCoupon;
import org.springframework.stereotype.Service;

@Service
public class IUserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {
}

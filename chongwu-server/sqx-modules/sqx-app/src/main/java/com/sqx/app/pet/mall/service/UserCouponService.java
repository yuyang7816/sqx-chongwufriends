package com.sqx.app.pet.mall.service;

import com.sqx.app.pet.mall.domain.bo.UserCouponGrantBatchBo;
import com.sqx.app.pet.mall.domain.bo.UserCouponGrantBo;
import com.sqx.app.pet.mall.domain.bo.UserCouponListBo;
import com.sqx.app.pet.mall.domain.bo.UserCouponUseBo;
import com.sqx.app.pet.mall.domain.vo.UserCouponVo;

import java.util.List;

public interface UserCouponService {
    List<UserCouponVo> myCoupons(Long userId);

    List<UserCouponVo> myCoupons(Long userId, UserCouponListBo bo);

    void useCoupon(Long userId, UserCouponUseBo bo);

    void grantCoupon(UserCouponGrantBo bo);

    void grantCouponBatch(UserCouponGrantBatchBo bo);
}

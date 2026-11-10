package com.sqx.app.pet.mall.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.mall.domain.bo.UserCouponListBo;
import com.sqx.app.pet.mall.domain.vo.UserCouponVo;
import com.sqx.app.pet.mall.service.UserCouponService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "App-Coupon", description = "优惠券")
@RestController
@RequestMapping("/app/pet/coupon")
public class AppCouponController {

    @Resource
    private UserCouponService userCouponService;

    @Login
    @Operation(summary = "我的优惠券")
    @PostMapping("list")
    public Result<List<UserCouponVo>> list(@RequestBody(required = false) UserCouponListBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(userCouponService.myCoupons(userId, bo));
    }
}

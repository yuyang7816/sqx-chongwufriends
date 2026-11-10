package com.sqx.app.pet.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.vo.MallOrderVo;
import com.sqx.app.pet.mall.domain.vo.UserCouponVo;
import com.sqx.app.pet.mall.service.MallOrderService;
import com.sqx.app.pet.mall.service.UserCouponService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "App-Mall Order", description = "商城订单")
@RestController
@RequestMapping("/app/pet/mall/order")
public class AppMallOrderController {

    @Resource
    private MallOrderService mallOrderService;
    @Resource
    private UserCouponService userCouponService;

    @Login
    @Idempotent
    @Operation(summary = "创建订单")
    @PostMapping("create")
    public Result<MallOrderVo> create(@RequestBody @Validated MallOrderCreateBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(mallOrderService.createOrder(userId, bo));
    }

    @Login
    @Idempotent
    @Operation(summary = "积分兑换下单")
    @PostMapping("exchange")
    public Result<MallOrderVo> exchange(@RequestBody @Validated MallOrderCreateBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(mallOrderService.createExchangeOrder(userId, bo));
    }

    @Login
    @Operation(summary = "我的订单分页")
    @PostMapping("page")
    public Result<IPage<MallOrderVo>> page(@RequestBody @Validated MallOrderPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(mallOrderService.listMyOrders(userId, bo));
    }

    @Login
    @Idempotent
    @Operation(summary = "取消订单")
    @PostMapping("cancel")
    public Result<Void> cancel(@RequestBody @Validated MallOrderCancelBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        mallOrderService.cancelOrder(userId, bo);
        return Result.success();
    }

    @Login
    @Operation(summary = "我的订单")
    @PostMapping("list")
    public Result<IPage<MallOrderVo>> list(@RequestBody @Validated MallOrderPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(mallOrderService.listMyOrders(userId, bo));
    }

    @Login
    @Operation(summary = "订单详情")
    @PostMapping("info")
    public Result<MallOrderVo> info(@RequestBody @Validated MallOrderInfoBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(mallOrderService.info(userId, bo));
    }

    @Login
    @Operation(summary = "我的优惠券")
    @PostMapping("coupon/list")
    public Result<List<UserCouponVo>> couponList() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(userCouponService.myCoupons(userId));
    }

    @Login
    @Idempotent
    @Operation(summary = "使用优惠券")
    @PostMapping("coupon/use")
    public Result<Void> couponUse(@RequestBody @Validated UserCouponUseBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        userCouponService.useCoupon(userId, bo);
        return Result.success();
    }
}

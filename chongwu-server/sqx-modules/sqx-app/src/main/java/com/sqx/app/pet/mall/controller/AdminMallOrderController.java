package com.sqx.app.pet.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.mall.domain.bo.MallOrderInfoBo;
import com.sqx.app.pet.mall.domain.bo.MallOrderPageBo;
import com.sqx.app.pet.mall.domain.bo.MallOrderRefundBo;
import com.sqx.app.pet.mall.domain.vo.MallOrderVo;
import com.sqx.app.pet.mall.service.MallOrderService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "管理端-商城订单")
@RestController
@RequestMapping("/admin/pet/mall/order")
public class AdminMallOrderController {

    @Resource
    private MallOrderService mallOrderService;

    @Operation(summary = "订单分页")
    @PostMapping("page")
    public Result<IPage<MallOrderVo>> page(@RequestBody @Validated MallOrderPageBo bo) {
        return Result.success(mallOrderService.adminPage(bo));
    }

    @Operation(summary = "订单详情")
    @PostMapping("info")
    public Result<MallOrderVo> info(@RequestBody @Validated MallOrderInfoBo bo) {
        return Result.success(mallOrderService.adminInfo(bo));
    }

    @Idempotent
    @Operation(summary = "取消订单")
    @PostMapping("cancel")
    public Result<Void> cancel(@RequestBody @Validated MallOrderInfoBo bo) {
        mallOrderService.adminCancel(bo);
        return Result.success();
    }

    @Idempotent
    @Operation(summary = "退款")
    @PostMapping("refund")
    public Result<Void> refund(@RequestBody @Validated MallOrderRefundBo bo) {
        mallOrderService.adminRefund(bo);
        return Result.success();
    }
}

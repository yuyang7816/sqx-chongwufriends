package com.sqx.app.pet.payment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.support.UserIdResolveHelper;
import com.sqx.app.pet.payment.db.service.IPayCallbackLogService;
import com.sqx.app.pet.payment.db.service.IPayOrderService;
import com.sqx.app.pet.payment.domain.entity.PayCallbackLog;
import com.sqx.app.pet.payment.domain.entity.PayOrder;
import com.sqx.common.core.domain.Result;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Tag(name = "管理端-支付日志")
@RestController
@RequestMapping("/admin/pet/payment")
public class AdminPaymentController {

    @Resource
    private IPayCallbackLogService iPayCallbackLogService;
    @Resource
    private IPayOrderService iPayOrderService;
    @Resource
    private UserIdResolveHelper userIdResolveHelper;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CallbackPageBo extends PageParam {
        private String orderNo;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PayOrderPageBo extends PageParam {
        private String payNo;
        private Integer status;
        private Long userId;
        private String phone;
        private String username;
    }

    @Operation(summary = "支付单分页")
    @PostMapping("order/page")
    public Result<IPage<PayOrder>> orderPage(@RequestBody @Validated PayOrderPageBo bo) {
        Page<PayOrder> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        List<Long> userIds = userIdResolveHelper.resolveOptional(bo.getUserId(), bo.getPhone(), bo.getUsername());
        if (userIds != null && userIds.isEmpty()) {
            page.setRecords(Collections.emptyList());
            page.setTotal(0);
            return Result.success(page);
        }
        QueryWrapper<PayOrder> qw = new QueryWrapper<>();
        qw.lambda()
                .like(bo.getPayNo() != null, PayOrder::getPayNo, bo.getPayNo())
                .eq(bo.getStatus() != null, PayOrder::getStatus, bo.getStatus())
                .in(userIds != null, PayOrder::getUserId, userIds)
                .orderByDesc(PayOrder::getCreateTime);
        return Result.success(iPayOrderService.page(page, qw));
    }

    @Operation(summary = "回调日志分页")
    @PostMapping("page")
    public Result<IPage<PayCallbackLog>> page(@RequestBody @Validated CallbackPageBo bo) {
        Page<PayCallbackLog> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<PayCallbackLog> qw = new QueryWrapper<>();
        qw.lambda()
                .like(bo.getOrderNo() != null, PayCallbackLog::getOrderNo, bo.getOrderNo())
                .orderByDesc(PayCallbackLog::getCreateTime);
        return Result.success(iPayCallbackLogService.page(page, qw));
    }

    @Operation(summary = "回调日志分页(别名)")
    @PostMapping("pay_callback_log/page")
    public Result<IPage<PayCallbackLog>> pageAlias(@RequestBody @Validated CallbackPageBo bo) {
        return page(bo);
    }
}

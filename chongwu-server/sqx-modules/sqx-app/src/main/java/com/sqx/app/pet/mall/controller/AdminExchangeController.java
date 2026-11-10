package com.sqx.app.pet.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.mall.domain.bo.MallOrderExchangeStatusBo;
import com.sqx.app.pet.mall.domain.bo.MallOrderPageBo;
import com.sqx.app.pet.mall.domain.vo.ExchangeRecordVo;
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

@Tag(name = "Admin-Exchange", description = "周边积分兑换")
@RestController
@RequestMapping("/admin/pet/exchange")
public class AdminExchangeController {

    @Resource
    private MallOrderService mallOrderService;

    @Operation(summary = "兑换记录分页")
    @PostMapping("page")
    public Result<IPage<ExchangeRecordVo>> page(@RequestBody @Validated MallOrderPageBo bo) {
        bo.setBizType(4);
        return Result.success(mallOrderService.listExchangeRecords(bo.getUserId(), bo));
    }

    @Idempotent
    @Operation(summary = "更新兑换状态")
    @PostMapping("status")
    public Result<Void> status(@RequestBody @Validated MallOrderExchangeStatusBo bo) {
        mallOrderService.adminUpdateExchangeStatus(bo);
        return Result.success();
    }
}

package com.sqx.app.pet.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.mall.domain.bo.MallOrderPageBo;
import com.sqx.app.pet.mall.domain.vo.ExchangeRecordVo;
import com.sqx.app.pet.mall.service.MallOrderService;
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

@Tag(name = "App-Exchange", description = "周边积分兑换")
@RestController
@RequestMapping("/app/pet/exchange")
public class AppPetExchangeController {

    @Resource
    private MallOrderService mallOrderService;

    @Login
    @Operation(summary = "兑换记录分页")
    @PostMapping("page")
    public Result<IPage<ExchangeRecordVo>> page(@RequestBody @Validated MallOrderPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(mallOrderService.listExchangeRecords(userId, bo));
    }
}

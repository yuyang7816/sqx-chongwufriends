package com.sqx.app.pet.marketing.controller;

import com.sqx.app.pet.marketing.domain.bo.*;
import com.sqx.app.pet.marketing.domain.vo.OperationBannerVo;
import com.sqx.app.pet.marketing.service.OperationBannerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "Admin-Operation Banner", description = "Admin-Operation Banner")
@RestController
@RequestMapping("/admin/pet/banner")
public class AdminOperationBannerController {

    @Resource
    private OperationBannerService operationBanner;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated OperationBannerSaveBo bo) {
        operationBanner.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = OperationBannerVo.class)))
    @PostMapping("page")
    public Result<IPage<OperationBannerVo>> page(@RequestBody @Validated OperationBannerPageBo bo) {
        return Result.success(operationBanner.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = OperationBannerVo.class)))
    @PostMapping("info")
    public Result<OperationBannerVo> info(@RequestBody @Validated OperationBannerInfoBo bo) {
        return Result.success(operationBanner.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated OperationBannerUpdateBo bo) {
        operationBanner.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated OperationBannerInfoBo bo) {
        operationBanner.delete(bo);
        return Result.success();
    }
}

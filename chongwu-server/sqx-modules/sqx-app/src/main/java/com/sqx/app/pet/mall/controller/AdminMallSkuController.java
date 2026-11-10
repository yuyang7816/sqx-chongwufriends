package com.sqx.app.pet.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.vo.MallSkuVo;
import com.sqx.app.pet.mall.service.MallSkuService;
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

@Tag(name = "Admin-Mall SKU", description = "商城商品规格管理")
@RestController
@RequestMapping("/admin/pet/mall/sku")
public class AdminMallSkuController {

    @Resource
    private MallSkuService mallSkuService;

    @Operation(summary = "规格分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MallSkuVo.class)))
    @PostMapping("page")
    public Result<IPage<MallSkuVo>> page(@RequestBody @Validated MallSkuPageBo bo) {
        return Result.success(mallSkuService.pageInfo(bo));
    }

    @Operation(summary = "新增规格")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated MallSkuSaveBo bo) {
        mallSkuService.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "更新规格")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated MallSkuUpdateBo bo) {
        mallSkuService.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "删除规格")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated MallSkuInfoBo bo) {
        mallSkuService.delete(bo);
        return Result.success();
    }
}

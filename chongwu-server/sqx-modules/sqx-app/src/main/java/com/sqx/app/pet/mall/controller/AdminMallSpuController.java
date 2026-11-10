package com.sqx.app.pet.mall.controller;

import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.vo.MallSpuVo;
import com.sqx.app.pet.mall.service.MallSpuService;
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

@Tag(name = "Admin-Mall SPU", description = "Admin-Mall SPU")
@RestController
@RequestMapping("/admin/pet/mall")
public class AdminMallSpuController {

    @Resource
    private MallSpuService mallSpu;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated MallSpuSaveBo bo) {
        mallSpu.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MallSpuVo.class)))
    @PostMapping("page")
    public Result<IPage<MallSpuVo>> page(@RequestBody @Validated MallSpuPageBo bo) {
        return Result.success(mallSpu.adminPageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MallSpuVo.class)))
    @PostMapping("info")
    public Result<MallSpuVo> info(@RequestBody @Validated MallSpuInfoBo bo) {
        return Result.success(mallSpu.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated MallSpuUpdateBo bo) {
        mallSpu.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated MallSpuInfoBo bo) {
        mallSpu.delete(bo);
        return Result.success();
    }
}

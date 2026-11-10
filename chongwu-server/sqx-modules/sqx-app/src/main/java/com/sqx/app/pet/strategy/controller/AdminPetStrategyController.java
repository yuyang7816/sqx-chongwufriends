package com.sqx.app.pet.strategy.controller;

import com.sqx.app.pet.strategy.domain.bo.*;
import com.sqx.app.pet.strategy.domain.vo.PetStrategyCategoryVo;
import com.sqx.app.pet.strategy.domain.vo.PetStrategyVo;
import com.sqx.app.pet.strategy.service.PetStrategyService;
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
import java.util.List;

@Tag(name = "Admin-Pet Strategy", description = "Admin-Pet Strategy")
@RestController
@RequestMapping("/admin/pet/strategy")
public class AdminPetStrategyController {

    @Resource
    private PetStrategyService petStrategy;

    @Operation(summary = "分类列表（含停用，便于筛选）")
    @PostMapping("categories")
    public Result<List<PetStrategyCategoryVo>> categories() {
        return Result.success(petStrategy.adminCategories());
    }

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated PetStrategySaveBo bo) {
        petStrategy.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetStrategyVo.class)))
    @PostMapping("page")
    public Result<IPage<PetStrategyVo>> page(@RequestBody @Validated PetStrategyPageBo bo) {
        return Result.success(petStrategy.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetStrategyVo.class)))
    @PostMapping("info")
    public Result<PetStrategyVo> info(@RequestBody @Validated PetStrategyInfoBo bo) {
        return Result.success(petStrategy.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated PetStrategyUpdateBo bo) {
        petStrategy.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated PetStrategyInfoBo bo) {
        petStrategy.delete(bo);
        return Result.success();
    }
}

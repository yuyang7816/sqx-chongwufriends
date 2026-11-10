package com.sqx.app.pet.island.controller;

import com.sqx.app.pet.island.domain.bo.*;
import com.sqx.app.pet.island.domain.vo.IslandPetVo;
import com.sqx.app.pet.island.service.IslandPetService;
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

@Tag(name = "Admin-Island Pet", description = "Admin-Island Pet")
@RestController
@RequestMapping("/admin/pet/island")
public class AdminIslandPetController {

    @Resource
    private IslandPetService islandPet;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated IslandPetSaveBo bo) {
        islandPet.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = IslandPetVo.class)))
    @PostMapping("page")
    public Result<IPage<IslandPetVo>> page(@RequestBody @Validated IslandPetPageBo bo) {
        return Result.success(islandPet.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = IslandPetVo.class)))
    @PostMapping("info")
    public Result<IslandPetVo> info(@RequestBody @Validated IslandPetInfoBo bo) {
        return Result.success(islandPet.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated IslandPetUpdateBo bo) {
        islandPet.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated IslandPetInfoBo bo) {
        islandPet.delete(bo);
        return Result.success();
    }
}

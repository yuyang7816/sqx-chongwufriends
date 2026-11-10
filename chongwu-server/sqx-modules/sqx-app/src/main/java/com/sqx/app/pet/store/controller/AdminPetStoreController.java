package com.sqx.app.pet.store.controller;

import com.sqx.app.pet.store.domain.bo.*;
import com.sqx.app.pet.store.domain.vo.PetStoreVo;
import com.sqx.app.pet.store.service.PetStoreService;
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

@Tag(name = "Admin-Pet Store", description = "Admin-Pet Store")
@RestController
@RequestMapping("/admin/pet/store")
public class AdminPetStoreController {

    @Resource
    private PetStoreService petStore;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated PetStoreSaveBo bo) {
        petStore.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetStoreVo.class)))
    @PostMapping("page")
    public Result<IPage<PetStoreVo>> page(@RequestBody @Validated PetStorePageBo bo) {
        return Result.success(petStore.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetStoreVo.class)))
    @PostMapping("info")
    public Result<PetStoreVo> info(@RequestBody @Validated PetStoreInfoBo bo) {
        return Result.success(petStore.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated PetStoreUpdateBo bo) {
        petStore.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated PetStoreInfoBo bo) {
        petStore.delete(bo);
        return Result.success();
    }
}

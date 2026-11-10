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
import java.util.List;

@Tag(name = "App-Pet Store", description = "App-Pet Store")
@RestController
@RequestMapping("/app/pet/store")
public class AppPetStoreController {

    @Resource
    private PetStoreService petStore;

    @Operation(summary = "list")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetStoreVo.class)))
    @PostMapping("list")
    public Result<List<PetStoreVo>> list() {
        return Result.success(petStore.list());
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
}

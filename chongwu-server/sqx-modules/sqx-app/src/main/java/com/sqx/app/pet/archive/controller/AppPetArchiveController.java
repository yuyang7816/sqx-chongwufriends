package com.sqx.app.pet.archive.controller;

import com.sqx.app.pet.archive.domain.bo.*;
import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;
import com.sqx.app.pet.archive.service.PetArchiveService;
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

@Tag(name = "App-Pet Archive", description = "App-Pet Archive")
@RestController
@RequestMapping("/app/pet/archive")
public class AppPetArchiveController {

    @Resource
    private PetArchiveService petArchive;

    @Operation(summary = "list")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetArchiveVo.class)))
    @PostMapping("list")
    public Result<List<PetArchiveVo>> list() {
        return Result.success(petArchive.list());
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetArchiveVo.class)))
    @PostMapping("page")
    public Result<IPage<PetArchiveVo>> page(@RequestBody @Validated PetArchivePageBo bo) {
        return Result.success(petArchive.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetArchiveVo.class)))
    @PostMapping("info")
    public Result<PetArchiveVo> info(@RequestBody @Validated PetArchiveInfoBo bo) {
        return Result.success(petArchive.appInfo(bo));
    }
}

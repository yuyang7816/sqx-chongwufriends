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

@Tag(name = "Admin-Pet Archive", description = "Admin-Pet Archive")
@RestController
@RequestMapping("/admin/pet/archive")
public class AdminPetArchiveController {

    @Resource
    private PetArchiveService petArchive;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated PetArchiveSaveBo bo) {
        petArchive.saveInfo(bo);
        return Result.success();
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
        return Result.success(petArchive.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated PetArchiveUpdateBo bo) {
        petArchive.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated PetArchiveInfoBo bo) {
        petArchive.delete(bo);
        return Result.success();
    }

    @Operation(summary = "audit")
    @PostMapping("audit")
    public Result<Void> audit(@RequestBody @Validated PetArchiveAuditBo bo) {
        petArchive.audit(bo);
        return Result.success();
    }

    @Operation(summary = "batchAudit")
    @PostMapping("batchAudit")
    public Result<Void> batchAudit(@RequestBody @Validated PetArchiveBatchAuditBo bo) {
        petArchive.batchAudit(bo);
        return Result.success();
    }
}

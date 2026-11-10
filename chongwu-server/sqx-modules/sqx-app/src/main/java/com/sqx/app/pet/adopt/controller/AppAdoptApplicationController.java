package com.sqx.app.pet.adopt.controller;

import com.sqx.app.pet.adopt.domain.bo.*;
import com.sqx.app.pet.adopt.domain.vo.AdoptApplicationVo;
import com.sqx.app.pet.adopt.service.AdoptApplicationService;
import com.sqx.app.pet.archive.domain.bo.PetArchiveInfoBo;
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
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.common.shiro.utils.UserContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "App-Adopt Application", description = "App-Adopt Application")
@RestController
@RequestMapping("/app/pet/adopt")
public class AppAdoptApplicationController {

    @Resource
    private AdoptApplicationService adoptApplication;
    @Resource
    private PetArchiveService petArchive;

    @Operation(summary = "退役可领养宠物分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetArchiveVo.class)))
    @PostMapping("pet/page")
    public Result<IPage<PetArchiveVo>> adoptablePetPage(@RequestBody @Validated com.sqx.app.pet.archive.domain.bo.PetArchivePageBo bo) {
        bo.setAdoptable(1);
        if (bo.getAuditStatus() == null) {
            bo.setAuditStatus(1);
        }
        return Result.success(petArchive.pageInfo(bo));
    }

    @Operation(summary = "可领养宠物详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetArchiveVo.class)))
    @PostMapping("pet/info")
    public Result<PetArchiveVo> adoptablePetInfo(@RequestBody @Validated PetArchiveInfoBo bo) {
        return Result.success(petArchive.adoptPetInfo(bo));
    }

    @Operation(summary = "list")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdoptApplicationVo.class)))
    @PostMapping("list")
    public Result<List<AdoptApplicationVo>> list() {
        return Result.success(adoptApplication.list());
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdoptApplicationVo.class)))
    @PostMapping("page")
    public Result<IPage<AdoptApplicationVo>> page(@RequestBody @Validated AdoptApplicationPageBo bo) {
        return Result.success(adoptApplication.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdoptApplicationVo.class)))
    @PostMapping("info")
    public Result<AdoptApplicationVo> info(@RequestBody @Validated AdoptApplicationInfoBo bo) {
        return Result.success(adoptApplication.info(bo));
    }

    @Login
    @Operation(summary = "我的领养申请分页")
    @PostMapping("my/page")
    public Result<IPage<AdoptApplicationVo>> myPage(@RequestBody @Validated AdoptApplicationPageBo bo) {
        bo.setUserId(UserContextHolder.getAppUserId());
        return Result.success(adoptApplication.pageInfo(bo));
    }

    @Login
    @Operation(summary = "我的领养申请详情")
    @PostMapping("my/info")
    public Result<AdoptApplicationVo> myInfo(@RequestBody @Validated AdoptApplicationInfoBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(adoptApplication.myInfo(userId, bo));
    }

    @Login
    @Idempotent
    @Operation(summary = "提交领养申请")
    @PostMapping("save")
    public Result<AdoptApplicationVo> save(@RequestBody @Validated AdoptApplicationSaveBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(adoptApplication.submitForUser(userId, bo));
    }
}

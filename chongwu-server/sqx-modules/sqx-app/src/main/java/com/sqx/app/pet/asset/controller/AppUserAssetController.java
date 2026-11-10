package com.sqx.app.pet.asset.controller;

import com.sqx.app.pet.asset.domain.bo.*;
import com.sqx.app.pet.asset.domain.vo.UserAssetVo;
import com.sqx.app.pet.asset.service.UserAssetService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.common.shiro.utils.UserContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "App-User Asset", description = "App-User Asset")
@RestController
@RequestMapping("/app/pet/asset")
public class AppUserAssetController {

    @Resource
    private UserAssetService userAsset;

    @Operation(summary = "list")
    @ApiResponse(content = @Content(schema = @Schema(implementation = UserAssetVo.class)))
    @PostMapping("list")
    public Result<List<UserAssetVo>> list() {
        return Result.success(userAsset.list());
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = UserAssetVo.class)))
    @PostMapping("page")
    public Result<IPage<UserAssetVo>> page(@RequestBody @Validated UserAssetPageBo bo) {
        return Result.success(userAsset.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = UserAssetVo.class)))
    @PostMapping("info")
    public Result<UserAssetVo> info(@RequestBody @Validated UserAssetInfoBo bo) {
        return Result.success(userAsset.info(bo));
    }

    @Login
    @Operation(summary = "当前用户资产")
    @GetMapping("my")
    public Result<UserAssetVo> my() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(userAsset.infoByUserId(userId));
    }
}

package com.sqx.app.pet.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.support.UserIdResolveHelper;
import com.sqx.app.pet.asset.domain.bo.*;
import com.sqx.app.pet.asset.domain.vo.UserAssetVo;
import com.sqx.app.pet.asset.service.UserAssetService;
import com.sqx.app.pet.common.db.service.IUserAssetLedgerService;
import com.sqx.app.pet.common.domain.entity.UserAssetLedger;
import com.sqx.common.core.domain.Result;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Tag(name = "Admin-User Asset", description = "Admin-User Asset")
@RestController
@RequestMapping("/admin/pet/asset")
public class AdminUserAssetController {

    @Resource
    private UserAssetService userAsset;
    @Resource
    private IUserAssetLedgerService iUserAssetLedgerService;
    @Resource
    private UserIdResolveHelper userIdResolveHelper;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated UserAssetSaveBo bo) {
        userAsset.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "管理员调整资产（增加/减少/设为）")
    @PostMapping("adjust")
    public Result<Void> adjust(@RequestBody @Validated UserAssetAdjustBo bo) {
        userAsset.adjust(bo);
        return Result.success();
    }

    @Operation(summary = "按用户ID查询资产")
    @PostMapping("infoByUserId")
    public Result<UserAssetVo> infoByUserId(@RequestBody UserAssetInfoByUserIdBo bo) {
        return Result.success(userAsset.infoByUserId(bo.getUserId()));
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

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated UserAssetUpdateBo bo) {
        userAsset.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated UserAssetInfoBo bo) {
        userAsset.delete(bo);
        return Result.success();
    }

    @Operation(summary = "资产流水分页")
    @PostMapping("ledger/page")
    public Result<IPage<UserAssetLedger>> ledgerPage(@RequestBody LedgerPageBo bo) {
        Page<UserAssetLedger> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        List<Long> userIds = userIdResolveHelper.resolveOptional(bo.getUserId(), bo.getPhone(), bo.getUsername());
        if (userIds != null && userIds.isEmpty()) {
            page.setRecords(Collections.emptyList());
            page.setTotal(0);
            return Result.success(page);
        }
        QueryWrapper<UserAssetLedger> qw = new QueryWrapper<>();
        qw.lambda()
                .in(userIds != null, UserAssetLedger::getUserId, userIds)
                .orderByDesc(UserAssetLedger::getCreateTime);
        return Result.success(iUserAssetLedgerService.page(page, qw));
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class LedgerPageBo extends PageParam {
        private Long userId;
        private String phone;
        private String username;
    }
}

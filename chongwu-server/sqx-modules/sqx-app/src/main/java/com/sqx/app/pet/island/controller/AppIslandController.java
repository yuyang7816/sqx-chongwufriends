package com.sqx.app.pet.island.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.island.domain.bo.IslandBuyMarketBo;
import com.sqx.app.pet.island.domain.bo.IslandFeedBo;
import com.sqx.app.pet.island.domain.bo.IslandMarketRecordPageBo;
import com.sqx.app.pet.island.domain.bo.IslandResidentSaveBo;
import com.sqx.app.pet.island.domain.bo.IslandTaskClaimBo;
import com.sqx.app.pet.island.domain.bo.IslandTaskCompleteBo;
import com.sqx.app.pet.island.domain.vo.IslandMarketItemVo;
import com.sqx.app.pet.island.domain.vo.IslandMarketRecordVo;
import com.sqx.app.pet.island.domain.vo.IslandPetVo;
import com.sqx.app.pet.island.domain.vo.IslandTaskVo;
import com.sqx.app.pet.island.service.IslandService;
import com.sqx.app.pet.island.service.IslandTaskService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "App-Island", description = "岛宅")
@RestController
@RequestMapping("/app/pet/island")
public class AppIslandController {

    @Resource
    private IslandService islandService;
    @Resource
    private IslandTaskService islandTaskService;

    @Login
    @Operation(summary = "我的岛宅")
    @PostMapping("info")
    public Result<IslandPetVo> info() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(islandService.getMyIsland(userId));
    }

    @Login
    @Operation(summary = "保存常驻宠物")
    @PostMapping("resident/save")
    public Result<IslandPetVo> residentSave(@RequestBody @Validated IslandResidentSaveBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(islandService.saveResidents(userId, bo));
    }

    @Login
    @Operation(summary = "常驻可选宠物（用户自有）")
    @PostMapping("resident/candidates")
    public Result<List<PetArchiveVo>> residentCandidates() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(islandService.listResidentCandidates(userId));
    }

    @Login
    @Idempotent
    @Operation(summary = "投喂")
    @PostMapping("feed")
    public Result<IslandPetVo> feed(@RequestBody(required = false) IslandFeedBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        if (bo == null) {
            bo = new IslandFeedBo();
        }
        return Result.success(islandService.feed(userId, bo));
    }

    @Operation(summary = "集市道具列表")
    @PostMapping("market/list")
    public Result<List<IslandMarketItemVo>> marketList() {
        return Result.success(islandService.listMarketItems());
    }

    @Login
    @Idempotent
    @Operation(summary = "购买集市道具")
    @PostMapping("market/buy")
    public Result<IslandPetVo> marketBuy(@RequestBody @Validated IslandBuyMarketBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(islandService.buyMarketItem(userId, bo));
    }

    @Login
    @Operation(summary = "我的集市兑换记录")
    @PostMapping("market/records")
    public Result<IPage<IslandMarketRecordVo>> marketRecords(@RequestBody(required = false) IslandMarketRecordPageBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        if (bo == null) {
            bo = new IslandMarketRecordPageBo();
        }
        return Result.success(islandService.listMarketRecords(userId, bo));
    }

    @Login
    @Operation(summary = "任务列表")
    @PostMapping("task/list")
    public Result<List<IslandTaskVo>> taskList() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(islandTaskService.listTasks(userId));
    }

    @Login
    @Idempotent
    @Operation(summary = "领取任务")
    @PostMapping("task/claim")
    public Result<Void> taskClaim(@RequestBody @Validated IslandTaskClaimBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        islandTaskService.claimTask(userId, bo);
        return Result.success();
    }

    @Login
    @Idempotent
    @Operation(summary = "完成任务")
    @PostMapping("task/complete")
    public Result<Void> taskComplete(@RequestBody @Validated IslandTaskCompleteBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        islandTaskService.completeTask(userId, bo);
        return Result.success();
    }
}

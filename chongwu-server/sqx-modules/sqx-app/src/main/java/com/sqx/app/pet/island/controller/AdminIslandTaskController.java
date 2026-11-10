package com.sqx.app.pet.island.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.island.db.service.IIslandTaskService;
import com.sqx.app.pet.island.domain.entity.IslandTask;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

@Tag(name = "管理端-岛宅任务")
@RestController
@RequestMapping("/admin/pet/island/task")
public class AdminIslandTaskController {

    @Resource
    private IIslandTaskService iIslandTaskService;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TaskPageBo extends PageParam {
    }

    @Data
    public static class TaskSaveBo {
        private Long id;
        private String title;
        private String description;
        private String rewardDesc;
        private String taskGroup;
        private String actionType;
        private Integer rewardCoins;
        private Integer rewardPoints;
        private Integer requiredLevel;
        private Integer sort;
        private Integer status;
    }

    @Data
    public static class IdBo {
        private Long id;
    }

    @Operation(summary = "分页")
    @PostMapping("page")
    public Result<IPage<IslandTask>> page(@RequestBody @Validated TaskPageBo bo) {
        Page<IslandTask> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<IslandTask> qw = new QueryWrapper<>();
        qw.lambda().orderByAsc(IslandTask::getSort);
        return Result.success(iIslandTaskService.page(page, qw));
    }

    @Operation(summary = "详情")
    @PostMapping("info")
    public Result<IslandTask> info(@RequestBody IdBo bo) {
        IslandTask task = iIslandTaskService.getById(bo.getId());
        if (task == null) {
            throw new BizException("任务不存在");
        }
        return Result.success(task);
    }

    @Idempotent
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody TaskSaveBo bo) {
        IslandTask entity = new IslandTask();
        entity.setTitle(bo.getTitle());
        entity.setDescription(bo.getDescription());
        entity.setRewardDesc(bo.getRewardDesc());
        entity.setTaskGroup(bo.getTaskGroup());
        entity.setActionType(bo.getActionType());
        entity.setRewardCoins(bo.getRewardCoins());
        entity.setRewardPoints(bo.getRewardPoints());
        entity.setRequiredLevel(bo.getRequiredLevel());
        entity.setSort(bo.getSort());
        entity.setStatus(bo.getStatus() == null ? 1 : bo.getStatus());
        iIslandTaskService.save(entity);
        return Result.success();
    }

    @Idempotent
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody TaskSaveBo bo) {
        IslandTask entity = iIslandTaskService.getById(bo.getId());
        if (entity == null) {
            throw new BizException("任务不存在");
        }
        entity.setTitle(bo.getTitle());
        entity.setDescription(bo.getDescription());
        entity.setRewardDesc(bo.getRewardDesc());
        entity.setTaskGroup(bo.getTaskGroup());
        entity.setActionType(bo.getActionType());
        entity.setRewardCoins(bo.getRewardCoins());
        entity.setRewardPoints(bo.getRewardPoints());
        entity.setRequiredLevel(bo.getRequiredLevel());
        entity.setSort(bo.getSort());
        if (bo.getStatus() != null) {
            entity.setStatus(bo.getStatus());
        }
        iIslandTaskService.updateById(entity);
        return Result.success();
    }

    @Idempotent
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody IdBo bo) {
        iIslandTaskService.removeById(bo.getId());
        return Result.success();
    }
}

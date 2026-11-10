package com.sqx.app.pet.island.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.common.service.AssetLedgerService;
import com.sqx.app.pet.island.db.service.IIslandTaskRecordService;
import com.sqx.app.pet.island.db.service.IIslandTaskService;
import com.sqx.app.pet.island.domain.bo.IslandTaskClaimBo;
import com.sqx.app.pet.island.domain.bo.IslandTaskCompleteBo;
import com.sqx.app.pet.island.domain.entity.IslandTask;
import com.sqx.app.pet.island.domain.entity.IslandTaskRecord;
import com.sqx.app.pet.island.domain.vo.IslandTaskVo;
import com.sqx.app.pet.island.service.IslandTaskService;
import com.sqx.common.core.exception.BizException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class IslandTaskServiceImpl implements IslandTaskService {

    @Resource
    private IIslandTaskService iIslandTaskService;
    @Resource
    private IIslandTaskRecordService iIslandTaskRecordService;
    @Resource
    private AssetLedgerService assetLedgerService;

    @Override
    public List<IslandTaskVo> listTasks(Long userId) {
        QueryWrapper<IslandTask> qw = new QueryWrapper<>();
        qw.lambda().eq(IslandTask::getStatus, 1).orderByAsc(IslandTask::getSort);
        List<IslandTask> tasks = iIslandTaskService.list(qw);
        Map<Long, IslandTaskRecord> recordMap = iIslandTaskRecordService.lambdaQuery()
                .eq(IslandTaskRecord::getUserId, userId)
                .list()
                .stream()
                .collect(Collectors.toMap(IslandTaskRecord::getTaskId, r -> r, (a, b) -> a));
        List<IslandTaskVo> voList = new ArrayList<>();
        for (IslandTask task : tasks) {
            IslandTaskVo vo = new IslandTaskVo();
            BeanUtils.copyProperties(task, vo);
            IslandTaskRecord record = recordMap.get(task.getId());
            vo.setRecordStatus(record == null ? 3 : record.getStatus());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimTask(Long userId, IslandTaskClaimBo bo) {
        IslandTask task = iIslandTaskService.getById(bo.getTaskId());
        if (task == null || !Objects.equals(task.getStatus(), 1)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        boolean exists = iIslandTaskRecordService.lambdaQuery()
                .eq(IslandTaskRecord::getUserId, userId)
                .eq(IslandTaskRecord::getTaskId, bo.getTaskId())
                .exists();
        if (exists) {
            return;
        }
        IslandTaskRecord record = new IslandTaskRecord();
        record.setUserId(userId);
        record.setTaskId(bo.getTaskId());
        record.setStatus(2);
        iIslandTaskRecordService.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long userId, IslandTaskCompleteBo bo) {
        IslandTask task = iIslandTaskService.getById(bo.getTaskId());
        if (task == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        IslandTaskRecord record = iIslandTaskRecordService.lambdaQuery()
                .eq(IslandTaskRecord::getUserId, userId)
                .eq(IslandTaskRecord::getTaskId, bo.getTaskId())
                .one();
        if (record == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        if (Objects.equals(record.getStatus(), 1)) {
            throw new BizException(PetError.TASK_ALREADY_DONE);
        }
        record.setStatus(1);
        record.setCompleteTime(LocalDateTime.now());
        iIslandTaskRecordService.updateById(record);
        if (task.getRewardCoins() != null && task.getRewardCoins() > 0) {
            assetLedgerService.change(userId, AssetLedgerService.COINS, task.getRewardCoins(), 1, "island_task", task.getId(), task.getTitle());
        }
        if (task.getRewardPoints() != null && task.getRewardPoints() > 0) {
            assetLedgerService.change(userId, AssetLedgerService.POINTS, task.getRewardPoints(), 1, "island_task", task.getId(), task.getTitle());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCompleteByAction(Long userId, String action) {
        if (action == null || action.trim().isEmpty()) {
            return;
        }
        String act = action.trim().toLowerCase();
        QueryWrapper<IslandTask> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(IslandTask::getStatus, 1)
                .eq(IslandTask::getActionType, act)
                .orderByAsc(IslandTask::getSort);
        List<IslandTask> tasks = iIslandTaskService.list(qw);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        for (IslandTask task : tasks) {
            IslandTaskRecord record = iIslandTaskRecordService.lambdaQuery()
                    .eq(IslandTaskRecord::getUserId, userId)
                    .eq(IslandTaskRecord::getTaskId, task.getId())
                    .one();
            if (record != null && Objects.equals(record.getStatus(), 1)) {
                continue;
            }
            if (record == null) {
                record = new IslandTaskRecord();
                record.setUserId(userId);
                record.setTaskId(task.getId());
                record.setStatus(2);
                iIslandTaskRecordService.save(record);
            }
            IslandTaskCompleteBo bo = new IslandTaskCompleteBo();
            bo.setTaskId(task.getId());
            completeTask(userId, bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCompleteFeedTask(Long userId) {
        // 兼容旧调用：投喂动作统一走 autoCompleteByAction("feed")，
        // 同时兼容未配置 action_type 但标题包含「投喂」或属于「每日待办」的旧任务。
        autoCompleteByAction(userId, "feed");
        QueryWrapper<IslandTask> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(IslandTask::getStatus, 1)
                .and(w -> w.isNull(IslandTask::getActionType).or().eq(IslandTask::getActionType, ""))
                .and(w -> w.eq(IslandTask::getTaskGroup, "每日待办").or().like(IslandTask::getTitle, "投喂"))
                .orderByAsc(IslandTask::getSort)
                .last("LIMIT 1");
        IslandTask task = iIslandTaskService.getOne(qw);
        if (task == null) {
            return;
        }
        IslandTaskRecord record = iIslandTaskRecordService.lambdaQuery()
                .eq(IslandTaskRecord::getUserId, userId)
                .eq(IslandTaskRecord::getTaskId, task.getId())
                .one();
        if (record != null && Objects.equals(record.getStatus(), 1)) {
            return;
        }
        if (record == null) {
            record = new IslandTaskRecord();
            record.setUserId(userId);
            record.setTaskId(task.getId());
            record.setStatus(2);
            iIslandTaskRecordService.save(record);
        }
        IslandTaskCompleteBo bo = new IslandTaskCompleteBo();
        bo.setTaskId(task.getId());
        completeTask(userId, bo);
    }
}

package com.sqx.app.pet.island.service;

import com.sqx.app.pet.island.domain.bo.IslandTaskClaimBo;
import com.sqx.app.pet.island.domain.bo.IslandTaskCompleteBo;
import com.sqx.app.pet.island.domain.vo.IslandTaskVo;

import java.util.List;

public interface IslandTaskService {
    List<IslandTaskVo> listTasks(Long userId);
    void claimTask(Long userId, IslandTaskClaimBo bo);
    void completeTask(Long userId, IslandTaskCompleteBo bo);

    /**
     * 根据用户动作自动完成匹配的任务（action_type 与传入 action 相同且未完成的任务）。
     * 用于「投喂/发帖/点赞/评论/分享/购买道具」等真实动作触发后自动结算任务。
     */
    void autoCompleteByAction(Long userId, String action);

    /** 岛宅投喂成功后自动完成「每日投喂」类任务（内部转调 autoCompleteByAction("feed")） */
    void autoCompleteFeedTask(Long userId);
}

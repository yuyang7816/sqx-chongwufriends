package com.sqx.app.pet.island.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "领取任务")
public class IslandTaskClaimBo {
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
}

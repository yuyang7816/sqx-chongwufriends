package com.sqx.app.pet.feed.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "赠送记录汇总")
public class FeedGiftSummaryVo implements Serializable {
    @Schema(description = "累计赠送次数")
    private Integer totalCount;
    @Schema(description = "已反馈次数")
    private Integer feedbackCount;
    @Schema(description = "待反馈次数")
    private Integer pendingCount;
    @Schema(description = "最近赠送描述")
    private String latestGiftText;
}

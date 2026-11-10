package com.sqx.app.pet.community.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "我的动态统计")
public class MyPostSummaryVo implements Serializable {
    @Schema(description = "全部数量")
    private Long total;
    @Schema(description = "待审核")
    private Long pendingCount;
    @Schema(description = "已通过")
    private Long approvedCount;
    @Schema(description = "已拒绝")
    private Long rejectedCount;
}

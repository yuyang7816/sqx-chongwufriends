package com.sqx.app.pet.community.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "朋友圈统计")
public class CommunityStatsVo {
    private Long pendingCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Long offlineCount;
    private Long totalCount;
    private Long todayCount;
    @Schema(description = "社区审核开关 1开0关")
    private String auditSwitch;
}

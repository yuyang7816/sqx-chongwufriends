package com.sqx.app.pet.community.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "社区配置")
public class CommunityConfigVo {
    @Schema(description = "审核开关 1开0关")
    private String auditSwitch;

    @Schema(description = "投诉类型")
    private List<String> complaintTypes;
}

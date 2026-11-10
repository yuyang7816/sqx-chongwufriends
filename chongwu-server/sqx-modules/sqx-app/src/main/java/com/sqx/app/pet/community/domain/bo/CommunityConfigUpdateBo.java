package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "社区配置更新")
public class CommunityConfigUpdateBo {
    @Schema(description = "审核开关 1开0关")
    private String auditSwitch;

    @Schema(description = "投诉类型列表")
    private List<String> complaintTypes;
}

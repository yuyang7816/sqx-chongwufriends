package com.sqx.app.pet.community.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "CommunityReportVo")
public class CommunityReportVo {
    private Long id;
    private Integer targetType;
    private Long targetId;
    private Long userId;
    private String userName;
    private String reason;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "目标类型文案")
    private String targetTypeText;
    @Schema(description = "状态文案")
    private String statusText;
    @Schema(description = "被投诉内容摘要")
    private String targetSummary;
}

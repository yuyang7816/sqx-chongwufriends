package com.sqx.app.pet.community.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "CommunityPostPageBo")
public class CommunityPostPageBo extends PageParam {
    @Schema(description = "审核状态 0待审 1通过 2拒绝")
    private Integer auditStatus;
    @Schema(description = "状态 1正常 2下架")
    private Integer status;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "门店ID")
    private Long storeId;
    @Schema(description = "用户昵称")
    private String userName;
    @Schema(description = "内容关键词")
    private String keyword;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}

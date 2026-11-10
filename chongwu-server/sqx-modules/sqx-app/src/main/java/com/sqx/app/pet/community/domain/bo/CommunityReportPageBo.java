package com.sqx.app.pet.community.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "投诉分页")
public class CommunityReportPageBo extends PageParam {
    private Integer targetType;
    private Integer status;
    @Schema(description = "内部使用")
    private Long userId;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "用户名/昵称")
    private String username;
    private Long targetId;
    private String keyword;
}

package com.sqx.app.base.user.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author chengming
 * @since 2025/4/24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ListUserBO extends PageParam {

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "状态 1正常 2禁用")
    private Integer status;

    @Schema(description = "用户类型 1普通用户 2vip用户")
    private Integer userType;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "邀请码")
    private String invitationCode;
}

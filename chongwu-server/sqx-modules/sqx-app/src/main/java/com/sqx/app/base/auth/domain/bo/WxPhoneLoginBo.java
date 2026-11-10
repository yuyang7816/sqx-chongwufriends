package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WxPhoneLoginBo {

    @NotBlank
    @Schema(description = "uni.login 返回的 code")
    private String loginCode;

    @NotBlank
    @Schema(description = "getPhoneNumber 返回的 code")
    private String phoneCode;

    @Schema(description = "邀请人邀请码")
    private String superiorInvitationCode;
}

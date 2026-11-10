package com.sqx.app.pet.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "宠物用户资料更新")
public class PetUserUpdateBo {
    @Schema(description = "昵称")
    private String userName;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "生日 yyyy-MM-dd")
    private String birthday;
    @Schema(description = "微信号")
    private String wechat;
}

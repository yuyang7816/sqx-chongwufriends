package com.sqx.app.pet.asset.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "UserAssetPageBo")
public class UserAssetPageBo extends PageParam {
    @Schema(description = "内部使用，详情Tab可用")
    private Long userId;
    @Schema(description = "手机号（精确）")
    private String phone;
    @Schema(description = "用户名/昵称（模糊）")
    private String username;
}

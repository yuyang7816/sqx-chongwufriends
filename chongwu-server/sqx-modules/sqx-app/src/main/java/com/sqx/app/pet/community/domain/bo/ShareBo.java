package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "分享")
public class ShareBo {
    @NotNull(message = "动态ID不能为空")
    private Long postId;
}

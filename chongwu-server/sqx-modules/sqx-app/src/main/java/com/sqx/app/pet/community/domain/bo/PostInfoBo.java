package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "动态详情")
public class PostInfoBo {
    @NotNull(message = "ID不能为空")
    private Long id;
    private String deviceId;
    @Schema(description = "是否记录浏览，编辑预加载时传 false")
    private Boolean recordView;
}

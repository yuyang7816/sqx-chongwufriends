package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "动态下架")
public class CommunityOfflineBo {
    @NotNull(message = "ID不能为空")
    private Long id;
}

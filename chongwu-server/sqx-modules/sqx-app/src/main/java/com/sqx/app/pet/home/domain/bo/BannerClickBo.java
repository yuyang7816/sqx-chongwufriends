package com.sqx.app.pet.home.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "Banner点击上报")
public class BannerClickBo {
    @NotNull(message = "bannerId不能为空")
    private Long bannerId;
}

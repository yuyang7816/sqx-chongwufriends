package com.sqx.app.pet.city.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "PetCitySaveBo")
public class PetCitySaveBo {
    @NotBlank(message = "城市名称不能为空")
    private String name;

    @Schema(description = "城市编码")
    private String code;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态 1启用 0禁用")
    private Integer status;
}

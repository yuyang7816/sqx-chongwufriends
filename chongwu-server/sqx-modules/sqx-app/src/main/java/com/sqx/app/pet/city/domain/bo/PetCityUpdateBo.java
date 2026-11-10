package com.sqx.app.pet.city.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "PetCityUpdateBo")
public class PetCityUpdateBo {
    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "城市名称不能为空")
    private String name;

    private String code;
    private Integer sort;
    private Integer status;
}

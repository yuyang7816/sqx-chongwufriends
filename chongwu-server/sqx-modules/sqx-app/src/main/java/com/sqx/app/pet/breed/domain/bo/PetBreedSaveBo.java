package com.sqx.app.pet.breed.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "PetBreedSaveBo")
public class PetBreedSaveBo {
    @NotBlank(message = "宠物类型不能为空")
    private String petType;
    @NotBlank(message = "品种名称不能为空")
    private String name;
    private Integer sort;
    private Integer status;
}

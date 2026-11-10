package com.sqx.app.pet.breed.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "PetBreedInfoBo")
public class PetBreedInfoBo {
    @NotNull(message = "ID不能为空")
    private Long id;
}

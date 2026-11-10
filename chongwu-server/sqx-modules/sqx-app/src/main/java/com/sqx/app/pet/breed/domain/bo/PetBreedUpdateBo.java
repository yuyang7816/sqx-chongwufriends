package com.sqx.app.pet.breed.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PetBreedUpdateBo")
public class PetBreedUpdateBo extends PetBreedSaveBo {
    @NotNull(message = "ID不能为空")
    private Long id;
}

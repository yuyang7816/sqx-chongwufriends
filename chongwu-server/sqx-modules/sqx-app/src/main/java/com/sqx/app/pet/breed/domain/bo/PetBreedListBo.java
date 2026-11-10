package com.sqx.app.pet.breed.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "PetBreedListBo")
public class PetBreedListBo {
    private String petType;
}
